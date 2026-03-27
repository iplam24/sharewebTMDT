<template>
  <div class="cart-page container">
    <div class="page-header">
      <h1>GIỎ HÀNG CỦA BẠN</h1>
      <RouterLink to="/shop" class="continue-shopping">
        <i class="fa-solid fa-arrow-left"></i> TIẾP TỤC MUA SẮM
      </RouterLink>
    </div>

    <div v-if="loading" class="loading-state">
      <i class="fa-solid fa-spinner fa-spin"></i> Đang tải giỏ hàng...
    </div>

    <div v-else-if="!cart || !cart.items || cart.items.length === 0" class="empty-cart">
      <i class="fa-solid fa-cart-arrow-down empty-icon"></i>
      <h3>GIỎ HÀNG TRỐNG</h3>
      <p>Hãy thêm các sản phẩm yêu thích vào giỏ!</p>
      <RouterLink to="/shop" class="go-shop-btn">MUA SẮM NGAY</RouterLink>
    </div>

    <div v-else class="cart-layout">
      
      <div class="cart-items">
        <!-- FILTER TOOLBAR -->
        <div class="cart-filters">
          <div class="filter-input">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" v-model="searchText" placeholder="Tìm theo tên sản phẩm..." />
          </div>
          <div class="filter-input">
            <i class="fa-solid fa-filter-circle-dollar"></i>
            <input type="number" v-model="filterMaxPrice" placeholder="Giá tối đa (VNĐ)..." />
          </div>
        </div>

        <!-- HEADER DESKTOP -->
        <div class="cart-header-row desktop-only">
          <span>SẢN PHẨM</span>
          <span style="text-align: center;">GIÁ</span>
          <span style="text-align: center;">SỐ LƯỢNG</span>
          <span style="text-align: right;">TẠM TÍNH</span>
        </div>

        <!-- ITEM -->
        <div v-if="filteredItems.length === 0" class="filter-empty">
          <p>Không tìm thấy sản phẩm nào phù hợp với tiêu chí lọc.</p>
        </div>

        <div v-else v-for="item in filteredItems" :key="item.id" class="cart-item">
          
          <div class="item-info">
            <img :src="item.imageUrl || 'https://via.placeholder.com/100'" alt="Product" class="item-img" />
            <div class="item-details">
              <h3>
                <RouterLink :to="`/product/${item.productId}`">{{ item.productName }}</RouterLink>
              </h3>
              <p class="variant-text">Phân loại: {{ item.color }} / {{ item.size }}</p>

              <!-- GIÁ DÙNG CHO MOBILE -->
              <p class="item-unit-price mobile-only">{{ formatPrice(item.price) }}</p>

              <button @click="handleRemove(item.id)" class="remove-btn">Xóa</button>
            </div>
          </div>

          <!-- GIÁ (DESKTOP) -->
          <div class="item-price desktop-only">
            {{ formatPrice(item.price) }}
          </div>

          <!-- SỐ LƯỢNG -->
          <div class="item-quantity">
            <div class="qty-box">
              <button @click="updateQuantity(item, -1)" :disabled="item.quantity <= 1 || isUpdating">-</button>
              <input type="text" :value="item.quantity" readonly />
              <button @click="updateQuantity(item, 1)" :disabled="isUpdating">+</button>
            </div>
          </div>

          <!-- TẠM TÍNH -->
          <div class="item-total">
            {{ formatPrice(item.price * item.quantity) }}
          </div>
        </div>
      </div>

      <div class="cart-summary-wrapper">
        <div class="cart-summary">
          <h2>TÓM TẮT ĐƠN HÀNG</h2>
          <div class="summary-row">
            <span>Tạm tính:</span>
            <span>{{ formatPrice(cart.totalPrice) }}</span>
          </div>
          <div class="summary-row">
            <span>Phí vận chuyển:</span>
            <span>Miễn phí</span>
          </div>

          <!-- MÃ GIẢM GIÁ -->
          <div class="coupon-section">
            <div class="input-group">
              <input v-model="couponCode" type="text" placeholder="Nhập mã giảm giá" :disabled="discountApplied">
              <button @click="applyCoupon" :disabled="!couponCode || discountApplied || isCheckingCoupon">
                {{ isCheckingCoupon ? '...' : (discountApplied ? 'Đã dùng' : 'Áp dụng') }}
              </button>
            </div>
            <p v-if="couponMessage" :class="couponSuccess ? 'text-success' : 'text-danger'" class="coupon-msg">
              {{ couponMessage }}
            </p>
            <div v-if="discountApplied" class="summary-row discount-row">
              <span>Giảm giá:</span>
              <span class="text-success">-{{ formatPrice(discountAmount) }}</span>
            </div>
            <button v-if="discountApplied" @click="removeCoupon" class="btn-remove-coupon">Bỏ mã</button>
          </div>

          <div class="divider"></div>
          <div class="summary-row total">
            <span>TỔNG CỘNG:</span>
            <span>{{ formatPrice(finalPrice) }}</span>
          </div>

          <button class="checkout-btn" @click="handleCheckout">
            TIẾN HÀNH THANH TOÁN
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import cartService from '@/services/cartService.js';
import discountService from '@/services/discountService.js';
import { useAuthStore } from '@/stores/authStore.js';
import { useCartStore } from '@/stores/cartStore.js';

