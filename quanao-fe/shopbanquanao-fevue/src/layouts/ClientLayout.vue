<template>
  <div class="client-layout">
    
    <div class="mobile-header">
      <div class="mobile-left">
        <button @click="toggleMenu" class="hamburger-btn">
          <i class="fa-solid fa-bars"></i>
        </button>
      </div>

      <RouterLink to="/" class="mobile-logo">CLOTHES VNUA</RouterLink>
      
      <RouterLink to="/cart" class="mobile-cart-btn">
        <i class="fa-solid fa-bag-shopping"></i>
        <span class="mobile-badge">{{ cartStore.cartCount }}</span>
      </RouterLink>
    </div>

    <aside class="client-sidebar" :class="{ 'is-open': isMenuOpen }">
      <div class="sidebar-container">
        
        <button class="close-menu-btn" @click="toggleMenu">
          <i class="fa-solid fa-xmark"></i>
        </button>

        <div class="logo-wrapper">
          <RouterLink to="/" class="logo">CLOTHES VNUA</RouterLink>
        </div>

<div class="search-box-wrapper">
  <div class="search-box">
    <input
      type="text"
      v-model="searchText"
      placeholder="Tìm kiếm sản phẩm..."
      @input="fetchSuggestions"
      @keyup.enter="handleSearch"
    />
    <button class="search-btn" @click="handleSearch">
      <i class="fa-solid fa-magnifying-glass"></i>
    </button>
  </div>

  <!-- GỢI Ý TÌM KIẾM -->
  <ul v-if="suggestions.length > 0" class="suggestion-list">
    <li v-for="item in suggestions" 
        :key="item.id"
        @click="goToProduct(item.id)">
      {{ item.name }}
    </li>
  </ul>
