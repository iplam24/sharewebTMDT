<template>
  <div class="home-view">
    <!-- TOP NOTICE BAR -->
    <div class="top-notice">
      <span>FREESHIP đơn từ 299K · Giảm 10% cho sinh viên VNUA · Hotline: 09xx xxx xxx</span>
    </div>

    <!-- HERO SECTION -->
    <section class="hero-section">
      <div class="marquee-bar">
        <span>CLOTHES VNUA · STREETWEAR CAMPUS · LIMITED COLLECTION · </span>
      </div>

      <div class="hero-content">
        <h1 class="hero-title">
          REFINED ORIGINS
          <i class="fa-solid fa-plane-departure"></i>
        </h1>
        <p class="hero-desc">
          LAUNCHED AS SWE (STREET WEAR EAZY), WE HAVE PROUDLY EMBRACED A BOLD STREET STYLE
          AND DEDICATED NINE YEARS TO BUILDING AND DEVELOPING OUR BRAND. TODAY, WE ARE EXCITED TO UNVEIL...
        </p>

        <div class="hero-actions">
          <RouterLink to="/shop" class="hero-btn primary">MUA NGAY</RouterLink>
          <a href="#new-products" class="hero-btn ghost">XEM BỘ SƯU TẬP</a>
        </div>
      </div>
    </section>

    <!-- FEATURE STRIP -->
    <section class="feature-strip">
      <div class="feature-item">
        <i class="fa-solid fa-truck-fast"></i>
        <div>
          <h4>FREESHIP TOÀN QUỐC</h4>
          <p>Đơn từ 299K trở lên</p>
        </div>
      </div>
      <div class="feature-item">
        <i class="fa-solid fa-rotate-left"></i>
        <div>
          <h4>ĐỔI TRẢ 7 NGÀY</h4>
          <p>Nếu sản phẩm lỗi / sai size</p>
        </div>
      </div>
      <div class="feature-item">
        <i class="fa-solid fa-headset"></i>
        <div>
          <h4>HỖ TRỢ NHANH</h4>
          <p>Inbox fanpage hoặc Zalo</p>
        </div>
      </div>
    </section>

    <!-- NEW PRODUCTS -->
 <section class="products-section" id="new-products">
      <div class="container">
        <div class="section-header">
          <h2>SẢN PHẨM MỚI</h2>
          <p>Các item mới nhất mùa này tại CLOTHES VNUA</p>
        </div>

        <div v-if="loading" class="loading-text">Đang tải sản phẩm...</div>

        <div v-else class="product-grid">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="goToDetail(product.id)"
            @mouseover="changeImage(product, 1)"
            @mouseleave="changeImage(product, 0)"
          >
            <div class="badge" v-if="product.isNew !== false">
              NEW
            </div>

            <div class="product-img-box">
              <img
                :src="product.hoverImage || product.imageUrls?.[0]"
                alt="Product Image"
              />
              <div class="overlay">
                <button class="quick-view-btn" @click.stop="goToDetail(product.id)">XEM CHI TIẾT</button>
              </div>
            </div>

            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-subtitle">{{ product.category }}</p>
              <div class="product-meta">
                <span class="price">{{ formatPrice(product.price) }}</span>
                <span class="variants-count" v-if="product.variants">
                  {{ product.variants.length }} loại
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="view-all-container">
          <RouterLink to="/shop" class="view-all-btn">
            XEM TẤT CẢ SẢN PHẨM
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- LOOKBOOK / ABOUT SECTION -->
    <section class="about-section">
      <div class="about-container">
        <div class="about-text">
          <h3>CLOTHES VNUA · CAMPUS STREETWEAR</h3>
          <p>
            Lấy cảm hứng từ đời sống sinh viên VNUA, CLOTHES VNUA mang đến những
            thiết kế đơn giản, dễ phối nhưng vẫn giữ được chất “streetwear” hiện đại.
          </p>
          <p>
            Từng sản phẩm được chọn chất liệu kỹ lưỡng, form dáng phù hợp với
            vóc dáng người Việt, đặc biệt là các bạn sinh viên thích style gọn gàng,
            thoải mái nhưng vẫn nổi bật.
          </p>
          <RouterLink to="/about" class="about-btn">
            TÌM HIỂU THÊM VỀ BRAND
          </RouterLink>
        </div>
        <div class="about-lookbook">
          <div class="lookbook-main"></div>
          <div class="lookbook-sub top"></div>
          <div class="lookbook-sub bottom"></div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import productService from '@/services/productService.js';
import router from '@/router'; // Import useRouter instance

const products = ref([]);
const loading = ref(true);

