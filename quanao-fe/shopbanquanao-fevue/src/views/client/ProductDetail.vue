<template>
  <div class="product-detail-page container" v-if="product">
    
    <div class="gallery-section">
    <div class="main-image">
  <button class="nav-btn prev" @click="prevImage">
    <i class="fa-solid fa-chevron-left"></i>
  </button>

  <img :src="currentImage" alt="Main Product" />

  <button class="nav-btn next" @click="nextImage">
    <i class="fa-solid fa-chevron-right"></i>
  </button>
</div>

      <div class="thumbnails">
        <img 
          v-for="(img, index) in product.imageUrls" 
          :key="index" 
          :src="img" 
          @click="currentImage = img"
          :class="{ active: currentImage === img }"
        />
      </div>
    </div>

    <div class="info-section">
      <p class="category-tag">{{ product.category }}</p>
      <h1 class="product-title">{{ product.name }}</h1>
      <p class="product-price">{{ formatPrice(product.price) }}</p>

      <!-- SAO TRUNG BÌNH + SỐ LƯỢNG -->
      <div class="rating-summary" v-if="reviews.length">
        <div class="avg-rating">
          <span class="avg-score">{{ averageRating }}</span>
          <div class="stars">
            <i v-for="n in 5" :key="n" 
               :class="n <= Math.round(averageRating) ? 'fa-solid active' : 'fa-regular'"></i>
          </div>
          <span class="review-count">({{ reviews.length }} đánh giá)</span>
        </div>
      </div>

      <div class="divider"></div>

      <p class="desc">{{ product.description }}</p>

      <!-- BIẾN THỂ -->
      <div class="variant-selector">
        <label>MÀU SẮC:</label>
        <div class="options">
          <button 
            v-for="color in uniqueColors" 
            :key="color"
            :class="{ selected: selectedColor === color }"
            @click="selectColor(color)"
          >
            {{ color }}
          </button>
        </div>
      </div>

      <div class="variant-selector" v-if="selectedColor">
        <label>KÍCH THƯỚC:</label>
        <div class="options">
          <button 
            v-for="size in availableSizes" 
            :key="size"
            :class="{ selected: selectedSize === size }"
            @click="selectedSize = size"
          >
            {{ size }}
          </button>
        </div>
      </div>

      <div v-if="selectedVariant" class="stock-info">
        <span v-if="selectedVariant.stockQuantity > 0" class="in-stock">
          Còn {{ selectedVariant.stockQuantity }} sản phẩm
        </span>
        <span v-else class="out-stock">
          Hết hàng
        </span>
      </div>

      <div class="actions">
        <div class="quantity-box">
          <button @click="quantity > 1 ? quantity-- : null">-</button>
          <input type="number" v-model="quantity" readonly />
          <button @click="quantity++">+</button>
        </div>
        
        <button 
          class="add-to-cart-btn" 
          :disabled="!canAddToCart"
          @click="handleAddToCart"
        >
          {{ adding ? 'ĐANG THÊM...' : 'THÊM VÀO GIỎ' }}
        </button>
      </div>

      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <!-- DANH SÁCH ĐÁNH GIÁ -->
      <div class="reviews-section" v-if="reviews.length">
        <h3>Đánh giá từ khách hàng</h3>
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-header">
            <strong>{{ review.username || 'Khách hàng' }}</strong>
            <div class="review-stars">
              <i v-for="n in 5" :key="n"
                 :class="n <= review.rating ? 'fa-solid active' : 'fa-regular'"></i>
              <span class="rating-text">{{ review.rating }}/5</span>
            </div>
          </div>
          <p class="review-comment">{{ review.comment }}</p>
          <div v-if="review.imageUrls?.length" class="review-images">
            <img v-for="url in review.imageUrls" :key="url" :src="url" @click="openImage(url)" />
          </div>
          <small class="review-date">{{ formatDate(review.createdAt) }}</small>
        </div>
      </div>

      <!-- Không có đánh giá -->
      <div v-else class="no-reviews">
        <p>Chưa có đánh giá nào.</p>
      </div>
    </div>
  </div>

  <!-- Hiển thị lỗi nếu không tải được sản phẩm -->
  <div v-else-if="errorMsg" class="loading" style="color: #dc2626;">
    <i class="fa-solid fa-circle-exclamation"></i> {{ errorMsg }}
  </div>

  <div v-else class="loading">
    <i class="fa-solid fa-spinner fa-spin"></i> Đang tải sản phẩm...
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productService from '@/services/productService.js';
import reviewService from '@/services/reviewService.js';
import cartService from '@/services/cartService.js';
import { useAuthStore } from '@/stores/authStore.js';
import { useCartStore } from '@/stores/cartStore.js';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const cartStore = useCartStore();

