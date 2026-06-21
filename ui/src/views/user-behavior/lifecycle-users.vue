<template>
  <div>
    <user-analysis-filter :mode="filterMode" @change="setFilterParams" />
    <div class="analysis-panel public-hoverItem">
      <div v-if="isRetained" class="mode-switch">
        <el-radio-group v-model="viewType" size="mini" @change="loadData">
          <el-radio-button label="churn">流失</el-radio-button>
          <el-radio-button label="retained">留存</el-radio-button>
        </el-radio-group>
      </div>
      <div class="public-firstHead">{{ pageTitle }}</div>
      <div ref="chart" class="trend-chart" />

      <el-table
        v-if="isRetained && viewType === 'retained'"
        v-loading="loading"
        border
        class="public-radius"
        :data="retentionRows"
        :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="statTime" label="日期" min-width="140" />
        <el-table-column prop="userCount" label="用户数" width="100" />
        <el-table-column label="次日" min-width="110">
          <template slot-scope="scope">{{ retentionCell(scope.row, 1) }}</template>
        </el-table-column>
        <el-table-column v-for="day in [2, 3, 4, 5, 6, 7]" :key="day" :label="`第${day}日`" min-width="110">
          <template slot-scope="scope">{{ retentionCell(scope.row, day) }}</template>
        </el-table-column>
      </el-table>

      <el-table
        v-else-if="isRetained"
        v-loading="loading"
        border
        class="public-radius"
        :data="lifeRows"
        :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="statTime" label="日期" min-width="140" />
        <el-table-column prop="userCount" label="用户数" width="110" />
        <el-table-column prop="churnUserCount" label="流失用户数" width="130" />
        <el-table-column label="流失率" width="110">
          <template slot-scope="scope">{{ percent(scope.row.churnUserCount, scope.row.userCount) }}</template>
        </el-table-column>
      </el-table>

      <el-table
        v-else
        v-loading="loading"
        border
        class="public-radius"
        :data="lifeRows"
        :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="statTime" label="日期" min-width="140" />
        <el-table-column prop="cumulativeUserCount" label="累计用户数" width="120" />
        <el-table-column prop="revisitUserCount" label="回流用户数" width="120" />
        <el-table-column prop="silentUserCount" label="沉默用户数" width="120" />
        <el-table-column prop="oldUserCount" label="老用户数" width="110" />
        <el-table-column prop="newUserCount" label="新用户数" width="110" />
        <el-table-column prop="userCount" label="用户数" width="100" />
      </el-table>

      <div class="desc-block">
        <template v-if="isRetained">
          <p>留存用户 选定时间范围开始日期前一日/周/月访问过且在选定时间范围内任意一日/周/月也有访问过的用户</p>
          <p>留存率 返回选定范围内每一日的留存数据，以统计日用户数为基准计算之后第 N 日留存率</p>
        </template>
        <template v-else>
          <p>回流用户 老访客中，当天有访问但前一日未访问的独立访客数。</p>
          <p>沉默用户 老访客中，当天未访问但前一日有访问的独立访客数。</p>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import echarts from 'echarts'
import UserAnalysisFilter from './components/user-analysis-filter'
import {
  getUserChurnTrendApi,
  getUserRemainTrendApi,
  getUserRevisitAndSilentTrendApi
} from '@/api/trackingapi/userAnalysis'

