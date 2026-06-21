<template>
  <div class="tag-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini" :model="query">
        <el-form-item label="标签分类">
          <el-select v-model="query.categoryId" clearable filterable placeholder="全部">
            <el-option v-for="item in categories" :key="item.id" :label="item.displayName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="更新方式">
          <el-select v-model="query.updateMode" clearable placeholder="全部">
            <el-option label="手动" value="manual" />
            <el-option label="例行" value="routine" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签状态">
          <el-select v-model="query.status" clearable placeholder="全部">
            <el-option label="启用" value="enabled" />
            <el-option label="停用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.keyword" clearable placeholder="标签名称、标签标识" @keyup.enter.native="loadList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-refresh-right" @click="loadList">刷新</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="table-head">
          <div class="public-tableHead">标签管理</div>
          <el-button size="mini" type="primary" icon="el-icon-plus" @click="openAdd">新建标签</el-button>
        </div>
        <el-table
          v-loading="loading"
          border
          class="public-radius"
          :data="rows"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="序号" width="70" />
          <el-table-column prop="displayName" label="标签名称" min-width="150" show-overflow-tooltip />
          <el-table-column prop="tagKey" label="标签标识" min-width="180" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="标签分类" min-width="120" />
          <el-table-column label="创建方式" width="100">
            <template slot-scope="scope">{{ createTypeText(scope.row.createType) }}</template>
          </el-table-column>
          <el-table-column label="更新方式" width="100">
            <template slot-scope="scope">{{ scope.row.updateMode === "routine" ? "例行" : "手动" }}</template>
          </el-table-column>
          <el-table-column prop="dataType" label="标签类型" width="100" />
          <el-table-column prop="matchUserCount" label="覆盖人数" width="100" />
          <el-table-column label="状态" width="90">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.status === 'enabled' ? 'success' : 'info'">
                {{ scope.row.status === "enabled" ? "启用" : "停用" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastExecuteTime" label="最新计算时间" width="170" />
          <el-table-column label="操作" width="180" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" icon="el-icon-user" @click="openAssign(scope.row)">赋值</el-button>
              <el-button type="text" icon="el-icon-edit" @click="openEdit(scope.row)" />
              <el-button type="text" icon="el-icon-delete" class="danger" @click="remove(scope.row)" />
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="pagination"
          next-text="下一页"
          :current-page="pageNum"
          :page-sizes="[10, 20, 30, 40]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="620px" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-form-item label="标签名称" prop="displayName">
          <el-input v-model="form.displayName" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签分类" prop="categoryId">
          <el-select v-model="form.categoryId" filterable placeholder="请选择">
            <el-option v-for="item in categories" :key="item.id" :label="item.displayName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签标识" prop="tagKey">
          <el-input v-model="form.tagKey" placeholder="如 user_tag_vip_level" />
        </el-form-item>
        <el-form-item label="标签类型">
          <el-select v-model="form.dataType">
            <el-option label="字符串" value="string" />
            <el-option label="数值" value="number" />
            <el-option label="布尔" value="boolean" />
            <el-option label="日期时间" value="datetime" />
          </el-select>
        </el-form-item>
        <el-form-item label="更新方式">
          <el-radio-group v-model="form.updateMode">
            <el-radio label="manual">手动</el-radio>
            <el-radio label="routine">例行</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            active-value="enabled"
            inactive-value="disabled"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="为用户赋标签" :visible.sync="assignVisible" width="560px" :close-on-click-modal="false">
      <el-form :model="assignForm" label-width="100px" size="small">
        <el-form-item label="标签">
          <span>{{ activeTag.displayName }}</span>
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input
            v-model="assignForm.distinctIdsText"
            type="textarea"
            :rows="5"
            placeholder="每行一个用户ID，也可使用英文逗号分隔"
          />
        </el-form-item>
        <el-form-item label="标签值">
          <el-input v-model="assignForm.tagValue" placeholder="如 高价值、VIP、A" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="assign">确定赋值</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  addTagApi,
  assignTagApi,
  deleteTagApi,
  editTagApi,
  getCategoryListApi,
  getTagPageListApi,
} from "@/api/sysmanage/userTag";

export default {
  name: "UserTag",
  data() {
    return {
      loading: false,
      rows: [],
      categories: [],
      total: 0,
      pageNum: 1,
      pageSize: 20,
      dialogVisible: false,
      dialogTitle: "新建标签",
      assignVisible: false,
      activeTag: {},
      query: {
        categoryId: "",
        updateMode: "",
        status: "",
        keyword: "",
      },
      form: this.emptyForm(),
      assignForm: {
        distinctIdsText: "",
        tagValue: "",
      },
      rules: {
        displayName: [{ required: true, message: "请输入标签名称", trigger: "blur" }],
        categoryId: [{ required: true, message: "请选择标签分类", trigger: "change" }],
        tagKey: [{ required: true, message: "请输入标签标识", trigger: "blur" }],
      },
    };
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName;
    },
  },
  watch: {
    projectName() {
      this.pageNum = 1;
      this.loadCategories();
      this.loadList();
    },
  },
  mounted() {
    this.loadCategories();
    this.loadList();
  },
  methods: {
    emptyForm() {
      return {
        id: "",
        categoryId: "",
        displayName: "",
        tagKey: "",
        dataType: "string",
        createType: "custom",
        updateMode: "manual",
        status: "enabled",
        description: "",
      };
    },
    loadCategories() {
      getCategoryListApi({ projectName: this.projectName }).then((res) => {
        this.categories = res.data || [];
      });
    },
    loadList() {
      this.loading = true;
      getTagPageListApi(Object.assign({ projectName: this.projectName }, this.query, {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
      }))
        .then((res) => {
          const data = res.data || {};
          this.rows = data.rows || [];
          this.total = data.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    resetQuery() {
      this.query = { categoryId: "", updateMode: "", status: "", keyword: "" };
      this.pageNum = 1;
      this.loadList();
    },
    openAdd() {
      if (!this.categories.length) {
        this.$message.warning("请先创建标签分类");
        return;
      }
      this.dialogTitle = "新建标签";
      this.form = this.emptyForm();
      this.dialogVisible = true;
    },
    openEdit(row) {
      this.dialogTitle = "编辑标签";
      this.form = Object.assign(this.emptyForm(), row);
      this.dialogVisible = true;
    },
    submit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return;
        const payload = Object.assign({ projectName: this.projectName }, this.form);
        const request = payload.id ? editTagApi(payload) : addTagApi(payload);
        request.then(() => {
          this.$message.success("保存成功");
          this.dialogVisible = false;
          this.loadList();
        });
      });
    },
    openAssign(row) {
      this.activeTag = row;
      this.assignForm = { distinctIdsText: "", tagValue: row.displayName };
      this.assignVisible = true;
    },
    assign() {
      const distinctIds = this.assignForm.distinctIdsText
        .split(/[\n,]/)
        .map((item) => item.trim())
        .filter(Boolean);
      if (!distinctIds.length) {
        this.$message.warning("请输入至少一个用户ID");
        return;
      }
      assignTagApi({
        projectName: this.projectName,
        tagId: this.activeTag.id,
        distinctIds,
        tagValue: this.assignForm.tagValue,
      }).then(() => {
        this.$message.success("赋值成功");
        this.assignVisible = false;
        this.loadList();
      });
    },
    remove(row) {
      this.$confirm(`确认删除标签“${row.displayName}”及其用户赋值吗？`, "提示", {
        type: "warning",
      }).then(() => {
        deleteTagApi({ id: row.id }).then(() => {
          this.$message.success("删除成功");
          this.loadList();
        });
      });
    },
    createTypeText(value) {
      return value === "custom" ? "自定义" : value || "--";
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.pageNum = 1;
      this.loadList();
    },
    handleCurrentChange(page) {
      this.pageNum = page;
      this.loadList();
    },
  },
};
</script>

<style lang="scss" scoped>
.tag-page {
  .table-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .pagination {
    margin-top: 18px;
    text-align: right;
  }

  .danger {
    color: #f56c6c;
  }

  .el-dialog .el-select,
  .el-dialog .el-input,
  .el-dialog .el-textarea {
    width: 100%;
  }
}
</style>
