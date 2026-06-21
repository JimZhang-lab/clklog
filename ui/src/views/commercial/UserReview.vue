<template>
  <div class="user-review-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini" :model="query" class="review-filter">
        <el-form-item label="用户筛选">
          <el-radio-group v-model="query.mode" size="mini" @change="handleModeChange">
            <el-radio-button label="id">按ID筛选</el-radio-button>
            <el-radio-button label="group">按分群筛选</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <template v-if="query.mode === 'id'">
          <el-form-item label="用户ID">
            <el-input v-model="query.userId" clearable placeholder="user_Id" @keyup.enter.native="startQuery" />
          </el-form-item>
          <el-form-item label="业务ID">
            <el-input v-model="query.loginId" clearable placeholder="login_id" @keyup.enter.native="startQuery" />
          </el-form-item>
          <el-form-item label="匿名ID">
            <el-input v-model="query.anonymousId" clearable placeholder="anonymous_id" @keyup.enter.native="startQuery" />
          </el-form-item>
        </template>
        <el-form-item v-else label="用户分群">
          <el-select v-model="query.groupId" clearable filterable placeholder="请选择分群">
            <el-option v-for="item in groups" :key="item.id" :label="item.displayName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="query.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" icon="el-icon-refresh-right" @click="loadAll">刷新</el-button>
          <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
          <el-button type="primary" icon="el-icon-search" @click="startQuery">开始查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="public-tableHead">用户细查</div>
        <el-table
          v-loading="loading"
          border
          class="public-radius"
          :data="pagedRows"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="序号" width="70" />
          <el-table-column label="用户ID（user_Id）" min-width="200" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.userId }}</template>
          </el-table-column>
          <el-table-column label="业务ID（login_id）" min-width="180" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.loginId }}</template>
          </el-table-column>
          <el-table-column label="匿名ID（anonymous_id）" min-width="200" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.anonymousId }}</template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" min-width="170" show-overflow-tooltip />
          <el-table-column prop="createTime" label="入库时间" min-width="170" show-overflow-tooltip />
          <el-table-column label="详情" width="100" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" @click="openDetail(scope.row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="pagination"
          :current-page="pageNum"
          :page-sizes="[10, 20, 30, 40]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredRows.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <dialogs ref="detailDialog" />
  </div>
</template>

<script>
import dialogs from '@/layout/components/dialog/index'
import { getVisitorListApi } from '@/api/trackingapi/visitor'
import { getPageListApi as getCdpPageListApi } from '@/api/sysmanage/cdpAsset'

export default {
  name: 'UserReview',
  components: {
    dialogs
  },
  data() {
    return {
      loading: false,
      rows: [],
      groups: [],
      query: this.emptyQuery(),
      pageNum: 1,
      pageSize: 20
    }
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName
    },
    selectedGroup() {
      return this.groups.find((item) => item.id === this.query.groupId) || {}
    },
    filteredRows() {
      const rows = this.rows.map(this.normalizeRow)
      if (this.query.mode === 'group') {
        const ids = this.selectedGroup.distinctIds || []
        if (!ids.length) {
          return this.query.groupId ? [] : rows
        }
        return rows.filter((item) => ids.includes(item.distinctId))
      }
      const userId = this.query.userId.trim()
      const loginId = this.query.loginId.trim()
      const anonymousId = this.query.anonymousId.trim()
      return rows.filter((item) => {
        const matchesUser = !userId || item.userId.indexOf(userId) !== -1
        const matchesLogin = !loginId || item.loginId.indexOf(loginId) !== -1
        const matchesAnonymous = !anonymousId || item.anonymousId.indexOf(anonymousId) !== -1
        return matchesUser && matchesLogin && matchesAnonymous
      })
    },
    pagedRows() {
      const from = (this.pageNum - 1) * this.pageSize
      return this.filteredRows.slice(from, from + this.pageSize)
    }
  },
  watch: {
    projectName() {
      this.loadAll()
    }
  },
  mounted() {
    this.loadAll()
  },
  methods: {
    emptyQuery() {
      return {
        mode: 'id',
        userId: '',
        loginId: '',
        anonymousId: '',
        groupId: '',
        timeRange: [this.daysAgo(30), this.today()]
      }
    },
    loadAll() {
      this.loading = true
      Promise.all([this.loadGroups(), this.loadVisitors()])
        .then(([groupRes, visitorRes]) => {
          const groupData = groupRes.data || {}
          const visitorData = visitorRes.data || {}
          this.groups = groupData.rows || []
          this.rows = visitorData.rows || []
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadGroups() {
      return getCdpPageListApi({
        projectName: this.projectName,
        assetType: 'group',
        status: 'enabled',
        pageNum: 1,
        pageSize: 500
      })
    },
    loadVisitors() {
      const range = this.query.timeRange || []
      return getVisitorListApi({
        projectName: this.projectName,
        startTime: range[0] || this.daysAgo(30),
        endTime: range[1] || this.today(),
        pageNum: 1,
        pageSize: 1000
      })
    },
    startQuery() {
      this.pageNum = 1
      this.loadAll()
    },
    reset() {
      this.query = this.emptyQuery()
      this.pageNum = 1
      this.loadAll()
    },
    handleModeChange() {
      this.query.userId = ''
      this.query.loginId = ''
      this.query.anonymousId = ''
      this.query.groupId = ''
      this.pageNum = 1
    },
    normalizeRow(row) {
      const distinctId = row.distinctId || ''
      const parts = distinctId.split('_')
      const loginId = row.loginId || row.login_id || (parts.length > 1 ? parts[0] : distinctId)
      const anonymousId = row.anonymousId || row.anonymous_id || (parts.length > 1 ? parts.slice(1).join('_') : distinctId)
      return Object.assign({}, row, {
        userId: row.userId || row.user_id || distinctId,
        loginId,
        anonymousId,
        updateTime: row.updateTime || row.latestTime || '--',
        createTime: row.createTime || row.firstTime || row.latestTime || '--'
      })
    },
    openDetail(row) {
      this.$refs.detailDialog.callMethod(row.distinctId)
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.pageNum = 1
    },
    handleCurrentChange(page) {
      this.pageNum = page
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
.user-review-page {
  .review-filter {
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
}
</style>