export default {
  name: 'LifecycleUsers',
  components: {
    UserAnalysisFilter
  },
  data() {
    return {
      loading: false,
      filterParams: {},
      lifeRows: [],
      retentionRows: [],
      viewType: 'retained',
      chart: null
    }
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName
    },
    mode() {
      return this.$route.meta.userAnalysisMode || 'retained'
    },
    isRetained() {
      return this.mode === 'retained'
    },
    filterMode() {
      return this.isRetained ? 'retained' : 'silent'
    },
    pageTitle() {
      if (!this.isRetained) {
        return '回流/沉默用户'
      }
      return this.viewType === 'retained' ? '留存趋势' : '流失趋势'
    }
  },
  watch: {
    '$route.path'() {
      this.lifeRows = []
      this.retentionRows = []
      this.viewType = 'retained'
      this.loadData()
    },
    projectName() {
      this.loadData()
    }
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    setFilterParams(params) {
      this.filterParams = params
      this.loadData()
    },
    loadData() {
      if (!this.filterParams.startTime) {
        return
      }
      this.loading = true
      const params = Object.assign({ projectName: this.projectName }, this.filterParams)
      const request = this.isRetained && this.viewType === 'retained'
        ? getUserRemainTrendApi(params)
        : this.isRetained
          ? getUserChurnTrendApi(params)
          : getUserRevisitAndSilentTrendApi(params)
      request
        .then((res) => {
          if (this.isRetained && this.viewType === 'retained') {
            this.retentionRows = res.data || []
          } else {
            this.lifeRows = res.data || []
          }
          this.$nextTick(this.renderChart)
        })
        .finally(() => {
          this.loading = false
        })
    },
    renderChart() {
      if (!this.chart) {
        this.chart = echarts.init(this.$refs.chart)
      }
      this.chart.clear()
      if (this.isRetained && this.viewType === 'retained') {
        const xData = this.retentionRows.map((item) => item.statTime)
        this.chart.setOption({
          tooltip: { trigger: 'axis' },
          legend: { data: ['次日', '第2日', '第3日', '第7日'] },
          grid: { left: 42, right: 24, top: 44, bottom: 52 },
          xAxis: { type: 'category', data: xData, axisLabel: { interval: 0, rotate: xData.length > 8 ? 30 : 0 }},
          yAxis: { type: 'value', axisLabel: { formatter: '{value}%' }},
          series: [
            { name: '次日', type: 'line', smooth: true, data: this.retentionRows.map((item) => item.day1Rate || 0) },
            { name: '第2日', type: 'line', smooth: true, data: this.retentionRows.map((item) => item.day2Rate || 0) },
            { name: '第3日', type: 'line', smooth: true, data: this.retentionRows.map((item) => item.day3Rate || 0) },
            { name: '第7日', type: 'line', smooth: true, data: this.retentionRows.map((item) => item.day7Rate || 0) }
          ]
        })
        return
      }
      const rows = this.lifeRows
      const xData = rows.map((item) => item.statTime)
      const series = this.isRetained
        ? [{ name: '流失用户数', type: 'bar', barWidth: 24, data: rows.map((item) => item.churnUserCount || 0) }]
        : [
          { name: '新用户', type: 'line', smooth: true, data: rows.map((item) => item.newUserCount || 0) },
          { name: '老用户', type: 'line', smooth: true, data: rows.map((item) => item.oldUserCount || 0) },
          { name: '回流用户', type: 'bar', barWidth: 18, data: rows.map((item) => item.revisitUserCount || 0) },
          { name: '沉默用户', type: 'bar', barWidth: 18, data: rows.map((item) => item.silentUserCount || 0) }
        ]
      this.chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: series.map((item) => item.name) },
        grid: { left: 42, right: 24, top: 44, bottom: 52 },
        xAxis: { type: 'category', data: xData, axisLabel: { interval: 0, rotate: xData.length > 8 ? 30 : 0 }},
        yAxis: { type: 'value' },
        series
      })
    },
    retentionCell(row, day) {
      const count = row[`day${day}Count`] || 0
      const rate = row[`day${day}Rate`] || 0
      return `${rate}% (${count})`
    },
    percent(value, total) {
      if (!total) {
        return '0%'
      }
      return `${((value || 0) * 100 / total).toFixed(2)}%`
    }
  }
}
</script>

<style lang="scss" scoped>
.analysis-panel {
  margin: 20px;
  padding: 18px 16px 22px;
  background: #fff;
}

.mode-switch {
  margin-bottom: 12px;
}

.trend-chart {
  width: 100%;
  height: 360px;
}

.desc-block {
  margin-top: 18px;
  padding: 14px 18px;
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
  background: #f7fafe;

  p {
    margin: 0;
  }
}
</style>
