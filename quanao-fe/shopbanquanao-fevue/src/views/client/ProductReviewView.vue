<!-- src/views/client/ProductReviewView.vue -->
<template>
  <div class="review-page container" v-if="product">
    <div class="review-layout">
      <!-- BÊN TRÁI: INFO SẢN PHẨM -->
      <div class="product-box">
        <h2>ĐÁNH GIÁ SẢN PHẨM</h2>

        <div class="product-info">
          <img
            :src="product.imageUrls?.[0] || 'https://via.placeholder.com/120x150?text=No+Image'"
            alt="Product"
            class="product-img"
          />
          <div class="product-meta">
            <h3>{{ product.name }}</h3>
            <p class="cat">{{ product.category }}</p>
            <p class="price">{{ formatPrice(product.price) }}</p>
          </div>
        </div>

        <button class="btn-back" @click="goBackToProduct">
          <i class="fa-solid fa-arrow-left"></i> Xem lại sản phẩm
        </button>
      </div>

      <!-- BÊN PHẢI: FORM ĐÁNH GIÁ -->
      <div class="form-box">
        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
        <div v-if="successMsg" class="success-msg">{{ successMsg }}</div>

        <!-- Đánh giá sao -->
        <div class="field">
          <label>ĐÁNH GIÁ CỦA BẠN:</label>
          <div class="stars">
            <i
              v-for="n in 5"
              :key="n"
              class="fa-star"
              :class="n <= (hoverRating || rating) ? 'fa-solid active' : 'fa-regular'"
              @mouseover="hoverRating = n"
              @mouseleave="hoverRating = 0"
              @click="rating = n"
            ></i>
            <span v-if="rating" class="rating-text">{{ rating }}/5 sao</span>
          </div>
        </div>

        <!-- Nhận xét -->
        <div class="field">
          <label>NHẬN XÉT:</label>
          <textarea
            v-model="comment"
            rows="5"
            placeholder="Chia sẻ cảm nhận của bạn về chất lượng, form dáng, chất vải..."
          ></textarea>
        </div>

        <!-- Upload ảnh -->
        <div class="field">
          <label>HÌNH ẢNH (tuỳ chọn, tối đa 5):</label>
          <input
            type="file"
            multiple
            accept="image/*"
            @change="handleFilesChange"
            ref="fileInput"
          />

          <div v-if="imagePreviews.length" class="preview-list">
            <div
              v-for="(preview, index) in imagePreviews"
              :key="index"
              class="preview-item"
            >
              <img :src="preview" alt="preview" />
              <button class="btn-remove" @click="removeImage(index)" type="button">×</button>
            </div>
          </div>
        </div>

        <!-- Nút gửi -->
        <button
          class="btn-submit"
          :disabled="isSubmitting || rating === 0"
          @click="submitReview"
        >
          {{ isSubmitting ? 'Đang gửi...' : 'GỬI ĐÁNH GIÁ' }}
        </button>
      </div>
    </div>
  </div>

  <!-- Hiển thị lỗi nếu load thất bại -->
  <div v-else-if="errorMsg" class="loading" style="color: #dc2626;">
    <i class="fa-solid fa-circle-exclamation"></i> {{ errorMsg }}
  </div>

  <!-- Loading -->
  <div v-else class="loading">
    <i class="fa-solid fa-spinner fa-spin"></i> Đang tải sản phẩm...
  </div>
</template>

<script>
import apiClient from '@/services/api.js'; 
import reviewService from '@/services/reviewService.js';

export default {
  data() {
    return {
      product: null,
      rating: 0,
      hoverRating: 0,
      comment: '',
      images: [],           // File objects
      imagePreviews: [],    // Data URLs
      errorMsg: '',
      successMsg: '',
      isSubmitting: false,
    };
  },

  async mounted() {
    await this.loadProduct();
  },

  methods: {
    // Load sản phẩm từ API
    async loadProduct() {
      const productId = this.$route.params.id;
      try {
        const response = await apiClient.get(`/api/products/${productId}`);
        this.product = response.data;
      } catch (err) {
        this.errorMsg = 'Không tải được sản phẩm. Vui lòng thử lại.';
        console.error(err);
      }
    },

    // Format giá
    formatPrice(price) {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price);
    },

    // Quay lại trang chi tiết
    goBackToProduct() {
      this.$router.push({
        name: 'ProductDetail',
        params: { id: this.$route.params.id }
      });
    },

    // Xử lý chọn file
    async handleFilesChange(e) {
      const files = Array.from(e.target.files);
      const validFiles = files.filter(f => f.type.startsWith('image/'));
      const total = this.images.length + validFiles.length;

      if (total > 5) {
        this.errorMsg = 'Chỉ được upload tối đa 5 ảnh!';
        return;
      }

      this.errorMsg = '';
      const previews = await Promise.all(
        validFiles.map(file => this.readFileAsDataURL(file))
      );

      this.images.push(...validFiles);
      this.imagePreviews.push(...previews);
    },

    // Đọc file → DataURL
    readFileAsDataURL(file) {
      return new Promise((resolve) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.readAsDataURL(file);
      });
    },

    // Xóa ảnh
    removeImage(index) {
      this.images.splice(index, 1);
      this.imagePreviews.splice(index, 1);
    },

    // Gửi đánh giá
    async submitReview() {
      if (this.rating === 0) {
        this.errorMsg = 'Vui lòng chọn số sao!';
        return;
      }

      this.errorMsg = '';
      this.isSubmitting = true;

      try {
        await reviewService.createReview({
          productId: this.$route.params.id,
          rating: this.rating,
          comment: this.comment,
          images: this.images,
        });

        this.successMsg = 'Cảm ơn bạn! Đánh giá đã được gửi thành công.';
        
        // Reset form sau 2s
        setTimeout(() => {
          this.resetForm();
          this.goBackToProduct();
        }, 2000);

      } catch (err) {
        const msg = err.response?.data?.message || 'Gửi đánh giá thất bại. Vui lòng thử lại.';
        this.errorMsg = msg;
      } finally {
        this.isSubmitting = false;
      }
    },

    // Reset form
    resetForm() {
      this.rating = 0;
      this.comment = '';
      this.images = [];
      this.imagePreviews = [];
      this.$refs.fileInput.value = '';
    },
  },
};
</script>

