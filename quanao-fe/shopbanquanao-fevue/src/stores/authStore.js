// src/stores/authStore.js
import { defineStore } from 'pinia';
import apiClient from '@/services/api.js';
import router from '@/router';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    role: null,
    isAuthenticated: false,
    initialized: false,
  }),

  getters: {
    isAdmin: (state) => state.role === 'ADMIN',
  },

  actions: {
async login(credentials, rememberMe) {
  try {
    await apiClient.post('/api/auth/login', { ...credentials, rememberMe });

    // ĐỢI fetchUser HOÀN TẤT RỒI MỚI ĐƯỢC DÙNG this.role
    await this.fetchUser();

    // BÂY GIỜ MỚI ĐƯỢC PUSH
    router.push(this.role === 'ADMIN' ? '/admin/dashboard' : '/');
  } catch (error) {
    throw error;
  }
},
    // 2. LẤY THÔNG TIN USER (DÙNG COOKIE)
 async fetchUser() {
    try {
        const res = await apiClient.get('/api/auth/me'); 
        this.user = res.data.username;
        this.role = res.data.role;
        this.isAuthenticated = true;
    } catch (error) {
        this.clearAuth();
    }
},

    // 3. ĐĂNG KÝ
    async register(registerData) {
      try {
        await apiClient.post('/api/auth/register', registerData);
        await this.login({
          username: registerData.username,
          password: registerData.password
        });
      } catch (error) {
        throw error;
      }
    },

    // 4. QUÊN MẬT KHẨU
    async forgotPassword(email) {
      const res = await apiClient.post('/api/auth/forgot-password', { email });
      return res.data;
    },

    async resetPassword(data) {
      const res = await apiClient.post('/api/auth/reset-password', data);
      return res.data;
    },

    // 5. ĐĂNG XUẤT
    async logout() {
      try {
        await apiClient.post('/api/auth/logout');
      } catch (error) {
        console.error('Logout error:', error);
      } finally {
        this.clearAuth();
        router.push('/login');
      }
    },

    // 6. XÓA AUTH KHI HẾT HẠN
    clearAuth() {
      this.user = null;
      this.role = null;
      this.isAuthenticated = false;
      localStorage.clear();
      sessionStorage.clear();
    },

    // 7. KHỞI TẠO KHI APP LOAD
    async init() {
      await this.fetchUser();
    }
  },
});