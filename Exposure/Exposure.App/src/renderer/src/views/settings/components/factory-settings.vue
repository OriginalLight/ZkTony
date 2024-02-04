<template>
  <a-list v-if="userStore.role === 0">
    <template #header>
      <div style="font-size: 20px">{{ t('settings.factory.title') }}</div>
    </template>
    <a-list-item>
      <a-list-item-meta :title="$t('settings.factory.debug')">
        <template #avatar>
          <Bug size="20" />
        </template>
      </a-list-item-meta>
      <template #actions>
        <a-button style="width: 60px">
          <template #icon>
            <icon-launch />
          </template>
        </a-button>
      </template>
    </a-list-item>
  </a-list>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@renderer/store'
import { Bug } from '@icon-park/vue-next'

const { t } = useI18n()

const userStore = useUserStore()
</script>
