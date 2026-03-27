// src/services/reviewService.js
import apiClient from './api';

const reviewService = {
  // LẤY DANH SÁCH REVIEW CỦA SẢN PHẨM
  getByProduct(productId) {
    return apiClient.get(`/api/reviews/product/${productId}`);
  },

  // TẠO REVIEW MỚI (rating + comment + images[])
  createReview({ productId, rating, comment, images }) {
    const formData = new FormData();

    formData.append('productId', productId);
    formData.append('rating', rating);
    formData.append('comment', comment);

    if (images && images.length > 0) {
      images.forEach((file) => {
        formData.append('images', file);
      });
    }

    return apiClient.post('/api/reviews', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // SỬA REVIEW (chỉ rating + comment)
  updateReview(id, payload) {
    return apiClient.put(`/api/reviews/${id}`, payload);
  },

  // XÓA REVIEW
  deleteReview(id) {
    return apiClient.delete(`/api/reviews/${id}`);
  },
};

export default reviewService;