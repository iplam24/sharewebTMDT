<template>
  <div class="checkout-page container">
    <h1 class="page-title">THANH TOÁN</h1>

    <div class="row">
      <div class="col-left">
        
        <div class="section-box">
          <div class="section-header">
            <i class="fa-solid fa-location-dot"></i> ĐỊA CHỈ NHẬN HÀNG
          </div>
          
          <div class="section-body">
            <div v-if="addresses.length > 0" class="address-list">
              <label 
                v-for="addr in addresses" 
                :key="addr.id" 
                class="address-item" 
                :class="{ 'selected': selectedAddressId === addr.id }"
              >
                <input 
                  type="radio" 
                  name="address" 
                  :value="addr.id"
                  v-model="selectedAddressId"
                >
                <div class="address-info">
                  <p class="addr-text"><strong>{{ addr.street }}</strong></p>
                  <p class="addr-sub">{{ addr.state }}, {{ addr.city }}, {{ addr.country }}</p>
                  <p class="addr-phone"><i class="fa-solid fa-phone"></i> {{ addr.phoneNumber }}</p>
                </div>
              </label>
            </div>
            <p v-else class="empty-text">Bạn chưa có địa chỉ nhận hàng.</p>

            <button class="btn-add-new" @click="showAddForm = !showAddForm">
              <i class="fa-solid" :class="showAddForm ? 'fa-minus' : 'fa-plus'"></i> 
              {{ showAddForm ? 'Đóng biểu mẫu' : 'Thêm địa chỉ mới' }}
            </button>

            <div v-if="showAddForm" class="add-form">
              <form @submit.prevent="handleAddAddress">
                <div class="form-group">
                  <input v-model="newAddress.street" type="text" placeholder="Số nhà, tên đường" required>
                </div>
                <div class="form-row">
                  <input v-model="newAddress.city" type="text" placeholder="Tỉnh / Thành phố" required>
                  <input v-model="newAddress.state" type="text" placeholder="Quận / Huyện" required>
                </div>
                <div class="form-group">
                  <input v-model="newAddress.phoneNumber" type="text" placeholder="Số điện thoại" required>
                </div>
                <button type="submit" class="btn-save">LƯU ĐỊA CHỈ</button>
              </form>
            </div>
          </div>
        </div>

        <div class="section-box">
          <div class="section-header">
            <i class="fa-regular fa-credit-card"></i> PHƯƠNG THỨC THANH TOÁN
          </div>
          <div class="section-body">
            
            <label class="payment-option" :class="{ selected: paymentMethod === 'COD' }">
              <input class="form-check-input" type="radio" name="payment" value="COD" v-model="paymentMethod">
              <span>Thanh toán khi nhận hàng (COD)</span>
              <i class="fa-solid fa-money-bill-1 ms-auto text-success"></i>
            </label>

            <label class="payment-option" :class="{ selected: paymentMethod === 'VNPAY' }">
              <input class="form-check-input" type="radio" name="payment" value="VNPAY" v-model="paymentMethod">
              <span>Thanh toán qua VNPay</span>
              <img src="https://vnpay.vn/assets/images/logo-icon/logo-primary.svg" alt="VNPay" style="height: 20px; margin-left: auto;">
            </label>

          </div>
        </div>
      </div>

      <div class="col-right">
        <div class="order-summary">
          <h3>ĐƠN HÀNG CỦA BẠN</h3>
          
          <div v-if="loadingCart" class="loading">Đang tải...</div>
          
          <div v-else>
            <div class="order-items">
              <div v-for="item in cart?.items" :key="item.id" class="order-item">
                <div class="item-img">
                  <img :src="item.imageUrl" alt="Product">
                  <span class="item-qty">{{ item.quantity }}</span>
                </div>
                <div class="item-desc">
                  <p class="name">{{ item.productName }}</p>
                  <p class="variant">{{ item.color }} / {{ item.size }}</p>
                </div>
                <div class="item-price">{{ formatPrice(item.price * item.quantity) }}</div>
              </div>
            </div>

            <div class="order-total">
              <div class="row">
                <span>Tạm tính</span>
                <span>{{ formatPrice(cart?.totalPrice || 0) }}</span>
              </div>
              <div class="row">
                <span>Phí vận chuyển</span>
                <span>Miễn phí</span>
              </div>

              <!-- Hiển thị giảm giá -->
              <div v-if="discountApplied" class="row" style="color: #10b981;">
                <span>Giảm giá ({{ couponCode }})</span>
                <span>-{{ formatPrice(discountAmount) }}</span>
              </div>

              <div class="divider"></div>
              <div class="row total">
                <span>TỔNG CỘNG</span>
                <span class="total-price">{{ formatPrice(finalTotalPrice) }}</span>
              </div>
            </div>

            <!-- Input mã giảm giá -->
            <div class="mt-3 mb-3">
               <div class="form-row" style="margin-bottom: 5px;">
                  <input type="text" placeholder="Nhập mã giảm giá" v-model="couponCodeInput" style="flex: 1;">
                  <button class="btn-save" type="button" @click="applyCouponManual" style="width: auto; margin-top: 0; padding: 0 20px;">Áp dụng</button>
               </div>
               <small v-if="couponMessage" :class="discountApplied ? 'text-success' : 'text-danger'">{{ couponMessage }}</small>
            </div>

            <button 
              @click="handlePlaceOrder" 
              class="btn-checkout"
              :disabled="submitting"
            >
              {{ submitting ? 'ĐANG XỬ LÝ...' : 'ĐẶT HÀNG NGAY' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import cartService from '@/services/cartService.js';
import addressService from '@/services/addressService.js';
import orderService from '@/services/orderService.js';
import vnpayService from '@/services/vnpayService.js';
import discountService from '@/services/discountService.js';

const router = useRouter();
const route = useRoute();

const cart = ref(null);
const addresses = ref([]);
const selectedAddressId = ref(null);
const loadingCart = ref(true);
const submitting = ref(false);

const paymentMethod = ref('COD'); // mặc định COD

// Coupon logic
const couponCode = ref('');
const couponCodeInput = ref('');
const discountAmount = ref(0);
const discountApplied = ref(false);
const couponMessage = ref('');

// Form thêm địa chỉ
const showAddForm = ref(false);
const newAddress = ref({
  street: '',
  city: '',
  state: '',
  phoneNumber: '',
  country: 'Việt Nam'
});

const finalTotalPrice = computed(() => {
  if (!cart.value) return 0;
  const total = cart.value.totalPrice - discountAmount.value;
  return total > 0 ? total : 0;
});

// ================== LOAD DATA ==================
const loadData = async () => {
  try {
    const [cartRes, addrRes] = await Promise.all([
      cartService.getCart(),
      addressService.getMyAddresses()
    ]);

    cart.value = cartRes.data;
    addresses.value = addrRes.data;

    if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id;
    }

    // Tự động áp dụng mã nếu được truyền từ Cart
    if (route.query.coupon) {
      couponCodeInput.value = route.query.coupon;
      await applyCouponManual();
    }

  } catch (err) {
    console.error('Lỗi khi tải dữ liệu Checkout:', err);
    alert('Không thể tải dữ liệu thanh toán. Vui lòng thử lại sau.');
    router.push('/cart');
  } finally {
    loadingCart.value = false;
  }
};

// ================== XỬ LÝ MÃ GIẢM GIÁ ==================
const applyCouponManual = async () => {
  if (!couponCodeInput.value.trim()) return;
  couponMessage.value = '';
  
  try {
    const res = await discountService.getDiscountByCode(couponCodeInput.value.trim());
    const discount = res.data;

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
    couponCode.value = discount.code;
    couponMessage.value = `Đã áp dụng mã ${discount.code}`;

  } catch (err) {
    console.error(err);
    couponMessage.value = err.response?.data?.message || err.message || 'Mã giảm giá không hợp lệ.';
    discountApplied.value = false;
    discountAmount.value = 0;
    couponCode.value = '';
  }
};

// ================== THÊM ĐỊA CHỈ ==================
const handleAddAddress = async () => {
  if (!newAddress.value.phoneNumber) {
    alert('Vui lòng nhập số điện thoại');
    return;
  }
  if (!newAddress.value.street || !newAddress.value.city || !newAddress.value.state) {
    alert('Vui lòng điền đầy đủ thông tin địa chỉ.');
    return;
  }

  try {
    await addressService.addAddress(newAddress.value);
    newAddress.value = {
      street: '',
      city: '',
      state: '',
      phoneNumber: '',
      country: 'Việt Nam'
    };
    showAddForm.value = false;

    const res = await addressService.getMyAddresses();
    addresses.value = res.data;
    if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[addresses.value.length - 1].id;
    }
    alert('Địa chỉ đã được thêm thành công!');
  } catch (err) {
    alert('Lỗi thêm địa chỉ: ' + (err.response?.data?.message || err.message));
  }
};

