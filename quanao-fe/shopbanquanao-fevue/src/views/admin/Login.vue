<template>
  <div class="admin-login-page">
    <div class="login-card">
      <h2>ADMIN PORTAL</h2>
      <p style="text-align: center; color: #666; margin-bottom: 20px;">Đăng nhập hệ thống quản trị</p>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>Username</label>
          <input type="text" v-model="username" required placeholder="admin" />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input type="password" v-model="password" required placeholder="admin123" />
        </div>
        <div class="form-group">
  <label><input type="checkbox" v-model="rememberMe" /> Ghi nhớ đăng nhập</label>
</div>
        
        <div v-if="error" class="error-msg">{{ error }}</div>
        
        <button type="submit" :disabled="loading">
          {{ loading ? 'Đang xử lý...' : 'Đăng nhập Admin' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/authStore.js';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const username = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);
const handleLogin = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    // SỬA DÒNG NÀY – THÊM rememberMe: true
    await authStore.login({ 
      username: username.value, 
      password: password.value 
    }, true);  // ← Ép true để Max-Age 7 ngày
    
    if (authStore.isAdmin) {
      router.push('/admin/dashboard');
    } else {
      authStore.logout();
      error.value = "Tài khoản này không có quyền truy cập Admin!";
    }
  } catch (err) {
    error.value = "Tên đăng nhập hoặc mật khẩu không đúng.";
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* ===========================
   ADMIN LOGIN – PREMIUM UI
   =========================== */
.admin-login-page {
  height: 100vh;
  background: linear-gradient(135deg, #1f2937, #111827); /* gradient dark */
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* Hiệu ứng bóng mờ moving */
.admin-login-page::before {
  content: "";
  position: absolute;
  width: 500px;
  height: 500px;
  background: rgba(59, 130, 246, 0.15);
  filter: blur(180px);
  border-radius: 50%;
  animation: floatGlow 8s infinite alternate ease-in-out;
}

@keyframes floatGlow {
  from { transform: translate(-150px, -100px); }
  to   { transform: translate(150px, 100px); }
}

.login-card {
  position: relative;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(255,255,255,0.15);
  padding: 40px 35px;
  border-radius: 14px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.3);
  animation: fadeIn 0.7s ease;
  color: white;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

h2 {
  text-align: center;
  margin-bottom: 8px;
  font-size: 1.8rem;
  font-weight: 800;
  letter-spacing: 1px;
  color: #fff;
}

.login-card p {
  color: #d1d5db !important;
}

.form-group {
  margin-bottom: 18px;
}

label {
  display: block;
  margin-bottom: 6px;
  font-weight: 600;
  color: #e5e7eb;
  font-size: 0.9rem;
}

input {
  width: 100%;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(255,255,255,0.3);
  background: rgba(255,255,255,0.07);
  color: #fff;
  font-size: 0.95rem;
  transition: all 0.25s ease;
}

input::placeholder {
  color: #9ca3af;
}

input:focus {
  border-color: #3b82f6;
  background: rgba(255,255,255,0.12);
  outline: none;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.3);
}

button {
  width: 100%;
  padding: 12px;
  border-radius: 8px;
  border: none;
  background: #3b82f6;
  color: white;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  margin-top: 10px;
  transition: 0.25s ease;
  letter-spacing: 0.5px;
}

button:hover {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(37, 99, 235, 0.3);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-msg {
  background: rgba(239, 68, 68, 0.15);
  border-left: 3px solid #ef4444;
  padding: 10px 14px;
  color: #fecaca;
  border-radius: 6px;
  margin-bottom: 12px;
  text-align: center;
  font-weight: 500;
}

</style>