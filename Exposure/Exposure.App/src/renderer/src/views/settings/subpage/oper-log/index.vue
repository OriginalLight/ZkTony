<template>
  <div class="container">
    <div class="bar">
      <a-space size="large">
        <a-date-picker v-model="searchDate" style="width: 200px" />
        <a-button shape="round" type="primary" @click="handleSearch">
          <template #icon>
            <icon-search />
          </template>
          <template #default>{{ t('operlog.bar.search') }}</template>
        </a-button>
      </a-space>
      <a-space size="large">
        <a-button
          type="primary"
          shape="round"
          status="danger"
          :disabled="isNoSelectedKeys"
          @click="visible = true"
        >
          <template #icon>
            <icon-delete />
          </template>
          <template #default>{{ t('operlog.bar.delete') }}</template>
        </a-button>
        <a-button type="primary" shape="round" :disabled="isNoSelectedKeys" @click="handleExport">
          <template #icon>
            <icon-export />
          </template>
          <template #default>{{ t('operlog.bar.export') }}</template>
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
    </a-table>
  </div>
  <a-modal v-model:visible="visible" draggable @ok="handleDelete" @cancel="visible = false">
    <template #title> {{ t('operlog.bar.delete.title') }} </template>
    <div>
      {{ t('operlog.bar.delete.content') }}
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Message, TableColumnData } from '@arco-design/web-vue'
import useLoading from '@renderer/hooks/loading'
import { OperLog, getOperLogByPage, deleteOperLog, exportOperLog } from '@renderer/api/operlog'

const { t } = useI18n()

// 加载
const { loading, setLoading } = useLoading()

// 删除弹窗
const visible = ref(false)

// 搜索日期
const searchDate = ref<Date | undefined>(undefined)

// 选中的key
const selectedKeys = ref([])

// 分页
const pagination = reactive({ current: 1, pageSize: 50, total: 0, showTotal: true })

// 表格列
const columns: TableColumnData[] = [
  {
    title: t('operlog.table.id'),
    width: 50,
    align: 'center',
    slotName: 'index'
  },
  {
    title: t('operlog.table.date'),
    dataIndex: 'time',
    width: 120,
    ellipsis: true,
    align: 'center'
  },
  {
    title: t('operlog.table.type'),
    dataIndex: 'type',
    ellipsis: true,
    width: 80,
    align: 'center'
  },
  {
    title: t('operlog.table.name'),
    dataIndex: 'user.name',
    ellipsis: true,
    width: 80,
    align: 'center'
  },
  {
    title: t('operlog.table.description'),
    dataIndex: 'description',
    ellipsis: true,
    width: 400,
    align: 'center'
  }
]

// 数据
const data = ref<OperLog[]>([])

// 选中的key是否为空
const isNoSelectedKeys = computed(() => selectedKeys.value.length === 0)

// 搜索日期转换
const formattedSearchDate = computed(() => {
  return searchDate.value ? dayjs(searchDate.value).format('YYYY-MM-DD') : null
})

// 获取数据
const fetchData = async () => {
  setLoading(true)
  try {
    const res = await getOperLogByPage({
      page: pagination.current,
      size: pagination.pageSize,
      date: formattedSearchDate.value
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
  visible.value = false
  try {
    await deleteOperLog(selectedKeys.value)
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

// 导出
const handleExport = async () => {
  try {
    await exportOperLog(selectedKeys.value)
    Message.success(t('operlog.bar.export.success'))
  } catch (error) {
    Message.error((error as Error).message)
  }
}

// 初始化数据
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
