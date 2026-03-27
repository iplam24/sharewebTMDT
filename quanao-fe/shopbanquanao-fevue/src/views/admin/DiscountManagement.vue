<template>
  <div class="discount-management p-4 container-fluid">
    <div class="header d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 class="fw-bold h3 text-gray-800 mb-1">Mã giảm giá</h2>
        <p class="text-muted small">Tạo và quản lý các chương trình khuyến mãi</p>
      </div>
      <button class="btn btn-dark d-flex align-items-center gap-2" @click="openModal()">
        <i class="fa-solid fa-plus"></i> <span>Tạo mã mới</span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-2 text-muted">Đang tải dữ liệu...</p>
    </div>

    <!-- Table List -->
    <div v-else class="card shadow border-0 rounded-3 overflow-hidden">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover mb-0 align-middle">
            <thead class="bg-light text-uppercase text-secondary small fw-bold">
              <tr>
                <th class="ps-4 py-3">ID</th>
                <th class="py-3">Mã Code</th>
                <th class="py-3">Giá trị giảm</th>
                <th class="py-3">Điều kiện</th>
                <th class="py-3">Thời gian áp dụng</th>
                <th class="py-3">Trạng thái</th>
                <th class="text-end pe-4 py-3">Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="discount in discounts" :key="discount.id">
                <td class="ps-4 fw-bold text-muted">#{{ discount.id }}</td>
                <td>
                  <span class="badge bg-light text-dark border px-3 py-2 fs-6">{{ discount.code }}</span>
                </td>
                <td class="fw-bold text-danger">
                  {{ discount.type === 'PERCENTAGE' ? `-${discount.value}%` : `-${formatPrice(discount.value)}` }}
                </td>
                <td>
                  <div class="small text-muted">
                    <div v-if="discount.minOrderAmount > 0">Đơn từ: {{ formatPrice(discount.minOrderAmount) }}</div>
                    <div v-if="discount.maxDiscountAmount > 0 && discount.type === 'PERCENTAGE'">Giảm tối đa: {{ formatPrice(discount.maxDiscountAmount) }}</div>
                    <div v-if="discount.usageLimit > 0">Giới hạn: {{ discount.usageLimit }} lượt</div>
                  </div>
                </td>
                <td>
                  <div class="small text-muted">
                    <div>Bắt đầu: {{ formatDate(discount.startDate) }}</div>
                    <div>Kết thúc: {{ formatDate(discount.endDate) }}</div>
                  </div>
                </td>
                <td>
                  <span :class="['badge', isActive(discount) ? 'bg-success' : 'bg-secondary']">
                    {{ isActive(discount) ? 'Đang hoạt động' : 'Hết hạn / Chưa tới' }}
                  </span>
                </td>
                <td class="text-end pe-4">
                  <button class="btn btn-sm btn-outline-primary me-2" @click="openModal(discount)" title="Sửa">
                    <i class="fa-solid fa-pen"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="handleDelete(discount.id)" title="Xóa">
                    <i class="fa-solid fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="discounts.length === 0">
                <td colspan="6" class="text-center py-5 text-muted">
                  <i class="fa-solid fa-ticket fs-1 mb-3 d-block opacity-25"></i>
                  Chưa có mã giảm giá nào được tạo.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal Form (Overlay) -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card animate-fade-in">
        <div class="modal-header d-flex justify-content-between align-items-center mb-4">
          <h4 class="m-0 fw-bold">{{ isEditing ? 'Cập nhật Mã' : 'Tạo Mã Mới' }}</h4>
          <button class="btn-close" @click="closeModal"></button>
        </div>
        
        <form @submit.prevent="handleSubmit">
          <div class="mb-3">
            <label class="form-label fw-bold">Mã Code <span class="text-danger">*</span></label>
            <input type="text" v-model="form.code" class="form-control" required placeholder="VD: SUMMER2025" :disabled="isEditing">
            <div class="form-text">Mã code là duy nhất và không thể sửa sau khi tạo.</div>
          </div>
          
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label fw-bold">Loại giảm giá</label>
              <select v-model="form.type" class="form-select">
                <option value="PERCENTAGE">Theo phần trăm (%)</option>
                <option value="FIXED_AMOUNT">Số tiền cố định (VNĐ)</option>
              </select>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label fw-bold">Giá trị giảm <span class="text-danger">*</span></label>
              <input type="number" v-model.number="form.value" class="form-control" required min="1" :placeholder="form.type === 'PERCENTAGE' ? 'VD: 10' : 'VD: 50000'">
            </div>
          </div>

          <div class="row">
            <div class="col-md-4 mb-3">
              <label class="form-label fw-bold small">Đơn tối thiểu</label>
              <input type="number" v-model.number="form.minOrderAmount" class="form-control" min="0" placeholder="0 = Không">
            </div>
            <div class="col-md-4 mb-3">
              <label class="form-label fw-bold small">Giảm tối đa</label>
              <input type="number" v-model.number="form.maxDiscountAmount" class="form-control" min="0" placeholder="0 = Không" :disabled="form.type !== 'PERCENTAGE'">
            </div>
            <div class="col-md-4 mb-3">
              <label class="form-label fw-bold small">Giới hạn lượt</label>
              <input type="number" v-model.number="form.usageLimit" class="form-control" min="0" placeholder="0 = Vô hạn">
            </div>
          </div>

          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label fw-bold">Ngày bắt đầu <span class="text-danger">*</span></label>
              <input type="date" v-model="form.startDate" class="form-control" required>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label fw-bold">Ngày kết thúc <span class="text-danger">*</span></label>
              <input type="date" v-model="form.endDate" class="form-control" required>
            </div>
          </div>

          <div class="mb-3 form-check form-switch">
            <input class="form-check-input" type="checkbox" id="isActiveSwitch" v-model="form.isActive">
            <label class="form-check-label fw-bold" for="isActiveSwitch">Kích hoạt mã này</label>
          </div>

          <div class="d-flex justify-content-end gap-2 mt-4 pt-3 border-top">
            <button type="button" class="btn btn-light border" @click="closeModal">Hủy bỏ</button>
            <button type="submit" class="btn btn-primary px-4">{{ isEditing ? 'Lưu thay đổi' : 'Tạo mã' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import discountService from '@/services/discountService.js';

const discounts = ref([]);
const loading = ref(false);
const showModal = ref(false);
const isEditing = ref(false);

const form = reactive({ 
  id: null, 
  code: '', 
  type: 'PERCENTAGE', 
  value: null, 
  minOrderAmount: 0,
  maxDiscountAmount: 0,
  usageLimit: 0,
  startDate: '', 
  endDate: '',
  isActive: true
});

const fetchDiscounts = async () => {
  loading.value = true;
  try {
    const res = await discountService.getAllDiscounts();
    discounts.value = res.data || [];
  } catch (err) {
    console.error(err);
    discounts.value = [];
  } finally {
    loading.value = false;
  }
};

const openModal = (discount = null) => {
  if (discount) {
    isEditing.value = true;
    form.id = discount.id;
    form.code = discount.code;
    form.type = discount.type || 'PERCENTAGE';
    form.value = discount.value;
    form.minOrderAmount = discount.minOrderAmount || 0;
    form.maxDiscountAmount = discount.maxDiscountAmount || 0;
    form.usageLimit = discount.usageLimit || 0;
    form.startDate = discount.startDate ? discount.startDate.split('T')[0] : '';
    form.endDate = discount.endDate ? discount.endDate.split('T')[0] : '';
    form.isActive = discount.isActive !== undefined ? discount.isActive : true;
  } else {
    isEditing.value = false;
    Object.assign(form, { id: null, code: '', type: 'PERCENTAGE', value: null, minOrderAmount: 0, maxDiscountAmount: 0, usageLimit: 0, startDate: '', endDate: '', isActive: true });
  }
  showModal.value = true;
};

const closeModal = () => { showModal.value = false; };

const handleSubmit = async () => {
  try {
    // Clone form để xử lý dữ liệu
    const payload = { ...form };

    // Backend (Java LocalDateTime) yêu cầu định dạng yyyy-MM-ddTHH:mm:ss
    // Input type="date" chỉ trả về yyyy-MM-dd nên cần nối thêm giờ
    if (payload.startDate && !payload.startDate.includes('T')) payload.startDate += 'T00:00:00';
    if (payload.endDate && !payload.endDate.includes('T')) payload.endDate += 'T23:59:59';

    if (isEditing.value) await discountService.updateDiscount(form.id, payload);
    else await discountService.createDiscount(payload);
    
    alert(isEditing.value ? 'Cập nhật thành công!' : 'Tạo mã thành công!');
    closeModal();
    fetchDiscounts();
  } catch (err) {
    console.error("Lỗi submit:", err);
    alert(err.response?.data?.message || 'Có lỗi xảy ra. Vui lòng kiểm tra lại dữ liệu.');
  }
};

const handleDelete = async (id) => {
  if (!confirm('Bạn có chắc muốn xóa mã này?')) return;
  try {
    await discountService.deleteDiscount(id);
    fetchDiscounts();
  } catch (err) {
    alert('Lỗi khi xóa mã');
  }
};

const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleDateString('vi-VN') : '';
const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

const isActive = (discount) => {
  if (discount.isActive === false) return false; // Ưu tiên cờ từ backend
  const now = new Date();
  const start = new Date(discount.startDate);
  const end = new Date(discount.endDate);
  // Set end date to end of day
  end.setHours(23, 59, 59, 999);
  return now >= start && now <= end;
};

onMounted(() => { fetchDiscounts(); });
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  backdrop-filter: blur(2px);
}
.modal-card {
  background: white;
  padding: 28px;
  border-radius: 16px;
  width: 90%;
  max-width: 520px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.3);
}
.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>