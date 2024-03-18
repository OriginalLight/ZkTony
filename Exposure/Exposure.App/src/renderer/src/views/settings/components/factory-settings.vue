<template>
  <a-card v-if="userStore.role === 0" :title="t('settings.factory.title')">
    <a-list>
      <a-list-item>
        <a-list-item-meta :title="$t('settings.factory.option')">
          <template #avatar>
            <Inbox size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button style="width: 60px" @click="handleOption">
            <template #icon>
              <icon-launch />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item>
        <a-list-item-meta :title="$t('settings.factory.debug')">
          <template #avatar>
            <Bug size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button style="width: 60px" @click="handleDebug">
            <template #icon>
              <icon-launch />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item>
        <a-list-item-meta :title="$t('settings.factory.fqc')">
          <template #avatar>
            <FactoryBuilding size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button style="width: 60px" @click="handleFQC">
            <template #icon>
              <icon-launch />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item>
        <a-list-item-meta :title="$t('settings.factory.update')">
          <template #avatar>
            <UpdateRotation size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button @click="handleUpdate">
            <template #icon>
              <icon-download />
            </template>
            {{ t('settings.factory.update.check') }}
          </a-button>
        </template>
      </a-list-item>
    </a-list>
  </a-card>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useUserStore } from '@renderer/store'
import { Message } from '@arco-design/web-vue'
import { Bug, UpdateRotation, FactoryBuilding, Inbox } from '@icon-park/vue-next'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

// 跳转到调试界面
const handleDebug = () => {
  router.push('/debug')
}

// 跳转到FQC界面
const handleFQC = () => {
  router.push('/fqc')
}

// 跳转到参数设置界面
const handleOption = () => {
  router.push('/option')
}

const handleUpdate = () => {
  Message.info('暂无更新')
}
</script>