const product = ref(null);
const reviews = ref([]);
const currentImage = ref('');
const selectedColor = ref(null);
const selectedSize = ref(null);
const quantity = ref(1);
const adding = ref(false);
const errorMsg = ref('');

// === TÍNH TOÁN ===
const uniqueColors = computed(() => {
  if (!product.value?.variants) return [];
  return [...new Set(product.value.variants.map(v => v.color))];
});
const currentIndex = computed(() => {
  return product.value?.imageUrls.indexOf(currentImage.value) || 0;
});

const prevImage = () => {
  if (!product.value?.imageUrls.length) return;
  let newIndex = currentIndex.value - 1;
  if (newIndex < 0) newIndex = product.value.imageUrls.length - 1;
  currentImage.value = product.value.imageUrls[newIndex];
};

const nextImage = () => {
  if (!product.value?.imageUrls.length) return;
  let newIndex = currentIndex.value + 1;
  if (newIndex >= product.value.imageUrls.length) newIndex = 0;
  currentImage.value = product.value.imageUrls[newIndex];
};

const availableSizes = computed(() => {
  if (!selectedColor.value || !product.value?.variants) return [];
  return product.value.variants
    .filter(v => v.color === selectedColor.value)
    .map(v => v.size);
});

const selectedVariant = computed(() => {
  if (!selectedColor.value || !selectedSize.value) return null;
  return product.value.variants.find(
    v => v.color === selectedColor.value && v.size === selectedSize.value
  );
});

const canAddToCart = computed(() => {
  return selectedVariant.value && selectedVariant.value.stockQuantity > 0;
});

// TÍNH SAO TRUNG BÌNH
const averageRating = computed(() => {
  if (!reviews.value.length) return 0;
  const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0);
  return (sum / reviews.value.length).toFixed(1);
});

// === HÀM ===
const selectColor = (color) => {
  selectedColor.value = color;
  selectedSize.value = null;
};

const fetchProduct = async () => {
  try {
    const [prodRes, revRes] = await Promise.all([
      productService.getProductById(route.params.id),
      reviewService.getByProduct(route.params.id)
    ]);
    product.value = prodRes.data;
    reviews.value = revRes.data || [];

    if (product.value.imageUrls?.length) {
      currentImage.value = product.value.imageUrls[0];
    }
  } catch (err) {
    console.error(err);
    if (err.response && (err.response.status === 401 || err.response.status === 403)) {
      errorMsg.value = 'Vui lòng đăng nhập để xem chi tiết sản phẩm.';
    } else {
      errorMsg.value = err.response?.data?.message || 'Không thể tải thông tin sản phẩm.';
    }
  }
};

const handleAddToCart = async () => {
  if (!authStore.isAuthenticated) {
    alert("Vui lòng đăng nhập!");
    router.push('/login');
    return;
  }

  adding.value = true;
  try {
    await cartService.addToCart({
      variantId: selectedVariant.value.id,
      quantity: quantity.value
    });
    await cartStore.fetchCartCount();
    alert("Đã thêm vào giỏ!");
  } catch (err) {
    errorMsg.value = err.response?.data?.message || "Lỗi khi thêm vào giỏ.";
  } finally {
    adding.value = false;
  }
};