</div>


        <nav class="main-nav">
          <RouterLink to="/" @click="closeMenu">TRANG CHỦ</RouterLink>
          <RouterLink to="/shop" @click="closeMenu">SẢN PHẨM</RouterLink>
          <RouterLink to="/shop?cat=tops" @click="closeMenu">TOP LƯỢT BÁN</RouterLink>
          <RouterLink to="/about" @click="closeMenu">GIỚI THIỆU</RouterLink>
          <router-link to="/wishlist">SẢN PHẨM YÊU THÍCH</router-link>
          <RouterLink to="/discounts" class="sale-link" @click="closeMenu">GIẢM GIÁ</RouterLink>
          <RouterLink to="/return-policy" class="action-link" @click="closeMenu">
            Chính sách đổi trả
          </RouterLink>
        </nav>

        <div class="sidebar-footer">
          <RouterLink to="/cart" class="action-link" @click="closeMenu">
            <i class="fa-solid fa-bag-shopping"></i> GIỎ HÀNG ({{ cartStore.cartCount }})
          </RouterLink>

        <div v-if="authStore.isAuthenticated" class="user-dropdown">
      <button class="dropdown-btn">
        <i class="fa-solid fa-circle-user"></i> 
        <span>{{ authStore.user }}</span>
        <i class="fa-solid fa-chevron-down arrow-icon"></i>
      </button>
      
      <div class="dropdown-content">
        <RouterLink to="/my-orders" @click="closeMenu">
          <i class="fa-solid fa-box-open"></i> Đơn hàng của tôi
        </RouterLink>
         
        <RouterLink to="/profile" @click="closeMenu"> <i class="fa-solid fa-address-card"></i> Tài khoản
        </RouterLink>
        
        <div class="divider"></div>
        
        <a href="#" @click.prevent="handleLogout" class="logout-item">
          <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
        </a>
      </div>
    </div>
          
          <RouterLink v-else to="/login" class="action-link" @click="closeMenu">
            ĐĂNG NHẬP
          </RouterLink>

          <div class="lang-selector">VN <i class="fa-solid fa-caret-down"></i></div>
        </div>
      </div>
    </aside>

    <div class="sidebar-overlay" v-if="isMenuOpen" @click="closeMenu"></div>

    <main class="client-main">
      <RouterView />
    </main>

    <div class="floating-btn right-btn">
      <i class="fa-brands fa-facebook-messenger"></i>
    </div>
  </div>

  <footer class="client-footer">
      <div class="container footer-container">
        <div class="footer-grid">
          
          <div class="footer-col">
            <h4>HỆ THỐNG CỬA HÀNG CLOTHES VNUA</h4>
            <p class="address-line">
              <strong>CLOTHES VNUA FLAGSHIP STORE:</strong><br>
              Trâu Quỳ - Gia Lâm - Hà Nội.
            </p>
            <div class="map-link">
              <a href="#">Xem bản đồ</a>
            </div>
          </div>

          <div class="footer-col">
            <h4>CHÍNH SÁCH</h4>
            <ul class="footer-links">
              <li><a href="#">Phương thức thanh toán</a></li>
              <li><RouterLink to="/return-policy">Chính sách đổi trả</RouterLink></li>
              <li><a href="#">Chính sách giao nhận - vận chuyển</a></li>
              <li><a href="#">Hướng dẫn mua hàng</a></li>
              <li><a href="#">Bảo mật thông tin</a></li>
            </ul>
          </div>

          <div class="footer-col">
            <h4>THÔNG TIN LIÊN HỆ</h4>
            <ul class="contact-info">
              <li><strong>CÔNG TY TNHH Clothes</strong></li>
              <li>Địa chỉ: Trâu Quỳ - Gia Lâm - Hà Nội</li>
              <li>SỐ CSKH: 0414232322</li>
              <li>Tuyển dụng: ads@gmail.com</li>
              <li>For business: contact@clothes.net</li>
            </ul>
          </div>

          <div class="footer-col">
            <h4>FOLLOW US ON SOCIAL MEDIA</h4>
            <div class="social-icons">
              <a href="#"><i class="fa-brands fa-facebook-square"></i></a>
              <a href="#"><i class="fa-brands fa-instagram"></i></a>
              <a href="#"><i class="fa-brands fa-tiktok"></i></a>
            </div>
            
            <div class="bct-badge">
              <img src="http://online.gov.vn/Content/EndUser/LogoCCDVSaleNoti/logoSaleNoti.png" alt="Đã thông báo Bộ Công Thương" width="150">
            </div>
          </div>

        </div>
        
        <div class="footer-bottom">
          <p>&copy; 2025 CLOTHES VNUA. ALL RIGHTS RESERVED.</p>
        </div>
      </div>
    </footer>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { RouterLink, RouterView, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore.js';
import { useCartStore } from '@/stores/cartStore.js';
import productService from '@/services/productService.js';

const authStore = useAuthStore();
const cartStore = useCartStore();
const router = useRouter();

const isMenuOpen = ref(false);
const searchText = ref('');

// 👉 danh sách gợi ý
const suggestions = ref([]);

// Khi load trang, nếu đã login thì lấy số lượng từ Store
onMounted(() => {
  if (authStore.isAuthenticated) {
    cartStore.fetchCartCount();
  }
});

const toggleMenu = () => { isMenuOpen.value = !isMenuOpen.value; };
const closeMenu = () => { isMenuOpen.value = false; };

const handleLogout = () => { 
  authStore.logout();
  cartStore.clearCart();
  closeMenu(); 
};

// 🔍 Gọi khi bấm icon kính lúp / Enter
const handleSearch = () => {
  const q = searchText.value.trim();
  if (!q) return;

  router.push({
    path: '/shop',
    query: { q }
  });

  suggestions.value = []; // clear gợi ý
  closeMenu();
};

// 🔎 Gợi ý khi gõ chữ
const fetchSuggestions = async () => {
  const q = searchText.value.trim().toLowerCase();

  if (!q) {
    suggestions.value = [];
    return;
  }

  try {
    // Lấy 1 trang sản phẩm rồi lọc FE
    const res = await productService.getAllProducts({
      page: 0,
      size: 50,
      sort: 'createdAt,desc'
    });

    const list = res.data.content || [];

    suggestions.value = list
      .filter(p => (p.name || '').toLowerCase().includes(q))
      .slice(0, 6); // tối đa 6 gợi ý

  } catch (e) {
    console.error('Lỗi gợi ý tìm kiếm:', e);
    suggestions.value = [];
  }
};

// Khi click vào 1 gợi ý
const goToProduct = (id) => {
  suggestions.value = [];
  searchText.value = '';
  closeMenu();
  router.push(`/product/${id}`);
};
</script>


<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oswald:wght@400;700&family=Roboto:wght@400;500;700&display=swap');

/* ========== LAYOUT CHUNG ========== */
.client-layout {
  font-family: 'Roboto', sans-serif;
  min-height: 100vh;
  background: #f5f5f5;
}

/* ========== USER DROPDOWN ========== */
.user-dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 700;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  padding: 5px 0;
}

.dropdown-btn i {
  font-size: 1.1rem;
  color: #555;
}

