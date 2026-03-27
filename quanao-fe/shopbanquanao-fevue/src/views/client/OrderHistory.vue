<template>
  <div class="history-page container">
    <div class="page-header">
      <h1>LỊCH SỬ ĐƠN HÀNG</h1>
      <p>Theo dõi trạng thái và lịch sử mua sắm của bạn</p>
    </div>
    
    <div class="filter-bar">
      <div class="filter-group">
        <label for="status-filter"><i class="fa-solid fa-filter"></i> Lọc theo Trạng thái:</label>
        <select v-model="filterStatus" id="status-filter">
          <option value="">Tất cả</option>
          <option value="PENDING">Chờ xử lý</option>
          <option value="SHIPPED">Đang giao hàng</option>
          <option value="DELIVERED">Giao thành công</option>
          <option value="CANCELLED">Đã hủy</option>
        </select>
      </div>
      
      <div class="pagination-controls">
        <span>Trang {{ currentPage }} / {{ totalPages }}</span>
        <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">
          &larr; Trang trước
        </button>
        <button :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">
          Trang sau &rarr;
        </button>
      </div>
    </div>
    
    <div v-if="loading" class="loading-state">
      <i class="fa-solid fa-spinner fa-spin"></i> Đang tải dữ liệu...
    </div>

    <div v-else-if="orders.length === 0" class="empty-state">
      <i class="fa-solid fa-box-open"></i>
      <h3>Không có đơn hàng nào phù hợp</h3>
      <RouterLink to="/shop" class="btn-shop">MUA SẮM NGAY</RouterLink>
    </div>
    
    <div v-else class="orders-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="header-left">
            <span class="order-id">ĐƠN HÀNG #{{ order.id }}</span>
            <span class="order-date">{{ formatDate(order.orderDate) }}</span>
          </div>
          <div class="header-right">
            <span class="status-badge" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </span>
            <span class="payment-badge" :class="getPaymentClass(order.paymentStatus)">
              {{ getPaymentText(order.paymentStatus) }}
            </span>
          </div>
        </div>

        <div class="order-body">
     <div v-for="item in order.items" :key="item.id" class="order-item">
  <div class="item-img">
    <img :src="item.imageUrl || 'https://via.placeholder.com/100x120?text=No+Image'" alt="Product">
  </div>

  <div class="item-info">
    <h4 class="item-name">{{ item.productName }}</h4>
    <p class="item-variant">Phân loại: {{ item.color }} / {{ item.size }}</p>
    <p class="item-qty">x{{ item.quantity }}</p>

    <!-- ⭐⭐⭐ NÚT ĐÁNH GIÁ SẢN PHẨM ⭐⭐⭐ -->
    <button
  v-if="order.status === 'DELIVERED'"
  class="btn-review"
  @click="goReview(item.productId)"
>
  <i class="fa-regular fa-star"></i> Đánh giá
</button>


  </div>

  <div class="item-price">
    {{ formatPrice(item.priceAtPurchase * item.quantity) }}
  </div>
</div>

        </div>

        <div class="order-footer">
          <div class="footer-left">
            <p class="shipping-address"><i class="fa-solid fa-location-dot"></i> {{ order.shippingAddress }}</p>
            <p class="payment-method"><i class="fa-regular fa-credit-card"></i> Thanh toán: <strong>{{ order.paymentMethod === 'VNPAY' ? 'VNPay' : 'Khi nhận hàng (COD)' }}</strong></p>
          </div>

         <div class="footer-right">
        <div class="total-row"><span>TỔNG CỘNG:</span><span class="total-price">{{ formatPrice(order.totalPrice) }}</span></div>
        
        <button 
          v-if="order.status === 'SHIPPED'" 
          class="btn-confirm" 
          @click="handleConfirmDelivery(order.id)"
        >
          ĐÃ NHẬN ĐƯỢC HÀNG
        </button>

        <button 
          v-if="order.status === 'PENDING'" 
          class="btn-cancel" 
          @click="handleCancelOrder(order.id)"
        >
          HỦY ĐƠN HÀNG
        </button>
    </div>
        </div>
      </div>
    </div>
    
    <div v-if="totalPages > 1" class="filter-bar mt-4">
       <div class="pagination-controls">
        <span>Trang {{ currentPage }} / {{ totalPages }}</span>
        <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">
          &larr; Trang trước
        </button>
        <button :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">
          Trang sau &rarr;
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { RouterLink,useRouter } from 'vue-router';
import orderService from '@/services/orderService.js';


const orders = ref([]);
const loading = ref(true);
const filterStatus = ref(''); 
const currentPage = ref(1); 
const totalPages = ref(1); 
const PAGE_SIZE = 5; 
const router = useRouter();

