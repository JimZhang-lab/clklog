import Layout from '@/layout'

const userPortraitRouter = {
  path: '/ups',
  alias: '/userPortrait',
  component: Layout,
  redirect: '/ups/tagCategory',
  name: 'userPortrait',
  alwaysShow: true,
  meta: {
    title: '用户画像管理(CDP）',
    icon: 'visitor'
  },
  children: [
    {
      path: 'tagCategory',
      alias: '/userPortrait/tagCategory',
      component: () => import('@/views/tag-system/category'),
      name: 'tagCategory',
      meta: { title: '标签分类' }
    },
    {
      path: 'userTag',
      alias: ['/userPortrait/userTag', '/ups/userTag/list'],
      component: () => import('@/views/tag-system/index'),
      name: 'userTag',
      meta: { title: '用户标签' }
    },
    {
      path: 'userGroup',
      alias: '/ups/userGroup/list',
      component: () => import('@/views/commercial/CdpList'),
      name: 'userGroup',
      meta: { title: '用户分群', cdpMode: 'group' }
    },
    {
      path: 'personalPortrait',
      alias: '/userBehavior/userDetail',
      component: () => import('@/views/user-behavior/index'),
      name: 'personalPortrait',
      meta: { title: '用户画像' }
    },
    {
      path: 'userportrait',
      alias: '/ups/userportrait/list',
      component: () => import('@/views/commercial/CdpList'),
      name: 'groupPortrait',
      meta: { title: '用户群画像', cdpMode: 'portrait' }
    },
    {
      path: 'userReview',
      alias: '/ups/userReview/list',
      component: () => import('@/views/commercial/UserReview'),
      name: 'userReview',
      meta: { title: '用户细查' }
    }
  ]
}

export default userPortraitRouter
