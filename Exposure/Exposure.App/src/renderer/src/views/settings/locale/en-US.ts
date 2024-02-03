import localErrLog from '../subpage/err-log/locale/en-US'
import localOperLog from '../subpage/oper-log/locale/en-US'
import localUserManage from '../subpage/user-manage/locale/en-US'

export default {
  ...localErrLog,
  ...localOperLog,
  ...localUserManage,
  'settings.system.title': 'System Settings',
  'settings.system.theme.title': 'System Theme',
  'settings.system.theme.light': 'Light',
  'settings.system.theme.dark': 'Dark',
  'settings.system.language.title': 'System Language',
  'settings.system.errlog.title': 'Error Log',
  'settings.system.version.title': 'Version Info',
  'settings.user.title': 'User Settings',
  'settings.user.mofify.password': 'Modify Password',
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
  'settings.user.operlog.title': 'Operation Log',
  'settings.user.manage.title': 'User Management',
  'settings.factory.title': 'Factory Settings',
  'settings.factory.com.1': 'Lower Computer Serial Port',
  'settings.factory.com.2': 'LED Serial Port'
}
