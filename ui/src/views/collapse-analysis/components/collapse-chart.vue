<template>
  <div class="chart_container">
    <div class="singItem">
      <div
        v-loading="loadingRefresh"
        class="block-main public-hoverItem collapseLeft"
      >
        <div class="block-head">
          <div class="block-title">崩溃率</div>
          <el-select
            v-model="headLege"
            class="custom_select"
            multiple
            placeholder="请选择"
            style="margin-left: 13px; min-width: 280px"
            @change="handleCheckPointer('all')"
          >
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <div style="display: flex; align-items: center">
                <span
                  class="checkbox__inner"
                ><div class="inner-box" /></span>
                <span style="">{{ item.label }}</span>
              </div>
            </el-option>
          </el-select>
        </div>

        <div id="countLine" style="width: 90%; height: 400px" />
      </div>
    </div>

    <div class="singItem" style="margin-top: 20px">
      <div
        v-loading="loadingRefresh"
        class="block-main public-hoverItem applicationLeft"
      >
        <div class="block-head">
          <div class="block-title">IOS|应用版本崩溃率</div>
          <el-select
            v-model="headLegeIos"
            class="custom_select"
            multiple
            placeholder="请选择"
            style="margin-left: 13px; min-width: 280px"
            @change="handleCheckPointer('IOS')"
          >
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <div style="display: flex; align-items: center">
                <span
                  class="checkbox__inner"
                ><div class="inner-box" /></span>
                <span style="">{{ item.label }}</span>
              </div>
            </el-option>
          </el-select>
        </div>
        <div style="display: flex; align-items: center" />
        <!-- 图表 -->
        <div
          id="Ioschart"
          style="width: 90%; height: 400px; margin-top: 10px"
        />
      </div>
      <div
        v-loading="loadingRefresh"
        class="block-main public-hoverItem applicationRight"
      >
        <div class="block-head">
          <div class="block-title">Andriod|应用版本崩溃率</div>
          <el-select
            v-model="headLegeAndior"
            class="custom_select"
            multiple
            placeholder="请选择"
            style="margin-left: 13px; min-width: 280px"
            @change="handleCheckPointer('Android')"
          >
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <div style="display: flex; align-items: center">
                <span
                  class="checkbox__inner"
                ><div class="inner-box" /></span>
                <span style="">{{ item.label }}</span>
              </div>
            </el-option>
          </el-select>
        </div>

        <!-- 图表 -->
        <div
          id="Andiorchart"
          style="width: 90%; height: 400px; margin-top: 10px"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { copyObj } from '@/utils/copy'
