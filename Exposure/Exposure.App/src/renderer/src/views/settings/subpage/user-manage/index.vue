<template>
  <div class="container">
    <div class="bar">
      <a-space size="large">
        <a-input
          v-model="searchKey"
          style="width: 200px"
          :placeholder="t('user.managemet.search.placeholder')"
        />
        <a-button type="primary" @click="handleSearch">
          <template #icon>
            <icon-search />
          </template>
          <template #default>{{ t('user.managemet.search') }}</template>
        </a-button>
      </a-space>
      <a-space size="large">
        <a-button type="primary" @click="visible.add = true">
          <template #icon>
            <icon-plus />
          </template>
          <template #default>{{ t('user.managemet.add') }}</template>
        </a-button>
        <a-button
          type="primary"
          status="danger"
          :disabled="isNoSelectedKeys"
          @click="visible.delete = true"
        >
          <template #icon>
            <icon-delete />
          </template>
          <template #default>{{ t('user.managemet.delete') }}</template>
        </a-button>
      </a-space>
    </div>
    <a-table
      v-model:selectedKeys="selectedKeys"
      class="table"
      :loading="loading"
      :bordered="{ cell: true }"
      row-key="id"
      :columns="columns"
      :stripe="true"
      :data="data"
      :row-selection="{ type: 'checkbox', showCheckedAll: true, checkStrictly: true }"
      :pagination="pagination"
      :scroll="{ y: '90%' }"
      :scrollbar="true"
      column-resizable
      @page-change="onPageChange"
    >
      <template #index="{ rowIndex }">
        {{ pagination.pageSize * (pagination.current - 1) + rowIndex + 1 }}
      </template>
      <template #role="{ rowIndex }">
        <a-select
          v-model="data[rowIndex].role"
          :disabled="userStore.role != 0"
          @change="handleChange(rowIndex)"
        >
          <a-option :value="1"> {{ t('user.managemet.table.role.1') }}</a-option>
          <a-option :value="2">{{ t('user.managemet.table.role.2') }}</a-option>
        </a-select>
      </template>
      <template #enabled="{ rowIndex }">
        <a-switch v-model="data[rowIndex].enabled" @change="handleChange(rowIndex)" />
      </template>
      <template #action="{ rowIndex }">
        <a-button type="primary" @click="(visible.reset = true), (resetIndex = rowIndex)">{{
          t('user.managemet.reset')
        }}</a-button>
      </template>
    </a-table>
  </div>
  <a-modal
    v-model:visible="visible.delete"
    draggable
    @ok="handleDelete"
    @cancel="visible.delete = false"
  >
    <template #title> {{ t('user.managemet.delete.title') }} </template>
    <div>
      {{ t('user.managemet.delete.content') }}
    </div>
  </a-modal>
  <a-modal
    v-model:visible="visible.add"
    draggable
    unmount-on-close
    :on-before-ok="handleAdd"
    @cancel="visible.add = false"
  >
    <template #title> {{ t('user.managemet.add.title') }} </template>
    <div>
      <a-form :model="addForm" auto-label-width>
        <a-form-item
          field="name"
          :rules="[{ required: true, message: t('user.managemet.add.form.name.errMsg') }]"
          :validate-trigger="['change', 'blur']"
          :max-length="32"
          hide-label
        >
          <a-input
            v-model="addForm.name"
            :placeholder="t('user.managemet.add.form.name.placeholder')"
          >
            <template #prefix>
              <icon-user />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item
          field="password"
          :rules="[{ required: true, message: t('user.managemet.add.form.password.errMsg') }]"
          :validate-trigger="['change', 'blur']"
          hide-label
        >
          <a-input-password
            v-model="addForm.password"
            :placeholder="t('user.managemet.add.form.password.placeholder')"
            :max-length="32"
            allow-clear
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item field="role" :label="t('user.managemet.add.form.role.lable')">
          <a-select v-model="addForm.role" :disabled="userStore.role != 0">
            <a-option :value="1" :label="t('user.managemet.table.role.1')"></a-option>
            <a-option :value="2" :label="t('user.managemet.table.role.2')"></a-option>
          </a-select>
        </a-form-item>

        <a-form-item field="enabled" :label="t('user.managemet.add.form.enabled.lable')">
          <a-switch v-model="addForm.enabled" />
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
  <a-modal
    v-model:visible="visible.reset"
    draggable
    @ok="handleReset"
    @cancel="(visible.reset = false), (resetIndex = 0)"
  >
    <template #title> {{ t('user.managemet.reset.title') }} </template>
    <div>
      {{ t('user.managemet.reset.content') }}
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Message, TableColumnData } from '@arco-design/web-vue'
import useLoading from '@renderer/hooks/loading'
import { User, getUserByPage, deleteUser, updateUser, addUser } from '@renderer/api/user'
import { useUserStore } from '@renderer/store'