// ================== ĐẶT HÀNG ==================
const handlePlaceOrder = async () => {
  if (!selectedAddressId.value) {
    alert('Vui lòng chọn địa chỉ giao hàng!');
    return;
  }
  if (!cart.value || !cart.value.items || cart.value.items.length === 0) {
    alert('Giỏ hàng của bạn đang trống!');
    return;
  }

  if (!confirm('Bạn có chắc chắn muốn đặt hàng không?')) {
    return;
  }

  submitting.value = true;

  try {
    // 1. Tạo đơn hàng để lấy orderId
    const createOrderPayload = {
      addressId: selectedAddressId.value,
      paymentMethod: paymentMethod.value,
      discountCode: discountApplied.value ? couponCode.value : null,
      discountAmount: discountAmount.value || 0,
      totalPrice: finalTotalPrice.value
    };

    const orderRes = await orderService.createOrder(createOrderPayload);
    console.log('Create order response:', orderRes.data);

    const body = orderRes.data;

    // 👉 LẤY orderId THEO NHIỀU KIỂU (tùy backend trả về)
    const orderId =
      body.orderId ??
      body.id ??
      body.data?.orderId ??
      body.data?.id;

    if (!orderId) {
      console.error('Không tìm được orderId từ response createOrder:', body);
      alert('Tạo đơn hàng thất bại: không lấy được mã đơn hàng từ server.');
      return;
    }

    // 2. Nếu chọn VNPAY -> gọi API tạo link thanh toán
    if (paymentMethod.value === 'VNPAY') {
      const vnpayRequestPayload = {
        orderId: orderId,
        amount: finalTotalPrice.value, // Sử dụng giá sau giảm
        orderInfo: `thanh toan don hang ${orderId}`
      };

      const vnpayRes = await vnpayService.createPayment(vnpayRequestPayload);
      console.log('VNPay createPayment response:', vnpayRes.data);

      const paymentUrl =
        vnpayRes.data?.paymentUrl ||
        vnpayRes.data?.data?.paymentUrl;

      if (paymentUrl) {
        // redirect sang trang VNPay
        window.location.href = paymentUrl;
      } else {
        alert('Không nhận được URL thanh toán từ VNPay. Vui lòng thử lại.');
      }
    } else {
      // COD
      alert('Đặt hàng thành công! Đơn hàng sẽ được xử lý.');
      router.push('/'); // hoặc /orders-history
    }
  } catch (err) {
    console.error('Lỗi khi đặt hàng:', err);

    if (err.response && err.response.data) {
      console.error('Chi tiết lỗi từ Backend:', err.response.data);

      const resData = err.response.data;

      // Xử lý lỗi cụ thể: Mã giảm giá đã được sử dụng hoặc không hợp lệ từ server
      if (resData.message && resData.message.toLowerCase().includes('mã giảm giá')) {
        couponMessage.value = resData.message;
        discountApplied.value = false; // Hủy trạng thái đã áp dụng
        discountAmount.value = 0;      // Reset số tiền giảm
      }

      if (resData.message) {
        alert('Đặt hàng thất bại: ' + resData.message);
      } else if (resData.errors) {
        const errorMessages = Object.values(resData.errors)
          .flat()
          .join('\n');
        alert('Đặt hàng thất bại:\n' + errorMessages);
      } else {
        alert('Đặt hàng thất bại: ' + JSON.stringify(resData, null, 2));
      }
    } else {
      alert('Đặt hàng thất bại: ' + (err.message || 'Đã có lỗi xảy ra.'));
    }
  } finally {
    submitting.value = false;
  }
};

