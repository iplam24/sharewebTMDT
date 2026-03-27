import apiClient from './api.js';

export default {
  getAllDiscounts() {
    return apiClient.get('/api/discounts');
  },

  getDiscountById(id) {
    // Lưu ý: Backend cần có API @GetMapping("/{id}")
    return apiClient.get(`/api/discounts/${id}`);
  },

  // Bổ sung hàm này để CartView sử dụng
  getDiscountByCode(code) {
    return apiClient.get(`/api/discounts/${code}`);
  },

  createDiscount(data) {
    return apiClient.post('/api/discounts', data);
  },

  updateDiscount(id, data) {
    return apiClient.put(`/api/discounts/${id}`, data);
  },

  deleteDiscount(id) {
    return apiClient.delete(`/api/discounts/${id}`);
  }
};