const router = useRouter();
const authStore = useAuthStore();
const cartStore = useCartStore();
const cart = ref(null);
const loading = ref(true);
const isUpdating = ref(false); 

// --- LOGIC MÃ GIẢM GIÁ ---
const couponCode = ref('');
const discountApplied = ref(false);
const discountAmount = ref(0);
const couponMessage = ref('');
const couponSuccess = ref(false);
const isCheckingCoupon = ref(false);

// --- LOGIC LỌC CART ---
const searchText = ref('');
const filterMaxPrice = ref('');

const filteredItems = computed(() => {
  if (!cart.value || !cart.value.items) return [];
  return cart.value.items.filter(item => {
    const matchName = !searchText.value || item.productName.toLowerCase().includes(searchText.value.toLowerCase());
    const matchPrice = !filterMaxPrice.value || item.price <= Number(filterMaxPrice.value);
    return matchName && matchPrice;
  });
});

const finalPrice = computed(() => {
  if (!cart.value) return 0;
  let total = cart.value.totalPrice - discountAmount.value;
  return total > 0 ? total : 0;
});

const applyCoupon = async () => {
  if (!couponCode.value.trim()) return;
  
  isCheckingCoupon.value = true;
  couponMessage.value = '';
  couponSuccess.value = false;

  try {
    const res = await discountService.getDiscountByCode(couponCode.value.trim());
    const discount = res.data;

    // Validate cơ bản phía Client (Backend cũng nên check lại khi đặt hàng)
    if (discount.isActive === false) throw new Error('Mã giảm giá này đã bị vô hiệu hóa.');
    const now = new Date();
    if (new Date(discount.startDate) > now) throw new Error('Mã giảm giá chưa đến đợt sử dụng.');
    if (new Date(discount.endDate) < now) throw new Error('Mã giảm giá đã hết hạn.');
    if (discount.usageLimit > 0 && discount.usedCount >= discount.usageLimit) throw new Error('Mã giảm giá đã hết lượt sử dụng.');
    if (cart.value.totalPrice < discount.minOrderAmount) throw new Error(`Đơn hàng tối thiểu phải từ ${formatPrice(discount.minOrderAmount)}.`);

    // Kiểm tra xem mã có giới hạn người dùng không
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    if (discount.allowedUserIds && discount.allowedUserIds.length > 0) {
      if (!currentUser.id || !discount.allowedUserIds.includes(currentUser.id)) {
        throw new Error('Mã giảm giá không áp dụng cho tài khoản của bạn.');
      }
    }

    // Kiểm tra xem user đã dùng mã này chưa (nếu backend trả về danh sách usedUserIds)
    if (discount.usedUserIds && discount.usedUserIds.includes(currentUser.id)) {
      throw new Error('Bạn đã sử dụng mã giảm giá này rồi.');
    }

    // Tính toán số tiền giảm
    let amount = 0;
    if (discount.type === 'PERCENTAGE') {
      amount = cart.value.totalPrice * (discount.value / 100);
      if (discount.maxDiscountAmount > 0 && amount > discount.maxDiscountAmount) {
        amount = discount.maxDiscountAmount;
      }
    } else {
      amount = discount.value;
    }

    discountAmount.value = amount;
    discountApplied.value = true;
    couponSuccess.value = true;
    couponMessage.value = `Áp dụng thành công: Giảm ${formatPrice(amount)}`;
  } catch (err) {
    console.error(err);
    couponMessage.value = err.response?.data?.message || err.message || 'Mã giảm giá không hợp lệ.';
    discountApplied.value = false;
    discountAmount.value = 0;
  } finally {
    isCheckingCoupon.value = false;
  }
};

