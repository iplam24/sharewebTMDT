import apiClient from './api.js';

export default {
   getAllProducts(params = {}) {
    return apiClient.get('/api/products', { params });
  },

  // Hàm mới: Lấy chi tiết 1 sản phẩm
  getProductById(id) {
    return apiClient.get(`/api/products/${id}`);
  },

  deleteProduct(id) {
    return apiClient.delete(`/api/products/${id}`);
  },

  createProduct(productData) {
    return apiClient.post('/api/products', productData);
  },

  // Hàm mới: Cập nhật sản phẩm
  updateProduct(id, productData) {
    return apiClient.put(`/api/products/${id}`, productData);
  }
};