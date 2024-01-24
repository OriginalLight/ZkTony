import localeLogin from '@renderer/views/login/locale/zh-CN'
import localeNavigation from '@renderer/components/navigation/locale/zh-CN'
import localSettings from '@renderer/views/settings/locale/zh-CN'
import localErrLog from '@renderer/views/errlog/locale/zh-CN'
import localOperLog from '@renderer/views/operlog/locale/zh-CN'
import localUserManagement from '@renderer/views/user-management/locale/zh-CN'
import localHome from '@renderer/views/home/locale/zh-CN'

export default {
  'menu.home': '主页',
  'menu.gallery': '图库',
  'menu.settings': '设置',
  ...localeNavigation,
  ...localSettings,
  ...localeLogin,
  ...localErrLog,
  ...localOperLog,
  ...localUserManagement,
  ...localHome
}
