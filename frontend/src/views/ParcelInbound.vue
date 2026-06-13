<template>
  <section class="content-panel inbound-page">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="110px"
      status-icon
      @submit.prevent
    >
      <el-form-item label="运单号" prop="trackingNo">
        <el-input v-model.trim="form.trackingNo" clearable placeholder="SF123456789" />
      </el-form-item>

      <el-form-item label="收件人手机" prop="recipientPhone">
        <el-input v-model.trim="form.recipientPhone" clearable placeholder="13800138000" />
      </el-form-item>

      <el-form-item label="快递公司" prop="expressCompany">
        <el-input v-model.trim="form.expressCompany" clearable placeholder="顺丰" />
      </el-form-item>

      <el-form-item label="货架位置" prop="shelfLocation">
        <el-input v-model.trim="form.shelfLocation" clearable placeholder="A-01-03" />
      </el-form-item>

      <el-alert
        v-if="errorMessage"
        class="form-alert"
        type="error"
        :title="errorMessage"
        show-icon
        :closable="false"
      />

      <el-form-item class="form-actions">
        <el-button type="primary" :loading="submitting" @click="submitForm">
          提交入库
        </el-button>
        <el-button :disabled="submitting" @click="resetForm">清空</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { createParcel } from '../api/parcel'

const phonePattern = /^1[3-9]\d{9}$/
const formRef = ref()
const submitting = ref(false)
const errorMessage = ref('')

const initialForm = {
  trackingNo: '',
  recipientPhone: '',
  expressCompany: '',
  shelfLocation: ''
}

const form = reactive({ ...initialForm })

const requiredRule = (label) => ({
  required: true,
  message: `请输入${label}`,
  trigger: 'blur'
})

const rules = {
  trackingNo: [requiredRule('运单号')],
  recipientPhone: [
    requiredRule('收件人手机号'),
    {
      pattern: phonePattern,
      message: '手机号格式错误',
      trigger: 'blur'
    }
  ],
  expressCompany: [requiredRule('快递公司')],
  shelfLocation: [requiredRule('货架位置')]
}

function clearForm() {
  Object.assign(form, initialForm)
}

function resetForm() {
  errorMessage.value = ''
  clearForm()
  formRef.value?.clearValidate()
}

async function submitForm() {
  if (!formRef.value) return

  errorMessage.value = ''
  await formRef.value.validate()

  submitting.value = true
  try {
    await createParcel({ ...form })
    ElMessage.success('包裹入库成功')
    clearForm()
    formRef.value.clearValidate()
  } catch (error) {
    errorMessage.value = error.message || '提交失败'
    ElMessage.error(errorMessage.value)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.inbound-page {
  max-width: 680px;
}

.form-alert {
  margin: 4px 0 18px;
}

.form-actions {
  margin-bottom: 0;
}
</style>
