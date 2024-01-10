import localeLogin from '@renderer/views/login/locale/en-US'
import localeNavigation from '@renderer/components/navigation/locale/en-US'
import localSettings from '@renderer/views/settings/locale/en-US'

export default {
  'menu.home': 'Home',
  'menu.gallery': 'Gallery',
  'menu.settings': 'Settings',
  ...localeNavigation,
  ...localSettings,
  ...localeLogin
}
