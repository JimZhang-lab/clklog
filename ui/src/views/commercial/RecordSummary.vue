<template>
  <div class="commercial-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini">
        <el-form-item label="项目名称">
          <el-input :value="projectName" disabled />
        </el-form-item>
        <el-form-item label="统计粒度">
          <el-radio-group v-model="timeType" @change="loadData">
            <el-radio-button label="day">按日</el-radio-button>
            <el-radio-button label="week">按周</el-radio-button>
            <el-radio-button label="month">按月</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            @change="loadData"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-refresh-right" @click="loadData">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="summary-grid">
        <div v-for="item in metrics" :key="item.label" class="metric-card public-hoverItem">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>

      <div class="block-main public-hoverItem">
        <div class="public-tableHead">数据汇总</div>
        <el-table
          v-loading="loading"
          border
          class="public-radius"
          :data="rows"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column prop="statTime" label="时间" min-width="120" />
          <el-table-column prop="pv" label="浏览量" min-width="100" />
          <el-table-column prop="visitCount" label="访问次数" min-width="100" />
          <el-table-column prop="uv" label="访客数" min-width="100" />
          <el-table-column prop="newUv" label="新用户数" min-width="100" />
          <el-table-column prop="ipCount" label="IP数" min-width="100" />
          <el-table-column prop="avgPv" label="平均访问页数" min-width="120" />
          <el-table-column label="平均访问时长" min-width="130">
            <template slot-scope="scope">{{ formatDuration(scope.row.avgVisitTime) }}</template>
          </el-table-column>
          <el-table-column label="跳出率" min-width="100">
            <template slot-scope="scope">{{ formatPercent(scope.row.bounceRate) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { getFlowDetailApi } from "@/api/trackingapi/flow";

export default {
  name: "RecordSummary",
  data() {
    return {
      loading: false,
      timeType: "day",
      dateRange: this.todayRange(),
      rows: [],
    };
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName;
    },
    totals() {
      return this.rows.reduce((acc, item) => {
        acc.pv += Number(item.pv || 0);
        acc.visitCount += Number(item.visitCount || 0);
        acc.uv += Number(item.uv || 0);
        acc.newUv += Number(item.newUv || 0);
        acc.ipCount += Number(item.ipCount || 0);
        acc.visitTime += Number(item.visitTime || 0);
        return acc;
      }, { pv: 0, visitCount: 0, uv: 0, newUv: 0, ipCount: 0, visitTime: 0 });
    },
    metrics() {
      return [
        { label: "浏览量", value: this.totals.pv },
        { label: "访问次数", value: this.totals.visitCount },
        { label: "访客数", value: this.totals.uv },
        { label: "新用户数", value: this.totals.newUv },
        { label: "IP数", value: this.totals.ipCount },
        { label: "总访问时长", value: this.formatDuration(this.totals.visitTime) },
      ];
    },
  },
  watch: {
    projectName() {
      this.loadData();
    },
  },
  mounted() {
    this.loadData();
  },
  methods: {
    todayRange() {
      const d = new Date();
      const value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
      return [value, value];
    },
    loadData() {
      if (!this.projectName || !this.dateRange || this.dateRange.length !== 2) {
        return;
      }
      this.loading = true;
      getFlowDetailApi({
        projectName: this.projectName,
        startTime: this.dateRange[0],
        endTime: this.dateRange[1],
        timeType: this.timeType,
        pageNum: 1,
        pageSize: 200,
      })
        .then((res) => {
          this.rows = Array.isArray(res.data) ? res.data : (res.data && res.data.rows) || [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    formatPercent(value) {
      if (value === null || value === undefined || value === "") {
        return "--";
      }
      return `${(Number(value) * 100).toFixed(2)}%`;
    },
    formatDuration(value) {
      const seconds = Math.round(Number(value || 0));
      const h = Math.floor(seconds / 3600);
      const m = Math.floor((seconds % 3600) / 60);
      const s = seconds % 60;
      return [h, m, s].map((item) => String(item).padStart(2, "0")).join(":");
    },
  },
};
</script>

<style lang="scss" scoped>
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.metric-card {
  min-height: 72px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  span {
    color: #727171;
    font-size: 13px;
  }

  strong {
    margin-top: 8px;
    color: #1f2d3d;
    font-size: 22px;
    font-weight: 600;
  }
}
</style>
