<template>
  <div class="shop-page container">
    
    <section class="shop-header">
      <h1 class="title">{{ categoryTitle }}</h1>
      <p class="subtitle" v-if="totalElements > 0">{{ totalElements }} sản phẩm</p>
    </section>

    <div class="toolbar">
      <div class="category-filter-box">
        <label for="category-select" class="label">
          <i class="fa-solid fa-tags"></i> Danh mục:
        </label>
        <select id="category-select" v-model="selectedCategoryName" @change="handleCategoryFilterChange">
          <option value="">Tất cả danh mục</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.name.toLowerCase()">
            {{ cat.name }}
          </option>
        </select>
      </div>

      <div class="sort-options">
        <span class="label">
          <i class="fa-solid fa-arrow-down-wide-short"></i> Sắp xếp:
        </span>

        <button
          class="filter-chip"
          :class="{ active: sortOption === 'createdAt,desc' }"
          @click="applySort('createdAt,desc')"
        >
          Mới nhất
        </button>
        <button
          class="filter-chip"
          :class="{ active: sortOption === 'price,asc' }"
          @click="applySort('price,asc')"
        >
          Giá thấp → cao
        </button>
        <button
          class="filter-chip"
          :class="{ active: sortOption === 'price,desc' }"
          @click="applySort('price,desc')"
        >
          Giá cao → thấp
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-text">
      <i class="fa-solid fa-spinner fa-spin"></i> Đang tải sản phẩm...
    </div>

    <div v-else-if="products.length === 0" class="empty-msg">
      <i class="fa-solid fa-box-open empty-icon"></i>
      <p>Không tìm thấy sản phẩm phù hợp.</p>
    </div>

    <div v-else>
      <div class="product-grid">
        <div v-for="product in products" :key="product.id" class="product-card">

          <div class="image-wrapper">

            <!-- ❤️ NÚT TRÁI TIM -->
            <button
              class="favorite-btn"
              @click.stop="toggleWishlist(product.id)"
              :title="isLiked(product.id) ? 'Bỏ khỏi yêu thích' : 'Thêm vào yêu thích'"
            >
              <i :class="[ isLiked(product.id) ? 'fa-solid fa-heart active' : 'fa-regular fa-heart' ]"></i>
            </button>

            <!-- HOVER ẢNH: Ảnh 1 -> Ảnh 2 -->
            <RouterLink :to="`/product/${product.id}`" class="image-link">
              <!-- Ảnh chính -->
              <img
                class="product-img primary"
                :src="product.imageUrls?.[0] || 'https://via.placeholder.com/300x420'"
                :alt="product.name"
              />
              <!-- Ảnh thứ 2 nếu có -->
              <img
                v-if="product.imageUrls && product.imageUrls.length > 1"
                class="product-img secondary":src="product.imageUrls[1]"
                :alt="product.name + ' preview'"
              />
            </RouterLink>

            <div class="hover-actions">
              <RouterLink :to="`/product/${product.id}`" class="btn-view">Chi tiết</RouterLink>
            </div>
          </div>

          <div class="product-info">
            <h3 class="name">{{ product.name }}</h3>
            <p class="category">{{ product.category }}</p>
            <p class="price">{{ formatPrice(product.price) }}</p>
          </div>

        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination-container">
        <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">
          &larr; Trang trước
        </button>
        <span class="page-info">Trang {{ currentPage }} / {{ totalPages }}</span>
        <button :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">
          Trang sau &rarr;
        </button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';

import productService from '@/services/productService.js';
import categoryService from '@/services/categoryService.js';
import wishlistService from '@/services/wishlistService.js';
import { useAuthStore } from '@/stores/authStore.js';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const products = ref([]);
const loading = ref(true);
const error = ref(null);

const currentPage = ref(1);
const totalPages = ref(1);
const totalElements = ref(0);
const pageSize = 12;

const sortOption = ref('createdAt,desc');
const categories = ref([]);
const selectedCategoryName = ref('');

// ===== WISHLIST =====
const wishlist = ref([]);

// Lấy wishlist hiện tại
const fetchWishlist = async () => {
  if (!authStore.isAuthenticated) {
    wishlist.value = [];
    return;
  }

  try {
    const res = await wishlistService.getMyWishlist();

    wishlist.value = res.data.map(item => {
      if (item.product && item.product.id) return item.product.id;
      if (item.productId) return item.productId;
      return item.id;
    });
  } catch (err) {
    console.error('Lỗi lấy wishlist:', err);
    wishlist.value = [];
  }
};

const isLiked = (productId) => wishlist.value.includes(productId);