const removeCoupon = () => {
  couponCode.value = '';
  discountApplied.value = false;
  discountAmount.value = 0;
  couponMessage.value = '';
  couponSuccess.value = false;
};

const fetchCart = async () => {
  loading.value = true;
  try {
    const res = await cartService.getCart();
    cart.value = res.data;
    if (res.data && res.data.items) {
        cartStore.cartCount = res.data.items.reduce((sum, item) => sum + item.quantity, 0);
    }
  } catch (err) {
    console.error("Lỗi tải giỏ hàng:", err);
    cart.value = { items: [], totalPrice: 0 }; 
  } finally {
    loading.value = false;
  }
};

const updateQuantity = async (item, change) => {
  const newQty = item.quantity + change;
  if (newQty < 1) return;

  isUpdating.value = true;
  try {
    await cartService.updateQuantity(item.id, newQty);
    await fetchCart();
    // Nếu đang áp mã, cần tính lại hoặc reset mã (ở đây chọn reset để user nhập lại cho chắc chắn)
    if (discountApplied.value) removeCoupon();
  } catch (err) {
    alert(err.response?.data?.message || "Không thể cập nhật số lượng.");
  } finally {
    isUpdating.value = false;
  }
};

const handleRemove = async (itemId) => {
  if (!confirm("Bạn có muốn xóa sản phẩm này khỏi giỏ?")) return;
  isUpdating.value = true;
  try {
    await cartService.removeFromCart(itemId);
    await fetchCart();
    if (discountApplied.value) removeCoupon();
  } catch (err) {
    alert("Lỗi khi xóa sản phẩm.");
  } finally {
    isUpdating.value = false;
  }
};

const handleCheckout = () => {
  if (!cart.value || cart.value.items.length === 0) {
    alert("Giỏ hàng trống! Vui lòng thêm sản phẩm.");
    return;
  }
  
  if (!authStore.isAuthenticated) {
    alert("Vui lòng đăng nhập để thanh toán.");
    router.push('/login');
    return;
  }

  if (discountApplied.value && couponCode.value) {
    router.push({ path: '/checkout', query: { coupon: couponCode.value } });
  } else {
    router.push('/checkout');
  }
};

const formatPrice = (val) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
};

onMounted(() => {
  fetchCart();
});
</script>

<style scoped>
.cart-page {
  padding-top: 120px;
  padding-bottom: 80px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: "Inter", system-ui, sans-serif;
  animation: fadeIn 0.4s ease;
}

/* FLAG */
.desktop-only { display: block; }
.mobile-only  { display: none; }

/* HEADER */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  margin-bottom: 30px;
  border-bottom: 2px solid #111827;
  gap: 12px;
}

.page-header h1 {
  font-family: "Oswald", sans-serif;
  font-size: 2.4rem;
  font-weight: 900;
  letter-spacing: 1px;
}

.continue-shopping {
  text-decoration: none;
  color: #4b5563;
  font-size: 0.9rem;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: 0.2s;
}
.continue-shopping:hover {
  color: #111827;
  transform: translateX(-3px);
}

/* EMPTY */
.empty-cart {
  text-align: center;
  padding: 60px 20px;
  max-width: 650px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(15,23,42,0.08);
}
.empty-icon {
  font-size: 3.5rem;
  color: #d1d5db;
  margin-bottom: 15px;
}
.go-shop-btn {
  background: #111827;
  color: #fff;
  padding: 13px 30px;
  display: inline-block;
  margin-top: 20px;
  border-radius: 999px;
  text-decoration: none;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: 0.2s;
}
.go-shop-btn:hover {
  background: #000;
  transform: translateY(-2px);
}

