<template>
  <div class="commercial-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini">
        <el-form-item :label="isGroupPortrait ? '群画像名称' : '分群名称'">
          <el-input v-model="keyword" clearable placeholder="请输入名称或标识搜索" @keyup.enter.native="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-refresh-right" @click="loadData">刷新</el-button>
          <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
          <el-button type="primary" icon="el-icon-plus" @click="openCreate">
            {{ isGroupPortrait ? "新增群画像" : "新建分群" }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="public-tableHead">{{ isGroupPortrait ? "画像列表" : "分群列表" }}</div>
        <el-table
          v-loading="loading"
          border
          class="public-radius"
          :data="filteredRows"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="序号" width="70" />
          <el-table-column prop="displayName" :label="isGroupPortrait ? '群画像名称' : '分群名称'" min-width="160" />
          <el-table-column prop="key" :label="isGroupPortrait ? '群画像标识' : '分群标识'" min-width="180" />
          <el-table-column prop="statusText" label="状态" width="100" />
          <el-table-column prop="createType" label="创建方式" width="100" />
          <el-table-column prop="updateMode" label="更新方式" width="100" />
          <el-table-column prop="matchUserCount" :label="isGroupPortrait ? '画像覆盖人数' : '分群覆盖人数'" width="130" />
          <el-table-column prop="lastExecuteTime" label="最新版本计算时间" min-width="170" />
          <el-table-column label="操作" width="190">
            <template slot-scope="scope">
              <el-button type="text" @click="openDetail(scope.row)">详情</el-button>
              <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
              <el-button type="text" class="danger" @click="remove(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="620px" :close-on-click-modal="false">
      <el-form :model="form" label-width="110px" size="small">
        <el-form-item :label="isGroupPortrait ? '群画像名称' : '分群名称'">
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item :label="isGroupPortrait ? '群画像标识' : '分群标识'">
          <el-input v-model="form.key" />
        </el-form-item>
        <el-form-item label="覆盖用户">
          <el-select v-model="form.distinctIds" multiple filterable placeholder="请选择用户">
            <el-option v-for="item in visitors" :key="item.distinctId" :label="item.distinctId" :value="item.distinctId" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="覆盖用户与标签画像" :visible.sync="detailVisible" width="760px">
      <div class="detail-title">{{ activeRow.displayName }}</div>
      <el-table border :data="detailUsers" size="mini">
        <el-table-column prop="distinctId" label="用户ID" min-width="160" />
        <el-table-column prop="visitorType" label="访客类型" width="100" />
        <el-table-column prop="pv" label="浏览量" width="90" />
        <el-table-column prop="visitCount" label="访问次数" width="100" />
        <el-table-column prop="latestTime" label="最近访问" min-width="160" />
      </el-table>
      <div class="tag-list">
        <el-tag v-for="item in tags" :key="item.id" size="mini" type="success">{{ item.displayName }}：{{ item.matchUserCount }}</el-tag>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getVisitorListApi } from "@/api/trackingapi/visitor";
import { getTagPageListApi } from "@/api/sysmanage/userTag";
import {
  addApi as addCdpApi,
  deleteApi as deleteCdpApi,
  editApi as editCdpApi,
  getPageListApi as getCdpPageListApi,
} from "@/api/sysmanage/cdpAsset";

