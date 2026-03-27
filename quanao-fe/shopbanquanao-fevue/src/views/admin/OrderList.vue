<template>
  <div class="order-list-page container-fluid mt-4">
    
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="h3 mb-0 text-gray-800">Quản lý Đơn hàng</h2>
    </div>

    <!-- Filter Section -->
    <div class="card shadow mb-4">
      <div class="card-body py-3">
        <div class="row g-3 align-items-end">
          <div class="col-md-4">
            <label class="form-label small fw-bold text-muted mb-1">Tìm kiếm</label>
            <div class="input-group">
              <span class="input-group-text bg-light border-end-0"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
              <input type="text" class="form-control border-start-0 ps-0" v-model="searchKeyword" placeholder="Mã đơn, Tên KH, SĐT...">
            </div>
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold text-muted mb-1">Trạng thái</label>
            <select class="form-select" v-model="filterStatus">
              <option value="">Tất cả trạng thái</option>
              <option value="PENDING">Chờ xử lý</option>
              <option value="PROCESSING">Đang đóng gói</option>
              <option value="SHIPPED">Đang giao</option>
              <option value="DELIVERED">Đã giao</option>
              <option value="CANCELLED">Đã hủy</option>
            </select>
          </div>
          <div class="col-md-5">
            <label class="form-label small fw-bold text-muted mb-1">Khoảng thời gian</label>
            <div class="input-group">
              <input type="date" class="form-control" v-model="startDate">
              <span class="input-group-text bg-light border-start-0 border-end-0">đến</span>
              <input type="date" class="form-control" v-model="endDate">
              <button class="btn btn-outline-secondary" type="button" @click="resetFilters" title="Xóa bộ lọc">
                <i class="fa-solid fa-rotate-right"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2">Đang tải đơn hàng...</p>
    </div>

    <div v-else-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <div v-else class="card shadow mb-4">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-striped mb-0 align-middle">
            <thead class="table-light">
              <tr>
                <th>Mã đơn</th>
                <th>Khách hàng</th>
                <th>Ngày đặt</th>
                <th>Tổng tiền</th>
                <th>Thanh toán</th>
                <th>P.thức TT</th> 
                <th>Địa chỉ</th>
                <th>Trạng thái</th>
                <th class="text-end">Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in filteredOrders" :key="order.id">
                <td class="fw-bold">#{{ order.id }}</td>
                <td>{{ order.username || 'N/A' }}</td> 
                <td>{{ formatDate(order.orderDate) }}</td>
                <td class="price text-danger fw-bold">{{ formatPrice(order.totalPrice) }}</td>
                
                <td>
                  <span class="badge" :class="order.paymentStatus === 'PAID' ? 'bg-success' : 'bg-secondary'">
                    {{ order.paymentStatus }}
                  </span>
                </td>
                
                <td class="fw-medium">
                  {{ formatPaymentMethod(order.paymentMethod) }}
                </td>

                <td class="address small text-muted">{{ order.shippingAddress }}</td>
                
                <td>
                  <div v-if="order.status === 'DELIVERED' || order.status === 'CANCELLED'">
                    <span class="badge" :class="getStatusClass(order.status)" style="font-size: 0.85rem;">
                      {{ getStatusText(order.status) }}
                    </span>
                  </div>
                  <select 
                    v-else
                    v-model="order.status" 
                    @change="handleStatusChange(order)"
                    class="form-select form-select-sm fw-bold"
                    :class="getStatusClass(order.status)"
                  >
                    <option value="PENDING" :disabled="isOptionDisabled(order.status, 'PENDING')">Chờ xử lý</option>
                    <option value="PROCESSING" :disabled="isOptionDisabled(order.status, 'PROCESSING')">Đang đóng gói</option>
                    <option value="SHIPPED" :disabled="isOptionDisabled(order.status, 'SHIPPED')">Đang giao</option>
                    <option value="DELIVERED">Đã giao</option>
                    <option value="CANCELLED">Đã hủy</option>
                  </select>
                </td>

                <td class="text-end">
                  <router-link :to="{ name: 'OrderDetail', params: { id: order.id } }" class="btn btn-sm btn-info text-white">
                    <i class="fa-solid fa-circle-info"></i> Chi tiết
                  </router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <div v-if="filteredOrders.length === 0" class="card-body py-5 text-center text-muted">
        Không tìm thấy đơn hàng nào.
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import orderService from '@/services/orderService.js';

const orders = ref([]);
const loading = ref(true);
const error = ref(null);

const searchKeyword = ref('');
const filterStatus = ref('');
const startDate = ref('');
const endDate = ref('');

// Load danh sách đơn hàng
const fetchOrders = async () => {
  loading.value = true;
  error.value = null;
  try {
    // API Admin lấy tất cả (không phân trang)
    const res = await orderService.getAllOrders(); 
    orders.value = res.data;
  } catch (err) {
    console.error(err);
    error.value = "Không thể tải danh sách đơn hàng";
  } finally {
    loading.value = false;
  }
};

