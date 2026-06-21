<template>
  <div class="funnel-page">
    <div class="documentation-container">
      <el-form :inline="true" size="mini">
        <el-form-item label="时间">
          <el-date-picker
            v-model="dateRange"
            value-format="yyyy-MM-dd"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :picker-options="pickerOptions"
          />
        </el-form-item>
        <el-form-item label="已保存漏斗">
          <el-select v-model="selectedFunnelId" clearable filterable placeholder="选择漏斗" @change="applyFunnel">
            <el-option v-for="item in funnels" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计算方式">
          <el-radio-group v-model="measurement">
            <el-radio-button label="UV">人数</el-radio-button>
            <el-radio-button label="PV">次数</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="转化周期">
          <el-select v-model="windowPreset" style="width: 120px">
            <el-option v-for="item in windowOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-video-play" :loading="loading" @click="runAnalysis">开始分析</el-button>
          <el-button icon="el-icon-plus" @click="addStep">添加步骤</el-button>
          <el-button icon="el-icon-folder-add" @click="openSave">保存漏斗</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="public-block">
      <div class="block-main public-hoverItem">
        <div class="public-tableHead">漏斗步骤</div>
        <div class="steps">
          <div v-for="(step, index) in steps" :key="index" class="step-row">
            <div class="step-index">{{ index + 1 }}</div>
            <el-select v-model="step.event" filterable clearable allow-create default-first-option placeholder="选择或输入事件">
              <el-option
                v-for="event in events"
                :key="event.id || event.eventName"
                :label="event.displayName || event.eventName"
                :value="event.eventName"
              />
            </el-select>
            <el-input v-model="step.name" placeholder="步骤名称" />
            <el-button icon="el-icon-delete" circle :disabled="steps.length <= 1" @click="removeStep(index)" />
          </div>
        </div>
      </div>
    </div>

    <div class="public-block" v-if="resultSteps.length">
      <div class="block-main public-hoverItem">
        <div class="public-tableHead">分析结果</div>
        <div class="funnel-chart">
          <div v-for="item in resultSteps" :key="item.event" class="funnel-bar-row">
            <div class="funnel-label">
              <span>{{ item.name }}</span>
              <strong>{{ item.userCount }}</strong>
            </div>
            <div class="funnel-bar-track">
              <div class="funnel-bar" :style="{ width: barWidth(item) }" />
            </div>
            <div class="funnel-rate">
              较上步 {{ percent(item.stepRate) }} / 总转化 {{ percent(item.totalRate) }}
            </div>
          </div>
        </div>
        <el-table
          border
          class="public-radius"
          :data="resultSteps"
          :header-cell-style="{ textAlign: 'center', background: '#f7fafe' }"
          :cell-style="{ textAlign: 'center' }"
        >
          <el-table-column type="index" label="步骤" width="80" />
          <el-table-column prop="name" label="步骤名称" />
          <el-table-column prop="event" label="事件名" />
          <el-table-column prop="userCount" :label="measurement === 'PV' ? '次数' : '用户数'" />
          <el-table-column label="较上步转化率">
            <template slot-scope="scope">{{ percent(scope.row.stepRate) }}</template>
          </el-table-column>
          <el-table-column label="总转化率">
            <template slot-scope="scope">{{ percent(scope.row.totalRate) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog title="保存漏斗" :visible.sync="saveVisible" width="480px" :close-on-click-modal="false">
      <el-form ref="saveForm" :model="saveForm" :rules="saveRules" label-width="90px" size="mini">
        <el-form-item label="漏斗名称" prop="name">
          <el-input v-model="saveForm.name" placeholder="如 注册转化漏斗" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="saveForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="mini" @click="saveVisible = false">取消</el-button>
        <el-button size="mini" type="primary" @click="saveFunnel">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { customAnalysisApi, funnelAnalysisApi } from "@/api/sysmanage/customAnalysis";
import { addsApi, getPageListApi } from "@/api/sysmanage/funnelAnalysis";
import { getPageListEventApi } from "@/api/sysmanage/manageEvent";

export default {
  name: "FunnelAnalysis",
  data() {
    const end = new Date();
    const start = new Date();
    start.setTime(start.getTime() - 6 * 24 * 3600 * 1000);
    return {
      loading: false,
      saveVisible: false,
      selectedFunnelId: "",
      measurement: "UV",
      windowPreset: "24HOUR",
      windowOptions: [
        { label: "5分钟", value: "5MINUTE", numeral: 5, unit: "MINUTE" },
        { label: "1小时", value: "1HOUR", numeral: 1, unit: "HOUR" },
        { label: "24小时", value: "24HOUR", numeral: 24, unit: "HOUR" },
        { label: "7天", value: "7DAY", numeral: 7, unit: "DAY" },
        { label: "30天", value: "30DAY", numeral: 30, unit: "DAY" },
      ],
      events: [],
      funnels: [],
      resultSteps: [],
      dateRange: [this.formatDate(start), this.formatDate(end)],
      steps: [
        { name: "第一步", event: "" },
        { name: "第二步", event: "" },
      ],
      saveForm: {
        name: "",
        description: "",
      },
      saveRules: {
        name: [{ required: true, message: "请输入漏斗名称", trigger: "blur" }],
      },
      pickerOptions: {
        disabledDate(date) {
          return date.getTime() > Date.now();
        },
      },
    };
  },
  computed: {
    projectName() {
      return this.$store.getters.projectName;
    },
    maxUserCount() {
      if (!this.resultSteps.length) return 0;
      return Math.max.apply(
        null,
        this.resultSteps.map((item) => item.userCount || 0)
      );
    },
  },
  watch: {
    projectName() {
      this.selectedFunnelId = "";
      this.resultSteps = [];
      this.loadEvents();
      this.loadFunnels();
    },
  },
  mounted() {
    this.loadEvents();
    this.loadFunnels();
  },
  methods: {
    formatDate(date) {
      const y = date.getFullYear();
      const m = `${date.getMonth() + 1}`.padStart(2, "0");
      const d = `${date.getDate()}`.padStart(2, "0");
      return `${y}-${m}-${d}`;
    },
    loadEvents() {
      getPageListEventApi({
        projectName: this.projectName,
        pageNum: 1,
        pageSize: 200,
      }).then((res) => {
        const data = res.data || {};
        this.events = data.rows || [];
        if (!this.events.length) {
          const end = new Date();
          const start = new Date();
          start.setTime(start.getTime() - 30 * 24 * 3600 * 1000);
          customAnalysisApi({
            projectName: this.projectName,
            startTime: this.formatDate(start),
            endTime: this.formatDate(end),
          }).then((eventRes) => {
            this.events = (eventRes.data || []).map((item) => ({
              id: item.event,
              eventName: item.event,
              displayName: item.event,
            }));
          });
        }
      });
    },
    loadFunnels() {
      getPageListApi({
        projectName: this.projectName,
        pageNum: 1,
        pageSize: 100,
      }).then((res) => {
        const data = res.data || {};
        this.funnels = data.rows || [];
      });
    },
    addStep() {
      this.steps.push({ name: `第${this.steps.length + 1}步`, event: "" });
    },
    removeStep(index) {
      this.steps.splice(index, 1);
    },
    applyFunnel(id) {
      const funnel = this.funnels.find((item) => item.id === id);
      if (!funnel || !funnel.steps) return;
      try {
        const query = typeof funnel.queryJson === "string"
          ? JSON.parse(funnel.queryJson || "{}")
          : (funnel.queryJson || {});
        const steps = typeof funnel.steps === "string" ? JSON.parse(funnel.steps) : funnel.steps;
        this.steps = steps.map((item, index) => ({
          name: item.name || item.customName || `第${index + 1}步`,
          event: item.event || item.eventName || "",
        }));
        this.measurement = funnel.measurement || query.measurement || "UV";
        const numeral = funnel.windowNumeral || (query.window && query.window.numeral) || 24;
        const unit = funnel.windowUnit || (query.window && query.window.unit) || "HOUR";
        const matchedWindow = this.windowOptions.find((item) => item.numeral === numeral && item.unit === unit);
        this.windowPreset = matchedWindow ? matchedWindow.value : "24HOUR";
        this.saveForm.name = funnel.name;
        this.saveForm.description = funnel.description || "";
      } catch (e) {
        this.$message.error("漏斗步骤格式不正确");
      }
    },
    validSteps() {
      const steps = this.steps.filter((item) => item.event);
      if (!this.dateRange || this.dateRange.length !== 2) {
        this.$message.warning("请选择分析时间");
        return null;
      }
      if (!steps.length) {
        this.$message.warning("请至少选择一个事件步骤");
        return null;
      }
      return steps;
    },
    runAnalysis() {
      const steps = this.validSteps();
      if (!steps) return;
      this.loading = true;
      const window = this.currentWindow();
      funnelAnalysisApi({
        projectName: this.projectName,
        startTime: this.dateRange[0],
        endTime: this.dateRange[1],
        measurement: this.measurement,
        window,
        windowNumeral: window.numeral,
        windowUnit: window.unit,
        steps,
      })
        .then((res) => {
          const data = res.data || {};
          this.resultSteps = data.steps || [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    openSave() {
      if (!this.validSteps()) return;
      this.saveVisible = true;
    },
    saveFunnel() {
      this.$refs.saveForm.validate((valid) => {
        if (!valid) return;
        const window = this.currentWindow();
        const query = {
          name: this.saveForm.name,
          measurement: this.measurement,
          window,
          steps: this.validSteps(),
          projectName: this.projectName,
        };
        addsApi({
          projectName: this.projectName,
          name: this.saveForm.name,
          description: this.saveForm.description,
          measurement: this.measurement,
          windowNumeral: window.numeral,
          windowUnit: window.unit,
          steps: query.steps,
          query,
        }).then(() => {
          this.$message.success("保存成功");
          this.saveVisible = false;
          this.loadFunnels();
        });
      });
    },
    percent(value) {
      if (value === null || value === undefined) return "0%";
      return `${(Number(value) * 100).toFixed(2)}%`;
    },
    currentWindow() {
      const selected = this.windowOptions.find((item) => item.value === this.windowPreset);
      return {
        numeral: selected ? selected.numeral : 24,
        unit: selected ? selected.unit : "HOUR",
      };
    },
    barWidth(item) {
      if (!this.maxUserCount) return "0%";
      return `${Math.max(4, (item.userCount / this.maxUserCount) * 100)}%`;
    },
  },
};
</script>

<style lang="scss" scoped>
.funnel-page {
  .steps {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-top: 16px;
  }

  .step-row {
    display: grid;
    grid-template-columns: 36px minmax(220px, 1fr) minmax(180px, 1fr) 44px;
    gap: 12px;
    align-items: center;
  }

  .step-index {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: #2c7be5;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
  }

  .funnel-chart {
    margin: 18px 0 22px;
  }

  .funnel-bar-row {
    margin-bottom: 14px;
  }

  .funnel-label {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 13px;
    margin-bottom: 6px;
  }

  .funnel-bar-track {
    height: 18px;
    background: #edf2f9;
    border-radius: 4px;
    overflow: hidden;
  }

  .funnel-bar {
    height: 100%;
    background: #2c7be5;
  }

  .funnel-rate {
    color: #788195;
    font-size: 12px;
    margin-top: 4px;
  }
}
</style>
