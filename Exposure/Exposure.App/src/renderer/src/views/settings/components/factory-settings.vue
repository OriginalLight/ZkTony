<template>
  <a-card v-if="userStore.role === 0" :title="t('settings.factory.title')">
    <a-list>
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
    </a-list>
  </a-card>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useUserStore } from '@renderer/store'
import { Bug, FactoryBuilding, Inbox, Tea } from '@icon-park/vue-next'
import { varsion } from '@renderer/api/machine'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const version = ref({
  ver1: '',
  ver2: ''
})

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
