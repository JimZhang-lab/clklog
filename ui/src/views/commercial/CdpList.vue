<template>
  <div class="commercial-page cdp-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini" :model="query" class="cdp-filter">
        <el-form-item :label="nameLabel">
          <el-input v-model="query.keyword" clearable placeholder="请输入名称或标识搜索" @keyup.enter.native="loadData" />
        </el-form-item>
        <el-form-item label="更新方式">
          <el-select v-model="query.updateMode" clearable placeholder="全部">
            <el-option label="手动" value="manual" />
            <el-option label="例行" value="routine" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isGroupPortrait" label="分群状态">
          <el-select v-model="query.status" clearable placeholder="全部">
            <el-option label="启用" value="enabled" />
            <el-option label="停用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isGroupPortrait" label="创建方式">
          <el-select v-model="query.createType" clearable placeholder="全部">
            <el-option label="自定义规则" value="custom" />
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
        <el-form-item class="filter-actions">
          <el-button type="primary" icon="el-icon-refresh-right" @click="loadData">刷新</el-button>
          <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
          <el-button type="primary" icon="el-icon-plus" @click="openCreate">{{ createButtonText }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="public-tableHead">{{ tableTitle }}</div>
        <el-table
          v-loading="loading"
          border
          class="public-radius"
          :data="rows"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="序号" width="70" />
          <el-table-column prop="displayName" :label="nameLabel" min-width="170" show-overflow-tooltip />
          <el-table-column v-if="!isGroupPortrait" label="分群状态" width="100">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.status === 'enabled' ? 'success' : 'info'">
                {{ scope.row.statusText || statusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="!isGroupPortrait" label="创建方式" width="120">
            <template slot-scope="scope">{{ createTypeText(scope.row.createTypeCode || scope.row.createType) }}</template>
          </el-table-column>
          <el-table-column label="更新方式" width="130">
            <template slot-scope="scope">{{ updateModeText(scope.row.updateModeCode || scope.row.updateMode) }}</template>
          </el-table-column>
          <el-table-column v-if="!isGroupPortrait" prop="matchUserCount" label="分群覆盖人数" width="130" />
          <el-table-column v-else prop="matchUserCount" label="画像覆盖人数" width="130" />
          <el-table-column v-if="!isGroupPortrait" label="最新版本计算状态" width="140">
            <template slot-scope="scope">
              <el-tag size="mini" :type="executeStatusType(scope.row.lastExecuteStatus)">
                {{ executeStatusText(scope.row.lastExecuteStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastExecuteTime" label="最新版本计算时间" min-width="170" show-overflow-tooltip />
          <el-table-column prop="createUser" label="创建人" width="100" />
          <el-table-column prop="createTime" label="创建时间" min-width="170" show-overflow-tooltip />
          <el-table-column prop="updateTime" label="更新时间" min-width="170" show-overflow-tooltip />
          <el-table-column prop="assetKey" :label="keyLabel" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="190" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" @click="openDetail(scope.row)">详情</el-button>
              <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
              <el-button type="text" class="danger" @click="remove(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="pagination"
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px" size="small">
        <el-form-item :label="nameLabel" prop="displayName">
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item :label="keyLabel" prop="key">
          <el-input v-model="form.key" />
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
        <el-form-item label="创建方式">
          <el-select v-model="form.createType">
            <el-option label="自定义规则" value="custom" />
            <el-option label="导入" value="import" />
          </el-select>
        </el-form-item>
        <el-form-item label="更新方式">
          <el-radio-group v-model="form.updateMode">
            <el-radio label="manual">手动</el-radio>
            <el-radio label="routine">例行 每 00:00</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="创建人">
          <el-input v-model="form.createUser" />
        </el-form-item>
        <el-form-item label="覆盖用户">
          <el-select v-model="form.distinctIds" multiple filterable collapse-tags placeholder="请选择用户">
            <el-option v-for="item in visitors" :key="item.distinctId" :label="item.distinctId" :value="item.distinctId" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则配置">
          <el-input
            v-model="form.ruleJson"
            type="textarea"
            :rows="4"
            placeholder="例如 {&quot;tag&quot;:&quot;价值等级&quot;,&quot;op&quot;:&quot;=&quot;,&quot;value&quot;:&quot;高价值&quot;}"
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

    <el-dialog :title="detailTitle" :visible.sync="detailVisible" width="820px">
      <el-tabs v-model="detailTab">
        <el-tab-pane label="覆盖用户" name="users">
          <el-table border :data="detailUsers" size="mini">
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column prop="distinctId" label="用户ID" min-width="170" show-overflow-tooltip />
            <el-table-column prop="visitorType" label="访客类型" width="100" />
            <el-table-column prop="pv" label="浏览量" width="90" />
            <el-table-column prop="visitCount" label="访问次数" width="100" />
            <el-table-column prop="latestTime" label="最近访问" min-width="160" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="标签画像" name="tags">
          <el-table border :data="tags" size="mini">
            <el-table-column prop="displayName" label="标签名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="categoryName" label="标签分类" min-width="120" show-overflow-tooltip />
            <el-table-column label="标签类型" width="100">
              <template slot-scope="scope">{{ dataTypeText(scope.row.dataType) }}</template>
            </el-table-column>
            <el-table-column prop="matchUserCount" label="覆盖人数" width="100" />
            <el-table-column label="更新方式" width="130">
              <template slot-scope="scope">{{ updateModeText(scope.row.updateMode) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="规则配置" name="rules">
          <div class="config-list">
            <div class="config-row">
              <span>{{ keyLabel }}</span>
              <strong>{{ activeRow.assetKey || activeRow.key || "--" }}</strong>
            </div>
            <div class="config-row">
              <span>创建方式</span>
              <strong>{{ createTypeText(activeRow.createTypeCode || activeRow.createType) }}</strong>
            </div>
            <div class="config-row">
              <span>更新方式</span>
              <strong>{{ updateModeText(activeRow.updateModeCode || activeRow.updateMode) }}</strong>
            </div>
            <div class="config-row config-row_block">
              <span>规则配置</span>
              <strong>{{ activeRow.ruleJson || "--" }}</strong>
            </div>
            <div class="config-row config-row_block">
              <span>描述</span>
              <strong>{{ activeRow.description || "--" }}</strong>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import { getVisitorListApi } from '@/api/trackingapi/visitor'
import { getTagPageListApi } from '@/api/sysmanage/userTag'
import {
  addApi as addCdpApi,
  deleteApi as deleteCdpApi,
  editApi as editCdpApi,
  getPageListApi as getCdpPageListApi
} from '@/api/sysmanage/cdpAsset'

export default {
  name: 'CdpList',
  data() {
    return {
      loading: false,
      rows: [],
      total: 0,
      pageNum: 1,
      pageSize: 20,
      visitors: [],
      tags: [],
      query: this.emptyQuery(),
      dialogVisible: false,
      detailVisible: false,
      detailTab: 'users',
      activeRow: {},
      form: this.emptyForm(),
      rules: {
        displayName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
        key: [{ required: true, message: '请输入标识', trigger: 'blur' }]
      }
    }
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName
    },
    isGroupPortrait() {
      return this.$route.meta.cdpMode === 'portrait'
    },
    nameLabel() {
      return this.isGroupPortrait ? '群画像名称' : '分群名称'
    },
    keyLabel() {
      return this.isGroupPortrait ? '群画像标识' : '分群标识'
    },
    tableTitle() {
      return this.isGroupPortrait ? '用户群画像管理' : '用户分群管理'
    },
    createButtonText() {
      return this.isGroupPortrait ? '新增群画像' : '新建分群'
    },
    dialogTitle() {
      const action = this.form.id ? '编辑' : (this.isGroupPortrait ? '新增' : '新建')
      return `${action}${this.isGroupPortrait ? '群画像' : '分群'}`
    },
    detailTitle() {
      return this.activeRow.displayName ? `${this.activeRow.displayName} - 详情` : '详情'
    },
    detailUsers() {
      const ids = this.activeRow.distinctIds || []
      if (!ids.length) {
        return []
      }
      return this.visitors.filter((item) => ids.includes(item.distinctId))
    }
  },
  watch: {
    '$route.path'() {
      this.query = this.emptyQuery()
      this.pageNum = 1
      this.loadData()
    },
    projectName() {
      this.pageNum = 1
      this.loadData()
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    emptyQuery() {
      return {
        keyword: '',
        updateMode: '',
        status: '',
        createType: '',
        executeTimeRange: []
      }
    },
    emptyForm() {
      return {
        id: '',
        displayName: '',
        key: '',
        status: 'enabled',
        createType: 'custom',
        updateMode: 'manual',
        createUser: 'clklog',
        distinctIds: [],
        ruleJson: '',
        description: ''
      }
    },
    loadData() {
      this.loading = true
      Promise.all([this.loadVisitors(), this.loadTags(), this.loadCdpRows()])
        .then(([visitorRes, tagRes, cdpRes]) => {
          const visitorData = visitorRes.data || {}
          const tagData = tagRes.data || {}
          const cdpData = cdpRes.data || {}
          this.visitors = visitorData.rows || []
          this.tags = tagData.rows || []
          this.rows = cdpData.rows || []
          this.total = cdpData.total || 0
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadVisitors() {
      return getVisitorListApi({
        projectName: this.projectName,
        startTime: this.daysAgo(30),
        endTime: this.today(),
        pageNum: 1,
        pageSize: 500
      })
    },
    loadTags() {
      return getTagPageListApi({
        projectName: this.projectName,
        pageNum: 1,
        pageSize: 500
      })
    },
    loadCdpRows() {
      const range = this.query.executeTimeRange || []
      return getCdpPageListApi({
        projectName: this.projectName,
        assetType: this.assetType(),
        keyword: this.query.keyword,
        updateMode: this.query.updateMode,
        status: this.query.status,
        createType: this.query.createType,
        lastExecuteStartTime: range[0] || '',
        lastExecuteEndTime: range[1] || '',
        pageNum: this.pageNum,
        pageSize: this.pageSize
      })
    },
    assetType() {
      return this.isGroupPortrait ? 'portrait' : 'group'
    },
    openCreate() {
      this.form = this.emptyForm()
      this.dialogVisible = true
    },
    submit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return
        const payload = {
          id: this.form.id,
          projectName: this.projectName,
          assetType: this.assetType(),
          displayName: this.form.displayName,
          assetKey: this.form.key,
          status: this.form.status,
          createType: this.form.createType,
          updateMode: this.form.updateMode,
          createUser: this.form.createUser,
          distinctIds: this.form.distinctIds,
          ruleJson: this.form.ruleJson,
          description: this.form.description
        }
        const request = this.form.id ? editCdpApi(payload) : addCdpApi(payload)
        request.then(() => {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadData()
        })
      })
    },
    openEdit(row) {
      this.form = Object.assign(this.emptyForm(), row, {
        key: row.key || row.assetKey,
        createType: row.createTypeCode || this.normalizeCreateType(row.createType),
        updateMode: row.updateModeCode || this.normalizeUpdateMode(row.updateMode),
        distinctIds: row.distinctIds || [],
        createUser: row.createUser || 'clklog'
      })
      this.dialogVisible = true
    },
    remove(row) {
      this.$confirm(`确认删除“${row.displayName}”吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        deleteCdpApi({ id: row.id, projectName: this.projectName }).then(() => {
          this.$message.success('删除成功')
          this.loadData()
        })
      })
    },
    openDetail(row) {
      this.activeRow = row
      this.detailTab = 'users'
      this.detailVisible = true
    },
    reset() {
      this.query = this.emptyQuery()
      this.pageNum = 1
      this.loadData()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.pageNum = 1
      this.loadData()
    },
    handleCurrentChange(page) {
      this.pageNum = page
      this.loadData()
    },
    createTypeText(value) {
      const map = {
        custom: '自定义规则',
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
    statusText(value) {
      return value === 'enabled' ? '启用' : '停用'
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
    normalizeCreateType(value) {
      if (value === '自定义' || value === '自定义规则') return 'custom'
      if (value === '导入') return 'import'
      return value || 'custom'
    },
    normalizeUpdateMode(value) {
      if (value === '手动') return 'manual'
      if (value && value.indexOf('例行') === 0) return 'routine'
      return value || 'manual'
    },
    today() {
      const d = new Date()
      return this.formatDate(d)
    },
    daysAgo(days) {
      const d = new Date()
      d.setDate(d.getDate() - days)
      return this.formatDate(d)
    },
    formatDate(date) {
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }
  }
}
</script>

<style lang="scss" scoped>
.cdp-page {
  .cdp-filter {
    .el-date-editor {
      width: 260px;
    }
  }

  .filter-actions {
    white-space: nowrap;
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

  .config-list {
    border: 1px solid #ebeef5;
    border-bottom: 0;
  }

  .config-row {
    display: grid;
    grid-template-columns: 130px 1fr;
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
}
</style>
