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
        <a-list-item-meta :title="t('settings.factory.updateFirmware')">
          <template #avatar>
            <NewComputer size="20" />
          </template>
        </a-list-item-meta>
        <template #actions>
          <a-button shape="round" style="width: 120px" @click="visible.updateFirmware = true">
            <template #icon>
              <NewComputer />
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
          <a-button
            status="danger"
            shape="round"
            style="width: 120px"
            @click="visible.reset = true"
          >
            <template #icon>
              <Delete />
            </template>
          </a-button>
        </template>
      </a-list-item>
    </a-list>
  </a-card>
  <a-modal
    v-model:visible="visible.updateFirmware"
    draggable
    @ok="handleUpdateFirmware"
    @cancel="visible.updateFirmware = false"
  >
    <template #title> {{ t('settings.factory.updateFirmware') }} </template>
    <div>
      {{ t('settings.factory.updateFirmware.confirm') }}
    </div>
  </a-modal>
  <a-modal
    v-model:visible="visible.reset"
    draggable
    @ok="handleReset"
    @cancel="visible.reset = false"
  >
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
import { Bug, FactoryBuilding, Inbox, Tea, Delete, NewComputer } from '@icon-park/vue-next'
import { reset, updateFirmware } from '@renderer/api/machine'
import { Message } from '@arco-design/web-vue'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const visible = ref({
  updateFirmware: false,
  reset: false
})

const handleReset = async () => {
  try {
    await reset()
    visible.value.reset = false
    router.push('/login')
  } catch (error) {
    console.error(error)
  }
}

const handleUpdateFirmware = async () => {
  try {
    await updateFirmware()
    visible.value.updateFirmware = false
  } catch (error) {
    Message.error((error as Error).message)
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
