import localeLogin from '@renderer/views/login/locale/en-US'
import localeNavigation from '@renderer/components/navigation/locale/en-US'
import localSettings from '@renderer/views/settings/locale/en-US'
import localErrLog from '@renderer/views/errlog/locale/en-US'
import localOperLog from '@renderer/views/operlog/locale/en-US'
import localUserManagement from '@renderer/views/user-management/locale/en-US'
import localHome from '@renderer/views/home/locale/en-US'

export default {
  'menu.home': 'Home',
  'menu.gallery': 'Gallery',
  'menu.settings': 'Settings',
  ...localeNavigation,
  ...localSettings,
  ...localeLogin,
  ...localErrLog,
  ...localOperLog,
  ...localUserManagement,
  ...localHome
}
