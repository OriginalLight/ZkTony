<template>
  <a-list v-if="userStore.role === 0">
    <template #header>
      <div style="font-size: 20px">{{ t('settings.factory.title') }}</div>
    </template>
    <a-list-item>
      <a-list-item-meta :title="$t('settings.factory.com.1')">
        <template #avatar>
          <IconUsb size="20" />
        </template>
      </a-list-item-meta>
      <template #actions>
        <a-select style="width: 120px" default-value="com1">
          <a-option value="com1" label="COM1"></a-option>
          <a-option value="com2" label="COM2"></a-option>
        </a-select>
      </template>
    </a-list-item>
    <a-list-item>
      <a-list-item-meta :title="$t('settings.factory.com.2')">
        <template #avatar>
          <IconUsb size="20" />
        </template>
      </a-list-item-meta>
      <template #actions>
        <a-select style="width: 120px" default-value="com1">
          <a-option value="com1" label="COM1"></a-option>
          <a-option value="com2" label="COM2"></a-option>
        </a-select>
      </template>
    </a-list-item>
  </a-list>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@renderer/store'
import { Usb as IconUsb } from '@icon-park/vue-next'

const { t } = useI18n()

const userStore = useUserStore()
</script>
