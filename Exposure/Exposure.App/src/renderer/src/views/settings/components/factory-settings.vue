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
          <a-button shape="round" style="width: 120px" @click="router.push('/option')">
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
          <a-button shape="round" style="width: 120px" @click="router.push('/debug')">
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
          <a-button shape="round" style="width: 120px" @click="router.push('/fqc')">
            <template #icon>
              <icon-launch />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item>
        <a-list-item-meta :title="$t('settings.factory.aging')">
          <template #avatar>
            <Tea size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button shape="round" style="width: 120px" @click="router.push('/aging')">
            <template #icon>
              <icon-launch />
            </template>
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
import { Bug, FactoryBuilding, Inbox, Tea } from '@icon-park/vue-next'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
</script>