/* LAYOUT */
.cart-layout {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

/* ITEMS WRAPPER */
.cart-items {
  flex: 2.2;
}

/* FILTERS */
.cart-filters {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.filter-input {
  position: relative;
  flex: 1;
  min-width: 200px;
}
.filter-input i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  font-size: 0.9rem;
}
.filter-input input {
  width: 100%;
  padding: 10px 10px 10px 36px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  outline: none;
  font-size: 0.9rem;
  transition: 0.2s;
}
.filter-input input:focus {
  border-color: #111827;
  box-shadow: 0 0 0 3px rgba(17, 24, 39, 0.1);
}
.filter-empty {
  text-align: center;
  padding: 30px;
  color: #6b7280;
  font-style: italic;
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 20px;
}

/* HEADER ROW (DESKTOP) */
.cart-header-row {
  background: #f9fafb;
  padding: 12px 18px;
  border-radius: 12px;
  display: grid;
  grid-template-columns: 4fr 2fr 2fr 2fr;
  font-size: 0.8rem;
  font-weight: 800;
  letter-spacing: 1px;
  color: #6b7280;
  text-transform: uppercase;
  margin-bottom: 10px;
}

/* EACH ITEM CARD */
.cart-item {
  margin-top: 14px;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(15,23,42,0.06);
  padding: 16px 18px;
  display: grid;
  grid-template-columns: 4fr 2fr 2fr 2fr;
  align-items: center;
  gap: 12px;
  transition: 0.2s;
}
.cart-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(15,23,42,0.10);
}

/* ITEM INFO */
.item-info {
  display: flex;
  gap: 14px;
  align-items: center;
}
.item-img {
  width: 80px;
  height: 100px;
  border-radius: 10px;
  object-fit: cover;
  background: #f3f4f6;
}
.item-details h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
}
.item-details h3 a {
  color: #111827;
  text-decoration: none;
  transition: 0.15s;
}
.item-details h3 a:hover {
  color: #dc2626;
}
.variant-text {
  color: #6b7280;
  font-size: 0.85rem;
  margin-top: 3px;
}
.item-unit-price {
  margin-top: 6px;
  font-weight: 700;
}
.remove-btn {
  margin-top: 6px;
  font-size: 0.8rem;
  border: none;
  background: none;
  color: #dc2626;
  font-weight: 700;
  cursor: pointer;
  text-decoration: underline;
  transition: 0.15s;
}
.remove-btn:hover {
  color: #b91c1c;
}

/* PRICE / TOTAL / QTY */
.item-price,
.item-total {
  font-weight: 700;
  font-size: 0.95rem;
  text-align: center;
}
.item-total {
  color: #dc2626;
}

.item-quantity {
  text-align: center;
}
.qty-box {
  display: inline-flex;
  border-radius: 999px;
  overflow: hidden;
  border: 1px solid #111827;
}
.qty-box button {
  width: 32px;
  border: none;
  background: #111827;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  transition: 0.15s;
}
.qty-box button:hover:not(:disabled) {
  background: #000;
}
.qty-box button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}
.qty-box input {
  width: 42px;
  border: none;
  text-align: center;
  font-size: 0.95rem;
}

/* ====== SUMMARY (TÓM TẮT ĐƠN HÀNG) ====== */

.cart-summary-wrapper {
  flex: 1;
  /* cho nó không dính sát cạnh quá */
  min-width: 280px;
}

/* CARD TÓM TẮT */
.cart-summary {
  background: radial-gradient(circle at top left, white 0, #020617 45%, white 100%);
  color: #f9fafb;
  padding: 26px 22px 24px;
  border-radius: 22px;
  box-shadow: 0 18px 40px rgba(15,23,42,0.45);
  position: sticky;
  top: 110px;
  overflow: hidden;
}

/* viền mờ bên trong cho sang */
.cart-summary::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  border: 1px solid rgba(248,250,252,0.08);
  pointer-events: none;
}

