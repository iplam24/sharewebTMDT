<template>
  <div class="wishlist-page container">
    <section class="wishlist-header">
      <h1 class="title">SẢN PHẨM YÊU THÍCH</h1>
      <p class="subtitle" v-if="products.length > 0">
        Bạn đang có <strong>{{ products.length }}</strong> sản phẩm yêu thích
      </p>
    </section>

    <!-- Loading -->
    <div v-if="loading" class="loading-text">
      <i class="fa-solid fa-spinner fa-spin"></i> Đang tải danh sách yêu thích...
    </div>

    <!-- Empty -->
    <div v-else-if="!products || products.length === 0" class="empty-msg">
      <i class="fa-regular fa-heart empty-icon"></i>
      <h3>Danh sách yêu thích trống</h3>
      <p>Hãy thêm những sản phẩm bạn thích để xem lại dễ dàng hơn nhé.</p>
      <RouterLink to="/shop" class="go-shop-btn">
        <i class="fa-solid fa-bag-shopping"></i> Mua sắm ngay
      </RouterLink>
    </div>

    <!-- List -->
    <div v-else>
      <div class="product-grid">
        <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="goToDetail(product.id)"
        >
          <div class="image-wrapper">
            <!-- Nút trái tim -->
            <button
              type="button"
              class="favorite-btn"
              @click.stop.prevent="handleToggleWishlist(product)"
              :title="isLiked(product.id) ? 'Bỏ khỏi yêu thích' : 'Thêm vào yêu thích'"
            >
              <i :class="[isLiked(product.id) ? 'fa-solid fa-heart active' : 'fa-regular fa-heart']"></i>
            </button>

            <RouterLink :to="`/product/${product.id}`">
              <img
                :src="product.imageUrls?.[0] || 'https://via.placeholder.com/300x420'"
                :alt="product.name"
              />
            </RouterLink>
          </div>

          <div class="product-info">
            <h3 class="name">{{ product.name }}</h3>
            <p class="category">{{ product.category }}</p>
            <p class="price">{{ formatPrice(product.price) }}</p>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRouter, RouterLink } from 'vue-router';
import { useWishlistStore } from '@/stores/wishlistStore';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();
const wishlistStore = useWishlistStore();

const loading = computed(() => wishlistStore.loading);
const products = computed(() => wishlistStore.items || []);

const isLiked = (productId) => wishlistStore.isInWishlist ? wishlistStore.isInWishlist(productId) : true;

const handleToggleWishlist = async (product) => {
  if (!authStore.isAuthenticated) {
    return router.push('/login');
  }
  await wishlistStore.toggle(product);
};

const goToDetail = (id) => {
  router.push(`/product/${id}`);
};

const formatPrice = (val) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login');
    return;
  }
  // Luôn tải lại wishlist để đảm bảo dữ liệu mới nhất
  await wishlistStore.fetchWishlist();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oswald:wght@400;700&family=Inter:wght@300;400;500;600;700&display=swap');

.wishlist-page {
  padding: 110px 20px 80px;
  max-width: 1300px;
  margin: 0 auto;
  font-family: "Inter", sans-serif;
  animation: fadeIn 0.3s ease;
}

/* HEADER */
.wishlist-header {
  text-align: center;
  margin-bottom: 40px;
}
.wishlist-header .title {
  font-size: 2.6rem;
  font-family: "Oswald", sans-serif;
  letter-spacing: 2px;
  font-weight: 900;
  text-transform: uppercase;
}
.wishlist-header .subtitle {
  margin-top: 8px;
  color: #6b7280;
  font-size: 0.95rem;
}

/* GRID */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 32px;
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

/* IMAGE */
.image-wrapper {
  position: relative;
  height: 320px;
  overflow: hidden;
  background: #f8f8f8;
}
.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: 0.5s ease;
}
.product-card:hover img {
  transform: scale(1.08);
}

/* HEART BUTTON */
.favorite-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  border-radius: 999px;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
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
  box-shadow: 0 6px 16px rgba(0,0,0,0.2);
}
.favorite-btn i.active {
  color: #ef4444;
  transform: scale(1.15);
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

/* EMPTY / LOADING */
.empty-msg {
  margin-top: 80px;
  text-align: center;
  color: #4b5563;
}
.empty-icon {
  font-size: 4rem;
  margin-bottom: 15px;
  color: #f97373;
}
.go-shop-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  padding: 10px 22px;
  background: #111827;
  color: #fff;
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 600;
  text-decoration: none;
}
.go-shop-btn:hover {
  background: #000;
}
.loading-text {
  text-align: center;
  padding: 40px 0;
  color: #999;
}


@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* RESPONSIVE */
@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 18px;
  }
  .image-wrapper {
    height: 220px;
  }
  .wishlist-header .title {
    font-size: 2.1rem;
  }
}
</style>
