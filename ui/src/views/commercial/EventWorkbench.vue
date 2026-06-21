<template>
  <div class="commercial-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini">
        <el-form-item v-if="needsDate" label="时间">
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
        <el-form-item v-if="isManageList">
          <el-input v-model="keyword" clearable placeholder="请输入关键字" @keyup.enter.native="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-refresh-right" @click="loadData">刷新</el-button>
          <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
          <el-button v-if="mode === 'bookmark'" type="primary" icon="el-icon-plus" @click="saveBookmark">
            保存书签
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="public-tableHead">{{ pageTitle }}</div>

        <el-table
          v-loading="loading"
          border
          class="public-radius"
          :data="rows"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="序号" width="70" />

          <template v-if="mode === 'metaEvent'">
            <el-table-column prop="displayName" label="事件显示名" min-width="140" />
            <el-table-column prop="eventName" label="事件名" min-width="150" />
            <el-table-column prop="groupName" label="分组" min-width="120" />
            <el-table-column prop="tag" label="标签" min-width="100" />
            <el-table-column prop="status" label="状态" width="100" />
          </template>

          <template v-else-if="mode === 'eventAttribute' || mode === 'userAttribute'">
            <el-table-column prop="displayName" label="属性显示名" min-width="140" />
            <el-table-column prop="propertyName" label="属性名" min-width="160" />
            <el-table-column prop="groupName" label="分组" min-width="120" />
            <el-table-column prop="dataType" label="数据类型" width="110" />
            <el-table-column prop="tag" label="标签" min-width="100" />
            <el-table-column prop="status" label="状态" width="100" />
          </template>

          <template v-else-if="mode === 'log'">
            <el-table-column prop="distinctId" label="用户ID" min-width="150" />
            <el-table-column prop="eventSessionId" label="会话ID" min-width="150" />
            <el-table-column label="时间" min-width="170">
              <template slot-scope="scope">{{ formatTime(scope.row.logTime || scope.row.time) }}</template>
            </el-table-column>
            <el-table-column prop="event" label="事件" min-width="120" />
            <el-table-column prop="title" label="页面标题" min-width="150" />
            <el-table-column prop="uri" label="页面地址" min-width="260" show-overflow-tooltip />
          </template>

          <template v-else-if="mode === 'bookmark'">
            <el-table-column prop="name" label="书签名称" min-width="160" />
            <el-table-column prop="analysisType" label="分析类型" min-width="120" />
            <el-table-column prop="description" label="描述" min-width="220" />
            <el-table-column prop="createTime" label="创建时间" min-width="170" />
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button type="text" class="danger" @click="removeBookmark(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </template>

          <template v-else>
            <el-table-column prop="event" label="事件" min-width="160" />
            <el-table-column prop="eventCount" label="事件次数" min-width="120" />
            <el-table-column prop="userCount" label="用户数" min-width="120" />
          </template>
        </el-table>

        <el-pagination
          v-if="isPaged"
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
import { customAnalysisApi } from "@/api/sysmanage/customAnalysis";
import { getPageListEventApi } from "@/api/sysmanage/manageEvent";
import { getPageListApi as getPropertyPageListApi } from "@/api/sysmanage/manageAttribute";
import {
  addApi as addBookmarkApi,
  deleteApi as deleteBookmarkApi,
  getPageListApi as getBookmarkPageListApi,
} from "@/api/sysmanage/bookMark";
import { getLogAnalysisListApi } from "@/api/trackingapi/visitor";

export default {
  name: "EventWorkbench",
  data() {
    return {
      loading: false,
      keyword: "",
      dateRange: this.todayRange(),
      rows: [],
      total: 0,
      pageNum: 1,
      pageSize: 20,
    };
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName;
    },
    mode() {
      return this.$route.meta.workbenchMode || "stat";
    },
    pageTitle() {
      return this.$route.meta.title || "数据统计";
    },
    needsDate() {
      return ["log", "stat", "custom"].includes(this.mode);
    },
    isManageList() {
      return ["metaEvent", "eventAttribute", "userAttribute", "bookmark"].includes(this.mode);
    },
    isPaged() {
      return ["metaEvent", "eventAttribute", "userAttribute", "log", "bookmark"].includes(this.mode);
    },
  },
  watch: {
    "$route.path"() {
      this.reset();
    },
    projectName() {
      this.reset();
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
      this.keyword = "";
      this.dateRange = this.todayRange();
      this.pageNum = 1;
      this.loadData();
    },
    loadData() {
      const loaders = {
        metaEvent: this.loadMetaEvents,
        eventAttribute: this.loadProperties,
        userAttribute: this.loadProperties,
        log: this.loadLogs,
        bookmark: this.loadBookmarks,
        stat: this.loadAnalysis,
        custom: this.loadAnalysis,
      };
      return (loaders[this.mode] || this.loadAnalysis)();
    },
    pageParams() {
      return {
        projectName: this.projectName,
        keyword: this.keyword,
        pageNum: this.pageNum,
        pageSize: this.pageSize,
      };
    },
    loadMetaEvents() {
      this.loading = true;
      getPageListEventApi(this.pageParams())
        .then((res) => {
          const data = res.data || {};
          this.rows = data.rows || [];
          this.total = data.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadProperties() {
      this.loading = true;
      getPropertyPageListApi(this.pageParams())
        .then((res) => {
          const data = res.data || {};
          this.rows = data.rows || [];
          this.total = data.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadLogs() {
      this.loading = true;
      getLogAnalysisListApi(Object.assign({}, this.pageParams(), {
        startTime: this.dateRange[0],
        endTime: this.dateRange[1],
      }))
        .then((res) => {
          const data = res.data || {};
          this.rows = data.rows || [];
          this.total = data.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadAnalysis() {
      this.loading = true;
      customAnalysisApi({
        projectName: this.projectName,
        startTime: this.dateRange[0],
        endTime: this.dateRange[1],
      })
        .then((res) => {
          this.rows = res.data || [];
          this.total = this.rows.length;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadBookmarks() {
      this.loading = true;
      getBookmarkPageListApi(Object.assign({}, this.pageParams(), {
        pageSize: 100,
      }))
        .then((res) => {
          const data = res.data || {};
          this.rows = data.rows || [];
          this.total = data.total || 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    saveBookmark() {
      addBookmarkApi({
        projectName: this.projectName,
        name: `${this.pageTitle}-${new Date().toLocaleString()}`,
        analysisType: "customAnalysis",
        description: "商业版兼容分析书签",
        queryJson: JSON.stringify({ route: this.$route.path }),
        createUser: this.$store.getters.name || "clklog",
      }).then(() => {
        this.$message.success("书签已保存");
        this.loadBookmarks();
      });
    },
    removeBookmark(row) {
      deleteBookmarkApi({ id: row.id, projectName: this.projectName }).then(() => {
        this.$message.success("书签已删除");
        this.loadBookmarks();
      });
    },
    handleCurrentChange(page) {
      this.pageNum = page;
      this.loadData();
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
