<template>
  <section class="content-panel">
    <div class="toolbar">
      <el-input v-model.trim="filters.phone" clearable placeholder="收件人手机号" />
      <el-input v-model.trim="filters.trackingNo" clearable placeholder="运单号" />
      <el-select v-model="filters.status" clearable placeholder="包裹状态">
        <el-option label="待取件" value="WAITING_PICKUP" />
        <el-option label="已取件" value="PICKED_UP" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="applyFilters">查询</el-button>
      <el-button :disabled="loading" @click="resetFilters">重置</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="records"
      border
      row-key="id"
      :row-class-name="rowClassName"
      empty-text="暂无包裹记录"
    >
      <el-table-column prop="trackingNo" label="运单号" min-width="170" />
      <el-table-column prop="recipientPhone" label="收件人手机" width="140" />
      <el-table-column prop="expressCompany" label="快递公司" width="120" />
      <el-table-column prop="shelfLocation" label="货架位置" width="120" />
      <el-table-column prop="inboundTime" label="入库时间" min-width="170" />
      <el-table-column prop="outboundTime" label="出库时间" min-width="170">
        <template #default="{ row }">
          {{ row.outboundTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="150">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PICKED_UP' ? 'info' : 'success'" effect="plain">
            {{ row.statusText || statusText(row.status) }}
          </el-tag>
          <el-tag v-if="row.overdue" class="overdue-tag" type="danger" effect="plain">
            滞留
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="table-footer">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        background
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        @size-change="loadPage"
        @current-change="loadPage"
      />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getParcelPage } from '../api/parcel'

const loading = ref(false)
const records = ref([])

const filters = reactive({
  phone: '',
  trackingNo: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

function rowClassName({ row }) {
  return row.overdue ? 'overdue-row' : ''
}

function statusText(status) {
  const map = {
    WAITING_PICKUP: '待取件',
    PICKED_UP: '已取件'
  }
  return map[status] || status || '-'
}

function buildParams() {
  return {
    page: pagination.page,
    pageSize: pagination.pageSize,
    phone: filters.phone || undefined,
    trackingNo: filters.trackingNo || undefined,
    status: filters.status || undefined
  }
}

async function loadPage() {
  loading.value = true
  try {
    const pageData = await getParcelPage(buildParams())
    records.value = pageData?.records || []
    pagination.total = Number(pageData?.total || 0)
    pagination.page = Number(pageData?.page || pagination.page)
    pagination.pageSize = Number(pageData?.pageSize || pagination.pageSize)
  } catch (error) {
    ElMessage.error(error.message || '列表加载失败')
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  pagination.page = 1
  loadPage()
}

function resetFilters() {
  filters.phone = ''
  filters.trackingNo = ''
  filters.status = ''
  pagination.page = 1
  loadPage()
}

onMounted(loadPage)
</script>

<style scoped>
.overdue-tag {
  margin-left: 6px;
}
</style>
