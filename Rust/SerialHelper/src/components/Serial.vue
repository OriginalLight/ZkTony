<script setup lang="ts">
import { Refresh } from "@element-plus/icons-vue";
import { invoke } from "@tauri-apps/api/tauri";
import { reactive } from "vue";

// do not use same name with ref
const form = reactive({
  ports: [],
  port: "",
  baudRates: [9600, 19200, 38400, 57600, 115200],
  baudRate: 115200,
});

async function refreshPorts() {
  form.ports = await invoke("serialports");
  if (form.ports.length > 0) {
    form.port = form.ports[0];
  } else {
    form.port = "";
  }
}

refreshPorts();
</script>

<template>
  <el-form :model="form" label-width="64px">
    <el-form-item label="端口">
      <el-space :size="16">
        <el-select v-model="form.port" placeholder="请选择端口">
          <el-option
            v-for="port in form.ports"
            :label="port"
            :value="port"
            :key="port"
          ></el-option>
        </el-select>
        <el-button
          type="primary"
          :icon="Refresh"
          circle
          @click="refreshPorts"
        />
      </el-space>
    </el-form-item>
    <el-form-item label="波特率">
      <el-select v-model="form.baudRate" placeholder="请选择波特率">
        <el-option
          v-for="baudRate in form.baudRates"
          :label="baudRate"
          :value="baudRate"
          :key="baudRate"
        ></el-option>
      </el-select>
    </el-form-item>
  </el-form>
</template>
