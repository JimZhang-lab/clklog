<template>
    <div class="TopFilter">
      <div class="radio-com public_border_color">
        <span class="radio-com-label">时间：</span>
        <el-radio-group v-model="timeType" class="radio-com-el">
          <el-radio label="day">今日</el-radio>
          <el-radio label="week">本周</el-radio>
          <el-radio label="month" >本月</el-radio>
          <!-- <el-radio label="year">本年</el-radio> -->
        </el-radio-group>
      </div>

      <div v-if="allChaneList.length > 0" class="radio-com public_border_color channel-filter">
        <span class="radio-com-label">渠道：</span>
        <el-radio-group class="radio-com-el" v-model="channelValue">
          <el-radio
            v-for="item in allChaneList"
            :key="item.id"
            :label="item.id"
          >
            {{ item.displayName }}
          </el-radio>
        </el-radio-group>
      </div>

      <div class="tabBarBtn">
        <el-button
          :loading="loadingApi"
          class="zc_btn refresh"
          icon="el-icon-refresh-right"
          element-loading-spinner="el-icon-loading"
          element-loading-background="#fff"
          @click="refreshApiList"
        >
          刷新
        </el-button>
        <el-button
          class="zc_btn_default reset"
          icon="el-icon-refresh"
          @click="resetApiList"
        >
          重置
        </el-button>
      </div>
    </div>
</template>

<script>
import { timestampToTime } from "@/utils/timestampToTime";
import { formatDate } from "@/utils/format";
export default {
  name: "TopFilter",
  data() {
    return {
      timeType: "day",
      channelValue: "",
      weekEnd: "",
      weekStart: "",
      loadingApi: false,
    };
  },
  computed: {
    allChaneList() {
      return this.$store.getters.channelList || [];
    },
    channel() {
      return [this.channelValue];
    },
    startTime() {
      switch (this.timeType) {
        case "day":
          return formatDate(new Date());
          break;
        case "week":
          // let toData =
          //   new Date(new Date().toLocaleDateString()).getTime() +
          //   8 * 3600 * 1000;
          // let timeDifference = toData - 6 * 3600 * 24 * 1000;
          // return timestampToTime(timeDifference);
          this.getWeek();
          return this.weekStart;
          break;
        case "month":
          let monthStart = new Date();
          monthStart.setDate(1);
          monthStart = this.formData(monthStart);
          return monthStart;
          break;
        case "year":
          let today = formatDate(new Date());
          let yearData = this.getToYear(today.slice(0, 4)).split("/");
          return yearData[0];
          break;
        default:
          return formatDate(new Date());
          break;
      }
    },
    endTime() {
      switch (this.timeType) {
        case "day":
          return formatDate(new Date());
          break;
        case "week":
          // return formatDate(new Date());
          this.getWeek();
          return this.weekEnd;
          break;
        case "month":
          let monthStart = new Date();
          monthStart.setDate(1);
          monthStart = this.formData(monthStart);
          let monthEnd = new Date(monthStart);
          monthEnd.setMonth(monthEnd.getMonth() + 1);
          monthEnd.setDate(0);
          monthEnd = this.formData(monthEnd);
          return monthEnd;
          break;
        case "year":
          let today = formatDate(new Date());
          let yearData = this.getToYear(today.slice(0, 4)).split("/");
          return yearData[1];
          break;

        default:
          return formatDate(new Date());
          break;
      }
    },
    implied() {
      const { startTime, endTime } = this;
      return { startTime, endTime };
    },
    filterParams() {
      const { timeType, channel } = this;
      return { timeType, channel };
    },
  },
  mounted() {
    this.setTopFilterParams(this.filterParams);
  },
  watch: {
    filterParams(val) {
      return this.setTopFilterParams(val);
    },
  },
  methods: {
    resetApiList() {
      this.timeType = "day";
      this.channelValue = "";
    },
    refreshApiList() {
      this.loadingApi = true;
      this.setTopFilterParams(this.filterParams);
      setTimeout(() => {
        this.loadingApi = false;
      }, 500);
    },
    // 获取week 的函数
    getWeek() {
      let date = new Date();
      let day = date.getDate();
      let week = date.getDay();
      let month = date.getMonth();
      let year = date.getFullYear();
      this.weekStart = this.formData(new Date(year, month, day - week + 1));
      // this.weekEnd = this.formData(new Date(year, month, day - week + 7));
      this.weekEnd = this.formData(new Date(year, month, day));
    },
    setTopFilterParams(val) {
      let _val = Object.assign(val, this.implied);
      this.$emit("setTopFilterParams", _val);
      this.$emit("timeTypeCheck", this.timeType);
    },
    formData(val) {
      return (
        val.getFullYear() +
        "-" +
        (val.getMonth() + 1 < 10
          ? "0" + (val.getMonth() + 1)
          : val.getMonth() + 1) +
        "-" +
        (val.getDate() < 10 ? "0" + val.getDate() : val.getDate())
      );
    },
    getToYear(vars) {
      return vars + "-01-01/" + vars + "-12-31";
    },
  },
};
</script>

<style lang="scss" scoped>
@import "~@/styles/components/TopFilter.scss";
::v-deep {
  @import "~@/styles/components/custom-radio.scss";
}
</style>
