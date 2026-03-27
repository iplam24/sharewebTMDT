<template>
  <div class="user-list-page container-fluid mt-4">
    
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="h3 mb-0 text-gray-800">Quản lý Người dùng</h2>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2">Đang tải danh sách người dùng...</p>
    </div>

    <div v-else-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <div v-else class="card shadow mb-4">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-striped mb-0 align-middle">
            <thead class="table-light">
              <tr>
                <th>ID</th>
                <th>Tên người dùng</th>
                <th>Email</th>
                <th>Ngày tạo</th>
                <th>Vai trò (Role)</th>
                <th class="text-end">Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td class="fw-bold">{{ user.id }}</td>
                <td>{{ user.username }}</td>
                <td>{{ user.email }}</td>
                <td>{{ formatDate(user.createdAt) }}</td>
                
                <td>
                  <select 
                    v-model="user.role" 
                    @change="handleRoleChange(user)"
                    class="form-select form-select-sm fw-bold"
                    :class="getRoleClass(user.role)"
                    :disabled="isUpdating"
                  >
                    <option value="ADMIN">ADMIN</option>
                    <option value="USER">USER</option>
                  </select>
                </td>

                <td class="text-end">
                  <button class="btn btn-sm btn-info text-white">
                    <i class="fa-solid fa-eye"></i> Xem
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <div v-if="totalPages > 1" class="card-footer d-flex justify-content-center py-3">
        <nav>
          <ul class="pagination mb-0">
            <li class="page-item" :class="{ disabled: currentPage === 0 }">
              <button class="page-link" @click="changePage(currentPage - 1)">Trước</button>
            </li>
            <li 
              class="page-item" 
              v-for="page in totalPages" 
              :key="page" 
              :class="{ active: currentPage === (page - 1) }"
            >
              <button class="page-link" @click="changePage(page - 1)">{{ page }}</button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
              <button class="page-link" @click="changePage(currentPage + 1)">Sau</button>
            </li>
          </ul>
        </nav>
      </div>
      
      <div v-if="users.length === 0 && !loading" class="card-body py-5 text-center text-muted">
        Không tìm thấy người dùng nào.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import userService from '@/services/userService.js'; // Giả định Service này tồn tại

const users = ref([]);
const loading = ref(true);
const error = ref(null);
const currentPage = ref(0); // Backend dùng 0-indexed
const totalPages = ref(1);
const limit = 10; // Số lượng người dùng trên mỗi trang
const isUpdating = ref(false); // Ngăn chặn nhiều hành động cùng lúc

// Load danh sách người dùng (có phân trang)
const fetchUsers = async () => {
  loading.value = true;
  error.value = null;
  try {
    // Gọi API: GET /api/admin/users?page={currentPage}&size={limit}
    const res = await userService.getUsers(currentPage.value, limit);
    
    // Giả định Backend trả về cấu trúc Page của Spring Data:
    users.value = res.data.content; 
    totalPages.value = res.data.totalPages; 
  } catch (err) {
    console.error("Lỗi tải người dùng:", err);
    error.value = "Không thể tải danh sách người dùng";
  } finally {
    loading.value = false;
  }
};

// Đổi trang
const changePage = (pageIndex) => {
  if (pageIndex >= 0 && pageIndex < totalPages.value) {
    currentPage.value = pageIndex;
    fetchUsers();
  }
};

// Xử lý khi Admin đổi Role
const handleRoleChange = async (user) => {
  if (isUpdating.value) return;

  if(!confirm(`Bạn có chắc muốn đổi vai trò của user ${user.username} thành ${user.role}?`)) {
    // Nếu user hủy, fetch lại data để reset select box về cũ
    fetchUsers();
    return;
  }

  isUpdating.value = true;
  try {
    // Gọi API: PUT /api/admin/users/{userId}/role?role={newRole}
    await userService.updateUserRole(user.id, user.role);
    alert('Cập nhật vai trò thành công!');
  } catch (err) {
    console.error("Lỗi cập nhật role:", err);
    alert(err.response?.data?.message || 'Lỗi khi cập nhật vai trò');
    fetchUsers(); // Reset lại nếu lỗi
  } finally {
    isUpdating.value = false;
  }
};

// --- Helpers ---
const formatDate = (dateString) => {
  if (!dateString) return '';
  // Giả định dateString là ISO 8601 (từ Java LocalDateTime)
  return new Date(dateString).toLocaleDateString('vi-VN');
};

const getRoleClass = (role) => {
  // Dùng class của Bootstrap cho Select box
  switch (role) {
    case 'ADMIN': return 'text-danger border-danger';
    case 'USER': return 'text-primary border-primary';
    default: return 'text-secondary';
  }
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.user-list-page {
  padding: 10px;
  font-family: 'Inter', sans-serif;
}

/* TITLE */
h2 {
  font-weight: 700;
  color: #111827;
}

/* CARD & TABLE */
.card {
  border: none;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  overflow: hidden;
}

.table {
  font-size: .92rem;
}

.table thead th {
  background: #f9fafb !important;
  color: #374151;
  font-size: 0.85rem;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 1px;
  padding: 14px 12px;
  border-bottom: 1px solid #e5e7eb;
}

.table tbody tr:hover {
  background: #f3f4f6;
}

.table td {
  padding: 16px 12px !important;
  vertical-align: middle;
}

/* SELECT ROLE STYLE */
.form-select-sm {
  padding: 6px 10px;
  font-size: .8rem;
  font-weight: 700;
  border-radius: 6px;
  cursor: pointer;
  background-color: #fff !important;
  transition: .2s ease;
  min-width: 120px;
}

.form-select-sm:hover {
  background: #f3f4f6;
}

/* ROLE COLORS */
.text-danger { color: #ef4444 !important; }
.border-danger { border-color: #ef4444 !important; } /* ADMIN */

.text-primary { color: #2563eb !important; }
.border-primary { border-color: #2563eb !important; } /* USER */


/* BUTTON DETAIL */
.btn-info {
  background: #2563eb;
  border: none;
  padding: 6px 12px;
  font-weight: 600;
  border-radius: 8px;
  transition: .2s ease;
}

.btn-info:hover {
  background: #1e40af;
}

/* PAGINATION */
.pagination .page-link {
    color: #4b5563;
    font-weight: 600;
    transition: background-color 0.2s;
}
.pagination .page-item.active .page-link {
    background-color: #2563eb;
    border-color: #2563eb;
    color: white;
}
.pagination .page-item:not(.active) .page-link:hover {
    background-color: #e5e7eb;
}
.pagination .page-item.disabled .page-link {
    color: #9ca3af;
    cursor: not-allowed;
}

/* SPIN & EMPTY STATE */
.text-muted {
  font-size: 0.95rem;
}
.spinner-border {
  width: 2.4rem;
  height: 2.4rem;
}
</style>