const fetchProducts = async () => {
    try {
        const res = await productService.getAllProducts();
        // Lấy 8 sản phẩm đầu tiên và thêm trường hoverImage (cho hiệu ứng)
        products.value = res.data.content.slice(0, 8).map(p => ({
            ...p,
            // Đảm bảo ảnh thứ 2 (index 1) là ảnh hover
            hoverImage: p.imageUrls?.length > 1 ? p.imageUrls[1] : p.imageUrls?.[0] 
        })); 
    } catch (err) {
        console.error("Lỗi tải sản phẩm:", err);
    } finally {
        loading.value = false;
    }
};

// === HÀM THIẾU CẦN THÊM VÀO SCRIPT ===
const goToDetail = (id) => {
    router.push(`/product/${id}`);
};
// =====================================

const changeImage = (product, index) => {
    // Logic lật ảnh khi hover
    if (product.imageUrls.length > 1) {
        if (index === 1) {
            product.hoverImage = product.imageUrls[1];
        } else {
            product.hoverImage = product.imageUrls[0];
        }
    }
};

const formatPrice = (value) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

onMounted(() => {
    fetchProducts();
});
</script>

<style scoped>
.home-view {
  width: 100%;
  animation: fadeIn 0.8s ease;
  background: #fff;
  color: #111;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

/* ======================= */
/*       TOP NOTICE        */
/* ======================= */
.top-notice {
  width: 100%;
  background: #111;
  color: #f5f5f5;
  text-align: center;
  font-size: 0.8rem;
  letter-spacing: 1px;
  padding: 6px 10px;
  text-transform: uppercase;
}

/* ======================= */
/*       HERO SECTION      */
/* ======================= */
.hero-section {
  background-image: url('https://images.unsplash.com/photo-1523381210434-271e8be1f52b?q=80&w=1470&auto=format&fit=crop');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  height: 90vh;
  position: relative;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px 90px;
  overflow: hidden;
}

.hero-section::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0.15));
}

.marquee-bar {
  position: absolute;
  top: 110px;
  width: 100%;
  font-size: 1.1rem;
  font-weight: 900;
  opacity: 0.15;
  overflow: hidden;
  white-space: nowrap;
  z-index: 2;
}

.marquee-bar span {
  display: inline-block;
  padding-left: 100%;
  animation: marquee 16s linear infinite;
}

.hero-content {
  position: relative;
  z-index: 3;
  text-align: center;
  color: #fff;
  max-width: 780px;
  margin: 0 auto;
  animation: fadeUp 0.9s ease;
}

.hero-title {
  font-size: 3.6rem;
  font-weight: 900;
  letter-spacing: 3px;
  text-transform: uppercase;
  margin-bottom: 12px;
  text-shadow: 0 8px 25px rgba(0,0,0,0.7);
}

.hero-desc {
  font-size: 0.9rem;
  line-height: 1.7;
  opacity: 0.95;
  max-width: 520px;
  margin: 0 auto 24px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 11px 30px;
  border-radius: 999px;
  border: 2px solid #fff;
  font-size: 0.9rem;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  text-decoration: none;
  cursor: pointer;
  transition: 0.25s ease;
}

.hero-btn.primary {
  background: #fff;
  color: #000;
}

.hero-btn.primary:hover {
  background: #000;
  color: #fff;
}

.hero-btn.ghost {
  background: transparent;
  color: #fff;
}

.hero-btn.ghost:hover {
  background: rgba(255,255,255,0.1);
}

/* ======================= */
/*      FEATURE STRIP      */
/* ======================= */
.feature-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  padding: 24px 20px;
  max-width: 1100px;
  margin: -40px auto 0;
  position: relative;
  z-index: 4;
}

.feature-item {
  background: #fff;
  border-radius: 12px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
}

.feature-item i {
  font-size: 1.6rem;
}

.feature-item h4 {
  font-size: 0.9rem;
  font-weight: 800;
  text-transform: uppercase;
}

.feature-item p {
  font-size: 0.8rem;
  color: #666;
}