const openImage = (url) => {
  window.open(url, '_blank');
};

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
const formatDate = (date) => new Date(date).toLocaleDateString('vi-VN', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric'
});

onMounted(() => fetchProduct());
</script>
<style scoped>
/* === RATING SUMMARY === */
.rating-summary {
  margin: 16px 0 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1rem;
}

.avg-score {
  font-size: 2.4rem;
  font-weight: 800;
  color: #f59e0b;
  line-height: 1;
}

.review-count {
  font-size: 0.9rem;
  color: #666;
  margin-left: 4px;
}

/* === REVIEW SECTION === */
.reviews-section {
  margin-top: 50px;
  border-top: 1px solid #eee;
  padding-top: 30px;
}

.reviews-section h3 {
  font-size: 1.4rem;
  margin-bottom: 20px;
  font-family: "Oswald", sans-serif;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.review-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 18px 0;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.review-stars {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.9rem;
}

.review-stars .fa-star {
  font-size: 1rem;
  color: #ddd;
}

.review-stars .fa-star.active {
  color: #f59e0b;
}

.rating-text {
  margin-left: 4px;
  font-weight: 600;
  color: #444;
}

.review-comment {
  margin: 10px 0;
  color: #444;
  line-height: 1.6;
  font-size: 0.95rem;
}

.review-images {
  display: flex;
  gap: 8px;
  margin: 12px 0;
  flex-wrap: wrap;
}
.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 46px;
  height: 46px;
  border-radius: 50%;
  border: none;
  background: rgba(0,0,0,0.45);
  color: white;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.25s;
  backdrop-filter: blur(4px);
  z-index: 10;
}

.nav-btn:hover {
  background: rgba(0,0,0,0.7);
}

.nav-btn.prev {
  left: 14px;
}

.nav-btn.next {
  right: 14px;
}

.main-image {
  position: relative;
}

.review-images img {
  width: 70px;
  height: 70px;
  object-fit: cover;
  border-radius: 6px;
  cursor: zoom-in;
  border: 1px solid #eee;
  transition: 0.2s;
}

.review-images img:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.review-date {
  color: #999;
  font-size: 0.8rem;
}

/* === KHÔNG CÓ ĐÁNH GIÁ === */
.no-reviews {
  margin-top: 40px;
  text-align: center;
  color: #999;
  font-style: italic;
}

/* === SAO CHUNG === */
.stars .fa-star {
  color: #ddd;
  font-size: 1.1rem;
}

.stars .fa-star.active {
  color: #f59e0b;
}
/* ========================= */
/*     PAGE LAYOUT           */
/* ========================= */

.product-detail-page {
  padding: 110px 20px 90px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 60px;
  max-width: 1280px;
  margin: 0 auto;
  animation: fadeIn 0.7s ease;
  font-family: "Inter", system-ui, sans-serif;
}

/* ========================= */
/*         GALLERY           */
/* ========================= */

.gallery-section {
  width: 100%;
}

.main-image {
  width: 100%;
  height: 560px;
  background: #f6f6f6;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 35px rgba(0,0,0,0.07);
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  animation: fadeInSoft 0.4s ease;
}

.thumbnails {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  gap: 12px;
}

.thumbnails img {
  width: 100%;
  height: 85px;
  object-fit: cover;
  border-radius: 10px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: 0.25s ease;
  background: #fafafa;
}

.thumbnails img:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}

.thumbnails img.active {
  border-color: #000;
  box-shadow: 0 6px 16px rgba(0,0,0,0.12);
}

/* ========================= */
/*       INFO SECTION        */
/* ========================= */

.info-section {
  display: flex;
  flex-direction: column;
}

