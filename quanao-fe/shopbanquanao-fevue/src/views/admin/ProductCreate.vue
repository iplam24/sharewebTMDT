<template>
  <div class="product-create-page">
    <div class="header">
      <h1>Tạo sản phẩm mới</h1>
      <RouterLink to="/admin/products" class="back-button">&larr; Quay lại</RouterLink>
    </div>

    <div v-if="error" class="error-message">{{ error }}</div>

    <form @submit.prevent="handleSubmit" class="create-form">
      <div class="form-column">
        
        <div class="form-group">
          <label>Tên sản phẩm</label>
          <input type="text" v-model="product.name" :class="{ 'input-error': validationErrors.name }" />
          <p v-if="validationErrors.name" class="error-inline">{{ validationErrors.name }}</p>
        </div>
        
        <div class="form-group">
          <label>Giá bán (VNĐ)</label>
          <input type="number" v-model.number="product.price" :class="{ 'input-error': validationErrors.price }" />
          <p v-if="validationErrors.price" class="error-inline">{{ validationErrors.price }}</p>
        </div>
        
        <div class="form-group">
          <label for="category">Phân loại</label>
          <select 
            id="category" 
            v-model="product.categoryId" 
            class="form-control" 
            :class="{ 'input-error': validationErrors.categoryId }"
          >
            <option :value="null">Chọn danh mục...</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
          <p v-if="validationErrors.categoryId" class="error-inline">{{ validationErrors.categoryId }}</p>
        </div>
        
        <div class="form-group">
          <label>Mô tả</label>
          <textarea v-model="product.description" rows="4" :class="{ 'input-error': validationErrors.description }"></textarea>
          <p v-if="validationErrors.description" class="error-inline">{{ validationErrors.description }}</p>
        </div>
      </div>

      <div class="form-column">
        <div class="variants-section">
          <div class="variants-header">
            <label>Phân loại hàng (Màu sắc & Kích thước)</label>
            <button type="button" @click="addVariant" class="add-variant-btn">+ Thêm loại</button>
          </div>
          
          <p v-if="validationErrors.variants" class="error-inline variants-error-message">{{ validationErrors.variants }}</p>

          <div class="variants-list">
            <div v-for="(variant, index) in product.variants" :key="index" class="variant-item">
              <input type="text" v-model="variant.color" placeholder="Màu (VD: Đỏ)" 
                     :class="{ 'input-error': validationErrors[`variant_${index}_color`] }">
              <input type="text" v-model="variant.size" placeholder="Size (VD: XL)" 
                     :class="{ 'input-error': validationErrors[`variant_${index}_size`] }">
              <input type="number" v-model.number="variant.stockQuantity" placeholder="Số lượng" 
                     :class="{ 'input-error': validationErrors[`variant_${index}_stockQuantity`] }">
              <button type="button" @click="removeVariant(index)" class="remove-btn" v-if="product.variants.length > 1">Xóa</button>
            </div>
          </div>
        </div>

        <div class="form-group" style="margin-top: 20px;">
          <label>Hình ảnh</label>
          <input type="file" @change="handleFileChange" multiple accept="image/*" class="file-input"
                 :class="{ 'input-error': validationErrors.images }" />
          <p v-if="validationErrors.images" class="error-inline">{{ validationErrors.images }}</p>
        </div>
        <div v-if="imagePreviews.length > 0" class="image-previews">
          <img v-for="(src, index) in imagePreviews" :key="index" :src="src" class="preview-img" />
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" :disabled="loading" class="submit-button">
          <span v-if="loading">Đang xử lý...</span>
          <span v-else>Lưu sản phẩm</span>
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, RouterLink } from 'vue-router';
import productService from '@/services/productService.js';
import fileUploadService from '@/services/fileUploadService.js';
import categoryService from '@/services/categoryService.js'; 
import apiClient from '@/services/api.js';


const router = useRouter();
const loading = ref(false);
const error = ref(null);
const categories = ref([]); 

const product = ref({
  name: '',
  price: 0,
  categoryId: null, 
  description: '',
  variants: [
    { color: '', size: '', stockQuantity: 0 }
  ]
});

// STATE MỚI: Lỗi validation cụ thể cho từng trường
const validationErrors = ref({});

const selectedFiles = ref(null);
const imagePreviews = ref([]);