/* ======================= */
/*    PRODUCTS SECTION     */
/* ======================= */
.products-section {
  padding: 60px 20px 70px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-header h2 {
  font-size: 2.2rem;
  font-weight: 900;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.section-header p {
  font-size: 0.9rem;
  color: #777;
}

.loading-text {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

/* GRID */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 30px;
}

/* CARD */
.product-card {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 6px 24px rgba(0,0,0,0.08);
  transition: 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(0,0,0,0.12);
}

/* Badge NEW */
.badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: #111;
  color: #fff;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
  z-index: 3;
}

/* IMAGE BOX */
.product-img-box {
  height: 350px;
  position: relative;
  overflow: hidden;
  background: #f2f2f2;
}

.product-img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.product-card:hover .product-img-box img {
  transform: scale(1.06);
}

/* Overlay */
.overlay {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  padding-bottom: 22px;
  background: linear-gradient(to top, rgba(0,0,0,0.65), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .overlay {
  opacity: 1;
}

.quick-view-btn {
  background: #fff;
  border: none;
  padding: 9px 24px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  cursor: pointer;
  transition: 0.2s ease;
}

.quick-view-btn:hover {
  background: #000;
  color: #fff;
}

/* INFO */
.product-info {
  padding: 16px 18px 18px;
  text-align: center;
}

.product-name {
  font-size: 1rem;
  font-weight: 700;
  text-transform: uppercase;
  margin-bottom: 6px;
  color: #222;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  font-size: 0.9rem;
}

.price {
  color: #d0021b;
  font-weight: 900;
}

.variants-count {
  font-size: 0.8rem;
  color: #777;
}

/* Product actions */
.product-actions {
  margin-top: 6px;
}

.product-link {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 700;
  text-decoration: none;
  color: #111;
  padding-bottom: 2px;
  border-bottom: 1px solid #111;
}

.product-link:hover {
  color: #d0021b;
  border-color: #d0021b;
}

/* VIEW ALL BTN */
.view-all-container {
  text-align: center;
  margin-top: 45px;
}

.view-all-btn {
  display: inline-block;
  padding: 13px 40px;
  border-radius: 999px;
  border: 2px solid #111;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1px;
  text-decoration: none;
  color: #111;
  transition: 0.25s ease;
}

.view-all-btn:hover {
  background: #111;
  color: #fff;
  transform: translateY(-3px);
}

/* ======================= */
/*      ABOUT SECTION      */
/* ======================= */
.about-section {
  padding: 60px 20px 80px;
  background: #f5f5f5;
}

.about-container {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr);
  gap: 40px;
  align-items: center;
}

.about-text h3 {
  font-size: 1.4rem;
  font-weight: 900;
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 14px;
}

.about-text p {
  font-size: 0.9rem;
  color: #555;
  margin-bottom: 10px;
  line-height: 1.6;
}

.about-btn {
  display: inline-block;
  margin-top: 14px;
  padding: 10px 26px;
  border-radius: 999px;
  background: #111;
  color: #fff;
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: 0.25s ease;
}

.about-btn:hover {
  background: #000;
  transform: translateY(-2px);
}

/* LOOKBOOK BLOCKS */
.about-lookbook {
  position: relative;
  height: 260px;
}

.lookbook-main,
.lookbook-sub {
  position: absolute;
  background-size: cover;
  background-position: center;
  border-radius: 18px;
  box-shadow: 0 12px 30px rgba(0,0,0,0.15);
}

.lookbook-main {
  inset: 0;
  background-image: url('https://images.unsplash.com/photo-1514996937319-344454492b37?q=80&w=1470&auto=format&fit=crop');
}

.lookbook-sub.top {
  width: 50%;
  height: 46%;
  top: -18px;
  right: -18px;
  background-image: url('https://images.unsplash.com/photo-1512436991641-6745cdb1723f?q=80&w=1470&auto=format&fit=crop');
}

.lookbook-sub.bottom {
  width: 45%;
  height: 42%;
  bottom: -18px;
  left: -10px;
  background-image: url('https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?q=80&w=1470&auto=format&fit=crop');
}

/* ======================= */
/*       ANIMATIONS        */
/* ======================= */
@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(25px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes marquee {
  0%   { transform: translateX(0); }
  100% { transform: translateX(-100%); }
}

/* ======================= */
/*       RESPONSIVE        */
/* ======================= */
@media (max-width: 1024px) {
  .hero-section {
    height: 80vh;
  }
  .feature-strip {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    margin-top: -30px;
  }
}

@media (max-width: 768px) {
  .hero-section {
    height: 75vh;
    padding-bottom: 70px;
  }
  .hero-title {
    font-size: 2.6rem;
  }
  .hero-desc {
    font-size: 0.8rem;
  }
  .hero-actions {
    flex-direction: column;
  }

  .feature-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    margin-top: -20px;
  }

  .product-img-box {
    height: 260px;
  }

  .about-container {
    grid-template-columns: minmax(0, 1fr);
  }
  .about-lookbook {
    height: 220px;
    margin-top: 10px;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2.1rem;
  }
  .marquee-bar {
    font-size: 0.85rem;
  }
  .feature-strip {
    grid-template-columns: 1fr;
    margin-top: -10px;
  }
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
  }
  .product-img-box {
    height: 210px;
  }
  .product-name {
    font-size: 0.85rem;
  }
  .price {
    font-size: 0.9rem;
  }
}
</style>
