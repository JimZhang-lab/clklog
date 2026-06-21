<template>
  <div class="tag-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini" :model="query" class="tag-filter">
        <el-form-item label="标签分类">
          <el-select v-model="query.categoryId" clearable filterable placeholder="全部">
            <el-option v-for="item in categories" :key="item.id" :label="item.displayName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="query.dataType" clearable placeholder="全部">
            <el-option label="文本" value="string" />
            <el-option label="数值" value="number" />
            <el-option label="布尔" value="boolean" />
            <el-option label="日期时间" value="datetime" />
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
        <el-form-item label="创建方式">
          <el-select v-model="query.createType" clearable placeholder="全部">
            <el-option label="自定义标签值" value="custom" />
            <el-option label="自定义规则" value="rule" />
            <el-option label="导入" value="import" />
          </el-select>
        </el-form-item>
        <el-form-item label="最新版本计算时间">
          <el-date-picker
            v-model="query.executeTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.keyword" clearable placeholder="标签名称、标签标识" @keyup.enter.native="loadList" />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" icon="el-icon-refresh-right" @click="loadList">刷新</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">新建标签</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="table-head">
          <div class="public-tableHead">用户标签管理</div>
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
          <el-table-column prop="displayName" label="标签名称" min-width="160" show-overflow-tooltip />
          <el-table-column label="标签状态" width="100">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.status === 'enabled' ? 'success' : 'info'">
                {{ scope.row.status === "enabled" ? "启用" : "停用" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="标签分类" min-width="130" show-overflow-tooltip />
          <el-table-column label="创建方式" width="120">
            <template slot-scope="scope">{{ createTypeText(scope.row.createType) }}</template>
          </el-table-column>
          <el-table-column label="更新方式" width="130">
            <template slot-scope="scope">{{ updateModeText(scope.row.updateMode) }}</template>
          </el-table-column>
          <el-table-column label="标签类型" width="100">
            <template slot-scope="scope">{{ dataTypeText(scope.row.dataType) }}</template>
          </el-table-column>
          <el-table-column label="标签覆盖人数" width="120">
            <template slot-scope="scope">
              <el-button type="text" @click="openInsight(scope.row)">{{ scope.row.matchUserCount || 0 }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="最新版本计算状态" width="140">
            <template slot-scope="scope">
              <el-tag size="mini" :type="executeStatusType(scope.row.lastExecuteStatus)">
                {{ executeStatusText(scope.row.lastExecuteStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastExecuteTime" label="最新版本计算时间" min-width="170" show-overflow-tooltip />
          <el-table-column label="创建人" width="100">
            <template slot-scope="scope">{{ scope.row.createUser || "clklog" }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="170" show-overflow-tooltip />
          <el-table-column prop="updateTime" label="修改时间" min-width="170" show-overflow-tooltip />
          <el-table-column prop="tagKey" label="标签标识" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="230" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" icon="el-icon-view" @click="openInsight(scope.row)">详情</el-button>
              <el-button type="text" icon="el-icon-user" @click="openAssign(scope.row)">赋值</el-button>
              <el-button type="text" icon="el-icon-edit" @click="openEdit(scope.row)">编辑</el-button>
              <el-button type="text" icon="el-icon-delete" class="danger" @click="remove(scope.row)">删除</el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="660px" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px" size="small">
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
        <el-form-item label="数据类型">
          <el-select v-model="form.dataType">
            <el-option label="文本" value="string" />
            <el-option label="数值" value="number" />
            <el-option label="布尔" value="boolean" />
            <el-option label="日期时间" value="datetime" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建方式">
          <el-select v-model="form.createType">
            <el-option label="自定义标签值" value="custom" />
            <el-option label="自定义规则" value="rule" />
            <el-option label="导入" value="import" />
          </el-select>
        </el-form-item>
        <el-form-item label="更新方式">
          <el-radio-group v-model="form.updateMode">
            <el-radio label="manual">手动</el-radio>
            <el-radio label="routine">例行 每 00:00</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标签状态">
          <el-switch
            v-model="form.status"
            active-value="enabled"
            inactive-value="disabled"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        <el-form-item label="规则配置">
          <el-input
            v-model="form.ruleJson"
            type="textarea"
            :rows="4"
            placeholder="例如 {&quot;event&quot;:&quot;pay&quot;,&quot;amount&quot;:&quot;>=100&quot;}"
          />
        </el-form-item>
        <el-form-item label="描述">
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

    <el-drawer :title="insightTitle" :visible.sync="insightVisible" size="760px" custom-class="tag-insight-drawer">
      <div class="drawer-body">
        <el-tabs v-model="insightTab">
          <el-tab-pane label="覆盖用户" name="users">
            <el-table v-loading="coverageLoading" border size="mini" :data="coverageRows">
              <el-table-column type="index" label="序号" width="70" />
              <el-table-column prop="distinctId" label="用户ID" min-width="180" show-overflow-tooltip />
              <el-table-column prop="tagValue" label="标签值" min-width="130" show-overflow-tooltip />
              <el-table-column prop="updateTime" label="更新时间" min-width="160" show-overflow-tooltip />
              <el-table-column label="操作" width="90">
                <template slot-scope="scope">
                  <el-button type="text" class="danger" @click="unassign(scope.row)">移除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="pagination"
              :current-page="coveragePageNum"
              :page-size="coveragePageSize"
              layout="total, prev, pager, next"
              :total="coverageTotal"
              @current-change="handleCoveragePage"
            />
          </el-tab-pane>
          <el-tab-pane label="标签值分布" name="distribution">
            <el-table border size="mini" :data="distributionRows">
              <el-table-column prop="name" label="标签值" min-width="180" show-overflow-tooltip />
              <el-table-column prop="value" label="用户数" width="100" />
              <el-table-column label="占比" min-width="220">
                <template slot-scope="scope">
                  <el-progress :percentage="distributionPercent(scope.row.value)" :stroke-width="10" />
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="标签配置" name="config">
            <div class="config-list">
              <div class="config-row">
                <span>标签标识</span>
                <strong>{{ activeTag.tagKey || "--" }}</strong>
              </div>
              <div class="config-row">
                <span>标签分类</span>
                <strong>{{ activeTag.categoryName || "--" }}</strong>
              </div>
              <div class="config-row">
                <span>创建方式</span>
                <strong>{{ createTypeText(activeTag.createType) }}</strong>
              </div>
              <div class="config-row">
                <span>更新方式</span>
                <strong>{{ updateModeText(activeTag.updateMode) }}</strong>
              </div>
              <div class="config-row config-row_block">
                <span>规则配置</span>
                <strong>{{ activeTag.ruleJson || "--" }}</strong>
              </div>
              <div class="config-row config-row_block">
                <span>描述</span>
                <strong>{{ activeTag.description || "--" }}</strong>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  addTagApi,
  assignTagApi,
  deleteTagApi,
  editTagApi,
  getCategoryListApi,
  getTagDistributeStatsApi,
  getTagPageListApi,
  getTagUserPageListApi,
  unassignTagApi
} from '@/api/sysmanage/userTag'

export default {
  name: 'UserTag',
  data() {
    return {
      loading: false,
      rows: [],
      categories: [],
      total: 0,
      pageNum: 1,
      pageSize: 20,
      dialogVisible: false,
      dialogTitle: '新建标签',
      assignVisible: false,
      insightVisible: false,
      insightTab: 'users',
      activeTag: {},
      coverageRows: [],
      coverageLoading: false,
      coverageTotal: 0,
      coveragePageNum: 1,
      coveragePageSize: 10,
      distributionRows: [],
      query: this.emptyQuery(),
      form: this.emptyForm(),
      assignForm: {
        distinctIdsText: '',
        tagValue: ''
      },
      rules: {
        displayName: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择标签分类', trigger: 'change' }],
        tagKey: [{ required: true, message: '请输入标签标识', trigger: 'blur' }]
      }
    }
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName
    },
    insightTitle() {
      return this.activeTag.displayName ? `${this.activeTag.displayName} - 标签详情` : '标签详情'
    }
  },
  watch: {
    projectName() {
      this.pageNum = 1
      this.loadCategories()
      this.loadList()
    }
  },
  mounted() {
    this.loadCategories()
    this.loadList()
  },
  methods: {
    emptyQuery() {
      return {
        categoryId: '',
        dataType: '',
        updateMode: '',
        status: '',
        createType: '',
        executeTimeRange: [],
        keyword: ''
      }
    },
    emptyForm() {
      return {
        id: '',
        categoryId: '',
        displayName: '',
        tagKey: '',
        dataType: 'string',
        createType: 'custom',
        updateMode: 'manual',
        status: 'enabled',
        ruleJson: '',
        description: ''
      }
    },
    loadCategories() {
      getCategoryListApi({ projectName: this.projectName }).then((res) => {
        this.categories = res.data || []
      })
    },
    buildQueryParams() {
      const range = this.query.executeTimeRange || []
      return Object.assign({ projectName: this.projectName }, this.query, {
        lastExecuteStartTime: range[0] || '',
        lastExecuteEndTime: range[1] || '',
        pageNum: this.pageNum,
        pageSize: this.pageSize
      })
    },
    loadList() {
      this.loading = true
      getTagPageListApi(this.buildQueryParams())
        .then((res) => {
          const data = res.data || {}
          this.rows = data.rows || []
          this.total = data.total || 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    resetQuery() {
      this.query = this.emptyQuery()
      this.pageNum = 1
      this.loadList()
    },
    openAdd() {
      if (!this.categories.length) {
        this.$message.warning('请先创建标签分类')
        return
      }
      this.dialogTitle = '新建标签'
      this.form = this.emptyForm()
      this.dialogVisible = true
    },
    openEdit(row) {
      this.dialogTitle = '编辑标签'
      this.form = Object.assign(this.emptyForm(), row)
      this.dialogVisible = true
    },
    submit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return
        const payload = Object.assign({ projectName: this.projectName }, this.form)
        const request = payload.id ? editTagApi(payload) : addTagApi(payload)
        request.then(() => {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadList()
        })
      })
    },
    openAssign(row) {
      this.activeTag = row
      this.assignForm = { distinctIdsText: '', tagValue: row.displayName }
      this.assignVisible = true
    },
    assign() {
      const distinctIds = this.assignForm.distinctIdsText
        .split(/[\n,]/)
        .map((item) => item.trim())
        .filter(Boolean)
      if (!distinctIds.length) {
        this.$message.warning('请输入至少一个用户ID')
        return
      }
      assignTagApi({
        projectName: this.projectName,
        tagId: this.activeTag.id,
        distinctIds,
        tagValue: this.assignForm.tagValue
      }).then(() => {
        this.$message.success('赋值成功')
        this.assignVisible = false
        this.loadList()
        if (this.insightVisible && this.activeTag.id) {
          this.loadCoverageUsers()
          this.loadDistribution()
        }
      })
    },
    openInsight(row) {
      this.activeTag = row
      this.insightTab = 'users'
      this.coveragePageNum = 1
      this.insightVisible = true
      this.loadCoverageUsers()
      this.loadDistribution()
    },
    loadCoverageUsers() {
      this.coverageLoading = true
      getTagUserPageListApi({
        projectName: this.projectName,
        tagId: this.activeTag.id,
        pageNum: this.coveragePageNum,
        pageSize: this.coveragePageSize
      })
        .then((res) => {
          const data = res.data || {}
          this.coverageRows = data.rows || []
          this.coverageTotal = data.total || 0
        })
        .finally(() => {
          this.coverageLoading = false
        })
    },
    loadDistribution() {
      getTagDistributeStatsApi({
        projectName: this.projectName,
        tagId: this.activeTag.id
      }).then((res) => {
        this.distributionRows = res.data || []
      })
    },
    unassign(row) {
      this.$confirm(`确认移除用户“${row.distinctId}”的该标签吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        unassignTagApi({
          projectName: this.projectName,
          tagId: this.activeTag.id,
          distinctId: row.distinctId
        }).then(() => {
          this.$message.success('移除成功')
          this.loadCoverageUsers()
          this.loadDistribution()
          this.loadList()
        })
      })
    },
    remove(row) {
      this.$confirm(`确认删除标签“${row.displayName}”及其用户赋值吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        deleteTagApi({ id: row.id, projectName: this.projectName }).then(() => {
          this.$message.success('删除成功')
          this.loadList()
        })
      })
    },
    createTypeText(value) {
      const map = {
        custom: '自定义标签值',
        rule: '自定义规则',
        import: '导入'
      }
      return map[value] || value || '--'
    },
    updateModeText(value) {
      const map = {
        manual: '手动',
        routine: '例行 每 00:00'
      }
      return map[value] || value || '--'
    },
    dataTypeText(value) {
      const map = {
        string: '文本',
        text: '文本',
        number: '数值',
        boolean: '布尔',
        datetime: '日期时间'
      }
      return map[value] || value || '--'
    },
    executeStatusText(value) {
      const map = {
        success: '计算成功',
        running: '计算中',
        failed: '计算失败'
      }
      return map[value] || '未计算'
    },
    executeStatusType(value) {
      if (value === 'success') return 'success'
      if (value === 'failed') return 'danger'
      if (value === 'running') return 'warning'
      return 'info'
    },
    distributionPercent(value) {
      const total = this.distributionRows.reduce((sum, item) => sum + Number(item.value || 0), 0)
      if (!total) return 0
      return Math.round((Number(value || 0) * 10000) / total) / 100
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.pageNum = 1
      this.loadList()
    },
    handleCurrentChange(page) {
      this.pageNum = page
      this.loadList()
    },
    handleCoveragePage(page) {
      this.coveragePageNum = page
      this.loadCoverageUsers()
    }
  }
}
</script>

<style lang="scss" scoped>
.tag-page {
  .tag-filter {
    .el-date-editor {
      width: 260px;
    }
  }

  .filter-actions {
    white-space: nowrap;
  }

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

  .drawer-body {
    padding: 0 20px 20px;
  }

  .config-list {
    border: 1px solid #ebeef5;
    border-bottom: 0;
  }

  .config-row {
    display: grid;
    grid-template-columns: 120px 1fr;
    min-height: 42px;
    border-bottom: 1px solid #ebeef5;

    span {
      display: flex;
      align-items: center;
      padding: 0 12px;
      background: #f7fafe;
      color: #606266;
      font-weight: 500;
    }

    strong {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      color: #303133;
      font-weight: 400;
      word-break: break-word;
    }
  }

  .el-dialog .el-select,
  .el-dialog .el-input,
  .el-dialog .el-textarea {
    width: 100%;
  }
}
</style>
