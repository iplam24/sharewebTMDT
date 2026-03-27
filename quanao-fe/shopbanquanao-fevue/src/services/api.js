// src/api/apiClient.js
import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL;

const apiClient = axios.create({
  baseURL: baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // QUAN TRỌNG: Gửi cookie tự động
});

// XÓA interceptor gắn token vào header
// → Không cần nữa vì token ở trong cookie

// (Tùy chọn) Interceptor xử lý lỗi 401
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      authStore.logout(); // Xóa user trong store
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
// cấu hình axios để call api