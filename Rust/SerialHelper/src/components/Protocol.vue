<script setup lang="ts">
import { ref, reactive } from "vue";
import ClipboardJS from "clipboard";

// do not use same name with ref
const form = reactive({
  ids: [0],
  steps: 0,
  acceleration: 0,
  deceleration: 0,
  speed: 0,
});

const command = ref("");

const generateCommand = () => {
  // 生成命令的逻辑
  command.value = `步数：${form.steps}\n加速度：${form.acceleration}\n减速度：${form.deceleration}\n速度：${form.speed}`;
};
const copyCommand = () => {
  const clipboard = new ClipboardJS(".el-input__inner");
  clipboard.on("success", () => {
    clipboard.destroy();
    console.log("Command copied to clipboard");
  });
  clipboard.on("error", () => {
    clipboard.destroy();
    console.error("Failed to copy command to clipboard");
  });
};
</script>

<template>
  <el-checkbox-group v-model="form.ids" size="large">
    <el-checkbox-button v-for="index in 16" :key="index" :label="index - 1">
      {{ "M" + (index - 1) }}
    </el-checkbox-button>
  </el-checkbox-group>
  <h2>0x01</h2>
  <div class="form">
    <div class="left">
      <el-form :model="form" label-width="60px">
        <el-form-item label="步数">
          <el-input
            v-model.number="form.steps"
            type="number"
            placeholder="请输入步数"
          ></el-input>
        </el-form-item>
        <el-form-item label="加速度">
          <el-input
            v-model.number="form.acceleration"
            type="number"
            placeholder="请输入加速度"
          ></el-input>
        </el-form-item>
        <el-form-item label="减速度">
          <el-input
            v-model.number="form.deceleration"
            type="number"
            placeholder="请输入减速度"
          ></el-input>
        </el-form-item>
        <el-form-item label="速度">
          <el-input
            v-model.number="form.speed"
            type="number"
            placeholder="请输入速度"
          ></el-input>
        </el-form-item>
      </el-form>
    </div>
    <div class="center">
      <el-button type="primary" @click="generateCommand">生成命令</el-button>
    </div>
    <div class="right">
      <el-form-item label="命令">
        <el-input
          v-model="command"
          type="textarea"
          :rows="4"
          placeholder="生成的命令将显示在这里"
          @dblclick="copyCommand"
        ></el-input>
      </el-form-item>
    </div>
  </div>
</template>

<style scoped>
.form {
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
}
.left {
  width: 40%;
}
.center {
  width: 20%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.right {
  width: 40%;
}
</style>
