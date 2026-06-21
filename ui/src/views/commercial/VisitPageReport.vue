<template>
  <div>
    <FilterBar ByArea @setFilterBarParams="setFilterBarParams" />
    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="section-head">
          <div class="public-tableHead">{{ pageTitle }}</div>
          <el-input
            v-if="mode !== 'tree'"
            v-model="keyword"
            clearable
            size="mini"
            class="keyword-input"
            placeholder="请输入页面标题或页面URL"
            @keyup.enter.native="loadData"
          />
          <el-button size="mini" type="primary" icon="el-icon-refresh-right" @click="loadData">刷新</el-button>
        </div>

        <el-table
          v-loading="loading"
          border
          class="public-radius"
          :data="rows"
          row-key="id"
          :tree-props="{ children: 'children' }"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="序号" width="70" />
          <el-table-column prop="title" label="页面标题" min-width="160" show-overflow-tooltip />
          <el-table-column prop="uri" label="页面URL" min-width="260" show-overflow-tooltip />
          <el-table-column prop="pv" label="浏览量" width="100" />
          <el-table-column prop="uv" label="访客数" width="100" />
          <el-table-column prop="visitCount" label="访问次数" width="100" />
          <el-table-column prop="ipCount" label="IP数" width="90" />
          <el-table-column prop="entryCount" label="入口页次数" width="120" />
          <el-table-column v-if="mode === 'exit'" prop="exitCount" label="退出页次数" width="120" />
          <el-table-column v-if="mode === 'exit'" label="退出率" width="100">
            <template slot-scope="scope">{{ formatPercent(scope.row.exitRate) }}</template>
          </el-table-column>
          <el-table-column label="平均访问时长" width="130">
            <template slot-scope="scope">{{ formatDuration(scope.row.avgVisitTime) }}</template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="mode !== 'tree'"
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
import {
  getVisitUriDetailApi,
  getVisitUriPathTreeTotalApi,
} from "@/api/trackingapi/visituri";

export default {
  name: "VisitPageReport",
  components: { FilterBar },
  data() {
    return {
      filterParams: {},
      keyword: "",
      loading: false,
      rows: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
    };
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName;
    },
    mode() {
      return this.$route.meta.reportMode || "visited";
    },
    pageTitle() {
      return this.$route.meta.title || "受访页面分析";
    },
    commonParams() {
      return Object.assign({ projectName: this.projectName }, this.filterParams);
    },
  },
  watch: {
    "$route.path"() {
      this.pageNum = 1;
      this.loadData();
    },
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
      if (this.mode === "tree") {
        this.loadTree();
      } else {
        this.loadList();
      }
    },
    loadTree() {
      this.loading = true;
      getVisitUriPathTreeTotalApi(this.commonParams)
        .then((res) => {
          this.rows = this.mapTree(res.data || [], "0");
          this.total = this.rows.length;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadList() {
      this.loading = true;
      const params = Object.assign({}, this.commonParams, {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        keyword: this.keyword,
      });
      getVisitUriDetailApi(params)
        .then((res) => {
          const data = res.data || {};
          const rows = data.rows || [];
          const filteredRows = rows.filter((item) => this.matchesMode(item));
          this.rows = filteredRows.length ? filteredRows : rows;
          this.total = data.total || this.rows.length;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    matchesMode(item) {
      if (this.mode === "entry") {
        return Number(item.entryCount || 0) > 0;
      }
      if (this.mode === "exit") {
        return Number(item.exitCount || 0) > 0;
      }
      return true;
    },
    mapTree(list, prefix) {
      return list.map((item, index) => {
        const detail = item.detail || item;
        const children = item.leafUri || item.children || [];
        return {
          id: `${prefix}-${index}`,
          title: item.uriPath || item.title || detail.title,
          uri: item.uriPath || detail.uri || detail.uriPath,
          pv: detail.pv,
          uv: detail.uv,
          visitCount: detail.visitCount,
          ipCount: detail.ipCount,
          entryCount: detail.entryCount,
          exitCount: detail.exitCount,
          exitRate: detail.exitRate,
          avgVisitTime: detail.avgVisitTime,
          children: this.mapTree(children, `${prefix}-${index}`),
        };
      });
    },
    handleCurrentChange(page) {
      this.pageNum = page;
      this.loadList();
    },
    formatPercent(value) {
      if (value === null || value === undefined || value === "") {
        return "--";
      }
      const numeric = Number(value);
      return `${(numeric > 1 ? numeric : numeric * 100).toFixed(2)}%`;
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
.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.keyword-input {
  width: 260px;
  margin-left: auto;
}
</style>