.arrow-icon {
  font-size: 0.7rem !important;
  margin-left: 2px;
}

.dropdown-content {
  display: none;
  position: absolute;
  bottom: 100%;
  left: 0;
  background-color: #fff;
  min-width: 190px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.15);
  border-radius: 6px;
  padding: 8px 0;
  z-index: 1000;
  border: 1px solid #eee;
}

.user-dropdown:hover .dropdown-content {
  display: block;
  animation: fadeIn 0.2s ease;
}

.dropdown-content a {
  color: #333;
  padding: 10px 15px;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.85rem;
  font-weight: 500;
  transition: background 0.2s;
}

.dropdown-content a:hover {
  background-color: #f8f9fa;
  color: #d0021b;
}

.dropdown-content i {
  width: 20px;
  text-align: center;
  color: #777;
}

.dropdown-content .divider {
  height: 1px;
  background-color: #eee;
  margin: 5px 0;
}

.logout-item {
  color: #d0021b !important;
}
.logout-item:hover {
  background-color: #fff5f5 !important;
}
.search-box-wrapper {
  margin-bottom: 28px;
  position: relative; /* để dropdown nằm ngay dưới */
}

/* DROPDOWN GỢI Ý */
.suggestion-list {
  position: absolute;
  top: 105%;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  list-style: none;
  padding: 6px 0;
  margin: 4px 0 0;
  box-shadow: 0 10px 25px rgba(0,0,0,0.12);
  max-height: 260px;
  overflow-y: auto;
  z-index: 1300;
}

.suggestion-list li {
  padding: 8px 14px;
  font-size: 0.88rem;
  cursor: pointer;
  color: #374151;
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suggestion-list li:hover {
  background: #f3f4f6;
  color: #111827;
}

/* Animation dropdown */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Mobile: dropdown hiện xuống dưới */
@media (max-width: 992px) {
  .dropdown-content {
    bottom: auto;
    top: 100%;
  }
}

/* ========== MOBILE HEADER ========== */
.mobile-header {
  display: none;
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 60px;
  background: #fff;
  z-index: 1100;
  padding: 0 20px;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.mobile-left {
  display: flex;
  gap: 15px;
  align-items: center;
}

.mobile-logo {
  font-family: 'Oswald', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #d0021b;
  text-decoration: none;
}

.hamburger-btn,
.mobile-cart-btn {
  font-size: 1.4rem;
  color: #333;
  background: none;
  border: none;
  cursor: pointer;
}

.mobile-cart-btn {
  position: relative;
}

.mobile-badge {
  position: absolute;
  top: -5px;
  right: -8px;
  background: #d0021b;
  color: #fff;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 999px;
}

/* ========== SIDEBAR ========== */
.client-sidebar {
  width: 280px;
  height: 100vh;
  background-color: #ffffff;
  position: fixed;
  left: 0; top: 0;
  z-index: 1200;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  padding: 40px 28px;
  transition: transform 0.3s ease-in-out;
}

.close-menu-btn {
  display: none;
}

.sidebar-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow-y: auto;
}

/* Logo */
.logo-wrapper {
  margin-bottom: 24px;
}
.logo {
  font-family: 'Oswald', sans-serif;
  font-size: 2rem;
  font-weight: 700;
  color: #d0021b;
  text-decoration: none;
  letter-spacing: 2px;
}

/* Search Box – FIX icon kính lúp */
.search-box-wrapper {
  margin-bottom: 28px;
}

.search-box {
  display: flex;
  align-items: center;
  border: 1px solid #e0e0e0;
  border-radius: 999px;
  padding: 6px 12px;
  background-color: #fafafa;
  transition: border-color 0.2s, background-color 0.2s;
}

.search-box:focus-within {
  border-color: #111;
  background-color: #fff;
}

.search-box input {
  border: none;
  background: transparent;
  width: 100%;
  padding: 6px 4px;
  outline: none;
  font-size: 0.9rem;
  color: #333;
}

.search-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  background: none;
  border: none;
  cursor: pointer;
  color: #777;
  font-size: 1rem;
  transition: color 0.2s;
}

.search-btn i {
  pointer-events: none;
}

.search-btn:hover {
  color: #d0021b;
}

/* Main Nav */
.main-nav {
  display: flex;
  flex-direction: column;
  gap: 18px;
  flex-grow: 1;
}

.main-nav a {
  text-decoration: none;
  color: #333;
  font-size: 0.9rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: color 0.2s, padding-left 0.2s;
}

