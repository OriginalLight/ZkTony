<template>
  <el-tabs type="border-card" v-model="activeTab" @tab-click="changeTab">
    <el-tab-pane label="Dashboard" name="/" />
    <el-tab-pane label="Serial" name="/serial" />
    <el-tab-pane label="Protocol" name="/protocol" />
    <router-view v-slot="{ Component }">
      <keep-alive>
        <component class="view" :is="Component" />
      </keep-alive>
    </router-view>
  </el-tabs>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const activeTab = ref<string>("/");

function changeTab(tab: any) {
  console.log(tab);
  router.push(tab.props.name);
}
</script>
