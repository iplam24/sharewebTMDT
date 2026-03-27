<template>
  <div class="container-fluid mt-4">
    
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="h3 mb-0 text-gray-800">Quản lý Sản phẩm</h2>
      <RouterLink to="/admin/products/create" class="btn btn-primary">
        <i class="fa-solid fa-plus"></i> Thêm mới
      </RouterLink>
    </div>


    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary"></div>
      <p class="mt-2">Đang tải dữ liệu...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <!-- Table -->
    <div v-else class="card shadow mb-4">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-striped mb-0 align-middle">
            <thead class="table-light">
              <tr>
                <th>ID</th>
                <th style="min-width: 120px;">Hình ảnh</th>
                <th>Tên sản phẩm</th>
                <th>Giá bán</th>
                <th>Phân loại</th>
                <th style="min-width: 200px;">Chi tiết tồn kho</th>
                <th class="text-end">Hành động</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="product in products" :key="product.id">
                <td>#{{ product.id }}</td>

                <!-- Images -->
                <td>
                  <div class="d-flex gap-1 align-items-center">
                    <img 
                      v-for="(url, i) in product.imageUrls?.slice(0, 3)"
                      :key="i"
                      :src="url"
                      class="rounded border"
                      style="width: 40px; height: 40px; object-fit: cover;"
                    />
                    <span 
                      v-if="product.imageUrls?.length > 3"
                      class="badge bg-secondary rounded-pill" style="font-size: 0.7rem;"
                    >
                      +{{ product.imageUrls.length - 3 }}
                    </span>
                  </div>
                </td>

                <td class="fw-bold">{{ product.name }}</td>
                <td class="text-danger">{{ formatPrice(product.price) }}</td>

                <td><span class="badge bg-info text-dark">{{ product.category }}</span></td>

                <!-- Variants -->
                <td>
                  <div class="d-flex flex-wrap gap-1">
                    <span
                      v-for="(v, idx) in product.variants"
                      :key="idx"
                      class="badge border text-dark fw-normal"
                      :class="v.stockQuantity > 0 ? 'bg-light' : 'bg-danger-subtle'"
                    >
                      {{ v.color }} - {{ v.size }}
                      <strong :class="v.stockQuantity > 0 ? 'text-success' : 'text-danger'">
                        ({{ v.stockQuantity }})
                      </strong>
                    </span>

                    <span v-if="product.variants?.length === 0" class="text-muted small fst-italic">
                      Chưa có phân loại
                    </span>
                  </div>
                </td>

                <td class="text-end">
                  <RouterLink :to="`/admin/products/edit/${product.id}`" class="btn btn-sm btn-warning me-2">
                    <i class="fa-solid fa-pen"></i>
                  </RouterLink>
                  <button @click="handleDelete(product.id)" class="btn btn-sm btn-danger">
                    <i class="fa-solid fa-trash"></i>
                  </button>
                </td>
              </tr>
            </tbody>

          </table>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="card-footer d-flex justify-content-between align-items-center py-3">
        <span class="text-muted small">Trang {{ currentPage }} / {{ totalPages }}</span>

        <nav>
          <ul class="pagination mb-0">
            
            <!-- Prev -->
            <li class="page-item" :class="{ disabled: currentPage <= 1 }">
              <button class="page-link" @click="changePage(currentPage - 1)">Trước</button>
            </li>

            <!-- Page numbers -->
            <li
              v-for="page in totalPages"
              :key="page"
              class="page-item"
              :class="{ active: page === currentPage }"
            >
              <button class="page-link" @click="changePage(page)">{{ page }}</button>
            </li>

            <!-- Next -->
            <li class="page-item" :class="{ disabled: currentPage >= totalPages }">
              <button class="page-link" @click="changePage(currentPage + 1)">Sau</button>
            </li>

          </ul>
        </nav>
      </div>

    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import productService from '@/services/productService.js';

const products = ref([]);
const loading = ref(true);
const error = ref(null);

const currentPage = ref(1);   // FE hiển thị 1,2,3,...
const totalPages = ref(1);
const pageSize = 10;

// Gọi API lấy danh sách
const fetchProducts = async () => {
  loading.value = true;
  error.value = null;

  try {
    const params = {
      page: currentPage.value - 1,  // FE (1) → BE (0)
      size: pageSize
    };

    const res = await productService.getAllProducts(params);

    products.value   = res.data.content;
    totalPages.value = res.data.totalPages;

    // 🔥 BE đã trả currentPage = 1,2,3... (theo JSON anh gửi)
    // nên gán thẳng, KHÔNG cộng thêm
    currentPage.value = res.data.currentPage;

  } catch (err) {
    console.error(err);
    error.value = 'Không thể tải danh sách sản phẩm.';
  } finally {
    loading.value = false;
  }
};

const changePage = (pageNumber) => {
  if (pageNumber >= 1 && pageNumber <= totalPages.value) {
    currentPage.value = pageNumber;
    fetchProducts();
  }
};

const handleDelete = async (id) => {
  if (!confirm('Xác nhận xóa sản phẩm?')) return;

  try {
    await productService.deleteProduct(id);

    // Nếu xóa xong còn 0 sản phẩm ở trang hiện tại và không phải trang 1 → lùi 1 trang
    if (products.value.length === 1 && currentPage.value > 1) {
      currentPage.value -= 1;
    }

    fetchProducts();

  } catch (err) {
    console.error(err);
    alert('Xóa thất bại: ' + (err.response?.data?.message || err.message));
  }
};

const formatPrice = (value) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);

onMounted(fetchProducts);
</script>


<style scoped>
.page-link { color: #000; }
.page-item.active .page-link {
  background-color: #007bff;
  color: white;
}
.table td, .table th {
  padding: 12px;
}
</style>


