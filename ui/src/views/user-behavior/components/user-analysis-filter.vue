<template>
  <div class="documentation-container user-analysis-filter">
    <div class="filter-row">
      <div class="filter-group public_border_color">
        <span>{{ modeTitle }}:</span>
        <el-radio-group v-model="timeType" class="checkBoxStyle" @change="emitChange">
          <el-radio v-for="item in timeOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
        </el-radio-group>
      </div>
      <div class="filter-group public_border_color">
        <span>时间:</span>
        <el-radio-group v-model="range" class="checkBoxStyle" @change="emitChange">
          <el-radio v-for="item in rangeOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
        </el-radio-group>
      </div>
      <el-button type="primary" size="mini" icon="el-icon-refresh-right" @click="emitChange">刷新</el-button>
      <el-button size="mini" icon="el-icon-refresh" @click="reset">重置</el-button>
    </div>
    <div class="filter-row second-row">
      <div class="filter-group public_border_color">
        <span>渠道:</span>
        <el-radio-group v-model="channelValue" class="checkBoxStyle" @change="emitChange">
          <el-radio v-for="item in channelOptions" :key="item.id" :label="item.id">{{ item.label }}</el-radio>
        </el-radio-group>
      </div>
      <div class="filter-group public_border_color">
        <span>地域:</span>
        <el-select v-model="provinceValue" size="mini" clearable filterable placeholder="全部" @change="emitChange">
          <el-option v-for="item in provinceData" :key="item.provinceName" :label="item.provinceName" :value="item.provinceName" />
        </el-select>
      </div>
    </div>
  </div>
</template>

<script>
import { province } from '@/utils/province'

export default {
  name: 'UserAnalysisFilter',
  props: {
    mode: {
      type: String,
      default: 'active'
    }
  },
  data() {
    return {
      timeType: 'day',
      range: '7',
      channelValue: '',
      provinceValue: '',
      channelOptions: [
        { label: '全部', id: '' },
        { label: '安卓', id: '安卓' },
        { label: '苹果', id: '苹果' },
        { label: '网站', id: '网站' },
        { label: '微信小程序', id: '微信小程序' }
      ],
      provinceData: province.filter((item) => item.provinceName !== '全部')
    }
  },
  computed: {
    modeTitle() {
      return this.mode === 'active' ? '活跃类型' : '统计方式'
    },
    timeOptions() {
      if (this.mode === 'active') {
        return [
          { label: '日活跃', value: 'day' },
          { label: '周活跃', value: 'week' },
          { label: '月活跃', value: 'month' }
        ]
      }
      return [
        { label: '按日', value: 'day' },
        { label: '按周', value: 'week' },
        { label: '按月', value: 'month' }
      ]
    },
    rangeOptions() {
      const firstText = this.mode === 'retained' ? '过去7天' : '近7天'
      const secondText = this.mode === 'retained' ? '过去14天' : '近14天'
      const thirdText = this.mode === 'retained' ? '过去30天' : '近30天'
      return [
        { label: firstText, value: '7' },
        { label: secondText, value: '14' },
        { label: thirdText, value: '30' }
      ]
    }
  },
  mounted() {
    this.emitChange()
  },
  methods: {
    reset() {
      this.timeType = 'day'
      this.range = '7'
      this.channelValue = ''
      this.provinceValue = ''
      this.emitChange()
    },
    emitChange() {
      const end = new Date()
      const start = new Date()
      start.setTime(end.getTime() - (Number(this.range) - 1) * 24 * 3600 * 1000)
      this.$emit('change', {
        timeType: this.timeType,
        startTime: this.formatDate(start),
        endTime: this.formatDate(end),
        channel: this.channelValue ? [this.channelValue] : [],
        province: this.provinceValue ? [this.provinceValue] : []
      })
    },
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
  }
}
</script>

<style lang="scss" scoped>
.user-analysis-filter {
  .filter-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 16px;
    padding-top: 15px;
  }

  .second-row {
    padding: 10px 0 15px;
  }

  .filter-group {
    display: flex;
    align-items: center;
    min-height: 30px;

    span {
      margin-right: 8px;
      color: #4d4d4d;
      font-size: 13px;
      font-weight: 500;
    }
  }
}
</style>
