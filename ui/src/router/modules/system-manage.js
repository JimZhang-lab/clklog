import Layout from "@/layout";
const systemManageRouter = {
  path: "/sysManage",
  component: Layout,
  redirect: "/sysManage/appManage",
  name: "sysManage",
  alwaysShow: true,
  meta: {
    title: "系统设置",
    icon: "setting",
  },
  children: [
    {
      path: "appManage",
      component: () => import("@/views/sys-manage/app-manage"),
      name: "appManage",
      meta: { title: "项目管理", icon: "" },
    },
    {
      path: "globalSetting",
      component: () => import("@/views/sys-manage/global-setting"),
      name: "globalSetting",
      meta: { title: "全局设置", icon: "" },
    },
    {
      path: "sysLog",
      component: () => import("@/views/commercial/SystemLogSummary"),
      name: "sysLog",
      meta: { title: "日志汇总", icon: "" },
    },
    {
      path: "eventManage",
      component: () => import("@/views/sys-manage/event-manage"),
      name: "eventManage",
      meta: { title: "事件管理", icon: "" },
      hidden: true,
    },
    {
      path: "propertyManage",
      component: () => import("@/views/sys-manage/property-manage"),
      name: "propertyManage",
      meta: { title: "属性管理", icon: "" },
      hidden: true,
    },
    {
      path: "user",
      component: () => import("@/views/auth-manage/User"),
      name: "user",
      meta: { title: "账号管理", icon: "" },
      hidden: true,
    },
  ],
};
export default systemManageRouter;