// --- LOGIC VALIDATION ---
const validateForm = () => {
    // Reset lỗi
    validationErrors.value = {}; 
    let isValid = true;

    // 1. Tên sản phẩm
    if (!product.value.name || product.value.name.length < 5) {
        validationErrors.value.name = 'Tên sản phẩm phải có ít nhất 5 ký tự.';
        isValid = false;
    }

    // 2. Giá bán
    if (!product.value.price || product.value.price <= 0) {
        validationErrors.value.price = 'Giá bán phải lớn hơn 0 VNĐ.';
        isValid = false;
    }
    
    // 3. Danh mục
    if (!product.value.categoryId) {
        validationErrors.value.categoryId = 'Vui lòng chọn danh mục.';
        isValid = false;
    }

    // 4. Mô tả
    if (!product.value.description || product.value.description.length < 1) {
        validationErrors.value.description = 'Mô tả cần chi tiết hơn (tối thiểu 1 ký tự).';
        isValid = false;
    }
    
    // 5. Hình ảnh
    if (!selectedFiles.value || selectedFiles.value.length === 0) {
        validationErrors.value.images = 'Vui lòng chọn ít nhất một hình ảnh.';
        isValid = false;
    }

    // 6. Variants
    let hasVariantError = false;
    product.value.variants.forEach((variant, index) => {
        if (!variant.color || !variant.size || variant.stockQuantity <= 0) {
            hasVariantError = true;
            // Gán lỗi cụ thể cho từng trường
            if (!variant.color) validationErrors.value[`variant_${index}_color`] = 'Thiếu màu';
            if (!variant.size) validationErrors.value[`variant_${index}_size`] = 'Thiếu size';
            if (!variant.stockQuantity || variant.stockQuantity <= 0) validationErrors.value[`variant_${index}_stockQuantity`] = 'SL > 0';
        }
    });

    if (product.value.variants.length === 0 || hasVariantError) {
      if (product.value.variants.length === 0) {
        validationErrors.value.variants = 'Cần ít nhất một phân loại.';
      } else if (hasVariantError) {
        validationErrors.value.variants = 'Vui lòng điền đủ thông tin Phân loại.';
      }
      isValid = false;
    }

    return isValid;
};

// --- LOGIC LẤY DANH MỤC (Giữ nguyên) ---
const fetchCategories = async () => {
    try {
        const res = await categoryService.getAllCategories();
        categories.value = res.data;
        if (categories.value.length > 0) {
             // Đặt null để bắt buộc người dùng chọn lại
            product.value.categoryId = null; 
        }
    } catch (err) {
        error.value = 'Lỗi tải danh mục: Vui lòng kiểm tra API.';
    }
};

// --- LOGIC FORM (Giữ nguyên) ---
const addVariant = () => {
  product.value.variants.push({ color: '', size: '', stockQuantity: 0 });
};

const removeVariant = (index) => {
  product.value.variants.splice(index, 1);
};

const handleFileChange = (event) => {
  selectedFiles.value = event.target.files;
  imagePreviews.value = [];
  validationErrors.value.images = null; // Xóa lỗi ảnh khi có file mới
  if (selectedFiles.value) {
    for (const file of selectedFiles.value) {
      const reader = new FileReader();
      reader.onload = (e) => imagePreviews.value.push(e.target.result);
      reader.readAsDataURL(file);
    }
  }
};

// ProductCreate.vue – sửa lại đúng 100%
const handleSubmit = async () => {
  loading.value = true;

  try {
    // BƯỚC 1: Upload ảnh trước → lấy URLs (đã có CORS + permitAll)
    let imageUrls = [];
    if (selectedFiles.value.length > 0) {
      const uploadRes = await fileUploadService.uploadFiles(selectedFiles.value);
      imageUrls = uploadRes.data.urls; // ← API này CHẠY NGON, KHÔNG BỊ CORS
    }

    // BƯỚC 2: Gửi thông tin sản phẩm + imageUrls → gọi API /api/products (text/json)
    const payload = {
      ...product.value,
      imageUrls: imageUrls
    };

    await apiClient.post('/api/products', payload, {
      headers: { 'Content-Type': 'application/json' } // ← json, không multipart
    });

    alert('Thêm sản phẩm thành công!');
    router.push('/admin/products');
  } catch (err) {
    console.error(err);
    alert('Lỗi: ' + (err.response?.data?.message || err.message));
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
    fetchCategories();
});
</script>

<style scoped>
/* Giữ nguyên các styles khác */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
/* ================================================= */
/* SỬA ĐỔI CSS CHO INLINE ERROR          */
/* ================================================= */

/* ERROR TEXT STYLES */
.error-inline {
    display: block;
    color: #ef4444; /* Màu đỏ */
    font-size: 0.8rem;
    margin-top: 4px;
    font-weight: 500;
}

.variants-error-message {
    margin-bottom: 10px;
}

