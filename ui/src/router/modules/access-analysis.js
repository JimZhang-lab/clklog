import Layout from "@/layout";
const RouterView = { render: (h) => h("router-view") };

const nestedRouter = {
  path: "/access",
  component: Layout,
  redirect: "/access/interviewdPage/visitedPage",
  name: "access",
  alwaysShow: true,
  meta: {
    title: "访问分析",
    icon: "access",
  },
  children: [
    {
      path: "interviewdPage",
      component: RouterView,
      redirect: "/access/interviewdPage/visitedPage",
      name: "interviewdPage",
      alwaysShow: true,
      meta: { title: "受访页面" },
      children: [
        {
          path: "visitedPage",
          alias: "/access/visitedPage",
          component: () => import("@/views/commercial/VisitPageReport"),
          name: "visitedPage",
          meta: { title: "受访页面分析", reportMode: "visited" },
        },
        {
          path: "treePage",
          component: () => import("@/views/commercial/VisitPageReport"),
          name: "treePage",
          meta: { title: "结构化页面分析", reportMode: "tree" },
        },
        {
          path: "portalpage",
          component: () => import("@/views/commercial/VisitPageReport"),
          name: "portalpage",
          meta: { title: "入口页分析", reportMode: "entry" },
        },
        {
          path: "exitpage",
          component: () => import("@/views/commercial/VisitPageReport"),
          name: "exitpage",
          meta: { title: "退出页分析", reportMode: "exit" },
        },
      ],
    },
    {
      path: "searchTerm",
      component: RouterView,
      redirect: "/access/searchTerm/siteoutSearch",
      name: "searchTerm",
      alwaysShow: true,
      meta: { title: "搜索词" },
      children: [
        {
          path: "siteonSearch",
          component: () => import("@/views/visitor-analysis/search-analysis"),
          name: "siteonSearch",
          meta: { title: "站内搜索" },
        },
        {
          path: "siteoutSearch",
          alias: "/access/search",
          component: () => import("@/views/visitor-analysis/search-analysis"),
          name: "siteoutSearch",
          meta: { title: "站外搜索" },
        },
      ],
    },
    {
        path: "visitedPageLegacy",
        component: () => import("@/views/visitor-analysis/visited-page"),
        name: "visitedPageLegacy",
        meta: { title: "受访页面" },
        hidden: true,
      },
      {
        path: "searchLegacy",
        component: () => import("@/views/visitor-analysis/search-analysis"),
        name: "searchLegacy",
        meta: { title: "站外搜索" },
        hidden: true,
      },
  ],
};

export default nestedRouter;