.category-tag {
  font-size: 0.75rem;
  letter-spacing: 2px;
  color: #666;
  text-transform: uppercase;
  margin-bottom: 8px;
  font-weight: 600;
}

.product-title {
  font-family: "Oswald", sans-serif;
  font-size: 2.8rem;
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 12px;
  line-height: 1.2;
}

.product-price {
  font-size: 1.9rem;
  color: #d0021b;
  font-weight: 800;
  margin-bottom: 20px;
}

.divider {
  height: 1px;
  width: 100%;
  background: #ddd;
  margin: 30px 0;
}

.desc {
  color: #444;
  font-size: 0.95rem;
  line-height: 1.7;
  margin-bottom: 35px;
}

/* ========================= */
/*        VARIANT BOX        */
/* ========================= */

.variant-selector {
  margin-bottom: 25px;
}

.variant-selector label {
  display: block;
  font-weight: 700;
  margin-bottom: 10px;
  font-size: 0.85rem;
  letter-spacing: 1px;
}

.options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.options button {
  padding: 9px 22px;
  border-radius: 999px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.85rem;
  transition: 0.2s;
}

.options button:hover {
  border-color: #555;
}

.options button.selected {
  background: #000;
  border-color: #000;
  color: #fff;
}

/* ========================= */
/*       STOCK INFO          */
/* ========================= */

.stock-info {
  margin-top: 4px;
  font-size: 0.9rem;
  font-weight: 600;
}

.in-stock {
  color: #1c8c36;
}

.out-stock {
  color: #d63031;
}

/* ========================= */
/*      ACTIONS (BUY)        */
/* ========================= */

.actions {
  display: flex;
  align-items: center;
  gap: 22px;
  margin-top: 35px;
}

.quantity-box {
  display: flex;
  border: 1px solid #ccc;
  border-radius: 6px;
  overflow: hidden;
  height: 50px;
}

.quantity-box button {
  width: 40px;
  font-size: 1.3rem;
  border: none;
  background: #f9f9f9;
  cursor: pointer;
  transition: 0.2s;
}

.quantity-box button:hover {
  background: #eee;
}

.quantity-box input {
  width: 55px;
  font-size: 1rem;
  border: none;
  text-align: center;
  outline: none;
  background: white;
}

.add-to-cart-btn {
  flex: 1;
  height: 50px;
  background: #111;
  color: #fff;
  border: none;
  border-radius: 6px;
  text-transform: uppercase;
  font-size: 0.95rem;
  font-weight: 800;
  cursor: pointer;
  letter-spacing: 1px;
  transition: 0.25s ease;
}

.add-to-cart-btn:hover {
  background: #333;
}

.add-to-cart-btn:disabled {
  background: #bbb;
  cursor: not-allowed;
}

/* ERROR MESSAGE */
.error-msg {
  margin-top: 12px;
  color: #ff4d4f;
  font-weight: 600;
}

/* ========================= */
/*       LOADING TEXT        */
/* ========================= */

.loading {
  padding: 120px;
  font-size: 1.2rem;
  text-align: center;
  letter-spacing: 1px;
  opacity: 0.6;
}

/* ========================= */
/*       ANIMATIONS          */
/* ========================= */

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInSoft {
  from { opacity: 0; }
  to   { opacity: 1; }
}

/* ========================= */
/*      RESPONSIVE MOBILE    */
/* ========================= */

@media (max-width: 1050px) {
  .product-detail-page {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .main-image { height: 480px; }
}

@media (max-width: 768px) {
  .product-detail-page {
    padding: 90px 20px;
  }
  .main-image { height: 380px; }
  .product-title { font-size: 2.2rem; }
}

@media (max-width: 480px) {
  .main-image { height: 310px; }
  .product-title { font-size: 1.9rem; }
  .options button {
    padding: 7px 16px;
  }
  .product-img-box { height: 200px; }
}

</style>