// Toggle tim – optimistic UI
const toggleWishlist = async (productId) => {
  if (!authStore.isAuthenticated) {
    return router.push('/login');
  }

  const currentlyLiked = isLiked(productId);

  // Cập nhật UI ngay
  if (currentlyLiked) {
    wishlist.value = wishlist.value.filter(id => id !== productId);
  } else {
    wishlist.value = [...wishlist.value, productId];
  }

  try {
    if (currentlyLiked) {
      // xoá khỏi wishlist
      await (wishlistService.remove || wishlistService.removeFromWishlist)(productId);
    } else {
      // thêm vào wishlist
      await (wishlistService.add || wishlistService.addToWishlist)(productId);
    }
  } catch (err) {
    console.error('Wishlist error:', err);
    // Có thể rollback lại nếu muốn
  }
};

// ===== TIÊU ĐỀ DANH MỤC =====
const categoryTitle = computed(() => {
  return selectedCategoryName.value
    ? selectedCategoryName.value.toUpperCase()
    : 'TẤT CẢ SẢN PHẨM';
});

// ===== LẤY DANH MỤC =====
const fetchCategories = async () => {
  try {
    const res = await categoryService.getAllPublicCategories();
    categories.value = res.data;
  } catch (err) {
    console.error('Lỗi tải danh mục:', err);
  }
};

// ===== LẤY SẢN PHẨM (CÓ PHÂN TRANG) =====
const fetchProducts = async () => {
  loading.value = true;
  error.value = null;

  const keyword = (route.query.q || '').toString().trim().toLowerCase();
  const categoryForApi = selectedCategoryName.value;

  try {
    const params = {
      page: currentPage.value - 1, // 0-based
      size: pageSize,
      category: categoryForApi,
      sort: sortOption.value,
    };

    const res = await productService.getAllProducts(params);
    const data = res.data || {};

    let list = data.content || [];

    // GÁN PHÂN TRANG TỪ BE (Spring Page: number, totalPages, totalElements)
    totalPages.value = data.totalPages ?? 1;
    totalElements.value = data.totalElements ?? list.length;

    // currentPage từ BE (number là 0-based)
    const backendPageIndex = data.number ?? params.page ?? 0;
    currentPage.value = backendPageIndex + 1;


    // ===== Lọc keyword phía FE =====
    if (keyword) {
      list = list.filter((p) => {
        const name = (p.name || '').toLowerCase();
        const cat = (p.category || '').toLowerCase();
        return name.includes(keyword) || cat.includes(keyword);
      });

      // Khi search thì set về 1 trang cho dễ hiểu
      totalElements.value = list.length;
      totalPages.value = 1;
      currentPage.value = 1;
    }

    // ===== Sort FE =====
    if (sortOption.value === 'price,asc') {
      list.sort((a, b) => a.price - b.price);
    } else if (sortOption.value === 'price,desc') {
      list.sort((a, b) => b.price - a.price);
    } else if (sortOption.value === 'createdAt,desc') {
      list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    }

    products.value = list;
  } catch (err) {
    console.error('Lỗi tải sản phẩm:', err);
    error.value = 'Không thể tải sản phẩm. Vui lòng thử lại sau.';
    products.value = [];
    totalPages.value = 1;
    totalElements.value = 0;
  } finally {
    loading.value = false;
  }
};

// ===== SORT =====
const applySort = (newSort) => {
  sortOption.value = newSort;
  currentPage.value = 1;
  fetchProducts();
};

// ===== CATEGORY CHANGE =====
const handleCategoryFilterChange = () => {
  currentPage.value = 1;
  fetchProducts();
};

// ===== PAGINATION =====
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;

  currentPage.value = page;
  fetchProducts();
};


// ===== WATCH URL (cat & q) =====
watch(
  () => [route.query.cat, route.query.q],
  ([cat, q]) => {
    selectedCategoryName.value = cat ? cat.toLowerCase() : '';

    if (cat) {
      currentPage.value = 1;
    }

    fetchProducts();
  }
);


// ===== MOUNTED =====
onMounted(() => {
  fetchCategories();
  fetchWishlist();
});

// ===== FORMAT PRICE =====
const formatPrice = (val) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oswald:wght@400;700&family=Inter:wght@300;400;500;600;700&display=swap');

/* BASE */
.shop-page {
  padding: 110px 20px 80px;
  max-width: 1300px;
  margin: auto;
  font-family: "Inter", sans-serif;
  animation: fadeIn 0.4s ease;
}

/* HEADER */
.shop-header {
  text-align: center;
  margin-bottom: 40px;
}
.shop-header .title {
  font-size: 3rem;
  font-family: "Oswald", sans-serif;
  letter-spacing: 2px;
  font-weight: 900;
  text-transform: uppercase;
}
.shop-header .subtitle {
  margin-top: 6px;
  color: #6b7280;
}

/* TOOLBAR */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 35px;
  background: #ffffff;
  padding: 14px 20px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 18px rgba(0,0,0,0.06);
  flex-wrap: wrap;
  gap: 15px;
}

