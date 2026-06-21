<template>
  <div>
    <user-analysis-filter mode="active" @change="setFilterParams" />
    <div class="analysis-panel public-hoverItem">
      <div class="public-firstHead">{{ activeLabel }}趋势</div>
      <div ref="chart" class="trend-chart" />
      <el-table
        v-loading="loading"
        border
        class="public-radius"
        :data="rows"
        :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="statTime" label="日期" min-width="160" />
        <el-table-column prop="userCount" label="用户总数" min-width="120" />
        <el-table-column prop="activeUserCount" :label="`${activeLabel}数`" min-width="130" />
      </el-table>
      <div class="desc-block">
        <p>活跃用户 打开应用的用户即为活跃用户，不考虑用户的使用情况</p>
        <p>日活跃用户 一日 (统计日)之内，访问了应用的用户 (去除重复访问的用户)</p>
        <p>周活跃用户 一周 (统计周)之内，访问了应用的用户 (去除重复访问的用户)</p>
        <p>月活跃用户 一月 (统计月) 之内，访问了应用的用户 (去除重复访问的用户)</p>
      </div>
    </div>
  </div>
</template>

<script>
import echarts from 'echarts'
import UserAnalysisFilter from './components/user-analysis-filter'
import { getUserActiveTrendApi } from '@/api/trackingapi/userAnalysis'

export default {
  name: 'ActiveUsers',
  components: {
    UserAnalysisFilter
  },
  data() {
    return {
      loading: false,
      filterParams: {},
      rows: [],
      chart: null
    }
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName
    },
    activeLabel() {
      const map = {
        day: '日活跃',
        week: '周活跃',
        month: '月活跃'
      }
      return map[this.filterParams.timeType] || '日活跃'
    }
  },
  watch: {
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
      getUserActiveTrendApi(Object.assign({ projectName: this.projectName }, this.filterParams))
        .then((res) => {
          this.rows = res.data || []
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
      const xData = this.rows.map((item) => item.statTime)
      const userCount = this.rows.map((item) => item.userCount || 0)
      const activeUserCount = this.rows.map((item) => item.activeUserCount || 0)
      this.chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['用户总数', `${this.activeLabel}数`] },
        grid: { left: 42, right: 24, top: 44, bottom: 52 },
        xAxis: { type: 'category', data: xData, axisLabel: { interval: 0, rotate: xData.length > 8 ? 30 : 0 }},
        yAxis: { type: 'value' },
        series: [
          { name: '用户总数', type: 'line', smooth: true, data: userCount, itemStyle: { color: '#2c7be5' }},
          { name: `${this.activeLabel}数`, type: 'bar', barWidth: 24, data: activeUserCount, itemStyle: { color: '#3abf8f' }}
        ]
      })
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
