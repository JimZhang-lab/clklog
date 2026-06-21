import Layout from "@/layout";
const nestedRouter = {
  path: "/userbehavior",
  alias: "/userBehavior",
  component: Layout,
  redirect: "/userbehavior/userBehavior",
  name: "userBehavior",
  alwaysShow: true,
  meta: {
    title: "用户分析",
    icon: "userBehavior",
  },
  children: [
    {
      path: "userBehavior",
      alias: "/userBehavior/portrait",
      component: () => import("@/views/user-behavior/index"),
      name: "portrait",
      meta: { title: "用户画像" },
    },
    {
      path: "activeUsers",
      component: () => import("@/views/user-behavior/index"),
      name: "activeUsers",
      meta: { title: "活跃用户分析" },
    },
    {
      path: "loyaltyAnalysis",
      alias: "/userBehavior/userLoyalty",
      component: () => import("@/views/visitor-analysis/user-loyalty-analysis"),
      name: "userLoyalty",
      meta: { title: "忠诚度分析" },
    },
    {
      path: "retainedUsers",
      component: () => import("@/views/commercial/CdpList"),
      name: "retainedUsers",
      meta: { title: "流失/留存用户", cdpMode: "group" },
    },
    {
      path: "silentUsers",
      component: () => import("@/views/commercial/CdpList"),
      name: "silentUsers",
      meta: { title: "回流/沉默用户", cdpMode: "group" },
    },
    {
      path: "userDetail",
      component: () => import("@/views/user-behavior/index"),
      name: "userDetail",
      meta: { title: "用户细查" },
      hidden: true,
    },
    {
      path: "funnelAnalysis",
      component: () => import("@/views/funnel-analysis/index"),
      name: "funnelAnalysisLegacy",
      meta: { title: "漏斗分析" },
      hidden: true,
    },
  ],
};

export default nestedRouter;
