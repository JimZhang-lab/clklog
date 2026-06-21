<template>
  <div class="commercial-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini">
        <el-form-item label="时间">
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
          <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
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
        <div class="public-tableHead">日志汇总</div>
        <el-table
          v-loading="loading"
          border
          class="public-radius"
          :data="[stat]"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column prop="projectName" label="应用名称" min-width="140" />
          <el-table-column prop="logRecordCount" label="receiver日志" min-width="130" />
          <el-table-column prop="dbRecordCount" label="clickhouse原始日志" min-width="160" />
          <el-table-column prop="logLatestTime" label="receiver最新时间" min-width="170" />
          <el-table-column prop="dbFirstTime" label="入库最早时间" min-width="170" />
          <el-table-column prop="dbLatestTime" label="入库最新时间" min-width="170" />
          <el-table-column label="数据库空间" min-width="120">
            <template slot-scope="scope">{{ formatBytes(scope.row.dbSpaceSize) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { getStat } from "@/api/sysmanage/globalsetting";

export default {
  name: "SystemLogSummary",
  data() {
    return {
      loading: false,
      dateRange: this.todayRange(),
      stat: {},
    };
  },
  computed: {
    metrics() {
      return [
        { label: "receiver日志", value: this.stat.logRecordCount || 0 },
        { label: "ClickHouse原始日志", value: this.stat.dbRecordCount || 0 },
        { label: "日志天数", value: this.stat.logDays || "--" },
        { label: "数据库空间", value: this.formatBytes(this.stat.dbSpaceSize) },
      ];
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
    reset() {
      this.dateRange = this.todayRange();
      this.loadData();
    },
    loadData() {
      this.loading = true;
      getStat({
        startTime: this.dateRange[0],
        endTime: this.dateRange[1],
      })
        .then((res) => {
          this.stat = res.data || {};
        })
        .finally(() => {
          this.loading = false;
        });
    },
    formatBytes(value) {
      const bytes = Number(value || 0);
      if (bytes < 1024) {
        return `${bytes} B`;
      }
      if (bytes < 1024 * 1024) {
        return `${(bytes / 1024).toFixed(2)} KB`;
      }
      return `${(bytes / 1024 / 1024).toFixed(2)} MB`;
    },
  },
};
</script>

<style lang="scss" scoped>
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
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
