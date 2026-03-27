<template>
  <div class="product-edit-page">
    <div class="header">
      <h1><i class="fa-solid fa-pen-to-square"></i> Sửa sản phẩm #{{ productId }}</h1>
      <RouterLink to="/admin/products" class="back-button">
        <i class="fa-solid fa-arrow-left"></i> Quay lại
      </RouterLink>
    </div>

    <div v-if="loadingData" class="loading-state">
      <i class="fa-solid fa-spinner fa-spin"></i> Đang tải dữ liệu...
    </div>

    <div v-else>
      <form @submit.prevent="handleUpdate" class="create-form">
        
        <div class="form-column">
          <div class="form-group">
            <label>Tên sản phẩm</label>
            <input type="text" v-model="product.name" required placeholder="Nhập tên sản phẩm..." />
          </div>
          
          <div class="form-group">
            <label>Giá bán (VNĐ)</label>
            <input type="number" v-model.number="product.price" required placeholder="0" />
          </div>
          
          <div class="form-group">
            <label>Phân loại (Tên hiển thị)</label>
            <input type="text" v-model="product.category" required placeholder="Ví dụ: Áo thun, Quần Jean" />
          </div>

          <input type="hidden" v-model.number="product.categoryId" />
          
          <div class="form-group">
            <label>Mô tả chi tiết</label>
            <textarea v-model="product.description" rows="5" placeholder="Mô tả sản phẩm..."></textarea>
          </div>
        </div>

        <div class="form-column">
          
          <div class="variants-section">
            <div class="variants-header">
              <label><i class="fa-solid fa-layer-group"></i> Phân loại hàng (Size/Màu)</label>
              
              <button type="button" @click="addVariant" class="btn-icon btn-add" title="Thêm loại mới">
                <i class="fa-solid fa-plus"></i>
              </button>
            </div>
            
            <div class="variants-list">
              <div v-for="(variant, index) in product.variants" :key="index" class="variant-item">
                <span class="variant-index">#{{ index + 1 }}</span>
                
                <input type="text" v-model="variant.color" placeholder="Màu (Đỏ...)" required>
                <input type="text" v-model="variant.size" placeholder="Size (M...)" required>
                <input type="number" v-model.number="variant.stockQuantity" placeholder="Tồn kho" required>
                
                <button type="button" @click="removeVariant(index)" class="btn-icon btn-remove" title="Xóa dòng này">
                  <i class="fa-solid fa-trash"></i>
                </button>
              </div>
            </div>
          </div>

          <div class="image-section">
            <div class="form-group">
              <label><i class="fa-solid fa-image"></i> Thay đổi hình ảnh</label>
              <span class="hint">(Để trống nếu muốn giữ ảnh cũ)</span>
              <input type="file" @change="handleFileChange" multiple accept="image/*" class="file-input"/>
            </div>
            
            <div v-if="product.imageUrls && product.imageUrls.length > 0 && !newFiles" class="image-previews">
              <p class="preview-label">Ảnh hiện tại:</p>
              <div class="preview-grid">
                <img v-for="(url, idx) in product.imageUrls" :key="idx" :src="url" class="preview-img" />
              </div>
            </div>
            
            <div v-if="newImagePreviews.length > 0" class="image-previews new-images">
               <p class="preview-label">Ảnh mới sẽ thay thế:</p>
               <div class="preview-grid">
                 <img v-for="(src, idx) in newImagePreviews" :key="idx" :src="src" class="preview-img" />
               </div>
            </div>
          </div>

        </div>

        <div class="form-actions">
          <button type="submit" :disabled="submitting" class="submit-button">
            <i v-if="submitting" class="fa-solid fa-spinner fa-spin"></i>
            {{ submitting ? ' Đang lưu...' : 'Cập nhật sản phẩm' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import productService from '@/services/productService.js';
import fileUploadService from '@/services/fileUploadService.js';

const route = useRoute();
const router = useRouter();
const productId = route.params.id; // Lấy ID từ URL

const product = ref({});
const loadingData = ref(true);
const submitting = ref(false);

const newFiles = ref(null);
const newImagePreviews = ref([]);

// 1. Tải dữ liệu khi vào trang
onMounted(async () => {
  try {
    const res = await productService.getProductById(productId);
    product.value = res.data;
    // Backend trả về variants, nếu rỗng thì thêm 1 dòng trống
    if (!product.value.variants || product.value.variants.length === 0) {
      product.value.variants = [{ color: '', size: '', stockQuantity: 0 }];
    }
  } catch (err) {
    alert('Không thể tải sản phẩm');
    console.error(err);
  } finally {
    loadingData.value = false;
  }
});

// Logic Biến thể
const addVariant = () => product.value.variants.push({ color: '', size: '', stockQuantity: 0 });
const removeVariant = (index) => product.value.variants.splice(index, 1);

// Logic chọn ảnh mới
const handleFileChange = (event) => {
  newFiles.value = event.target.files;
  newImagePreviews.value = [];
  if (newFiles.value) {
    for (const file of newFiles.value) {
      const reader = new FileReader();
      reader.onload = (e) => newImagePreviews.value.push(e.target.result);
      reader.readAsDataURL(file);
    }
  }
};

// Logic Update
const handleUpdate = async () => {
  submitting.value = true;
  try {
    let finalImageUrls = product.value.imageUrls; // Mặc định dùng ảnh cũ

    // Nếu có chọn file mới -> Upload và lấy URL mới
    if (newFiles.value && newFiles.value.length > 0) {
      const uploadRes = await fileUploadService.uploadFiles(newFiles.value);
      finalImageUrls = uploadRes.data.urls;
    }

    // Chuẩn bị dữ liệu gửi đi (categoryId được lấy từ trường ẩn đã load)
    const payload = {
  name: product.value.name,
  description: product.value.description,
  price: product.value.price,
  categoryId: product.value.categoryId,  
  imageUrls: finalImageUrls,
  variants: product.value.variants
};

    await productService.updateProduct(productId, payload);
    alert('Cập nhật thành công!');
    router.push('/admin/products');

  } catch (err) {
    console.error(err);
    alert('Cập nhật thất bại: ' + err.message);
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.product-edit-page {
  font-family: 'Segoe UI', sans-serif;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.header h1 { font-size: 1.5rem; color: #333; }

.back-button {
  background: #6c757d;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.9rem;
  transition: background 0.2s;
}
.back-button:hover { background: #5a6268; }

.loading-state { text-align: center; font-size: 1.2rem; color: #666; margin-top: 50px; }

/* LAYOUT FORM */
.create-form { display: flex; gap: 2rem; flex-wrap: wrap; }
.form-column { flex: 1; min-width: 350px; }

.form-group { margin-bottom: 1.5rem; }
.form-group label { display: block; font-weight: 600; margin-bottom: 0.5rem; color: #444; }
.form-group input[type="text"],
.form-group input[type="number"],
.form-group textarea { 
  width: 100%; 
  padding: 10px; 
  border: 1px solid #ced4da; 
  border-radius: 6px; 
  font-size: 0.95rem;
  transition: border-color 0.2s;
}
.form-group input:focus, .form-group textarea:focus { border-color: #007bff; outline: none; }

/* --- CSS BIẾN THỂ (ĐÃ SỬA ĐẸP) --- */
.variants-section { 
  background: #ffffff; 
  padding: 20px; 
  border-radius: 8px; 
  border: 1px solid #e0e0e0; 
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

.variants-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.variants-header label { margin: 0; font-size: 1rem; display: flex; align-items: center; gap: 8px; }

.variant-item { 
  display: flex; 
  gap: 10px; 
  margin-bottom: 10px; 
  align-items: center; 
}

.variant-index { font-size: 0.8rem; color: #888; width: 25px; text-align: center; }

.variant-item input { 
  flex: 1; 
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

/* Nút Icon Chung */
.btn-icon {
  width: 32px; height: 32px;
  border: none; border-radius: 4px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.2s;
}

/* Nút Thêm (Xanh) */
.btn-add { background-color: #e7f1ff; color: #007bff; }
.btn-add:hover { background-color: #007bff; color: white; }

/* Nút Xóa (Đỏ) */
.btn-remove { background-color: #fff0f1; color: #dc3545; }
.btn-remove:hover { background-color: #dc3545; color: white; }

/* --- CSS ẢNH --- */
.image-section {
  background: #f9f9f9; padding: 20px; border-radius: 8px; border: 1px dashed #ccc;
}
.hint { font-size: 0.8rem; color: #666; font-weight: normal; margin-left: 5px; }
.file-input { background: white; padding: 5px; }

.image-previews { margin-top: 15px; }
.preview-label { font-size: 0.9rem; font-weight: 600; margin-bottom: 8px; color: #555; }
.preview-grid { display: flex; gap: 10px; flex-wrap: wrap; }
.preview-img { width: 70px; height: 70px; object-fit: cover; border-radius: 4px; border: 1px solid #ddd; }
.new-images { margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee; }

/* --- FORM ACTIONS --- */
.form-actions {
  width: 100%;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  text-align: right;
}

.submit-button {
  background-color: #ffc107; /* Màu vàng cho Edit */
  color: #212529;
  padding: 12px 30px;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
  display: inline-flex; align-items: center; gap: 8px;
}
.submit-button:hover { background-color: #e0a800; }
.submit-button:disabled { background-color: #ffeeba; cursor: not-allowed; }
</style>