/* COUPON STYLES */
.coupon-section {
  margin: 15px 0;
  padding: 15px 0;
  border-top: 1px dashed rgba(148,163,184,0.4);
  border-bottom: 1px dashed rgba(148,163,184,0.4);
}
.input-group {
  display: flex;
  gap: 8px;
}
.input-group input {
  flex: 1;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #4b5563;
  background: rgba(255,255,255,0.1);
  color: #fff;
  font-size: 0.9rem;
}
.input-group button {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  background: #facc15;
  color: #000;
  font-weight: 700;
  cursor: pointer;
  font-size: 0.85rem;
}
.input-group button:disabled {
  background: #6b7280;
  cursor: not-allowed;
}
.coupon-msg { font-size: 0.85rem; margin-top: 8px; margin-bottom: 0; }
.text-success { color: #4ade80 !important; }
.text-danger { color: #f87171 !important; }
.discount-row { margin-top: 10px; font-weight: 700; }
.btn-remove-coupon { background: none; border: none; color: #9ca3af; font-size: 0.8rem; text-decoration: underline; cursor: pointer; margin-top: 5px; }
.btn-remove-coupon:hover { color: #fff; }

/* header */
.cart-summary h2 {
  font-family: "Oswald", sans-serif;
  font-size: 1.7rem;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px dashed rgba(148,163,184,0.6);
  letter-spacing: 1px;
}

/* mỗi dòng thông tin */
.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 0.95rem;
  color: #e5e7eb;
}

.summary-row span:first-child {
  color: #9ca3af;
}

/* dòng tổng cộng */
.summary-row.total {
  margin-top: 16px;
  font-size: 1.4rem;
  font-weight: 900;
  color: #f9fafb;
}

.summary-row.total span:first-child {
  letter-spacing: 1px;
}

/* nút thanh toán */
.checkout-btn {
  width: 100%;
  margin-top: 22px;
  border: none;
  border-radius: 999px;
  padding: 14px;
  background: linear-gradient(90deg, #f97316, #facc15);
  color: white;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 1px;
  cursor: pointer;
  box-shadow: 0 12px 30px rgba(0,0,0,0.45);
  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
}

.checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 40px rgba(0,0,0,0.6);
  filter: brightness(1.03);
}

.checkout-btn:active {
  transform: translateY(0);
  box-shadow: 0 10px 24px rgba(0,0,0,0.45);
}

/* ====== RESPONSIVE SUMMARY ====== */

@media (max-width: 992px) {
  .cart-summary {
    position: static;
  }
}

@media (max-width: 768px) {
  .cart-summary-wrapper {
    width: 100%;
  }

  .cart-summary {
    margin-bottom: 16px;
    border-radius: 18px;
  }
}

/* LOADING */
.loading-state {
  text-align: center;
  font-size: 1.05rem;
  color: #6b7280;
  padding: 60px 0;
}

/* ANIM */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; }
}

/* ====================== */
/*      RESPONSIVE        */
/* ====================== */

@media (max-width: 992px) {
  .cart-page {
    padding-top: 100px;
    padding-inline: 16px;
  }
  .cart-layout {
    flex-direction: column;
  }
  .cart-summary {
    position: static;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .page-header h1 {
    font-size: 2rem;
  }

  .desktop-only { display: none; }
  .mobile-only  { display: block; }

  .cart-items {
    width: 100%;
  }

  .cart-item {
    grid-template-columns: 1fr;
    padding: 14px 12px;
  }

  .item-info {
    align-items: flex-start;
  }

  .item-img {
    width: 74px;
    height: 92px;
  }

  /* tổng tiền xuống dưới, dễ đọc */
  .item-total {
    text-align: right;
    margin-top: 6px;
  }

  .item-quantity {
    margin-top: 10px;
    text-align: left;
  }
  .qty-box {
    margin-top: 4px;
  }

  .cart-summary {
    padding: 20px 16px;
    margin-top: 8px;
  }
}

@media (max-width: 480px) {
  .cart-page {
    padding-top: 90px;
    padding-bottom: 60px;
  }
  .cart-item {
    padding: 12px 10px;
  }
  .item-details h3 {
    font-size: 0.95rem;
  }
  .variant-text {
    font-size: 0.8rem;
  }
}
</style>
