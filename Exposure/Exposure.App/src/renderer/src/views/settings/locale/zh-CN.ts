import localFQC from '../subpage/fqc/locale/zh-CN'
import localDebug from '../subpage/debug/locale/zh-CN'
import localErrLog from '../subpage/err-log/locale/zh-CN'
import localOperLog from '../subpage/oper-log/locale/zh-CN'
import localOption from '../subpage/option/locale/zh-CN'
import localUserManage from '../subpage/user-manage/locale/zh-CN'
import loaclAging from '../subpage/aging/locale/zh-CN'

export default {
  ...localFQC,
  ...localDebug,
  ...loaclAging,
  ...localErrLog,
  ...localOperLog,
  ...localOption,
  ...localUserManage,
  'settings.system.title': '系统设置',
  'settings.system.theme.title': '系统主题',
  'settings.system.theme.light': '亮色',
  'settings.system.theme.dark': '暗色',
  'settings.system.language.title': '系统语言',
  'settings.system.errlog.title': '错误日志',
  'settings.system.version.title': '版本信息',
  'settings.system.machine.title': '设备序列号',
  'settings.system.update': '检查更新',
  'settings.system.update.check': '检查更新',
  'settings.system.update.no': '没有新版本',
  'settings.user.title': '用户设置',
  'settings.user.mofify.password': '修改密码',
  'settings.user.mofify.password.new': '新密码',
  'settings.user.mofify.password.empty.errMsg': '不能为空',
  'settings.user.mofify.password.old.placeholder': '请输入旧密码',
  'settings.user.mofify.password.new.placeholder': '请输入新密码',
  'settings.user.mofify.password.new.errMsg': '新密码不能和旧密码相同',
  'settings.user.mofify.password.confirm': '确认密码',
  'settings.user.mofify.password.confirm.errMsg': '两次输入的密码不一致',
  'settings.user.mofify.password.confirm.placeholder': '请再次输入新密码',
  'settings.user.modify.password.success': '修改密码成功',
  'settings.user.modify.password.fail': '修改密码失败',
  'settings.user.operlog.title': '操作日志',
  'settings.user.manage.title': '用户管理',
  'settings.factory.title': '工厂设置',
  'settings.factory.option': '参数',
  'settings.factory.debug': '调试',
  'settings.factory.fqc': 'FQC',
  'settings.factory.aging': '老化'
}