.category-filter-box {
  display: flex;
  align-items: center;
  gap: 8px;
}
.category-filter-box .label {
  font-weight: 600;
  color: #111827;
  display: flex;
  align-items: center;
  gap: 5px;
}
.category-filter-box i {
  color: #ef4444;
}
.category-filter-box select {
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  font-size: 0.9rem;
  background-color: #fff;
  cursor: pointer;
  outline: none;
  min-width: 180px;
}
.category-filter-box select:focus {
  border-color: #111827;
}

/* RIGHT SORT OPTIONS */
.sort-options {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.sort-options .label {
  font-weight: 600;
  color: #111827;
  display: flex;
  align-items: center;
  gap: 5px;
}
.sort-options i {
  color: #4b5563;
}

/* FILTER CHIPS */
.filter-chip {
  background: #f3f4f6;
  border-radius: 999px;
  padding: 6px 14px;
  border: none;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  transition: 0.2s;
  text-transform: uppercase;
  white-space: nowrap;
}
.filter-chip:hover {
  background: #e5e7eb;
}
.filter-chip.active {
  background: #111827;
  color: #ffffff;
}

/* GRID */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 32px;
  padding: 0 25px;
}

/* CARD */
.product-card {
  background: #ffffff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  transition: 0.3s ease;
  cursor: pointer;
}
.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(0,0,0,0.16);
}

/* IMAGE WRAPPER */
.image-wrapper {
  position: relative;
  height: 360px;
  overflow: hidden;
  background: #f8f8f8;
}

/* Link chứa 2 ảnh */
.image-link {
  display: block;
  width: 100%;
  height: 100%;
  position: relative;
}

/* Hai ảnh xếp chồng lên nhau */
.product-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.4s ease, transform 0.5s ease;
}

/* Ảnh chính */
.product-img.primary {
  opacity: 1;
  z-index: 1;
}

/* Ảnh thứ 2 ẩn mặc định */
.product-img.secondary {
  opacity: 0;
  z-index: 2;
  transform: scale(1.05);
}

/* Hover card → đổi ảnh */
.product-card:hover .product-img.primary {
  opacity: 0;
  transform: scale(1.08);
}

.product-card:hover .product-img.secondary {
  opacity: 1;
  transform: scale(1.08);
}

/* ❤️ FAVORITE BUTTON */
.favorite-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
z-index: 30 !important;
  pointer-events: auto;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transition: transform 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.favorite-btn i {
  font-size: 1.2rem;
  color: #9ca3af;
  transition: color 0.15s ease, transform 0.15s ease;
}

.favorite-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 16px rgba(0,0,0,0.20);
}

.favorite-btn i.active {
  color: #ef4444;
  transform: scale(1.15);
}

/* OVERLAY: Xem chi tiết */
.hover-actions {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  opacity: 0;
  transition: 0.3s ease;
}
.product-card:hover .hover-actions {
  opacity: 1;
}
.btn-view {
  box-sizing: border-box;
  background: #ffffff;
  padding: 10px 26px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 700;
  text-decoration: none;
  color: #111827;
  letter-spacing: 1px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.15);
}
.btn-view:hover {
  background: #f3f4f6;
}

/* INFO */
.product-info {
  text-align: center;
  padding: 18px;
}
.name {
  font-size: 1.05rem;
  font-weight: 700;
  margin-bottom: 4px;
  text-transform: uppercase;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.category {
  font-size: 0.75rem;
  color: #6b7280;
  margin-bottom: 6px;
}
.price {
  font-weight: 900;
  font-size: 1.05rem;
  color: #d0021b;
}

/* PAGINATION */
.pagination-container {
  margin-top: 45px;
  display: flex;
  justify-content: center;
  gap: 22px;
}
.pagination-container button {
  padding: 10px 22px;
  border: 1px solid #111827;
  background: #ffffff;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s;
  border-radius: 6px;
}
.pagination-container button:hover:not(:disabled) {
  background: #111827;
  color: #ffffff;
}
.pagination-container button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.page-info {
  font-weight: 600;
  color: #374151;
}

/* EMPTY / LOADING */
.empty-msg {
  margin-top: 80px;
  text-align: center;
  color: #777;
  font-size: 1rem;
  letter-spacing: 0.5px;
}
.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  color: #ccc;
}
.loading-text {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

/* ANIMATION */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* RESPONSIVE */
@media (max-width: 992px) {
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 18px;
    padding: 0 10px;
  }
  .image-wrapper {
    height: 220px;
  }
  .name {
    font-size: 0.9rem;
  }
  .price {
    font-size: 1rem;
  }
  .shop-header .title {
    font-size: 2.2rem;
  }
}
</style>