const formatPrice = (val) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

onMounted(() => loadData());
</script>


<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oswald:wght@400;700&family=Inter:wght@300;400;500;600;700&display=swap');

.checkout-page {
  padding: 110px 20px 80px;
  font-family: "Inter", system-ui, sans-serif;
  max-width: 1100px;
  margin: 0 auto;
}

/* TITLE */
.page-title {
  font-family: "Oswald", sans-serif;
  text-align: center;
  font-size: 2.4rem;
  letter-spacing: .18em;
  text-transform: uppercase;
  margin-bottom: 40px;
}

/* 2 CỘT */
.row {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.col-left  { flex: 1.7; }
.col-right { flex: 1.1; }

/* ====== SECTION BOX (Địa chỉ + Payment) ====== */
.section-box {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  margin-bottom: 28px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.section-header {
  background: #f9fafb;
  padding: 14px 20px;
  font-weight: 700;
  font-size: .9rem;
  display: flex;
  align-items: center;
  gap: 10px;
  text-transform: uppercase;
  letter-spacing: .14em;
  color: #111827;
  border-bottom: 1px solid #e5e7eb;
}

.section-body {
  padding: 20px;
}

/* ====== ADDRESS LIST ====== */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}

.address-item {
  display: flex;
  gap: 14px;
  padding: 14px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  cursor: pointer;
  transition: border-color .2s, box-shadow .2s, background .2s, transform .1s;
}

.address-item:hover {
  border-color: #111827;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
  transform: translateY(-1px);
}

.address-item.selected {
  border-color: #111827;
  background: #f3f4ff;
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.12);
}

