import { createRouter, createWebHashHistory } from 'vue-router'
import NotFound from '@renderer/views/not-found/index.vue'
import Login from '@renderer/views/login/index.vue'
import Home from '@renderer/views/home/index.vue'
import Gallery from '@renderer/views/gallery/index.vue'
import Settings from '@renderer/views/settings/index.vue'
import ErrLog from '@renderer/views/errlog/index.vue'
import OperLog from '@renderer/views/operlog/index.vue'
import UserManagement from '@renderer/views/user-management/index.vue'

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
      component: Login
    },
    {
      path: '/home',
      name: 'home',
      component: Home,
      meta: {
        locale: 'menu.home',
        nav: true
      }
    },
    {
      path: '/gallery',
      name: 'gallery',
      component: Gallery,
      meta: {
        locale: 'menu.gallery',
        nav: true
      }
    },
    {
      path: '/settings',
      name: 'settings',
      component: Settings,
      meta: {
        locale: 'menu.settings',
        nav: true
      }
    },
    {
      path: '/errlog',
      name: 'errlog',
      component: ErrLog
    },
    {
      path: '/operlog',
      name: 'operlog',
      component: OperLog
    },
    {
      path: '/userManagement',
      name: 'userManagement',
      component: UserManagement
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'notFound',
      component: NotFound
    }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