<style scoped>
/* === LAYOUT === */
.review-page {
  padding: 110px 20px 80px;
  max-width: 1100px;
  margin: 0 auto;
  font-family: "Inter", sans-serif;
}

.review-layout {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 28px;
}

/* === PRODUCT BOX === */
.product-box {
  background: #fafafa;
  border-radius: 14px;
  padding: 24px 20px;
  border: 1px solid #e5e7eb;
}

.product-box h2 {
  font-family: "Oswald", sans-serif;
  font-size: 1.6rem;
  margin-bottom: 18px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.product-info {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 18px;
}

.product-img {
  width: 90px;
  height: 110px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}

.product-meta h3 {
  font-size: 1rem;
  font-weight: 700;
  margin: 0;
}

.product-meta .cat {
  font-size: .85rem;
  color: #6b7280;
  margin: 4px 0;
}

.product-meta .price {
  font-weight: 800;
  color: #d0021b;
  font-size: 1.1rem;
}

.btn-back {
  margin-top: 10px;
  background: #111827;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: 0.2s;
}

.btn-back:hover {
  background: #000;
}

/* === FORM BOX === */
.form-box {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  padding: 24px 22px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.04);
}

.field {
  margin-bottom: 20px;
}

.field label {
  display: block;
  font-weight: 700;
  font-size: .9rem;
  margin-bottom: 8px;
  color: #111827;
}

/* === STARS === */
.stars {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stars .fa-star {
  font-size: 1.6rem;
  cursor: pointer;
  transition: 0.15s;
  color: #d1d5db;
}

.stars .fa-star.active {
  color: #f59e0b;
  transform: translateY(-1px);
}

.rating-text {
  font-size: .9rem;
  color: #374151;
  margin-left: 6px;
}

/* === TEXTAREA === */
textarea {
  width: 100%;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  padding: 10px 12px;
  font-family: inherit;
  resize: vertical;
  font-size: 0.95rem;
}

textarea:focus {
  outline: none;
  border-color: #111827;
  box-shadow: 0 0 0 2px rgba(17,24,39,0.1);
}

/* === UPLOAD & PREVIEW === */
input[type="file"] {
  width: 100%;
  padding: 8px 0;
  font-size: 0.9rem;
}

.preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.preview-item {
  position: relative;
  width: 80px;
  height: 80px;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.btn-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  border: none;
  background: #ef4444;
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* === SUBMIT BUTTON === */
.btn-submit {
  width: 100%;
  margin-top: 10px;
  background: #111827;
  color: white;
  border: none;
  border-radius: 999px;
  padding: 12px 20px;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.2s;
  text-transform: uppercase;
  font-size: 0.9rem;
}

.btn-submit:hover:not(:disabled) {
  background: #000;
  transform: translateY(-1px);
}

.btn-submit:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
}

/* === MESSAGES === */
.error-msg, .success-msg {
  font-size: 0.9rem;
  margin-bottom: 12px;
  padding: 8px 12px;
  border-radius: 6px;
}

.error-msg {
  color: #dc2626;
  background: #fee2e2;
  border: 1px solid #fecaca;
}

.success-msg {
  color: #16a34a;
  background: #dcfce7;
  border: 1px solid #bbf7d0;
}

/* === LOADING === */
.loading {
  padding: 120px 20px;
  text-align: center;
  font-size: 1.1rem;
  color: #6b7280;
}

.loading i {
  margin-right: 8px;
}

/* === RESPONSIVE === */
@media (max-width: 900px) {
  .review-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .review-page {
    padding: 90px 16px 70px;
  }
  .form-box, .product-box {
    padding: 18px 16px;
  }
  .product-img {
    width: 70px;
    height: 90px;
  }
}
</style>