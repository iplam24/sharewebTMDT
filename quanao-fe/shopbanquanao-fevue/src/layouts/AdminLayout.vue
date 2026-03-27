<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h3><i class="fa-solid fa-user-shield"></i> Admin</h3>
        <p class="user-greeting" v-if="authStore.user">
          Xin chào, {{ authStore.user }}
        </p>
      </div>
      
      <nav class="sidebar-nav">
        <RouterLink to="/admin/dashboard">
          <i class="fa-solid fa-gauge-high"></i>
          <span>Dashboard</span>
        </RouterLink>
        <RouterLink to="/admin/users" class="nav-item">
          <i class="fa-solid fa-users"></i> Quản lý người dùng
        </RouterLink>
        <RouterLink to="/admin/discounts">
          <i class="fa-solid fa-ticket"></i>
          <span>Mã giảm giá</span>
        </RouterLink>
        <RouterLink to="/admin/notifications">
          <i class="fa-solid fa-bell"></i>
          <span>Thông báo</span>
        </RouterLink>
        <RouterLink to="/admin/products">
          <i class="fa-solid fa-shirt"></i>
          <span>Sản phẩm</span>
        </RouterLink>
        <RouterLink to="/admin/categories">
        <i class="fa-solid fa-tags"></i>
        <span>Danh mục</span>
    </RouterLink>
        
        <RouterLink to="/admin/orders">
          <i class="fa-solid fa-file-invoice-dollar"></i>
          <span>Đơn hàng</span>
        </RouterLink> 
      </nav>
      
      <div class="sidebar-footer">
        <button @click="logout" class="logout-button">
          <i class="fa-solid fa-right-from-bracket"></i>
          Đăng xuất
        </button>
      </div>
    </aside>
    
    <main class="main-content">
      <div class="content-wrapper">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router';
import { useAuthStore } from '@/stores/authStore.js';

const authStore = useAuthStore();

const logout = () => {
  if(confirm("Bạn có chắc chắn muốn đăng xuất?")) {
    authStore.logout();
  }
};
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  font-family: 'Inter', 'Segoe UI', sans-serif;
  background-color: #f1f4f9;
}

/* ---------------- SIDEBAR ---------------- */
.sidebar {
  width: 260px;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100%;
  padding-top: 10px;
}

/* Header */
.sidebar-header {
  padding: 1.6rem 1rem;
  text-align: center;
  border-bottom: 1px solid #f1f1f1;
}

.sidebar-header h3 {
  font-size: 1.45rem;
  font-weight: 800;
  margin: 0;
  color: #1f2937;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.sidebar-header i {
  color: #3b82f6;
  font-size: 1.3rem;
}

.user-greeting {
  margin-top: 6px;
  font-size: 0.9rem;
  color: #6b7280;
  font-weight: 500;
}

/* Navigation */
.sidebar-nav {
  flex-grow: 1;
  padding: 1.2rem 0.8rem;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-nav a {
  display: flex;
  align-items: center;
  padding: 0.85rem 1.2rem;
  font-weight: 600;
  border-radius: 10px;
  cursor: pointer;
  text-decoration: none;
  color: #4b5563;
  transition: all 0.25s ease;
}

.sidebar-nav a i {
  width: 26px;
  margin-right: 10px;
  font-size: 1.1rem;
  color: #6b7280;
  transition: 0.25s;
}

.sidebar-nav a:hover {
  background-color: #eff6ff;
  color: #2563eb;
  transform: translateX(5px);
}

.sidebar-nav a:hover i {
  color: #2563eb;
}

/* ACTIVE STATE – cực đẹp */
.sidebar-nav a.router-link-active {
  background: #2563eb;
  color: white;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.35);
}

.sidebar-nav a.router-link-active i {
  color: white;
}

/* Footer */
.sidebar-footer {
  padding: 1.5rem 1rem;
  border-top: 1px solid #f1f1f1;
}

.logout-button {
  width: 100%;
  padding: 0.85rem;
  border-radius: 10px;
  font-weight: 700;
  background: #fee2e2;
  color: #dc2626;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.25s;
}

.logout-button:hover {
  background: #dc2626;
  color: white;
}

/* ---------------- MAIN CONTENT ---------------- */
.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 2rem;
  background-color: #f3f4f6;
  min-height: 100vh;
}

.content-wrapper {
  background: white;
  padding: 2rem;
  border-radius: 16px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.06);
  min-height: 85vh;
  animation: fadeIn 0.35s ease;
}

/* Animation smooth */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