import echarts from 'echarts'
import { trendSummaryApi } from '@/api/trackingapi/collapse'
export default {
  data() {
    return {
      channel: '',
      headLege: ['崩溃触发次数', '崩溃率', '崩溃用户数'],
      headLegeAndior: ['崩溃触发次数', '崩溃率', '崩溃用户数'],
      headLegeIos: ['崩溃触发次数', '崩溃率', '崩溃用户数'],
      options: [
        {
          value: '访问次数',
          label: '访问次数',
          disabled: false
        },
        {
          value: '崩溃触发次数',
          label: '崩溃触发次数',
          disabled: false
        },
        {
          value: '崩溃率',
          label: '崩溃率',
          disabled: false
        },
        {
          value: '崩溃用户数',
          label: '崩溃用户数',
          disabled: false
        },
        {
          value: '用户数',
          label: '用户数',
          disabled: false
        },
        {
          value: '崩溃用户占比',
          label: '崩溃用户占比',
          disabled: false
        }
      ],
      commonParams: {},
      channelAllList: [],
      channelIosList: [],
      channelAndroidList: [],
      loadingRefresh: false,
      cologStyle: this.$store.state.settings.cologStyle,
      statTime: []
    }
  },
  methods: {
    paramsEvent(params) {
      this.commonParams = copyObj(params)
      this.silientTableEvent()
      this.silientTableEvent('安卓')
      this.silientTableEvent('苹果')
    },
    silientTableEvent(type) {
      this.loadingRefresh = true
      const params = copyObj(this.commonParams)
      params.channel = type ? [type] : []
      trendSummaryApi(params)
        .then((res) => {
          if (res.code === 200) {
            this.loadingRefresh = false
            if (!type) {
              this.channelAllList = res.data
              this.filterEvent('all')
            } else if (type === '苹果') {
              this.channelIosList = res.data
              this.filterEvent('IOS')
            } else if (type === '安卓') {
              this.channelAndroidList = res.data
              this.filterEvent('Android')
            }
          }
        })
        .catch(() => {
          this.loadingRefresh = false
        })
    },
    filterEvent(type) {
      this.statTime = []
      const visitCount = []
      const crashedCount = []
      const pvRate = []
      const crashedUv = []
      const uv = []
      const uvRate = []
      let newHeadLege = []
      let dynamicTrendList = []

      switch (type) {
        case 'all':
          newHeadLege = this.headLege
          dynamicTrendList = this.channelAllList
          break
        case 'IOS':
          newHeadLege = this.headLegeIos
          dynamicTrendList = this.channelIosList
          break
        case 'Android':
          newHeadLege = this.headLegeAndior
          dynamicTrendList = this.channelAndroidList
          break
      }
      for (let i = 0; i < dynamicTrendList.length; i++) {
        for (let j = 0; j < newHeadLege.length; j++) {
          if (newHeadLege[j] === '崩溃触发次数') {
            crashedCount.push(dynamicTrendList[i].crashedCount)
          }
          if (newHeadLege[j] === '访问次数') {
            visitCount.push(dynamicTrendList[i].visitCount)
          }
          if (newHeadLege[j] === '崩溃率') {
            pvRate.push(dynamicTrendList[i].pvRate)
          }
          if (newHeadLege[j] === '崩溃用户数') {
            crashedUv.push(dynamicTrendList[i].crashedUv)
          }
          if (newHeadLege[j] === '用户数') {
            uv.push(dynamicTrendList[i].uv)
          }
          if (newHeadLege[j] === '崩溃用户占比') {
            uvRate.push(dynamicTrendList[i].uvRate)
          }
        }
        this.statTime.push(dynamicTrendList[i].statTime)
      }

      const echartList = [
        {
          data: visitCount,
          type: 'line',
          name: '访问次数',
          smooth: true,
          color: this.cologStyle.$pv
        },
        {
          data: crashedCount,
          type: 'line',
          smooth: true,
          name: '崩溃触发次数',
          color: this.cologStyle.$uv
        },
        {
          data: pvRate,
          type: 'line',
          smooth: true,
          name: '崩溃率',
          color: this.cologStyle.$visitCount
        },
        {
          data: crashedUv,
          type: 'line',
          smooth: true,
          name: '崩溃用户数',
          color: this.cologStyle.$ipCount
        },
        {
          data: uv,
          type: 'line',
          smooth: true,
          name: '用户数',
          color: this.cologStyle.$bounceRate
        },
        {
          data: uvRate,
          type: 'line',
          smooth: true,
          name: '崩溃用户占比',
          color: 'pink'
        }
      ]

      switch (type) {
        case 'all':
          this.CountLineEcharts(echartList)
          break
        case 'IOS':
          this.IosLineEcharts(echartList)
          break
        case 'Android':
          this.AndiorLineEcharts(echartList)
          break
      }
    },
    handleCheckPointer(type) {
      this.filterEvent(type)
    },
    CountLineEcharts(echartList) {
      const countLine = echarts.init(document.getElementById('countLine'))
      const _that = this
      const countOption = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            lineStyle: {
              color: '#C0CADB',
              width: 2
            }
          },
          formatter: function(params) {
            var html = params[0].name + '<br>'
            let number = 0
            for (var i = 0; i < params.length; i++) {
              number = params[i].value
              if (
                params[i].seriesName === '崩溃率' ||
                params[i].seriesName === '崩溃用户占比'
              ) {
                number = _that.$options.filters.percenTable(params[i].value)
              }
              html +=
                '<div>' +
                params[i].marker +
                params[i].seriesName +
                '：' +
                number +
                '</div>'
            }
            return html + '<br>'
          }
        },
        legend: {
          x: 'center',
          y: 'top',
          data: this.headLege
        },
        grid: {
          left: '10%',
          right: '2%',
          top: '10%',
          bottom: '10%',
          containLabel: true
        },
        dataZoom: [
          {
            type: 'inside',
            startValue: 0,
            endValue: 30,
            minValueSpan: 10,
            zoomOnMouseWheel: false,
            moveOnMouseWheel: true,
            moveOnMouseMove: true
          }
        ],
        xAxis: {
          type: 'category',
          data: this.statTime,
          axisTick: {
            show: false
          },
          axisLabel: {
            interval: 2,
            rotate: '45'
          }
        },
        yAxis: {
          axisTick: {
            show: false
          },
          type: 'value'
        },
        series: echartList
      }
      countLine.setOption(countOption, true)
      window.addEventListener('resize', () => {
        countLine.resize()
      })
    },

    IosLineEcharts(echartList) {
      const Ioschart = echarts.init(document.getElementById('Ioschart'))
      const _that = this
      const Iosoption = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            lineStyle: {
              color: '#C0CADB',
              width: 2
            }
          },
          formatter: function(params) {
            var html = params[0].name + '<br>'
            let number = 0
            for (var i = 0; i < params.length; i++) {
              number = params[i].value
              if (
                params[i].seriesName === '崩溃率' ||
                params[i].seriesName === '崩溃用户占比'
              ) {
                number = _that.$options.filters.percenTable(params[i].value)
              }
              html +=
                '<div>' +
                params[i].marker +
                params[i].seriesName +
                '：' +
                number +
                '</div>'
            }
            return html + '<br>'
          }
        },
        legend: {
          x: 'center',
          y: 'top',
          data: this.headLegeIos
        },
        grid: {
          left: '10%',
          right: '2%',
          top: '10%',
          bottom: '10%',
          containLabel: true
        },

        dataZoom: [
          {
            type: 'inside',
            startValue: 0,
            endValue: 30,
            minValueSpan: 10,
            zoomOnMouseWheel: false,
            moveOnMouseWheel: true,
            moveOnMouseMove: true
          }
        ],

        xAxis: {
          type: 'category',
          data: this.statTime,
          axisTick: {
            show: false
          },
          axisLabel: {
            interval: 2,
            rotate: '45'
          }
        },
        yAxis: {
          axisTick: {
            show: false
          },
          type: 'value'
        },
        series: echartList
      }
      Ioschart.setOption(Iosoption, true)
      window.addEventListener('resize', () => {
        Ioschart.resize()
      })
    },
    AndiorLineEcharts(echartList) {
      const Andiorchart = echarts.init(document.getElementById('Andiorchart'))
      const _that = this
      const AndiorOption = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            lineStyle: {
              color: '#C0CADB',
              width: 2
            }
          },
          formatter: function(params) {
            var html = params[0].name + '<br>'
            let number = 0
            for (var i = 0; i < params.length; i++) {
              number = params[i].value
              if (
                params[i].seriesName === '崩溃率' ||
                params[i].seriesName === '崩溃用户占比'
              ) {
                number = _that.$options.filters.percenTable(params[i].value)
              }
              html +=
                '<div>' +
                params[i].marker +
                params[i].seriesName +
                '：' +
                number +
                '</div>'
            }
            return html + '<br>'
          }
        },
        legend: {
          x: 'center',
          y: 'top',
          data: this.headLegeAndior
        },
        grid: {
          left: '10%',
          right: '2%',
          top: '10%',
          bottom: '10%',
          containLabel: true
        },

        dataZoom: [
          {
            type: 'inside',
            startValue: 0,
            endValue: 30,
            minValueSpan: 10,
            zoomOnMouseWheel: false,
            moveOnMouseWheel: true,
            moveOnMouseMove: true
          }
        ],

        xAxis: {
          type: 'category',
          data: this.statTime,
          axisTick: {
            show: false
          },
          axisLabel: {
            interval: 2,
            rotate: '45'
          }
        },
        yAxis: {
          axisTick: {
            show: false
          },
          type: 'value'
        },
        series: echartList
      }
      Andiorchart.setOption(AndiorOption, true)
      window.addEventListener('resize', () => {
        Andiorchart.resize()
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/components/custom-select.scss";
::v-deep {
  .appli_select .el-input__inner {
    border-radius: 0px;
    border-top-width: 0px;
    border-left-width: 0px;
    border-right-width: 0px;
    border-bottom-width: 1px;
    border-bottom: 1px solid #acb2ba;
    background-color: transparent;
    font-size: 13px;
    transform: scale(0.9);
    height: 30px;
    line-height: 37px;
    border-bottom-width: 0;
  }
  .el-radio-button--mini .el-radio-button__inner {
    height: 30px;
  }
}
.chart_container {
  .singItem {
    display: flex;
    width: 100%;
    box-sizing: border-box;
    justify-content: space-between;
    .collapseLeft {
      width: 100%;
      min-height: 400px;
    }

    .applicationRight {
      width: calc(50% - 10px);
      min-height: 500px;
    }
    .applicationLeft {
      width: calc(50% - 10px);
      min-height: 500px;
    }
  }
}
</style>
