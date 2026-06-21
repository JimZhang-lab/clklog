<template>
  <div class="commercial-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini">
        <el-form-item :label="isMenu ? '菜单名称' : '角色名'">
          <el-input v-model="keyword" clearable :placeholder="isMenu ? '请输入菜单名称' : '请输入角色名或显示名'" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadRows">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">{{ isMenu ? "新增菜单" : "新增角色" }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="public-tableHead">{{ isMenu ? "菜单管理" : "角色管理" }}</div>
        <el-table
          v-loading="loading"
          border
          class="public-radius"
          :data="filteredRows"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="序号" width="70" />
          <template v-if="isMenu">
            <el-table-column prop="title" label="菜单名称" min-width="150" />
            <el-table-column prop="path" label="路由地址" min-width="220" show-overflow-tooltip />
            <el-table-column prop="component" label="组件地址" min-width="220" show-overflow-tooltip />
            <el-table-column prop="roles" label="访问角色" min-width="160" />
            <el-table-column prop="sortOrder" label="顺序号" width="90" />
          </template>
          <template v-else>
            <el-table-column prop="roleName" label="角色名" min-width="140" />
            <el-table-column prop="displayName" label="显示名" min-width="140" />
            <el-table-column prop="sortOrder" label="排序号" width="90" />
            <el-table-column prop="roleType" label="角色类型" width="120" />
            <el-table-column prop="createTime" label="创建日期" min-width="170" />
            <el-table-column prop="updateTime" label="修改日期" min-width="170" />
          </template>
          <el-table-column label="操作" width="130">
            <template slot-scope="scope">
              <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
              <el-button type="text" class="danger" @click="remove(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="640px" :close-on-click-modal="false">
      <el-form :model="form" label-width="110px" size="small">
        <template v-if="isMenu">
          <el-form-item label="菜单名称"><el-input v-model="form.title" /></el-form-item>
          <el-form-item label="路由地址"><el-input v-model="form.path" /></el-form-item>
          <el-form-item label="组件地址"><el-input v-model="form.component" /></el-form-item>
          <el-form-item label="接口权限">
            <el-input v-model="form.permissions" type="textarea" :rows="4" placeholder="每行一个接口权限，如 /user/add" />
          </el-form-item>
          <el-form-item label="访问角色"><el-input v-model="form.roles" placeholder="多个角色用逗号分隔" /></el-form-item>
          <el-form-item label="顺序号"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        </template>
        <template v-else>
          <el-form-item label="角色名"><el-input v-model="form.roleName" /></el-form-item>
          <el-form-item label="显示名"><el-input v-model="form.displayName" /></el-form-item>
          <el-form-item label="排序号"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
          <el-form-item label="角色类型">
            <el-select v-model="form.roleType">
              <el-option label="系统管理员" value="系统管理员" />
              <el-option label="项目管理员" value="项目管理员" />
              <el-option label="元数据管理" value="元数据管理" />
              <el-option label="数据查看" value="数据查看" />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  addApi as addMenuApi,
  deleteApi as deleteMenuApi,
  editApi as editMenuApi,
  getPageListApi as getMenuPageListApi,
} from "@/api/sysmanage/menuManage";
import {
  addApi as addRoleApi,
  deleteApi as deleteRoleApi,
  editApi as editRoleApi,
  getPageListApi as getRolePageListApi,
} from "@/api/sysmanage/roleManage";

export default {
  name: "PermissionManage",
  data() {
    return {
      loading: false,
      keyword: "",
      rows: [],
      dialogVisible: false,
      form: {},
    };
  },
  computed: {
    isMenu() {
      return this.$route.meta.permissionMode === "menu";
    },
    dialogTitle() {
      return `${this.form.id ? "编辑" : "新增"}${this.isMenu ? "菜单" : "角色"}`;
    },
    filteredRows() {
      if (!this.keyword) {
        return this.rows;
      }
      const keyword = this.keyword.toLowerCase();
      return this.rows.filter((item) => JSON.stringify(item).toLowerCase().includes(keyword));
    },
  },
  watch: {
    "$route.path"() {
      this.loadRows();
    },
  },
  mounted() {
    this.loadRows();
  },
  methods: {
    loadRows() {
      this.loading = true;
      this.pageApi()({
        keyword: this.keyword,
        pageNum: 1,
        pageSize: 200,
      })
        .then((res) => {
          const data = res.data || {};
          this.rows = data.rows || [];
          if (!this.rows.length && !this.keyword) {
            return this.seedDefaults().then(() => this.loadRows());
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    defaultRows() {
      if (this.isMenu) {
        return [
          { title: "数据概览", path: "/index", component: "views/home/index", roles: "数据查看", sortOrder: 10 },
          { title: "事件分析", path: "/mete/eventAnalysis", component: "views/commercial/EventWorkbench", roles: "元数据管理,数据查看", sortOrder: 20 },
          { title: "用户画像管理(CDP）", path: "/ups/userGroup", component: "views/commercial/CdpList", roles: "项目管理员", sortOrder: 30 },
        ];
      }
      const now = new Date().toLocaleString();
      return [
        { roleName: "sys_admin", displayName: "系统管理员", sortOrder: 1, roleType: "系统管理员", createTime: now, updateTime: now },
        { roleName: "project_admin", displayName: "项目管理员", sortOrder: 2, roleType: "项目管理员", createTime: now, updateTime: now },
        { roleName: "metadata_manager", displayName: "元数据管理", sortOrder: 3, roleType: "元数据管理", createTime: now, updateTime: now },
        { roleName: "data_viewer", displayName: "数据查看", sortOrder: 4, roleType: "数据查看", createTime: now, updateTime: now },
      ];
    },
    pageApi() {
      return this.isMenu ? getMenuPageListApi : getRolePageListApi;
    },
    addApi() {
      return this.isMenu ? addMenuApi : addRoleApi;
    },
    editApi() {
      return this.isMenu ? editMenuApi : editRoleApi;
    },
    deleteApi() {
      return this.isMenu ? deleteMenuApi : deleteRoleApi;
    },
    seedDefaults() {
      return Promise.all(this.defaultRows().map((item) => this.addApi()(Object.assign({ status: "enabled" }, item))));
    },
    emptyForm() {
      return this.isMenu
        ? { id: "", title: "", path: "", component: "", permissions: "", roles: "", sortOrder: 0 }
        : { id: "", roleName: "", displayName: "", sortOrder: 0, roleType: "数据查看" };
    },
    openAdd() {
      this.form = this.emptyForm();
      this.dialogVisible = true;
    },
    openEdit(row) {
      this.form = Object.assign({}, row);
      this.dialogVisible = true;
    },
    submit() {
      const request = this.form.id ? this.editApi()(this.form) : this.addApi()(this.form);
      request.then(() => {
        this.$message.success("保存成功");
        this.dialogVisible = false;
        this.loadRows();
      });
    },
    remove(row) {
      this.deleteApi()({ id: row.id }).then(() => {
        this.$message.success("删除成功");
        this.loadRows();
      });
    },
    reset() {
      this.keyword = "";
      this.loadRows();
    },
  },
};
</script>
