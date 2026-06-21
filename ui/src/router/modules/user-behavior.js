import Layout from '@/layout'
const nestedRouter = {
  path: '/userbehavior',
  alias: '/userBehavior',
  component: Layout,
  redirect: '/userbehavior/userBehavior',
  name: 'userBehavior',
  alwaysShow: true,
  meta: {
    title: '用户分析',
    icon: 'userBehavior'
  },
  children: [
    {
      path: 'userBehavior',
      alias: '/userBehavior/portrait',
      component: () => import('@/views/user-behavior/index'),
      name: 'portrait',
      meta: { title: '用户画像' }
    },
    {
      path: 'activeUsers',
      component: () => import('@/views/user-behavior/active-users'),
      name: 'activeUsers',
      meta: { title: '活跃用户分析' }
    },
    {
      path: 'loyaltyAnalysis',
      alias: '/userBehavior/userLoyalty',
      component: () => import('@/views/visitor-analysis/user-loyalty-analysis'),
      name: 'userLoyalty',
      meta: { title: '忠诚度分析' }
    },
    {
      path: 'retainedUsers',
      component: () => import('@/views/user-behavior/lifecycle-users'),
      name: 'retainedUsers',
      meta: { title: '流失/留存用户', userAnalysisMode: 'retained' }
    },
    {
      path: 'silentUsers',
      component: () => import('@/views/user-behavior/lifecycle-users'),
      name: 'silentUsers',
      meta: { title: '回流/沉默用户', userAnalysisMode: 'silent' }
    },
    {
      path: 'userDetail',
      component: () => import('@/views/user-behavior/index'),
      name: 'userDetail',
      meta: { title: '用户细查' },
      hidden: true
    },
    {
      path: 'funnelAnalysis',
      component: () => import('@/views/funnel-analysis/index'),
      name: 'funnelAnalysisLegacy',
      meta: { title: '漏斗分析' },
      hidden: true
    }
  ]
}

export default nestedRouter
