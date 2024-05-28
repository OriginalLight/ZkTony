import localFQC from '../subpage/fqc/locale/en-US'
import localDebug from '../subpage/debug/locale/en-US'
import localErrLog from '../subpage/err-log/locale/en-US'
import localOperLog from '../subpage/oper-log/locale/en-US'
import localOption from '../subpage/option/locale/en-US'
import localUserManage from '../subpage/user-manage/locale/en-US'
import loaclAging from '../subpage/aging/locale/en-US'

export default {
  ...localFQC,
  ...localDebug,
  ...localErrLog,
  ...loaclAging,
  ...localOperLog,
  ...localOption,
  ...localUserManage,
  'settings.system.title': 'System Settings',
  'settings.system.theme.title': 'Theme',
  'settings.system.theme.light': 'Light',
  'settings.system.theme.dark': 'Dark',
  'settings.system.language.title': 'Language',
  'settings.system.sound.title': 'Sound',
  'settings.system.sound.0': 'Mute',
  'settings.system.sound.1': 'Ring',
  'settings.system.sound.2': 'Voice',
  'settings.system.errlog.title': 'Error Log',
  'settings.system.version.title': 'Version',
  'settings.system.machine.title': 'Machine ID',
  'settings.system.update': 'Update',
  'settings.system.update.check': 'Update',
  'settings.system.update.no': 'No new version',
  'settings.user.title': 'User Settings',
  'settings.user.mofify.password': 'Password',
  'settings.user.mofify.password.new': 'New Password',
  'settings.user.mofify.password.empty.errMsg': 'Cannot be empty',
  'settings.user.mofify.password.old.placeholder': 'Please enter the old password',
  'settings.user.mofify.password.new.placeholder': 'Please enter the new password',
  'settings.user.mofify.password.new.errMsg':
    'The new password cannot be the same as the old password',
  'settings.user.mofify.password.confirm': 'Confirm Password',
  'settings.user.mofify.password.confirm.errMsg': 'The two passwords entered are inconsistent',
  'settings.user.mofify.password.confirm.placeholder': 'Please enter the new password again',
  'settings.user.modify.password.success': 'Password modified successfully',
  'settings.user.modify.password.fail': 'Failed to modify password',
  'settings.user.operlog.title': 'Operation Log',
  'settings.user.manage.title': 'User Management',
  'settings.factory.title': 'Factory Settings',
  'settings.factory.option': 'Option',
  'settings.factory.debug': 'Debug',
  'settings.factory.fqc': 'FQC',
  'settings.factory.aging': 'Aging'
}
