<template>
  <div>
    <FilterBar by-area @setFilterBarParams="setFilterBarParams" />
    <div class="user-loyalty public-hoverItem">
      <div class="public-firstHead loyalty-title">忠诚度分析</div>
      <el-tabs v-model="activeTab" @tab-click="renderChart">
        <el-tab-pane v-for="item in tabConfig" :key="item.name" :label="item.label" :name="item.name" />
      </el-tabs>
      <div ref="chart" class="loyalty-chart" />
      <el-table
        border
        class="public-radius"
        :data="activeRows"
        :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column :label="activeConfig.dimensionLabel" prop="key" min-width="220" />
        <el-table-column :label="activeConfig.valueLabel" prop="value" min-width="120" />
        <el-table-column label="所占比列" min-width="120">
          <template slot-scope="scope">{{ formatRate(scope.row.rate) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import echarts from 'echarts'
import { FilterBar } from '@/layout/components'
import {
  getUserDepthApi,
  getUserLatestTimeApi,
  getUserPvApi,
  getUserVisitApi,
  getUserVisitTimeApi
} from '@/api/trackingapi/uservisit'
import { copyObj } from '@/utils/copy'

export default {
  name: 'UserLoyaltyAnalysis',
  components: {
    FilterBar
  },
  data() {
    return {
      filterBarParams: {},
      activeTab: 'visitPage',
      chart: null,
      rowsMap: {
        visitPage: [],
        visitDepth: [],
        visitTime: [],
        latestTime: [],
        visitNum: []
      },
      tabConfig: [
        { name: 'visitPage', label: '访问页数', dimensionLabel: '单次访问页数(页面不去重)', valueLabel: '访问次数' },
        { name: 'visitDepth', label: '访问深度', dimensionLabel: '单次访问页数(页面去重)', valueLabel: '访问次数' },
        { name: 'visitTime', label: '访问时长', dimensionLabel: '单次访问时长', valueLabel: '访问次数' },
        { name: 'latestTime', label: '上次访问时间', dimensionLabel: '上次访问时间', valueLabel: '访客数' },
        { name: 'visitNum', label: '访问频次', dimensionLabel: '访问频次', valueLabel: '访客数' }
      ]
    }
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName
    },
    commonParams() {
      const { projectName } = this
      return Object.assign({ projectName }, this.filterBarParams)
    },
    activeConfig() {
      return this.tabConfig.find((item) => item.name === this.activeTab) || this.tabConfig[0]
    },
    activeRows() {
      return this.rowsMap[this.activeTab] || []
    }
  },
  watch: {
    commonParams() {
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
    setFilterBarParams(val) {
      this.filterBarParams = copyObj(val)
    },
    loadData() {
      Promise.all([
        getUserPvApi(this.commonParams),
        getUserDepthApi(this.commonParams),
        getUserVisitTimeApi(this.commonParams),
        getUserLatestTimeApi(this.commonParams),
        getUserVisitApi(this.commonParams)
      ]).then(([pvRes, depthRes, visitTimeRes, latestTimeRes, visitNumRes]) => {
        const pvRows = pvRes.data || []
        this.rowsMap.visitPage = pvRows
        this.rowsMap.visitDepth = depthRes.data || []
        this.rowsMap.visitTime = visitTimeRes.data || []
        this.rowsMap.latestTime = latestTimeRes.data || []
        this.rowsMap.visitNum = visitNumRes.data || []
        this.$nextTick(this.renderChart)
      })
    },
    renderChart() {
      if (!this.chart) {
        this.chart = echarts.init(this.$refs.chart)
      }
      this.chart.clear()
      const xData = this.activeRows.map((item) => item.key)
      const yData = this.activeRows.map((item) => item.value || 0)
      this.chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 42, right: 24, top: 28, bottom: 72 },
        xAxis: {
          type: 'category',
          data: xData,
          axisLabel: { interval: 0, rotate: xData.length > 8 ? 30 : 0 }
        },
        yAxis: { type: 'value' },
        series: [
          {
            name: this.activeConfig.label,
            type: 'bar',
            barWidth: 28,
            data: yData,
            label: { show: true, position: 'top' },
            itemStyle: { color: '#2c7be5' }
          }
        ]
      })
    },
    formatRate(rate) {
      const value = Number(rate || 0)
      return `${(value * 100).toFixed(2)}%`
    }
  }
}
</script>

<style lang="scss" scoped>
.user-loyalty {
  margin: 20px;
  padding: 18px 16px 24px;
  background: #fff;
}

.loyalty-title {
  margin-bottom: 8px;
}

.loyalty-chart {
  width: 100%;
  height: 360px;
}
</style>