.address-item input {
  margin-top: 4px;
}

.address-info p {
  margin: 0 0 4px;
  font-size: .9rem;
  color: #4b5563;
}

.address-info strong {
  font-size: .98rem;
  color: #111827;
}

.addr-phone {
  color: #2563eb;
  font-weight: 600;
}

/* Text khi chưa có địa chỉ */
.empty-text {
  text-align: center;
  font-size: .9rem;
  color: #6b7280;
  margin-bottom: 15px;
}

/* ====== BTN THÊM ĐỊA CHỈ ====== */
.btn-add-new {
  width: 100%;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px dashed #9ca3af;
  background: #fff;
  font-size: .85rem;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: #374151;
  transition: background .2s, border-color .2s, color .2s;
  margin-top: 5px;
}

.btn-add-new:hover {
  border-color: #111827;
  color: #111827;
  background: #f3f4f6;
}

/* ====== FORM THÊM ĐỊA CHỈ ====== */
.add-form {
  margin-top: 18px;
  padding: 18px 16px 16px;
  border-radius: 10px;
  background: #f9fafb;
  border: 1px dashed #e5e7eb;
}

.form-group {
  margin-bottom: 10px;
}

.form-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.add-form input {
  width: 100%;
  padding: 9px 11px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  font-size: .9rem;
  outline: none;
  background: #ffffff;
  transition: border-color .2s, box-shadow .2s;
}

.add-form input:focus {
  border-color: #111827;
  box-shadow: 0 0 0 1px #11182715;
}

