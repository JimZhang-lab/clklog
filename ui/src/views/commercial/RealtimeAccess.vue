<template>
  <div>
    <FilterBar @setFilterBarParams="setFilterBarParams" />
    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="section-head">
          <div class="public-tableHead">实时热门页面</div>
          <el-button size="mini" type="primary" icon="el-icon-refresh-right" @click="loadData">刷新</el-button>
        </div>
        <el-table
          v-loading="hotLoading"
          border
          class="public-radius"
          :data="hotPages"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="排名" width="70" />
          <el-table-column prop="title" label="页面标题" min-width="140" show-overflow-tooltip />
          <el-table-column prop="uri" label="页面Url" min-width="260" show-overflow-tooltip />
          <el-table-column prop="pv" label="访问量" width="120" />
          <el-table-column label="占比" width="120">
            <template slot-scope="scope">{{ formatPercent(scope.row.percent) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="block-main public-hoverItem">
        <div class="public-tableHead">实时访问明细</div>
        <el-table
          v-loading="logLoading"
          border
          class="public-radius"
          :data="logs"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="序号" width="70" />
          <el-table-column prop="distinctId" label="访客ID" min-width="160" show-overflow-tooltip />
          <el-table-column prop="eventSessionId" label="会话ID" min-width="150" show-overflow-tooltip />
          <el-table-column label="访问时间" min-width="170">
            <template slot-scope="scope">{{ formatTime(scope.row.logTime || scope.row.time) }}</template>
          </el-table-column>
          <el-table-column prop="title" label="访问页面" min-width="160" show-overflow-tooltip />
          <el-table-column prop="uri" label="页面地址" min-width="260" show-overflow-tooltip />
          <el-table-column prop="latestReferrer" label="访问来源" min-width="160" show-overflow-tooltip />
          <el-table-column prop="lib" label="访问设备" width="110" />
        </el-table>
        <el-pagination
          class="pagination"
          next-text="下一页"
          :current-page="pageNum"
          :page-size="pageSize"
          layout="total, prev, pager, next, jumper"
          :total="total"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { FilterBar } from "@/layout/components";
import { getVisitUriApi } from "@/api/trackingapi/visituri";
import { getLogAnalysisListApi } from "@/api/trackingapi/visitor";

export default {
  name: "RealtimeAccess",
  components: { FilterBar },
  data() {
    return {
      filterParams: {},
      hotLoading: false,
      logLoading: false,
      hotPages: [],
      logs: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
    };
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName;
    },
    commonParams() {
      return Object.assign({ projectName: this.projectName }, this.filterParams);
    },
  },
  watch: {
    commonParams() {
      this.pageNum = 1;
      this.loadData();
    },
  },
  methods: {
    setFilterBarParams(params) {
      this.filterParams = Object.assign({}, params);
    },
    loadData() {
      this.loadHotPages();
      this.loadLogs();
    },
    loadHotPages() {
      this.hotLoading = true;
      getVisitUriApi(this.commonParams)
        .then((res) => {
          this.hotPages = Array.isArray(res.data) ? res.data : [];
        })
        .finally(() => {
          this.hotLoading = false;
        });
    },
    loadLogs() {
      this.logLoading = true;
      getLogAnalysisListApi(Object.assign({}, this.commonParams, {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
      }))
        .then((res) => {
          const data = res.data || {};
          this.logs = data.rows || [];
          this.total = data.total || 0;
        })
        .finally(() => {
          this.logLoading = false;
        });
    },
    handleCurrentChange(page) {
      this.pageNum = page;
      this.loadLogs();
    },
    formatPercent(value) {
      if (value === null || value === undefined || value === "") {
        return "--";
      }
      return `${(Number(value) * 100).toFixed(2)}%`;
    },
    formatTime(value) {
      if (!value) {
        return "--";
      }
      const date = new Date(Number(value));
      if (Number.isNaN(date.getTime())) {
        return value;
      }
      const pad = (item) => String(item).padStart(2, "0");
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
    },
  },
};
</script>

<style lang="scss" scoped>
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
</style>
