import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '@renderer/views/login/index.vue'
import Home from '@renderer/views/home/index.vue'
import Gallery from '@renderer/views/gallery/index.vue'
import Settings from '@renderer/views/settings/index.vue'

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
        icon: 'icon-home',
        locale: 'menu.home',
        nav: true
      }
    },
    {
      path: '/gallery',
      name: 'gallery',
      component: Gallery,
      meta: {
        icon: 'icon-image',
        locale: 'menu.gallery',
        nav: true
      }
    },
    {
      path: '/settings',
      name: 'settings',
      component: Settings,
      meta: {
        icon: 'icon-settings',
        locale: 'menu.settings',
        nav: true
      }
    }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