.btn-save {
  width: 100%;
  margin-top: 4px;
  border-radius: 999px;
  border: none;
  padding: 11px;
  background: #111827;
  color: #fff;
  font-weight: 700;
  letter-spacing: .15em;
  font-size: .75rem;
  text-transform: uppercase;
  cursor: pointer;
  transition: background .2s, box-shadow .2s, transform .08s;
}

.btn-save:hover {
  background: #020617;
  box-shadow: 0 8px 20px rgba(15,23,42,.35);
  transform: translateY(-1px);
}

/* ====== PAYMENT OPTIONS ====== */
.payment-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  margin-bottom: 10px;
  background: #ffffff;
  cursor: pointer;
  transition: border-color .2s, background .2s, box-shadow .2s, transform .08s;
}

.payment-option span {
  font-size: .9rem;
  font-weight: 500;
  color: #111827;
}

.payment-option:hover {
  border-color: #111827;
  background: #f9fafb;
  transform: translateY(-1px);
}

.payment-option.selected {
  border-color: #111827;
  background: #eef2ff;
  box-shadow: 0 8px 18px rgba(15,23,42,.12);
}

.form-check-input:checked {
  accent-color: #111827;
}

/* ====== ORDER SUMMARY (CỘT PHẢI) ====== */
.order-summary {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 22px 22px 24px;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.14);
  position: sticky;
  top: 100px;
}

.order-summary h3 {
  font-family: "Oswald", sans-serif;
  font-size: 1.25rem;
  letter-spacing: .18em;
  text-transform: uppercase;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 12px;
  margin-bottom: 16px;
}

/* Item trong tóm tắt đơn */
.order-items {
  max-height: 260px;
  overflow-y: auto;
  padding-right: 4px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.item-img {
  position: relative;
  width: 52px;
  height: 64px;
  flex-shrink: 0;
}

.item-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.item-qty {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #4b5563;
  color: #fff;
  font-size: .7rem;
  padding: 2px 6px;
  border-radius: 999px;
}

.item-desc {
  flex: 1;
  min-width: 0;
}

.item-desc .name {
  font-size: .9rem;
  font-weight: 600;
  margin: 0 0 3px;
}

.item-desc .variant {
  font-size: .78rem;
  color: #6b7280;
  margin: 0;
}

.item-price {
  font-size: .9rem;
  font-weight: 600;
  white-space: nowrap;
}

/* ====== TỔNG TIỀN ====== */
.order-total {
  margin-top: 18px;
  border-top: 1px solid #e5e7eb;
  padding-top: 14px;
}

.order-total .row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: .9rem;
  color: #4b5563;
}

.order-total .row.total {
  margin-top: 8px;
  padding-top: 10px;
  border-top: 1px dashed #d1d5db;
  font-size: 1.05rem;
  font-weight: 700;
}

.total-price {
  color: #d0021b;
  font-weight: 800;
  font-size: 1.2rem;
}

/* ====== NÚT ĐẶT HÀNG ====== */
.btn-checkout {
  margin-top: 18px;
  width: 100%;
  border-radius: 999px;
  border: none;
  padding: 14px;
  background: #111827;
  color: #fff;
  font-weight: 700;
  letter-spacing: .18em;
  font-size: .78rem;
  text-transform: uppercase;
  cursor: pointer;
  transition: background .2s, box-shadow .2s, transform .08s;
}

.btn-checkout:hover:not(:disabled) {
  background: #020617;
  box-shadow: 0 12px 26px rgba(15,23,42,.4);
  transform: translateY(-1px);
}

.btn-checkout:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  box-shadow: none;
}

/* Loading text */
.loading {
  font-size: .9rem;
  color: #4b5563;
}

/* ====== RESPONSIVE ====== */
@media (max-width: 900px) {
  .row {
    flex-direction: column;
  }
  .order-summary {
    position: static;
    margin-top: 8px;
  }
}

@media (max-width: 600px) {
  .checkout-page {
    padding: 90px 14px 60px;
  }
  .page-title {
    font-size: 2rem;
    letter-spacing: .12em;
  }
}
</style>