<template>
  <div class="container">
    <div class="bar">
      <a-space size="large">
        <a-date-picker v-model="searchDate" style="width: 200px" />
        <a-button type="primary" @click="handleSearch">
          <template #icon>
            <icon-search />
          </template>
          <template #default>{{ t('errlog.bar.search') }}</template>
        </a-button>
      </a-space>
      <a-space size="large">
        <a-button
          type="primary"
          status="danger"
          :disabled="selectedKeys.length === 0"
          @click="visible = true"
        >
          <template #icon>
            <icon-delete />
          </template>
          <template #default>{{ t('errlog.bar.delete') }}</template>
        </a-button>
        <a-button type="primary" :disabled="selectedKeys.length === 0" @click="handleExport">
          <template #icon>
            <icon-export />
          </template>
          <template #default>{{ t('errlog.bar.export') }}</template>
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
        {{ rowIndex + 1 }}
      </template>
    </a-table>
  </div>
  <a-modal v-model:visible="visible" draggable @ok="handleDelete" @cancel="visible = false">
    <template #title> {{ t('errlog.bar.delete.title') }} </template>
    <div>
      {{ t('errlog.bar.delete.content') }}
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Message, TableColumnData } from '@arco-design/web-vue'
import useLoading from '@renderer/hooks/loading'
import { ErrLog, getErrLogByPage, deleteErrLog, exportErrLog } from '@renderer/api/errlog'

const { t } = useI18n()

// 加载
const { loading, setLoading } = useLoading()

// 弹窗
const visible = ref(false)

// 搜索日期
const searchDate = ref<Date | undefined>(undefined)

// 选中的key
const selectedKeys = ref([])

// 分页
const pagination = reactive({ current: 1, pageSize: 15, total: 0 })

// 表格列
const columns: TableColumnData[] = [
  {
    title: t('errlog.table.id'),
    width: 50,
    align: 'center',
    slotName: 'index'
  },
  {
    title: t('errlog.table.date'),
    dataIndex: 'time',
    width: 120,
    align: 'center',
    ellipsis: true
  },
  {
    title: t('errlog.table.type'),
    dataIndex: 'type',
    width: 180,
    align: 'center',
    ellipsis: true
  },
  {
    title: t('errlog.table.message'),
    dataIndex: 'message',
    width: 400,
    align: 'center',
    ellipsis: true
  }
]

// 数据
const data = ref<ErrLog[]>([])

// 获取数据
const fetchData = async () => {
  setLoading(true)
  try {
    const res = await getErrLogByPage({
      page: pagination.current,
      size: pagination.pageSize,
      date: searchDate.value ? dayjs(searchDate.value).format('YYYY-MM-DD') : null
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
    await deleteErrLog(selectedKeys.value)
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
    await exportErrLog(selectedKeys.value)
    Message.success(t('errlog.bar.export.success'))
  } catch (error) {
    Message.error((error as Error).message)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  padding: 16px;
  height: calc(100vh - 66px - 32px);

  .bar {
    display: flex;
    justify-content: space-between;
  }

  .table {
    flex: 1;
    margin-top: 16px;
  }
}
</style>