const goReview = (productId) => {
  router.push(`/product/${productId}/review`);
};

const fetchOrders = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value - 1,
      size: PAGE_SIZE,
      status: filterStatus.value || undefined
    };
    
    const res = await orderService.getMyOrders(params); 
    
    // GÁN DỮ LIỆU PHÂN TRANG VÀO BIẾN
    orders.value = res.data.content; 
    totalPages.value = res.data.totalPages;
    currentPage.value = res.data.currentPage; 
    
  } catch (err) {
    console.error(err);
    alert('Lỗi tải đơn hàng. Vui lòng thử lại.');
    orders.value = [];
  } finally {
    loading.value = false;
  }
};
const handleConfirmDelivery = async (orderId) => {
    if (!confirm("Xác nhận đã nhận đơn hàng thành công?")) { return; }
    try {
        await orderService.confirmDelivery(orderId);
        alert("Xác nhận thành công! Cảm ơn bạn.");
        fetchOrders(); // Tải lại danh sách
    } catch (err) {
        alert("Lỗi: " + (err.response?.data?.message || err.message));
    }
};
const changePage = (pageNumber) => {
  if (pageNumber > 0 && pageNumber <= totalPages.value) {
    currentPage.value = pageNumber;
    fetchOrders(); 
  }
};

const handleCancelOrder = async (orderId) => {
  if (!confirm("Bạn có chắc chắn muốn hủy đơn hàng này?")) { return; }
  try {
    await orderService.cancelOrder(orderId);
    alert("Đã hủy đơn hàng thành công.");
    fetchOrders(); 
  } catch (err) {
    alert("Lỗi: " + (err.response?.data?.message || err.message));
  }
};

watch(filterStatus, (newStatus) => {
  currentPage.value = 1; // Reset về trang 1 khi đổi bộ lọc
  fetchOrders();
});


// --- HELPERS (Giữ nguyên) ---
const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
const formatDate = (dateString) => {
  if(!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit'});
};
const getStatusText = (status) => {
  const map = { 'PENDING': 'Chờ xử lý', 'PROCESSING': 'Đang đóng gói', 'SHIPPED': 'Đang giao hàng', 'DELIVERED': 'Giao thành công', 'CANCELLED': 'Đã hủy' };
  return map[status] || status;
};
const getStatusClass = (status) => {
  switch(status) {
    case 'PENDING': return 'st-pending';
    case 'PROCESSING': return 'st-processing';
    case 'SHIPPED': return 'st-shipped';
    case 'DELIVERED': return 'st-delivered';
    case 'CANCELLED': return 'st-cancelled';
    default: return '';
  }
};
const getPaymentText = (status) => {
  const map = { 'UNPAID': 'Chưa thanh toán', 'PAID': 'Đã thanh toán', 'FAILED': 'Thanh toán lỗi' };
  return map[status] || status;
};
const getPaymentClass = (status) => {
  return status === 'PAID' ? 'pay-paid' : 'pay-unpaid';
};

onMounted(() => fetchOrders());
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oswald:wght@400;700&family=Inter:wght@300;400;500;600;700&display=swap');

.history-page {
  padding: 110px 20px 80px;
  max-width: 1050px;
  margin: auto;
  min-height: 70vh;
  font-family: "Inter", sans-serif;
}

/* ===========================
   HEADER
=========================== */
.page-header {
  text-align: center;
  margin-bottom: 45px;
}
.page-header h1 {
  font-family: 'Oswald', sans-serif;
  font-size: 2.6rem;
  letter-spacing: 2px;
}
.page-header p {
  color: #6b7280;
  margin-top: 6px;
  font-size: .95rem;
}

/* ===========================
   FILTER BAR
=========================== */
.filter-bar {
  background: #ffffff;
  border: 1px solid #ececec;
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 35px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 6px 18px rgba(0,0,0,0.05);
}

.btn-confirm {
  background: #28a745; 
  color: white; 
  border: none; 
  padding: 8px 20px; 
  font-weight: 700; 
  font-size: 0.85rem; 
  cursor: pointer; 
  transition: all 0.2s; 
  letter-spacing: 1px; 
  white-space: nowrap;
  border-radius: 4px;
}
.btn-confirm:hover {
  background: #218838;
}
.filter-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-group label {
  font-size: .9rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-group select {
  padding: 9px 12px;
  font-size: .9rem;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  background: #fafafa;
}

/* Pagination buttons */
.pagination-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
}
.pagination-controls button {
  border: none;
  background: #111827;
  color: white;
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
  transition: .2s;
}
.pagination-controls button:hover:not(:disabled) {
  background: #000;
}
.pagination-controls button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

/* ===========================
   EMPTY STATE
=========================== */
.empty-state {
  border: 2px dashed #ddd;
  text-align: center;
  padding: 50px 30px;
  border-radius: 14px;
}
.empty-state i {
  font-size: 4.2rem;
  color: #ccc;
}
.empty-state h3 {
  font-family: 'Oswald', sans-serif;
  margin-top: 10px;
  font-size: 1.4rem;
}
.btn-shop {
  margin-top: 22px;
  background: #000;
  padding: 12px 32px;
  color: #fff;
  text-decoration: none;
  border-radius: 6px;
  font-weight: 700;
}
.btn-shop:hover {
  background: #333;
}

/* ===========================
   ORDER CARD — PREMIUM
=========================== */
.order-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 0;
  margin-bottom: 40px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 8px 22px rgba(17,24,39,0.05);
  overflow: hidden;
}

