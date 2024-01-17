<template>
  <a-list>
    <template #header>
      <div style="font-size: 20px">{{ t('settings.system.title') }}</div>
    </template>
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
        <a-button style="width: 60px" @click="handleErrlog">
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
        <div style="font-size: medium; font-style: italic; letter-spacing: 5px">
          {{ config.version }}
        </div>
      </template>
    </a-list-item>
  </a-list>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { LOCALE_OPTIONS } from '@renderer/locale'
import useLocale from '@renderer/hooks/locale'
import useTheme from '@renderer/hooks/themes'
import config from '../../../../../../package.json'

const { t } = useI18n()
const locales = [...LOCALE_OPTIONS]
const { changeLocale, currentLocale } = useLocale()
const { changeTheme, currentTheme } = useTheme()
const router = useRouter()

// 查看错误日志
const handleErrlog = () => {
  router.push({
    path: '/errlog'
  })
}
// 软件版本
</script>