/* ERROR INPUT STYLES */
.input-error {
    border-color: #ef4444 !important;
    background-color: #fef2f2 !important; /* Màu nền đỏ nhạt */
}

.input-error:focus {
    box-shadow: 0 0 0 1px rgba(239, 68, 68, 0.5) !important;
}



.product-create-page {
  font-family: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  padding: 24px 24px 40px;
}

/* HEADER */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h1 {
  font-size: 1.7rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.back-button {
  text-decoration: none;
  font-size: 0.9rem;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  color: #4b5563;
  background: #fff;
  transition: all 0.2s ease;
}

.back-button:hover {
  background: #111827;
  color: #fff;
  border-color: #111827;
}

/* ERROR */
.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  padding: 10px 14px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 0.9rem;
}

/* FORM LAYOUT */
.create-form {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr);
  gap: 24px;
  background: #f9fafb;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

/* COLUMN CARD */
.form-column {
  background: #ffffff;
  border-radius: 14px;
  padding: 18px 18px 20px;
  border: 1px solid #e5e7eb;
}

/* FORM GROUP */
.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  padding: 9px 11px;
  font-size: 0.92rem;
  outline: none;
  background: #f9fafb;
  transition: all 0.18s ease;
}

.form-group textarea {
  resize: vertical;
  min-height: 96px;
}

.form-group input:focus:not(.input-error),
.form-group textarea:focus:not(.input-error),
.form-group select:focus:not(.input-error) {
  border-color: #2563eb;
  background: #fff;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.1);
}

/* VARIANTS SECTION */
.variants-section {
  background: #f9fafb;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 14px 14px 10px;
}

.variants-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.variants-header label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #111827;
}

.add-variant-btn {
  border: none;
  padding: 6px 12px;
  font-size: 0.8rem;
  border-radius: 999px;
  background: #111827;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.add-variant-btn:hover {
  background: #000;
}

/* Variant item row */
.variants-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 260px;
  overflow-y: auto;
  padding-right: 4px;
}

.variant-item {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr 0.8fr auto;
  gap: 8px;
  align-items: center;
}

.variant-item input {
  border-radius: 8px;
  border: 1px solid #d1d5db;
  padding: 7px 10px;
  font-size: 0.9rem;
  background: #fff;
}

.variant-item input:focus:not(.input-error) {
  border-color: #2563eb;
}

.remove-btn {
  border: none;
  padding: 6px 10px;
  font-size: 0.78rem;
  border-radius: 999px;
  background: #fee2e2;
  color: #b91c1c;
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.18s ease;
}

.remove-btn:hover {
  background: #b91c1c;
  color: #fff;
}

/* FILE INPUT + PREVIEW */
.file-input {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px dashed #cbd5f5;
  width: 100%;
  background: #f9fafb;
  font-size: 0.9rem;
  cursor: pointer;
}

.file-input.input-error {
  border-color: #ef4444 !important;
  border-style: solid;
}

.file-input:hover {
  border-color: #6366f1;
}

.image-previews {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.preview-img {
  width: 78px;
  height: 78px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  object-fit: cover;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.08);
}

/* FORM ACTIONS */
.form-actions {
  grid-column: 1 / -1;
  margin-top: 10px;
}

.submit-button {
  width: 100%;
  padding: 12px;
  border-radius: 999px;
  border: none;
  font-size: 0.98rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  background: #111827;
  color: #f9fafb;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-button:hover:not(:disabled) {
  background: #000000;
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.2);
}

.submit-button:disabled {
  background: #d1d5db;
  cursor: not-allowed;
  box-shadow: none;
}

/* RESPONSIVE */
@media (max-width: 900px) {
  .create-form {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .product-create-page {
    padding: 16px;
  }

  .header h1 {
    font-size: 1.4rem;
  }

  .form-column {
    padding: 14px;
  }

  /* Thay đổi bố cục Variants trên mobile */
  .variant-item {
    grid-template-columns: 1fr 1fr; /* 2 cột chính */
    grid-auto-rows: auto;
    row-gap: 6px;
  }

  /* Đặt Số lượng và nút Xóa trên cùng một hàng */
  .variant-item input:nth-child(3) { /* Số lượng */
    grid-column: 1 / 2;
  }

  .remove-btn {
    grid-column: 2 / 3;
    justify-self: end;
    margin-left: auto;
    width: fit-content;
  }
  
  /* Căn chỉnh lại input màu và size */
  .variant-item input:nth-child(1) { grid-column: 1 / 3; } /* Màu */
  .variant-item input:nth-child(2) { grid-column: 1 / 3; } /* Size */
}
</style>