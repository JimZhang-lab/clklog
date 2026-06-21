<template>
  <div class="metadata-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini" :model="query">
        <el-form-item label="事件">
          <el-input v-model="query.keyword" clearable placeholder="事件名/显示名" @keyup.enter.native="loadList" />
        </el-form-item>
        <el-form-item label="分组">
          <el-select v-model="query.groupName" clearable filterable placeholder="全部">
            <el-option v-for="item in groupOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="query.tag" clearable filterable placeholder="全部">
            <el-option v-for="item in tagOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadList">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="table-head">
          <div class="public-tableHead">事件管理</div>
          <el-button class="zc_btn_default" size="mini" icon="el-icon-plus" @click="openAdd">新增事件</el-button>
        </div>
        <el-table
          v-loading="loading"
          border
          class="public-radius"
          :data="rows"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="序号" width="80" />
          <el-table-column prop="eventName" label="事件名" min-width="160" show-overflow-tooltip />
          <el-table-column prop="displayName" label="显示名" min-width="160" show-overflow-tooltip />
          <el-table-column prop="groupName" label="分组" min-width="120" show-overflow-tooltip />
          <el-table-column prop="tag" label="标签" min-width="120" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" width="130">
            <template slot-scope="scope">
              <i class="el-icon-edit edit_btn" @click="openEdit(scope.row)" />
              <i class="el-icon-delete delete_btn" @click="remove(scope.row)" />
            </template>
          </el-table-column>
        </el-table>
        <div class="block">
          <el-pagination
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
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px" size="mini">
        <el-form-item label="事件名" prop="eventName">
          <el-input v-model="form.eventName" placeholder="如 $pageview / buy_click" />
        </el-form-item>
        <el-form-item label="显示名">
          <el-input v-model="form.displayName" placeholder="用于页面展示" />
        </el-form-item>
        <el-form-item label="分组">
          <el-input v-model="form.groupName" placeholder="如 页面事件 / 业务事件" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tag" placeholder="如 核心、转化、留存" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" value="enabled" />
            <el-option label="停用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="mini" @click="dialogVisible = false">取消</el-button>
        <el-button size="mini" type="primary" @click="submit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  addEventApi,
  deleteEventApi,
  editEventApi,
  getGroupNameListEventApi,
  getPageListEventApi,
  getTagListEventApi,
} from "@/api/sysmanage/manageEvent";

export default {
  name: "EventManage",
  data() {
    return {
      loading: false,
      dialogVisible: false,
      dialogTitle: "新增事件",
      rows: [],
      total: 0,
      pageNum: 1,
      pageSize: 20,
      groupOptions: [],
      tagOptions: [],
      query: {
        keyword: "",
        groupName: "",
        tag: "",
      },
      form: this.emptyForm(),
      rules: {
        eventName: [{ required: true, message: "请输入事件名", trigger: "blur" }],
      },
    };
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName;
    },
  },
  mounted() {
    this.loadOptions();
    this.loadList();
  },
  methods: {
    emptyForm() {
      return {
        id: "",
        eventName: "",
        displayName: "",
        groupName: "",
        tag: "",
        status: "enabled",
        description: "",
      };
    },
    baseParams() {
      return Object.assign({ projectName: this.projectName }, this.query);
    },
    loadOptions() {
      const params = { projectName: this.projectName };
      getGroupNameListEventApi(params).then((res) => (this.groupOptions = res.data || []));
      getTagListEventApi(params).then((res) => (this.tagOptions = res.data || []));
    },
    loadList() {
      this.loading = true;
      const params = Object.assign(this.baseParams(), {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
      });
      getPageListEventApi(params)
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
      this.query = { keyword: "", groupName: "", tag: "" };
      this.pageNum = 1;
      this.loadList();
    },
    openAdd() {
      this.dialogTitle = "新增事件";
      this.form = this.emptyForm();
      this.dialogVisible = true;
    },
    openEdit(row) {
      this.dialogTitle = "编辑事件";
      this.form = Object.assign(this.emptyForm(), row);
      this.dialogVisible = true;
    },
    submit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return;
        const payload = Object.assign({ projectName: this.projectName }, this.form);
        const request = payload.id ? editEventApi(payload) : addEventApi(payload);
        request.then(() => {
          this.$message.success("保存成功");
          this.dialogVisible = false;
          this.loadOptions();
          this.loadList();
        });
      });
    },
    remove(row) {
      this.$confirm(`确认删除 ${row.displayName || row.eventName} 吗？`, "提示", {
        type: "warning",
      }).then(() => {
        deleteEventApi({ id: row.id }).then(() => {
          this.$message.success("删除成功");
          this.loadOptions();
          this.loadList();
        });
      });
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
.metadata-page {
  .table-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .edit_btn,
  .delete_btn {
    margin: 0 8px;
    cursor: pointer;
  }
}
</style>
