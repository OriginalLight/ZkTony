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
  </a-list>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { LOCALE_OPTIONS } from '@renderer/locale'
import useLocale from '@renderer/hooks/locale'
import useTheme from '@renderer/hooks/themes'

const { t } = useI18n()
const locales = [...LOCALE_OPTIONS]
const { changeLocale, currentLocale } = useLocale()
const { changeTheme, currentTheme } = useTheme()
</script>
