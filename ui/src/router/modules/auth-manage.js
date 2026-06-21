import Layout from "@/layout";

const authManageRouter = {
  path: "/authManage",
  component: Layout,
  redirect: "/authManage/user",
  name: "authManage",
  alwaysShow: true,
  meta: {
    title: "权限管理",
    icon: "user",
  },
  children: [
    {
      path: "user",
      component: () => import("@/views/auth-manage/User"),
      name: "authUser",
      meta: { title: "账号管理" },
    },
    {
      path: "role",
      component: () => import("@/views/commercial/PermissionManage"),
      name: "roleManage",
      meta: { title: "角色管理", permissionMode: "role" },
    },
    {
      path: "menu",
      component: () => import("@/views/commercial/PermissionManage"),
      name: "menuManage",
      meta: { title: "菜单管理", permissionMode: "menu" },
    },
  ],
};

export default authManageRouter;