/* HEADER */
.order-header {
  padding: 18px 25px;
  background: #f9fafb;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
}

.order-id {
  font-family: 'Oswald', sans-serif;
  font-size: 1.2rem;
  letter-spacing: 1.2px;
}

.order-date {
  font-size: .85rem;
  color: #6b7280;
}

/* Badges */
.status-badge,
.payment-badge {
  padding: 6px 12px;
  font-size: .72rem;
  font-weight: 700;
  border-radius: 5px;
  text-transform: uppercase;
}

/* Status colors – Soft Pastel Premium */
.st-pending     { background: #fff7db; color: #946400; }
.st-processing  { background: #d8f3ff; color: #046fa7; }
.st-shipped     { background: #e2eaff; color: #003f8c; }
.st-delivered   { background: #dbffe8; color: #116c37; }
.st-cancelled   { background: #ffe2e2; color: #9b1c1c; }

.pay-paid       { background: #dbffe8; color: #0d7a3e; }
.pay-unpaid     { background: #f2f2f2; color: #444; }

/* ===========================
   ORDER ITEMS
=========================== */
.order-body {
  padding: 25px;
}

.order-item {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  padding-bottom: 22px;
  border-bottom: 1px dashed #e5e7eb;
  margin-bottom: 18px;
}

.order-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.item-img img {
  width: 75px;
  height: 95px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.item-name {
  font-size: 1rem;
  font-weight: 600;
  font-family: "Inter", sans-serif;
}
.item-variant,
.item-qty {
  font-size: .85rem;
  color: #6b7280;
}
.item-price {
  font-weight: 700;
  font-size: 1rem;
  white-space: nowrap;
}

/* ===========================
   FOOTER
=========================== */
.order-footer {
  padding: 22px 25px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-end;
}

.footer-left p {
  margin: 6px 0;
  color: #444;
  font-size: .9rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.total-row {
  font-family: 'Oswald', sans-serif;
  font-size: 1.2rem;
}
.total-price {
  color: #d0021b;
  font-weight: 700;
  font-size: 1.35rem;
}

/* CANCEL BUTTON */
.btn-cancel {
  border: 1px solid #dc3545;
  padding: 9px 24px;
  background: white;
  font-weight: 700;
  color: #dc3545;
  border-radius: 6px;
  transition: .2s;
}
.btn-cancel:hover {
  background: #dc3545;
  color: white;
}

/* ===========================
   RESPONSIVE
=========================== */
@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    gap: 14px;
    align-items: flex-start;
  }
  .order-header {
    align-items: flex-start;
  }
  .order-footer {
    flex-direction: column;
    align-items: flex-start;
  }
  .footer-right {
    width: 100%;
    display: flex;
    justify-content: space-between;
  }
  .btn-cancel {
    width: 100%;
    text-align: center;
  }
  .footer-right { 
        width: 100%; 
        display: flex; 
        flex-direction: column; /* Xếp dọc các nút */
        gap: 10px; /* Thêm khoảng cách giữa 2 nút */
        align-items: stretch; /* Kéo dãn nút */
    }
    .btn-cancel, .btn-confirm {
        width: 100%; text-align: center; padding: 12px; 
    }
  }
  .btn-review {
  margin-top: 8px;
  padding: 7px 14px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: .85rem;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: 0.2s;
}

.btn-review:hover {
  background: #1e40af;
}
@media (max-width: 768px) {
  .btn-review {
    width: 100%;
    justify-content: center;
    padding: 12px;
    font-size: .95rem;
    margin-top: 10px;
  }
}
@media (max-width: 480px) {
  .btn-review {
    padding: 12px 16px;
    font-size: 1rem;
    border-radius: 8px;
  }
}

</style>