export default {
  name: "CdpList",
  data() {
    return {
      loading: false,
      keyword: "",
      rows: [],
      visitors: [],
      tags: [],
      dialogVisible: false,
      detailVisible: false,
      activeRow: {},
      form: this.emptyForm(),
    };
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName;
    },
    isGroupPortrait() {
      return this.$route.meta.cdpMode === "portrait";
    },
    dialogTitle() {
      const action = this.form.id ? "编辑" : (this.isGroupPortrait ? "新增" : "新建");
      return `${action}${this.isGroupPortrait ? "群画像" : "分群"}`;
    },
    filteredRows() {
      if (!this.keyword) {
        return this.rows;
      }
      return this.rows.filter((item) =>
        `${item.displayName} ${item.key}`.toLowerCase().includes(this.keyword.toLowerCase())
      );
    },
    detailUsers() {
      const ids = this.activeRow.distinctIds || [];
      return this.visitors.filter((item) => ids.includes(item.distinctId));
    },
  },
  watch: {
    "$route.path"() {
      this.loadData();
    },
    projectName() {
      this.loadData();
    },
  },
  mounted() {
    this.loadData();
  },
  methods: {
    emptyForm() {
      return {
        id: "",
        displayName: "",
        key: "",
        distinctIds: [],
        description: "",
      };
    },
    loadData() {
      this.loading = true;
      Promise.all([
        getVisitorListApi({
          projectName: this.projectName,
          startTime: this.today(),
          endTime: this.today(),
          pageNum: 1,
          pageSize: 200,
        }),
        getTagPageListApi({
          projectName: this.projectName,
          pageNum: 1,
          pageSize: 200,
        }),
        this.loadCdpRows(),
      ])
        .then(([visitorRes, tagRes, cdpRes]) => {
          const visitorData = visitorRes.data || {};
          const tagData = tagRes.data || {};
          this.visitors = visitorData.rows || [];
          this.tags = tagData.rows || [];
          const cdpData = cdpRes.data || {};
          this.rows = cdpData.rows || [];
          if (!this.rows.length && !this.keyword) {
            return this.seedDefaultRows().then(() =>
              this.loadCdpRows().then((res) => {
                const data = res.data || {};
                this.rows = data.rows || [];
              })
            );
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadCdpRows() {
      return getCdpPageListApi({
        projectName: this.projectName,
        assetType: this.assetType(),
        keyword: this.keyword,
        pageNum: 1,
        pageSize: 200,
      });
    },
    seedDefaultRows() {
      const ids = this.visitors.map((item) => item.distinctId).filter(Boolean);
      return addCdpApi({
        projectName: this.projectName,
        assetType: this.assetType(),
        displayName: this.isGroupPortrait ? "高价值用户画像" : "高价值用户",
        assetKey: this.isGroupPortrait ? "portrait_high_value_users" : "group_high_value_users",
        status: "enabled",
        createType: "custom",
        updateMode: "manual",
        distinctIds: ids,
        description: "基于本地演示用户自动生成",
      });
    },
    assetType() {
      return this.isGroupPortrait ? "portrait" : "group";
    },
    openCreate() {
      this.form = this.emptyForm();
      this.dialogVisible = true;
    },
    submit() {
      if (!this.form.displayName || !this.form.key) {
        this.$message.warning("名称和标识不能为空");
        return;
      }
      const payload = {
        id: this.form.id,
        projectName: this.projectName,
        assetType: this.assetType(),
        displayName: this.form.displayName,
        assetKey: this.form.key,
        status: "enabled",
        createType: "custom",
        updateMode: "manual",
        distinctIds: this.form.distinctIds,
        description: this.form.description,
      };
      const request = this.form.id ? editCdpApi(payload) : addCdpApi(payload);
      request.then(() => {
        this.$message.success("保存成功");
        this.dialogVisible = false;
        this.loadData();
      });
    },
    openEdit(row) {
      this.form = Object.assign({}, row, {
        key: row.key || row.assetKey,
        distinctIds: row.distinctIds || [],
      });
      this.dialogVisible = true;
    },
    remove(row) {
      deleteCdpApi({ id: row.id, projectName: this.projectName }).then(() => {
        this.$message.success("删除成功");
        this.loadData();
      });
    },
    openDetail(row) {
      this.activeRow = row;
      this.detailVisible = true;
    },
    reset() {
      this.keyword = "";
      this.loadData();
    },
    today() {
      const d = new Date();
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    },
  },
};
</script>

<style lang="scss" scoped>
.detail-title {
  margin-bottom: 12px;
  font-weight: 600;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}
</style>
