<template>
  <div class="mobile-summary-page">
    <div class="mobile-shell">
      <div class="mobile-header">
        <div>
          <span>{{ projectName }}</span>
          <strong>数据汇总</strong>
        </div>
        <el-button size="mini" icon="el-icon-refresh" circle :loading="loading" @click="loadData" />
      </div>

      <el-radio-group v-model="timeType" size="mini" class="time-tabs" @change="loadData">
        <el-radio-button label="day">日</el-radio-button>
        <el-radio-button label="week">周</el-radio-button>
        <el-radio-button label="month">月</el-radio-button>
      </el-radio-group>

      <el-date-picker
        v-model="dateRange"
        class="date-range"
        type="daterange"
        size="mini"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        @change="loadData"
      />

      <div class="metric-grid">
        <div v-for="item in metrics" :key="item.label" class="metric-item">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>

      <div v-loading="loading" class="summary-list">
        <div v-for="row in rows" :key="row.statTime" class="summary-row">
          <div class="row-head">
            <strong>{{ row.statTime }}</strong>
            <span>{{ formatPercent(row.bounceRate) }}</span>
          </div>
          <div class="row-grid">
            <span>PV {{ row.pv || 0 }}</span>
            <span>UV {{ row.uv || 0 }}</span>
            <span>访问 {{ row.visitCount || 0 }}</span>
            <span>IP {{ row.ipCount || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getFlowDetailApi } from '@/api/trackingapi/flow'

export default {
  name: 'MobileSummary',
  data() {
    return {
      loading: false,
      timeType: 'day',
      dateRange: this.todayRange(),
      rows: []
    }
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName || 'clklogapp'
    },
    totals() {
      return this.rows.reduce((acc, item) => {
        acc.pv += Number(item.pv || 0)
        acc.visitCount += Number(item.visitCount || 0)
        acc.uv += Number(item.uv || 0)
        acc.newUv += Number(item.newUv || 0)
        acc.ipCount += Number(item.ipCount || 0)
        return acc
      }, { pv: 0, visitCount: 0, uv: 0, newUv: 0, ipCount: 0 })
    },
    metrics() {
      return [
        { label: '浏览量', value: this.totals.pv },
        { label: '访问次数', value: this.totals.visitCount },
        { label: '访客数', value: this.totals.uv },
        { label: '新用户数', value: this.totals.newUv }
      ]
    }
  },
  watch: {
    projectName() {
      this.loadData()
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    todayRange() {
      const d = new Date()
      const value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      return [value, value]
    },
    loadData() {
      if (!this.dateRange || this.dateRange.length !== 2) {
        return
      }
      this.loading = true
      getFlowDetailApi({
        projectName: this.projectName,
        startTime: this.dateRange[0],
        endTime: this.dateRange[1],
        timeType: this.timeType,
        pageNum: 1,
        pageSize: 30
      })
        .then((res) => {
          this.rows = Array.isArray(res.data) ? res.data : (res.data && res.data.rows) || []
        })
        .finally(() => {
          this.loading = false
        })
    },
    formatPercent(value) {
      if (value === null || value === undefined || value === '') {
        return '--'
      }
      return `${(Number(value) * 100).toFixed(2)}%`
    }
  }
}
</script>

<style lang="scss" scoped>
.mobile-summary-page {
  min-height: 100vh;
  padding: 18px;
  background: #eef3f8;
}

.mobile-shell {
  max-width: 430px;
  min-height: 680px;
  margin: 0 auto;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 22px rgba(42, 56, 78, 0.08);
}

.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;

  span,
  strong {
    display: block;
  }

  span {
    color: #7b8794;
    font-size: 12px;
  }

  strong {
    margin-top: 4px;
    color: #1f2d3d;
    font-size: 20px;
  }
}

.time-tabs,
.date-range {
  width: 100%;
  margin-bottom: 12px;
}

::v-deep .time-tabs .el-radio-button {
  width: 33.333%;
}

::v-deep .time-tabs .el-radio-button__inner {
  width: 100%;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 14px 0;
}

.metric-item {
  padding: 12px;
  border: 1px solid #e7edf5;
  border-radius: 6px;
  background: #f8fbff;

  span {
    display: block;
    color: #7b8794;
    font-size: 12px;
  }

  strong {
    display: block;
    margin-top: 8px;
    color: #1f2d3d;
    font-size: 22px;
  }
}

.summary-row {
  padding: 12px 0;
  border-top: 1px solid #ecf0f5;
}

.row-head,
.row-grid {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.row-head {
  color: #1f2d3d;
}

.row-head span {
  color: #409eff;
}

.row-grid {
  flex-wrap: wrap;
  margin-top: 8px;
  color: #7b8794;
  font-size: 12px;
}

@media (max-width: 480px) {
  .mobile-summary-page {
    padding: 0;
  }

  .mobile-shell {
    min-height: 100vh;
    border-radius: 0;
    box-shadow: none;
  }
}
</style>
