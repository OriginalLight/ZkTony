<template>
  <a-card v-if="userStore.role === 0" :title="t('settings.factory.title')">
    <a-list>
      <a-list-item class="nav" @click="router.push('/option')">
        <a-list-item-meta :title="t('settings.factory.option')">
          <template #avatar>
            <Inbox size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button shape="round" style="width: 120px" @click="router.push('/option')">
            <template #icon>
              <icon-right />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item class="nav" @click="router.push('/debug')">
        <a-list-item-meta :title="t('settings.factory.debug')">
          <template #avatar>
            <Bug size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button shape="round" style="width: 120px" @click="router.push('/debug')">
            <template #icon>
              <icon-right />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item class="nav" @click="router.push('/fqc')">
        <a-list-item-meta :title="t('settings.factory.fqc')">
          <template #avatar>
            <FactoryBuilding size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button shape="round" style="width: 120px" @click="router.push('/fqc')">
            <template #icon>
              <icon-right />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item class="nav" @click="router.push('/aging')">
        <a-list-item-meta :title="t('settings.factory.aging')">
          <template #avatar>
            <Tea size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button shape="round" style="width: 120px" @click="router.push('/aging')">
            <template #icon>
              <icon-right />
            </template>
          </a-button>
        </template>
      </a-list-item>
      <a-list-item>
        <a-list-item-meta :title="t('settings.factory.reset')">
          <template #avatar>
            <Delete size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button status="danger" shape="round" style="width: 120px" @click="visible = true">
            <template #icon>
              <Delete />
            </template>
          </a-button>
        </template>
      </a-list-item>
    </a-list>
  </a-card>
  <a-modal v-model:visible="visible" draggable @ok="handleReset" @cancel="visible = false">
    <template #title> {{ t('settings.factory.reset') }} </template>
    <div>
      {{ t('settings.factory.reset.confirm') }}
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useUserStore } from '@renderer/store'
import { Bug, FactoryBuilding, Inbox, Tea, Delete } from '@icon-park/vue-next'
import { reset } from '@renderer/api/machine'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const visible = ref(false)

const handleReset = async () => {
  try {
    await reset()
    visible.value = false
    router.push('/login')
  } catch (error) {
    console.error(error)
  }
}
</script>

<style lang="less" scoped>
.nav {
  &:hover {
    background: var(--color-neutral-1);
  }
}
</style>
