<template>
  <section class="content-panel">
    <div class="toolbar">
      <el-input
        v-model.trim="phone"
        clearable
        maxlength="11"
        placeholder="输入收件人手机号"
        @keyup.enter="searchParcels"
      />
      <el-button type="primary" :loading="loading" @click="searchParcels">查询</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="parcels"
      border
      row-key="id"
      :row-class-name="rowClassName"
      empty-text="当前手机号没有待取件包裹"
    >
      <el-table-column prop="trackingNo" label="运单号" min-width="170" />
      <el-table-column prop="expressCompany" label="快递公司" width="120" />
      <el-table-column prop="shelfLocation" label="货架位置" width="130" />
      <el-table-column prop="inboundTime" label="入库时间" min-width="170" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.overdue" type="danger" effect="plain">滞留</el-tag>
          <el-tag v-else type="success" effect="plain">待取件</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="120">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :loading="pickingId === row.id"
            @click="confirmPickup(row)"
          >
            确认取件
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { getWaitingParcels, pickupParcel } from '../api/parcel'

const phonePattern = /^1[3-9]\d{9}$/
const phone = ref('')
const parcels = ref([])
const loading = ref(false)
const pickingId = ref(null)

function rowClassName({ row }) {
  return row.overdue ? 'overdue-row' : ''
}

async function searchParcels() {
  if (!phonePattern.test(phone.value)) {
    ElMessage.error('请输入正确的收件人手机号')
    return
  }

  loading.value = true
  try {
    parcels.value = await getWaitingParcels(phone.value)
  } catch (error) {
    ElMessage.error(error.message || '查询失败')
  } finally {
    loading.value = false
  }
}

async function confirmPickup(row) {
  try {
    await ElMessageBox.confirm(
      `确认 ${row.shelfLocation} 货架的包裹 ${row.trackingNo} 已完成取件？`,
      '确认取件',
      {
        confirmButtonText: '确认取件',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  pickingId.value = row.id
  try {
    await pickupParcel(row.id)
    ElMessage.success('取件完成')
    await searchParcels()
  } catch (error) {
    ElMessage.error(error.message || '取件失败')
  } finally {
    pickingId.value = null
  }
}
</script>
