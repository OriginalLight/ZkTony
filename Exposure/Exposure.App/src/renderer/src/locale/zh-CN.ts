import localeLogin from '@renderer/views/login/locale/zh-CN'
import localeNavigation from '@renderer/components/navigation/locale/zh-CN'
import localSettings from '@renderer/views/settings/locale/zh-CN'
import localHome from '@renderer/views/home/locale/zh-CN'
import localGallery from '@renderer/views/gallery/locale/zh-CN'

export default {
  'menu.home': '主页',
  'menu.gallery': '图库',
  'menu.settings': '设置',
  ...localeNavigation,
  ...localSettings,
  ...localeLogin,
  ...localHome,
  ...localGallery
}
