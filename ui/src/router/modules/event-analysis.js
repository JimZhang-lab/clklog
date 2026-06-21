import Layout from "@/layout";

const RouterView = { render: (h) => h("router-view") };

const eventAnalysisRouter = {
  path: "/mete",
  component: Layout,
  redirect: "/mete/eventAnalysis",
  name: "mete",
  alwaysShow: true,
  meta: {
    title: "事件分析",
    icon: "form",
  },
  children: [
    {
      path: "meteDate",
      component: RouterView,
      redirect: "/mete/meteDate/manage",
      name: "meteDate",
      alwaysShow: true,
      meta: { title: "元数据" },
      children: [
        {
          path: "manage",
          alias: "/sysManage/eventManage",
          component: () => import("@/views/sys-manage/event-manage"),
          name: "metaEventManage",
          meta: { title: "元事件" },
        },
        {
          path: "eventAttribute",
          alias: "/sysManage/propertyManage",
          component: () => import("@/views/sys-manage/property-manage"),
          name: "eventAttribute",
          meta: { title: "事件属性", workbenchMode: "eventAttribute" },
        },
        {
          path: "userAttribute",
          component: () => import("@/views/commercial/EventWorkbench"),
          name: "userAttribute",
          meta: { title: "用户属性", workbenchMode: "userAttribute" },
        },
      ],
    },
    {
      path: "logAnalysis",
      component: () => import("@/views/commercial/EventWorkbench"),
      name: "logAnalysis",
      meta: { title: "日志查询", workbenchMode: "log" },
    },
    {
      path: "eventAnalysis",
      component: () => import("@/views/commercial/EventWorkbench"),
      name: "eventAnalysis",
      meta: { title: "数据统计", workbenchMode: "stat" },
    },
    {
      path: "bookMark",
      component: () => import("@/views/commercial/EventWorkbench"),
      name: "bookMark",
      meta: { title: "我的书签", workbenchMode: "bookmark" },
    },
    {
      path: "customAnalysis",
      component: () => import("@/views/commercial/EventWorkbench"),
      name: "customAnalysis",
      meta: { title: "自定义分析", workbenchMode: "custom" },
    },
    {
      path: "funnelAnalysis",
      alias: "/userBehavior/funnelAnalysis",
      component: () => import("@/views/funnel-analysis/index"),
      name: "commercialFunnelAnalysis",
      meta: { title: "漏斗分析" },
    },
  ],
};

export default eventAnalysisRouter;
