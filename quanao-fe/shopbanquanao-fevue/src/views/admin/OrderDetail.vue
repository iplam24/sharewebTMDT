<template>
  <div class="order-detail-page container-fluid mt-4" v-if="order">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 class="h3 mb-1 text-gray-800">Chi tiết đơn hàng #{{ order.id }}</h2>
        <p class="text-muted small mb-0">Ngày đặt: {{ formatDate(order.orderDate) }}</p>
      </div>
      <div class="d-flex gap-2">
        <router-link to="/admin/orders" class="btn btn-outline-secondary">
          <i class="fa-solid fa-arrow-left"></i> Quay lại
        </router-link>
        <button class="btn btn-primary" @click="printOrder">
          <i class="fa-solid fa-print"></i> In hóa đơn
        </button>
      </div>
    </div>

    <div class="row">
      <!-- Left Column: Order Items -->
      <div class="col-lg-8">
        <div class="card shadow mb-4">
          <div class="card-header py-3">
            <h6 class="m-0 fw-bold text-primary">Danh sách sản phẩm</h6>
          </div>
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table align-middle mb-0">
                <thead class="bg-light">
                  <tr>
                    <th class="ps-4">Sản phẩm</th>
                    <th class="text-center">Đơn giá</th>
                    <th class="text-center">Số lượng</th>
                    <th class="text-end pe-4">Tổng</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in order.items" :key="item.id">
                    <td class="ps-4">
                      <div class="d-flex align-items-center">
                        <img :src="item.imageUrl || 'https://via.placeholder.com/50'" class="rounded border me-3" width="50" height="60" style="object-fit: cover;">
                        <div>
                          <div class="fw-bold text-dark">{{ item.productName }}</div>
                          <div class="small text-muted">Phân loại: {{ item.color }} / {{ item.size }}</div>
                        </div>
                      </div>
                    </td>
                    <td class="text-center">{{ formatPrice(item.priceAtPurchase) }}</td>
                    <td class="text-center">x{{ item.quantity }}</td>
                    <td class="text-end pe-4 fw-bold">{{ formatPrice(item.priceAtPurchase * item.quantity) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Info -->
      <div class="col-lg-4">
        <!-- Status Card -->
        <div class="card shadow mb-4">
          <div class="card-header py-3">
            <h6 class="m-0 fw-bold text-primary">Trạng thái đơn hàng</h6>
          </div>
          <div class="card-body">
            <div class="mb-3">
              <label class="form-label fw-bold small text-muted">TRẠNG THÁI HIỆN TẠI</label>
              <div v-if="order.status === 'DELIVERED' || order.status === 'CANCELLED'">
                 <span class="badge w-100 py-2" :class="getStatusClass(order.status)" style="font-size: 1rem;">
                    {{ getStatusText(order.status) }}
                 </span>
              </div>
              <select 
                v-else
                v-model="order.status" 
                @change="handleStatusChange"
                class="form-select fw-bold"
                :class="getStatusClass(order.status)"
              >
                <option value="PENDING" :disabled="isOptionDisabled(order.status, 'PENDING')">Chờ xử lý</option>
                <option value="PROCESSING" :disabled="isOptionDisabled(order.status, 'PROCESSING')">Đang đóng gói</option>
                <option value="SHIPPED" :disabled="isOptionDisabled(order.status, 'SHIPPED')">Đang giao</option>
                <option value="DELIVERED">Đã giao</option>
                <option value="CANCELLED">Đã hủy</option>
              </select>
            </div>
            <div class="mb-3">
               <label class="form-label fw-bold small text-muted">THANH TOÁN</label>
               <div class="d-flex justify-content-between align-items-center border rounded p-2 bg-light">
                  <span class="fw-bold">{{ formatPaymentMethod(order.paymentMethod) }}</span>
                  <span class="badge" :class="order.paymentStatus === 'PAID' ? 'bg-success' : 'bg-secondary'">
                    {{ order.paymentStatus === 'PAID' ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                  </span>
               </div>
            </div>
          </div>
        </div>

        <!-- Customer Info -->
        <div class="card shadow mb-4">
          <div class="card-header py-3">
            <h6 class="m-0 fw-bold text-primary">Thông tin khách hàng</h6>
          </div>
          <div class="card-body">
            <div class="d-flex align-items-center mb-3">
              <div class="bg-light rounded-circle p-3 me-3 text-primary">
                <i class="fa-solid fa-user fa-lg"></i>
              </div>
              <div>
                <div class="fw-bold">{{ order.username || 'Khách vãng lai' }}</div>
                <div class="small text-muted">ID: {{ order.userId || 'N/A' }}</div>
                <div class="small text-muted" v-if="order.phoneNumber">SĐT: {{ order.phoneNumber }}</div>
              </div>
            </div>
            <hr class="my-3">
            <div class="d-flex align-items-start mb-3">
               <i class="fa-solid fa-location-dot mt-1 me-3 text-muted"></i>
               <div>
                 <div class="fw-bold small text-muted mb-1">ĐỊA CHỈ GIAO HÀNG</div>
                 <div>{{ order.shippingAddress }}</div>
               </div>
            </div>
            <div class="d-flex align-items-center" v-if="order.phoneNumber">
               <i class="fa-solid fa-phone me-3 text-muted"></i>
               <div>
                 <div class="fw-bold small text-muted mb-1">SỐ ĐIỆN THOẠI</div>
                 <div>{{ order.phoneNumber }}</div>
               </div>
            </div>
          </div>
        </div>

        <!-- Order Summary -->
        <div class="card shadow mb-4">
          <div class="card-header py-3">
            <h6 class="m-0 fw-bold text-primary">Tổng quan tài chính</h6>
          </div>
          <div class="card-body">
            <div class="d-flex justify-content-between mb-2">
              <span class="text-muted">Tạm tính</span>
              <span class="fw-bold">{{ formatPrice(calculateSubtotal(order)) }}</span>
            </div>
            <div class="d-flex justify-content-between mb-2">
              <span class="text-muted">Phí vận chuyển</span>
              <span class="fw-bold">0 ₫</span>
            </div>
            <div class="d-flex justify-content-between mb-2" v-if="order.discountAmount > 0">
              <span class="text-success">Giảm giá ({{ order.discountCode || 'Voucher' }})</span>
              <span class="fw-bold text-success">-{{ formatPrice(order.discountAmount) }}</span>
            </div>
            <hr>
            <div class="d-flex justify-content-between align-items-center">
              <span class="fw-bold h6 mb-0">TỔNG CỘNG</span>
              <span class="fw-bold h5 mb-0 text-danger">{{ formatPrice(order.totalPrice) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-else-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-2">Đang tải chi tiết đơn hàng...</p>
  </div>

  <div v-else class="text-center py-5">
    <div class="alert alert-danger d-inline-block">
      Không tìm thấy đơn hàng hoặc có lỗi xảy ra.
    </div>
    <div class="mt-3">
      <router-link to="/admin/orders" class="btn btn-secondary">Quay lại danh sách</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import orderService from '@/services/orderService.js';
import apiClient from '@/services/api.js';

const route = useRoute();
const order = ref(null);
const loading = ref(true);

const fetchOrderDetail = async () => {
  loading.value = true;
  try {
    const id = route.params.id;
    // Gọi API lấy chi tiết đơn hàng
    const res = await apiClient.get(`/api/orders/${id}`);
    order.value = res.data;
  } catch (err) {
    console.error("Lỗi tải chi tiết đơn hàng:", err);
  } finally {
    loading.value = false;
  }
};

const handleStatusChange = async () => {
  if(!confirm(`Xác nhận đổi trạng thái đơn hàng sang ${order.value.status}?`)) {
    await fetchOrderDetail(); // Reset nếu hủy
    return;
  }
  try {
    await orderService.updateOrderStatus(order.value.id, order.value.status);
    alert('Cập nhật trạng thái thành công!');
  } catch (err) {
    console.error(err);
    alert('Lỗi khi cập nhật trạng thái');
    await fetchOrderDetail();
  }
};

const calculateSubtotal = (order) => {
  if (!order.items) return 0;
  return order.items.reduce((sum, item) => sum + (item.priceAtPurchase * item.quantity), 0);
};

const printOrder = () => {
  window.print();
};

// --- Helpers ---
const formatPrice = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);

const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString('vi-VN');
};

const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return 'Chờ xử lý';
    case 'PROCESSING': return 'Đang đóng gói';
    case 'SHIPPED': return 'Đang giao';
    case 'DELIVERED': return 'Đã giao';
    case 'CANCELLED': return 'Đã hủy';
    default: return status;
  }
};

const formatPaymentMethod = (method) => {
    switch (method) {
        case 'COD': return 'Thanh toán khi nhận hàng';
        case 'BANK_TRANSFER': return 'Chuyển khoản Ngân hàng';
        case 'MOMO': return 'Ví điện tử MoMo';
        case 'VNPAY': return 'VNPAY';
        default: return method || 'Chưa xác định';
    }
};

const getStatusClass = (status) => {
  switch (status) {
    case 'PENDING': return 'text-warning border-warning';
    case 'PROCESSING': return 'text-info border-info';
    case 'SHIPPED': return 'text-primary border-primary';
    case 'DELIVERED': return 'text-success border-success';
    case 'CANCELLED': return 'text-danger border-danger';
    default: return 'text-secondary';
  }
};

const isOptionDisabled = (currentStatus, optionValue) => {
  const levels = { 'PENDING': 0, 'PROCESSING': 1, 'SHIPPED': 2, 'DELIVERED': 3, 'CANCELLED': 4 };
  const currentLevel = levels[currentStatus] !== undefined ? levels[currentStatus] : -1;
  const optionLevel = levels[optionValue] !== undefined ? levels[optionValue] : -1;
  return optionLevel < currentLevel;
};

onMounted(() => {
  fetchOrderDetail();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.order-detail-page {
  font-family: 'Inter', sans-serif;
  padding-bottom: 40px;
}

.card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background-color: #fff;
  border-bottom: 1px solid #e3e6f0;
}

.table th {
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.8rem;
  letter-spacing: 0.5px;
  color: #6b7280;
}

@media print {
  /* Ẩn toàn bộ giao diện web (sidebar, header, footer...) */
  body {
    visibility: hidden;
  }

  /* Chỉ hiển thị nội dung trang chi tiết đơn hàng */
  .order-detail-page {
    visibility: visible;
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    margin: 0;
    padding: 0;
    background: white;
    z-index: 9999;
  }

  .order-detail-page * {
    visibility: visible;
  }

  /* Ẩn các nút bấm, select box không cần thiết khi in */
  .btn, select, .d-flex.gap-2 {
    display: none !important;
  }
  .card {
    box-shadow: none !important;
    border: 1px solid #ddd !important;
  }
}
</style>