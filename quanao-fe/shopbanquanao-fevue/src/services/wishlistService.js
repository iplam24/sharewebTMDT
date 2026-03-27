// src/services/wishlistService.js
import apiClient from './api';

const wishlistService = {
  getMyWishlist() {
    // thêm /api
    return apiClient.get('/api/favorites');
  },
  add(productId) {
    return apiClient.post(`/api/favorites/${productId}`);
  },
  remove(productId) {
    return apiClient.delete(`/api/favorites/${productId}`);
  },
};

export default wishlistService;
