import Vue from 'vue'
import Router from 'vue-router'
Vue.use(Router)
import Layout from '@/layout'
import visitorAnalysis from './modules/visitor-analysis'
import accessAnalysis from './modules/access-analysis'
import userBehavior from './modules/user-behavior'
import systemManageRouter from './modules/system-manage'
import userPortraitRouter from './modules/user-portrait'
import eventAnalysisRouter from './modules/event-analysis'
import authManageRouter from './modules/auth-manage'
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },
  {
    path: '/auth-redirect',
    component: () => import('@/views/login/auth-redirect'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error-page/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error-page/401'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/home/index'),
        name: 'Documentation',
        meta: { title: '数据概览', icon: 'homepage', affix: true }
      }
    ]
  },
  {
    path: '/realtime',
    component: Layout,
    redirect: '/realtime/access',
    children: [
      {
        path: 'access',
        component: () => import('@/views/commercial/RealtimeAccess'),
        name: 'realtimeAccess',
        meta: { title: '实时访问', icon: 'eye-open', affix: true }
      }
    ]
  },
  {
    path: '/record',
    component: Layout,
    redirect: '/record/summary',
    children: [
      {
        path: 'summary',
        component: () => import('@/views/commercial/RecordSummary'),
        name: 'recordSummary',
        meta: { title: '数据汇总', icon: 'chart', affix: true }
      }
    ]
  },
  {
    path: '/mobileSummary/index',
    alias: '/mobileSummary',
    component: () => import('@/views/commercial/MobileSummary'),
    name: 'mobileSummary',
    meta: { title: '数据汇总(移动端展示)', icon: 'phone', affix: true }
  },
  {
    path: '/trendAnalysis',
    component: Layout,
    redirect: '/trend',
    children: [
      {
        path: 'trend',
        component: () => import('@/views/visitor-analysis/trend-analysis'),
        name: 'trend',
        meta: { title: '趋势分析', icon: 'trend', affix: true }
      }
    ]
  }

]

export const asyncRoutes = [
  visitorAnalysis,
  accessAnalysis,
  userBehavior,
  {
    path: '/crashAnalysis',
    component: Layout,
    redirect: '/crashAnalysis/crash',
    children: [
      {
        path: 'crash',
        component: () => import('@/views/collapse-analysis/index'),
        name: 'crash',
        meta: { title: 'App崩溃分析', icon: 'trend', affix: true }
      }
    ]
  },
  eventAnalysisRouter,
  userPortraitRouter,
  {
    path: '/tabix',
    component: Layout,
    redirect: '/tabix/query',
    children: [
      {
        path: 'query',
        name: 'tabix',
        component: () => import('@/views/commercial/CustomSqlQuery'),
        meta: { title: '自定义SQL查询', icon: 'documentation', affix: true }
      }
    ]
  },
  systemManageRouter,
  authManageRouter,
  {
    path: '/apiKey',
    component: Layout,
    redirect: '/apiKey/manage',
    children: [
      {
        path: 'manage',
        component: () => import('@/views/sys-manage/api-key.vue'),
        name: 'manage',
        meta: { title: '密钥管理', icon: 'setting', affix: true }
      }
    ]
  },
  { path: '*', redirect: '/index', hidden: true }
]

const createRouter = () =>
  new Router({
    // mode: 'history', // require service support
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRoutes
  })

const router = createRouter()
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
