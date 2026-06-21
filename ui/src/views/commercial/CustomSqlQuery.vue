<template>
  <div class="commercial-page">
    <div class="documentation-container">
      <el-form size="mini" :inline="true">
        <el-form-item label="返回行数">
          <el-input-number v-model="pageSize" :min="1" :max="1000" :step="50" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-caret-right" :loading="loading" @click="runQuery">执行</el-button>
          <el-button icon="el-icon-refresh" @click="resetSql">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="public-tableHead">自定义SQL查询</div>
        <el-input
          v-model="sql"
          type="textarea"
          :rows="8"
          resize="vertical"
          spellcheck="false"
          class="sql-editor"
        />
        <div v-if="result.executedSql" class="query-meta">
          <span>{{ result.rowCount }} 行</span>
          <span>{{ result.elapsedMs }} ms</span>
          <span>{{ result.executedSql }}</span>
        </div>
        <el-table
          v-loading="loading"
          border
          class="public-radius result-table"
          :data="result.rows"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="序号" width="70" />
          <el-table-column
            v-for="column in result.columns"
            :key="column"
            :prop="column"
            :label="column"
            min-width="150"
            show-overflow-tooltip
          />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { queryCustomSqlApi } from '@/api/trackingapi/customSql'

export default {
  name: 'CustomSqlQuery',
  data() {
    return {
      loading: false,
      pageSize: 100,
      sql: this.defaultSql(),
      result: {
        columns: [],
        rows: [],
        rowCount: 0,
        elapsedMs: 0,
        executedSql: ''
      }
    }
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName || 'clklogapp'
    }
  },
  mounted() {
    this.resetSql()
  },
  methods: {
    defaultSql() {
      return [
        'select event, count() as event_count, countDistinct(distinct_id) as user_count',
        'from log_analysis',
        "where project_name = 'clklogapp'",
        'group by event',
        'order by event_count desc'
      ].join('\n')
    },
    resetSql() {
      this.sql = this.defaultSql().replace('clklogapp', this.projectName)
      this.result = {
        columns: [],
        rows: [],
        rowCount: 0,
        elapsedMs: 0,
        executedSql: ''
      }
    },
    runQuery() {
      this.loading = true
      queryCustomSqlApi({
        sql: this.sql,
        pageSize: this.pageSize,
        projectName: this.projectName
      })
        .then((res) => {
          this.result = res.data || this.result
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.sql-editor {
  margin-bottom: 14px;

  ::v-deep .el-textarea__inner {
    font-family: Menlo, Monaco, Consolas, "Courier New", monospace;
    line-height: 1.55;
  }
}

.query-meta {
  display: flex;
  gap: 14px;
  margin-bottom: 12px;
  color: #606266;
  font-size: 12px;
}

.result-table {
  min-height: 280px;
}
</style>