const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    // 1. Lọc theo từ khóa (ID, Username, SĐT)
    const term = searchKeyword.value.toLowerCase().trim();
    const matchesSearch = !term || 
      order.id.toString().includes(term) ||
      (order.username && order.username.toLowerCase().includes(term)) ||
      (order.phoneNumber && order.phoneNumber.includes(term));

    // 2. Lọc theo trạng thái
    const matchesStatus = !filterStatus.value || order.status === filterStatus.value;

    // 3. Lọc theo ngày
    let matchesDate = true;
    if (startDate.value) {
      matchesDate = matchesDate && new Date(order.orderDate) >= new Date(startDate.value);
    }
    if (endDate.value) {
      const end = new Date(endDate.value);
      end.setHours(23, 59, 59, 999); // Hết ngày
      matchesDate = matchesDate && new Date(order.orderDate) <= end;
    }

    return matchesSearch && matchesStatus && matchesDate;
  });
});

const resetFilters = () => {
  searchKeyword.value = '';
  filterStatus.value = '';
  startDate.value = '';
  endDate.value = '';
};

// Xử lý khi Admin đổi trạng thái
const handleStatusChange = async (order) => {
  if(!confirm(`Bạn có chắc muốn đổi trạng thái đơn #${order.id} thành ${order.status}?`)) {
    // Nếu user hủy, load lại data để reset select box về cũ
    fetchOrders();
    return;
  }

  try {
    // Gọi API PUT /api/orders/{orderId}/status?status={newStatus}
    await orderService.updateOrderStatus(order.id, order.status);
    alert('Cập nhật trạng thái thành công!');
  } catch (err) {
    console.error(err);
    alert('Lỗi khi cập nhật trạng thái');
    fetchOrders(); // Reset lại nếu lỗi
  }
};

// --- Helpers ---
const formatPrice = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

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

const isOptionDisabled = (currentStatus, optionValue) => {
  const levels = {
    'PENDING': 0,
    'PROCESSING': 1,
    'SHIPPED': 2,
    'DELIVERED': 3,
    'CANCELLED': 4
  };
  const currentLevel = levels[currentStatus] !== undefined ? levels[currentStatus] : -1;
  const optionLevel = levels[optionValue] !== undefined ? levels[optionValue] : -1;
  return optionLevel < currentLevel;
};

/**
 * Helper để dịch Payment Method
 */
const formatPaymentMethod = (method) => {
    switch (method) {
        case 'COD': return 'Thanh toán khi nhận hàng';
        case 'BANK_TRANSFER': return 'Chuyển khoản Ngân hàng';
        case 'MOMO': return 'Ví điện tử MoMo';
        default: return method || 'Chưa xác định';
    }
};

const getStatusClass = (status) => {
  // Dùng class của Bootstrap cho Select box
  switch (status) {
    case 'PENDING': return 'text-warning border-warning';
    case 'PROCESSING': return 'text-info border-info';
    case 'SHIPPED': return 'text-primary border-primary';
    case 'DELIVERED': return 'text-success border-success';
    case 'CANCELLED': return 'text-danger border-danger';
    default: return 'text-secondary';
  }
};

onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.order-list-page {
  padding: 10px;
  font-family: 'Inter', sans-serif;
}

/* TITLE */
h2 {
  font-weight: 700;
  color: #111827;
}

/* CARD */
.card {
  border: none;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  overflow: hidden;
}

.card-header {
  font-weight: 700;
  background: white;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

/* TABLE */
.table {
  margin: 0;
  font-size: .92rem;
}

.table thead th {
  background: #f9fafb !important;
  color: #374151;
  font-size: 0.85rem;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 1px;
  padding: 14px 12px;
  border-bottom: 1px solid #e5e7eb;
}

.table tbody tr {
  transition: background 0.15s ease;
}

.table tbody tr:hover {
  background: #f3f4f6;
}

.table td {
  padding: 16px 12px !important;
  vertical-align: middle;
}

/* PRICE */
.price {
  font-weight: 700;
  color: #e11d48;
}

/* ADDRESS */
.address {
  max-width: 180px;
  white-space: normal;
  line-height: 1.4;
}

/* BADGES */
.badge {
  padding: 6px 10px;
  font-size: 0.75rem;
  font-weight: 700;
  border-radius: 6px;
  text-transform: uppercase;
}

/* SELECT STATUS STYLE */
.form-select-sm {
  padding: 6px 10px;
  font-size: .8rem;
  font-weight: 700;
  border-radius: 6px;
  cursor: pointer;
  background-color: #fff !important;
  transition: .2s ease;
}

.form-select-sm:hover {
  background: #f3f4f6;
}

/* STATUS COLORS */
.text-warning { color: #f59e0b !important; }
.border-warning { border-color: #f59e0b !important; }

.text-info { color: #0ea5e9 !important; }
.border-info { border-color: #0ea5e9 !important; }

.text-primary { color: #2563eb !important; }
.border-primary { border-color: #2563eb !important; }

.text-success { color: #10b981 !important; }
.border-success { border-color: #10b981 !important; }

.text-danger { color: #ef4444 !important; }
.border-danger { border-color: #ef4444 !important; }

/* BUTTON DETAIL */
.btn-info {
  background: #2563eb;
  border: none;
  padding: 6px 12px;
  font-weight: 600;
  border-radius: 8px;
  transition: .2s ease;
}

.btn-info:hover {
  background: #1e40af;
}

/* EMPTY STATE */
.text-muted {
  font-size: 0.95rem;
}

.spinner-border {
  width: 2.4rem;
  height: 2.4rem;
}
</style>