.main-nav a:hover {
  color: #d0021b;
  padding-left: 5px;
}

.sale-link {
  color: #d0021b !important;
}

/* Sidebar Footer */
.sidebar-footer {
  display: flex;
  flex-direction: column;
  gap: 15px;
  border-top: 1px solid #eee;
  padding-top: 24px;
  margin-top: 20px;
}

.action-link,
.user-menu {
  text-decoration: none;
  color: #555;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.action-link i {
  font-size: 1rem;
}

.action-link:hover {
  color: #000;
}

/* Overlay khi mở menu mobile */
.sidebar-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0,0,0,0.45);
  z-index: 1150;
}

/* ========== MAIN CONTENT ========== */
.client-main {
  margin-left: 280px;
  background-color: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 80px;
  transition: margin-left 0.3s ease;
}

/* ========== FLOATING BUTTON (MESSENGER) ========== */
.floating-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.6rem;
  cursor: pointer;
  z-index: 999;
  box-shadow: 0 4px 10px rgba(0,0,0,0.2);
  background-color: #0084ff;
  transition: transform 0.2s;
  animation: pulse-blue 2s infinite;
}

.floating-btn:hover {
  transform: scale(1.08);
}

/* Animation Pulse */
@keyframes pulse-blue {
  0%   { transform: scale(1);   box-shadow: 0 0 0 0 rgba(0,132,255,0.7); }
  70%  { transform: scale(1.1); box-shadow: 0 0 0 15px rgba(0,132,255,0); }
  100% { transform: scale(1);   box-shadow: 0 0 0 0 rgba(0,132,255,0); }
}

/* ========== FOOTER ========== */
.client-footer {
  background-color: #ffffff;
  color: #333;
  padding: 60px 40px 20px;
  border-top: 1px solid #e5e5e5;
  margin-top: auto;
  font-family: 'Roboto', sans-serif;
  /* footer full width nhưng content thẳng hàng với main */
  width: 100%;
  box-sizing: border-box;
  padding-left: calc(40px + 280px); /* 280 sidebar + 40 padding */
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  margin-bottom: 40px;
}

.footer-col h4 {
  font-family: 'Oswald', sans-serif;
  font-size: 0.9rem;
  font-weight: 700;
  text-transform: uppercase;
  margin-bottom: 18px;
  letter-spacing: 0.5px;
  color: #000;
}

.footer-col p,
.footer-col li,
.footer-col a {
  font-size: 0.85rem;
  line-height: 1.8;
  color: #333;
  text-decoration: none;
  list-style: none;
}

.footer-col ul {
  padding: 0;
  margin: 0;
}

.footer-links li a:hover {
  color: #d0021b;
  text-decoration: underline;
}

.address-line {
  margin-bottom: 10px;
}

.map-link a {
  font-weight: 600;
  text-decoration: underline;
}

.social-icons {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.social-icons a {
  font-size: 2rem;
  color: #555;
  transition: color 0.2s;
}

.social-icons a:hover {
  color: #000;
}

.bct-badge img {
  max-width: 100%;
  height: auto;
  width: 150px;
  cursor: pointer;
}

.footer-bottom {
  border-top: 1px solid #eee;
  padding-top: 20px;
  text-align: center;
  font-size: 0.75rem;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* ========== RESPONSIVE ========== */
@media (max-width: 1200px) {
  .footer-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1024px) {
  .footer-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .mobile-header {
    display: flex;
  }

  .client-sidebar {
    transform: translateX(-100%);
    width: 280px;
    box-shadow: 2px 0 10px rgba(0,0,0,0.16);
  }

  .client-sidebar.is-open {
    transform: translateX(0);
  }

  .close-menu-btn {
    display: block;
    position: absolute;
    top: 15px;
    right: 15px;
    background: none;
    border: none;
    font-size: 1.5rem;
    color: #333;
    cursor: pointer;
  }

  .client-main {
    margin-left: 0;
    padding-top: 70px;
  }

  .client-footer {
    padding-left: 20px; /* bỏ offset sidebar trên mobile */
    padding-right: 20px;
  }

  .logo-wrapper {
    margin-top: 20px;
    margin-bottom: 20px;
  }
}

@media (max-width: 600px) {
  .footer-grid {
    grid-template-columns: 1fr;
    gap: 30px;
  }
}

.history-link {
  color: #555;
  margin: 0 10px;
  font-size: 1.1rem;
}
.history-link:hover {
  color: #000;
}
</style>
