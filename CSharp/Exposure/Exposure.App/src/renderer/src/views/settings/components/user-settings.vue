<template>
  <a-card :title="t('settings.user.title')">
    <a-list>
      <a-list-item>
        <a-list-item-meta :title="userStore.name" :description="getRoleName(userStore.role)">
          <template #avatar>
            <icon-user size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button shape="round" style="width: 120px" @click="visible = true">
            <template #icon>
              <icon-edit />
            </template>
            <template #default>{{ t('settings.user.mofify.password') }}</template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item v-if="userStore.role < 2" class="nav" @click="handleOperlog">
        <a-list-item-meta :title="t('settings.user.operlog.title')">
          <template #avatar>
            <icon-file size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button shape="round" style="width: 120px" @click="handleOperlog">
            <template #icon>
              <icon-right />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item v-if="userStore.role < 2" class="nav" @click="handleManage">
        <a-list-item-meta :title="t('settings.user.manage.title')">
          <template #avatar>
            <icon-nav size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button shape="round" style="width: 120px" @click="handleManage">
            <template #icon>
              <icon-right />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item>
        <a-list-item-meta :title="t('settings.system.version.title')">
          <template #avatar>
            <icon-question-circle size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-space>
            <a-tag v-if="version.ver2 != ''"> {{ version.ver2 }} </a-tag>
            <a-tag v-if="version.ver1 != ''"> {{ version.ver1 }} </a-tag>
          </a-space>
        </template>
      </a-list-item>
    </a-list>
  </a-card>

  <a-modal
    v-model:visible="visible"
    draggable
    unmount-on-close
    :on-before-ok="handleModifyPassword"
    @cancel="visible = false"
  >
    <template #title> {{ t('settings.user.mofify.password') }} </template>
    <div>
      <a-form :model="form">
        <a-form-item
          field="oldPassword"
          :rules="[{ required: true, message: t('settings.user.mofify.password.empty.errMsg') }]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input-password
            v-model="form.oldPassword"
            :placeholder="t('settings.user.mofify.password.old.placeholder')"
            :max-length="32"
            allow-clear
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>
        <a-form-item
          field="newPassword"
          :rules="[
            { required: true, message: t('settings.user.mofify.password.empty.errMsg') },
            { validator: validateNewPassword }
          ]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input-password
            v-model="form.newPassword"
            :placeholder="t('settings.user.mofify.password.new.placeholder')"
            :max-length="32"
            allow-clear
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>
        <a-form-item
          field="confirmPassword"
          :rules="[
            { required: true, message: t('settings.user.mofify.password.empty.errMsg') },
            { validator: validateConfirmPassword }
          ]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input-password
            v-model="form.confirmPassword"
            :placeholder="t('settings.user.mofify.password.confirm.placeholder')"
            :max-length="32"
            allow-clear
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useUserStore } from '@renderer/store'
import { Message } from '@arco-design/web-vue'
import { updateUser } from '@renderer/api/user'
import { varsion } from '@renderer/api/machine'

const { t } = useI18n()
const userStore = useUserStore()
const router = useRouter()

// 修改密码弹窗
const visible = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const version = ref({
  ver1: '',
  ver2: ''
})

// 根据role值返回对应的角色名称
const getRoleName = (role: number) => {
  switch (role) {
    case 0:
      return t('user.managemet.table.role.0')
    case 1:
      return t('user.managemet.table.role.1')
    case 2:
      return t('user.managemet.table.role.2')
    default:
      return t('user.managemet.table.role.2')
  }
}

// 校验新密码
const validateNewPassword = (value, cb) => {
  return new Promise<void>((resolve) => {
    if (value === form.oldPassword) {
      cb(t('settings.user.mofify.password.new.errMsg'))
    } else {
      cb()
    }
    resolve()
  })
}

// 校验确认密码
const validateConfirmPassword = (value, cb) => {
  return new Promise<void>((resolve) => {
    if (value !== form.newPassword) {
      cb(t('settings.user.mofify.password.confirm.errMsg'))
    } else {
      cb()
    }
    resolve()
  })
}

// 修改密码
const handleModifyPassword = async () => {
  try {
    if (form.newPassword === '' || form.confirmPassword === '' || form.oldPassword === '') {
      Message.error(t('settings.user.mofify.password.empty.errMsg'))
      return false
    }
    if (form.oldPassword === form.newPassword) {
      Message.error(t('settings.user.mofify.password.new.errMsg'))
      return false
    }
    if (form.newPassword !== form.confirmPassword) {
      Message.error(t('settings.user.mofify.password.confirm.errMsg'))
      return false
    }
    await updateUser({
      id: userStore.id,
      name: userStore.name,
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      role: userStore.role,
      enabled: userStore.enabled
    })
    visible.value = false
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    Message.success(t('settings.user.modify.password.success'))
    return true
  } catch (error) {
    Message.error((error as Error).message)
    return false
  }
}

// 跳转到操作日志页面
const handleOperlog = () => {
  router.push('/operlog')
}
// 跳转到用户管理页面
const handleManage = () => {
  router.push('/user-manage')
}

onMounted(async () => {
  const res = await varsion()
  const ver = res.data
  if (ver.Ver1) {
    version.value.ver1 = ver.Ver1
  }
  if (ver.Ver2) {
    version.value.ver2 = ver.Ver2
  }
})
</script>

<style lang="less" scoped>
.nav {
  &:hover {
    background: var(--color-neutral-1);
  }
}
</style>
