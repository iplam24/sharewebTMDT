<template>
  <div class="category-list-page">
    <div class="header">
      <h1>Quản lý Danh mục Sản phẩm</h1>
    </div>

    <div class="card add-form-card">
      <div class="card-body">
        <h5 class="card-title">Thêm Danh mục mới</h5>
        <form @submit.prevent="handleAddCategory" class="d-flex align-items-center">
          <input 
            type="text" 
            v-model="newCategoryName" 
            placeholder="Ví dụ: Áo Thun, Quần Jean, Mũ" 
            required 
            class="form-control me-2"
          >
          <button type="submit" class="btn btn-primary" :disabled="loadingAdd">
            <i v-if="loadingAdd" class="fa-solid fa-spinner fa-spin"></i>
            <i v-else class="fa-solid fa-plus"></i> 
          </button>
        </form>
        <div v-if="errorAdd" class="text-danger small mt-2">{{ errorAdd }}</div>
      </div>
    </div>

    <div class="card shadow mt-4">
      <div class="card-header">Danh sách ({{ categories.length }} mục)</div>
      <div v-if="loadingList" class="text-center py-4 text-muted">Đang tải...</div>
      
      <ul v-else class="list-group list-group-flush">
        <li v-for="category in categories" :key="category.id" class="list-group-item d-flex justify-content-between align-items-center">
          <div>
            <span class="category-name">{{ category.name }}</span>
            <span class="slug text-muted small ms-3">{{ category.slug }}</span>
          </div>
          <button @click="handleDeleteCategory(category.id)" class="btn btn-sm btn-outline-danger">
            <i class="fa-solid fa-trash"></i> Xóa
          </button>
        </li>
      </ul>
      <div v-if="categories.length === 0 && !loadingList" class="text-center p-4 text-muted">
          Chưa có danh mục nào được tạo.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import categoryService from '@/services/categoryService.js';

const categories = ref([]);
const newCategoryName = ref('');
const loadingAdd = ref(false);
const loadingList = ref(true);
const errorAdd = ref('');

// --- LOGIC FETCH & UPDATE ---

const fetchCategories = async () => {
    loadingList.value = true;
    try {
        const res = await categoryService.getAllCategories();
        categories.value = res.data;
    } catch (err) {
        console.error(err);
    } finally {
        loadingList.value = false;
    }
};

const handleAddCategory = async () => {
    loadingAdd.value = true;
    errorAdd.value = '';
    try {
        const payload = { name: newCategoryName.value };
        const res = await categoryService.createCategory(payload);
        
        categories.value.push(res.data); // Thêm vào list mà không cần tải lại toàn bộ
        newCategoryName.value = ''; // Reset input
    } catch (err) {
        errorAdd.value = err.response?.data?.message || 'Lỗi: Không thể thêm danh mục.';
    } finally {
        loadingAdd.value = false;
    }
};

const handleDeleteCategory = async (id) => {
    if (!confirm('Bạn có chắc chắn muốn xóa danh mục này? Nó sẽ bị lỗi nếu đang có sản phẩm sử dụng!')) return;
    
    try {
        await categoryService.deleteCategory(id);
        // Cập nhật list bằng cách lọc ra
        categories.value = categories.value.filter(c => c.id !== id);
        alert('Đã xóa thành công!');
    } catch (err) {
        alert(err.response?.data?.message || 'Lỗi xóa danh mục. Vui lòng kiểm tra sản phẩm đang dùng.');
    }
};

onMounted(() => {
    fetchCategories();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.category-list-page {
  max-width: 860px;
  margin: 0 auto;
  padding-top: 24px;
  font-family: "Inter", system-ui, sans-serif;
}

/* HEADER */
.header h1 {
  font-size: 1.9rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 20px;
  text-align: center;
}

/* ADD FORM CARD */
.add-form-card {
  border: none;
  border-radius: 14px;
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.08);
  overflow: hidden;
}

.add-form-card .card-body {
  padding: 22px;
}

.card-title {
  margin-bottom: 14px;
  font-size: 1.15rem;
  font-weight: 700;
  color: #1f2937;
}

/* INPUT */
.form-control {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 0.95rem;
  border: 1px solid #d1d5db;
  background: #f9fafb;
  transition: 0.2s;
}

.form-control:focus {
  background: #fff;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, .15);
}

/* ADD BUTTON */
.btn-primary {
  border-radius: 10px;
  padding: 10px 16px;
  font-weight: 600;
  background: #111827;
  border: none;
  transition: 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #000;
  transform: translateY(-1px);
}

/* --- CATEGORY LIST CARD --- */
.card.shadow {
  border-radius: 14px;
  border: none;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.card-header {
  background: #fff;
  padding: 16px 20px;
  font-weight: 700;
  font-size: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

/* LIST ITEMS */
.list-group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border: none;
  border-bottom: 1px solid #f1f5f9;
  font-size: 0.95rem;
  transition: background 0.15s;
}

.list-group-item:last-child {
  border-bottom: none;
}

.list-group-item:hover {
  background: #f9fafb;
}

.category-name {
  font-weight: 600;
  color: #111827;
}

.slug {
  font-size: 0.8rem;
  color: #6b7280;
}

/* DELETE BUTTON */
.btn-outline-danger {
  border: 1px solid #fecaca;
  color: #b91c1c;
  background: #fff0f0;
  font-size: 0.8rem;
  padding: 6px 12px;
  border-radius: 8px;
  font-weight: 600;
  transition: 0.2s;
}

.btn-outline-danger:hover {
  background: #b91c1c;
  color: white;
  border-color: #b91c1c;
  transform: translateY(-1px);
}

/* EMPTY TEXT */
.text-muted {
  font-size: 0.95rem;
}

/* RESPONSIVE */
@media (max-width: 600px) {
  .list-group-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .btn-outline-danger {
    align-self: flex-end;
  }

  .header h1 {
    font-size: 1.6rem;
  }
}
</style>
