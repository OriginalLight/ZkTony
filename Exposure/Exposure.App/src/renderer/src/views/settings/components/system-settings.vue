<template>
  <a-card :title="t('settings.system.title')">
    <a-list>
      <a-list-item>
        <a-list-item-meta :title="$t('settings.system.theme.title')">
          <template #avatar>
            <icon-sun size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-select
            style="width: 120px"
            :default-value="currentTheme === 'dark' ? 'dark' : 'light'"
            @change="changeTheme"
          >
            <a-option value="light" :label="$t('settings.system.theme.light')"></a-option>
            <a-option value="dark" :label="$t('settings.system.theme.dark')"></a-option>
          </a-select>
        </template>
      </a-list-item>
      <a-list-item>
        <a-list-item-meta :title="$t('settings.system.language.title')">
          <template #avatar>
            <icon-language size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-select
            style="width: 120px"
            :default-value="locales.find((item) => item.value === currentLocale)?.value"
            @change="changeLocale"
          >
            <a-option
              v-for="item in locales"
              :key="item.value"
              :value="item.value"
              :label="item.label"
            ></a-option>
          </a-select>
        </template>
      </a-list-item>
      <a-list-item>
        <a-list-item-meta :title="$t('settings.system.errlog.title')">
          <template #avatar>
            <icon-file size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button style="width: 120px" @click="handleErrlog">
            <template #icon>
              <icon-launch />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item>
        <a-list-item-meta :title="$t('settings.system.version.title')">
          <template #avatar>
            <icon-question-circle size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-space>
            <a-tag> {{ config.version }} </a-tag>
            <a-tag> {{ machine.version }} </a-tag>
          </a-space>
        </template>
      </a-list-item>
      <a-list-item v-if="userStore.role < 2">
        <a-list-item-meta :title="$t('settings.system.update')">
          <template #avatar>
            <UpdateRotation size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button style="width: 120px" @click="handleUpdate">
            <template #icon>
              <icon-download />
            </template>
            {{ t('settings.system.update.check') }}
          </a-button>
        </template>
      </a-list-item>
      <a-list-item>
        <a-list-item-meta :title="$t('settings.system.machine.title')">
          <template #avatar>
            <icon-code size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-tag> {{ machine.id }} </a-tag>
        </template>
      </a-list-item>
    </a-list>
  </a-card>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { LOCALE_OPTIONS } from '@renderer/locale'
import useLocale from '@renderer/hooks/locale'
import useTheme from '@renderer/hooks/themes'
import { varsion } from '@renderer/api/machine'
import { getOption } from '@renderer/api/option'
import config from '../../../../../../package.json'
import { Message } from '@arco-design/web-vue'
import { UpdateRotation } from '@icon-park/vue-next'
import { useUserStore } from '@renderer/store'

const { t } = useI18n()
const userStore = useUserStore()
const locales = [...LOCALE_OPTIONS]
const { changeLocale, currentLocale } = useLocale()
const { changeTheme, currentTheme } = useTheme()
const router = useRouter()
const machine = ref({
  id: 'None',
  version: 'None'
})

// 查看错误日志
const handleErrlog = () => {
  router.push('/errlog')
}

const handleUpdate = () => {
  Message.info(t('settings.system.update.no'))
}

// 软件版本
onMounted(async () => {
  try {
    const res = await varsion()
    machine.value.version = res.data
    const res1 = await getOption({ key: 'MachineId' })
    machine.value.id = res1.data
  } catch (error) {
    console.error(error)
  }
})
</script>