const { t } = useI18n()
const { loading, setLoading } = useLoading(true)
const userStore = useUserStore()
// 搜索关键字
const searchKey = ref('')
// 选中的用户
const selectedKeys = ref([])
// 重置密码
const resetIndex = ref(0)
// 弹窗
const visible = reactive({
  add: false,
  delete: false,
  reset: false
})
// 添加用户表单
const addForm = reactive({
  name: '',
  password: '',
  role: 2,
  enabled: true
})

// 分页
const pagination = reactive({ current: 1, pageSize: 20, total: 0, showTotal: true })

// 表格列
const columns: TableColumnData[] = [
  {
    title: t('user.managemet.table.id'),
    width: 50,
    align: 'center',
    slotName: 'index'
  },
  {
    title: t('user.managemet.table.name'),
    dataIndex: 'name',
    width: 120,
    ellipsis: true,
    align: 'center'
  },
  {
    title: t('user.managemet.table.role'),
    dataIndex: 'role',
    ellipsis: true,
    width: 80,
    align: 'center',
    slotName: 'role'
  },
  {
    title: t('user.managemet.table.enabled'),
    dataIndex: 'enabled',
    ellipsis: true,
    width: 80,
    align: 'center',
    slotName: 'enabled'
  },
  {
    title: t('user.managemet.table.lastLoginTime'),
    dataIndex: 'lastLoginTime',
    ellipsis: true,
    width: 200,
    align: 'center'
  },
  {
    title: t('user.managemet.table.action'),
    width: 80,
    align: 'center',
    slotName: 'action'
  }
]
const data = ref<User[]>([])

// 选中的key是否为空
const isNoSelectedKeys = computed(() => selectedKeys.value.length === 0)

// 获取用户列表
const fetchData = async () => {
  setLoading(true)
  try {
    const res = await getUserByPage({
      page: pagination.current,
      size: pagination.pageSize,
      name: searchKey.value
    })
    data.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    setLoading(false)
  }
}

// 搜索
const handleSearch = () => {
  selectedKeys.value = []
  fetchData()
}

// 分页
const onPageChange = (current) => {
  pagination.current = current
  fetchData()
}

// 删除
const handleDelete = async () => {
  visible.delete = false
  try {
    await deleteUser(selectedKeys.value)
    // 删除成功后更新当前页
    const total = pagination.total - selectedKeys.value.length
    const totalPage = Math.ceil(total / pagination.pageSize)
    if (totalPage < pagination.current) {
      pagination.current = 1
    }
    selectedKeys.value = []
    fetchData()
  } catch (error) {
    Message.error((error as Error).message)
  }
}

// 添加
const handleAdd = async () => {
  try {
    if (addForm.name === '') {
      Message.error(t('user.managemet.add.form.name.errMsg'))
      return false
    }
    if (addForm.password === '') {
      Message.error(t('user.managemet.add.form.password.errMsg'))
      return false
    }
    await addUser({
      name: addForm.name,
      password: addForm.password,
      role: addForm.role,
      enabled: addForm.enabled
    })
    visible.add = false
    addForm.name = ''
    addForm.password = ''
    addForm.role = 2
    addForm.enabled = true
    fetchData()
    return true
  } catch (error) {
    Message.error((error as Error).message)
    return false
  }
}

// 重置密码
const handleReset = async () => {
  try {
    setLoading(true)
    const user = data.value[resetIndex.value]
    if (!user) {
      return
    }
    await updateUser({
      id: user.id,
      name: user.name,
      oldPassword: '',
      newPassword: user.name,
      role: user.role,
      enabled: user.enabled
    })
    Message.success(t('user.managemet.reset.success'))
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    setLoading(false)
  }
}

// 修改角色和状态
const handleChange = async (index: number) => {
  try {
    setLoading(true)
    const user = data.value[index]
    await updateUser({
      id: user.id,
      name: user.name,
      oldPassword: '',
      newPassword: '',
      role: user.role,
      enabled: user.enabled
    })
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    setLoading(false)
  }
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 66px - 32px);
  padding: 8px;
  margin: 8px;
  border-radius: 4px;
  background: var(--color-bg-2);
  overflow: hidden;

  .bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px;
  }

  .table {
    flex: 1;
    margin-top: 8px;
  }
}
</style>
