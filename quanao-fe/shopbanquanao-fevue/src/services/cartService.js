import apiClient from './api.js';

export default {
  // Thêm vào giỏ
  addToCart(data) {
    // data = { variantId, quantity }
    return apiClient.post('/api/cart/add', data);
  },

  // Lấy giỏ hàng
  getCart() {
    return apiClient.get('/api/cart');
  },

  // Cập nhật số lượng
  updateQuantity(cartItemId, quantity) {
    return apiClient.put(`/api/cart/update/${cartItemId}?quantity=${quantity}`);
  },

  // Xóa món hàng
  removeFromCart(cartItemId) {
    return apiClient.delete(`/api/cart/remove/${cartItemId}`);
  }
};