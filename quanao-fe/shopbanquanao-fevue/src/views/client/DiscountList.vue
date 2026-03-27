<template>
  <div class="discount-list-page container">
    <!-- Header -->
    <div class="page-header text-center">
      <h1 class="title">KHO VOUCHER</h1>
      <p class="subtitle">Săn ngay mã giảm giá hấp dẫn cho đơn hàng của bạn</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <i class="fa-solid fa-spinner fa-spin"></i>
      <p>Đang tải danh sách ưu đãi...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="activeDiscounts.length === 0" class="empty-state">
      <i class="fa-solid fa-ticket-simple-slash empty-icon"></i>
      <h3>Chưa có mã giảm giá</h3>
      <p>Hiện tại chưa có chương trình khuyến mãi nào đang diễn ra. Vui lòng quay lại sau!</p>
    </div>

    <!-- Discount Grid -->
    <div v-else class="discount-grid">
      <div v-for="discount in activeDiscounts" :key="discount.id" class="coupon-card">
        <!-- Left Side (Color Block) -->
        <div class="coupon-left">
          <div class="d-value">
            {{ discount.type === 'PERCENTAGE' ? discount.value + '%' : formatK(discount.value) }}
          </div>
          <div class="d-label">GIẢM</div>
        </div>

        <!-- Right Side (Info) -->
        <div class="coupon-right">
          <div class="coupon-info">
            <h3 class="code-text">{{ discount.code }}</h3>
            <p class="expiry-text">
              <i class="fa-regular fa-clock"></i> HSD: {{ formatDate(discount.endDate) }}
            </p>
            <p class="desc-text">
              {{ discount.minOrderAmount > 0 ? `Đơn từ ${formatPrice(discount.minOrderAmount)}` : 'Áp dụng cho mọi đơn hàng' }}
              <span v-if="discount.usageLimit > 0"> (SL có hạn)</span>
            </p>
          </div>
          
          <button 
            class="copy-btn" 
            :class="{ 'copied': copiedCode === discount.code }"
            @click="copyCode(discount.code)"
          >
            <span v-if="copiedCode === discount.code"><i class="fa-solid fa-check"></i> Đã chép</span>
            <span v-else>Sao chép</span>
          </button>
        </div>

        <!-- Visual Cutouts -->
        <div class="circle top"></div>
        <div class="circle bottom"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import discountService from '@/services/discountService.js';

const discounts = ref([]);
const loading = ref(true);
const copiedCode = ref(null);

const fetchDiscounts = async () => {
  loading.value = true;
  try {
    const res = await discountService.getAllDiscounts();
    discounts.value = res.data || [];
  } catch (err) {
    console.error("Lỗi tải mã giảm giá:", err.response || err);
    discounts.value = [];
  } finally {
    loading.value = false;
  }
};

// Chỉ hiển thị các mã đang còn hạn sử dụng
const activeDiscounts = computed(() => {
  const now = new Date();
  return discounts.value.filter(d => {
    // Kiểm tra trạng thái kích hoạt (isActive)
    if (d.isActive === false) return false;
    // Kiểm tra giới hạn lượt dùng (usageLimit)
    if (d.usageLimit > 0 && (d.usedCount || 0) >= d.usageLimit) return false;

    const start = new Date(d.startDate);
    const end = new Date(d.endDate);
    // Tính đến hết ngày kết thúc (23:59:59)
    end.setHours(23, 59, 59, 999);
    return now >= start && now <= end;
  });
});

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString('vi-VN');
};

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

const formatK = (val) => {
  if (val >= 1000) return Math.floor(val / 1000) + 'k';
  return val;
};

const copyCode = (code) => {
  navigator.clipboard.writeText(code);
  copiedCode.value = code;
  
  // Reset trạng thái nút sau 2 giây
  setTimeout(() => {
    if (copiedCode.value === code) {
      copiedCode.value = null;
    }
  }, 2000);
};

onMounted(() => {
  fetchDiscounts();
});
</script>

<style scoped>
.discount-list-page {
  padding-top: 120px; /* Tránh header fixed */
  padding-bottom: 80px;
  font-family: 'Inter', sans-serif;
  min-height: 80vh;
}

.page-header {
  margin-bottom: 40px;
}
.title {
  font-weight: 900;
  font-size: 2.2rem;
  color: #111827;
  margin-bottom: 8px;
  font-family: 'Oswald', sans-serif;
  letter-spacing: 1px;
}
.subtitle {
  color: #6b7280;
  font-size: 1rem;
}

/* GRID LAYOUT */
.discount-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

/* COUPON CARD */
.coupon-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  overflow: hidden;
  position: relative;
  border: 1px solid #e5e7eb;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.coupon-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0,0,0,0.1);
}

.coupon-left {
  background: #111827;
  color: #fff;
  width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-right: 2px dashed #fff;
}
.d-value { font-size: 1.6rem; font-weight: 800; line-height: 1; white-space: nowrap; }
.d-label { font-size: 0.8rem; font-weight: 600; margin-top: 4px; }

.coupon-right {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.code-text { font-size: 1.2rem; font-weight: 800; color: #111827; margin: 0 0 4px; }
.expiry-text { font-size: 0.85rem; color: #ef4444; margin-bottom: 4px; font-weight: 600; }
.desc-text { font-size: 0.85rem; color: #6b7280; margin-bottom: 12px; }

.copy-btn {
  align-self: flex-start;
  background: #fff;
  border: 1px solid #111827;
  color: #111827;
  padding: 6px 16px;
  border-radius: 99px;
  font-size: 0.85rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}
.copy-btn:hover { background: #111827; color: #fff; }
.copy-btn.copied { background: #22c55e; border-color: #22c55e; color: #fff; }

/* CUTOUT CIRCLES */
.circle {
  position: absolute;
  width: 24px;
  height: 24px;
  background: #fff; /* Trùng màu nền trang web, giả sử là trắng */
  border-radius: 50%;
  left: 88px; /* Canh chỉnh vị trí cắt */
  z-index: 10;
}
.circle.top { top: -12px; }
.circle.bottom { bottom: -12px; }

/* LOADING & EMPTY */
.loading-state, .empty-state {
  text-align: center;
  padding: 60px 0;
  color: #6b7280;
}
.empty-icon { font-size: 3rem; margin-bottom: 16px; opacity: 0.3; }
</style>