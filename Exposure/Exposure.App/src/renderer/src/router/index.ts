import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: 'login'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@renderer/views/login/index.vue')
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('@renderer/views/home/index.vue'),
      meta: {
        keepAlive: true,
        locale: 'menu.home',
        nav: true
      }
    },
    {
      path: '/gallery',
      name: 'gallery',
      component: () => import('@renderer/views/gallery/index.vue'),
      meta: {
        keepAlive: true,
        locale: 'menu.gallery',
        nav: true
      }
    },
    {
      path: '/gallery-chart',
      name: 'gallery-chart',
      component: () => import('@renderer/views/gallery/subpage/gallery-chart/index.vue')
    },
    {
      path: '/gallery-detail',
      name: 'gallery-detail',
      component: () => import('@renderer/views/gallery/subpage/gallery-detail/index.vue')
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@renderer/views/settings/index.vue'),
      meta: {
        keepAlive: true,
        locale: 'menu.settings',
        nav: true
      }
    },
    {
      path: '/debug',
      name: 'debug',
      component: () => import('@renderer/views/settings/subpage/debug/index.vue')
    },
    {
      path: '/errlog',
      name: 'errlog',
      component: () => import('@renderer/views/settings/subpage/err-log/index.vue')
    },
    {
      path: '/operlog',
      name: 'operlog',
      component: () => import('@renderer/views/settings/subpage/oper-log/index.vue')
    },
    {
      path: '/user-manage',
      name: 'user-manage',
      component: () => import('@renderer/views/settings/subpage/user-manage/index.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'notFound',
      component: () => import('@renderer/views/not-found/index.vue')
    }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
