import apiClient from './api.js';

export default {
  // --- API CHO ADMIN ---
  
  // Lấy tất cả đơn hàng
  getAllOrders() {
    return apiClient.get('/api/orders/all');
  },

  // Cập nhật trạng thái đơn hàng
  updateOrderStatus(orderId, status) {
    return apiClient.put(`/api/orders/${orderId}/status`, null, {
      params: { status: status }
    });
  },

  /**
   * Tạo đơn hàng (Checkout)
   * @param {Object} data - { addressId, paymentMethod }
   */
  createOrder(data) {
    return apiClient.post('/api/orders/checkout', data);
  },

  // Lấy lịch sử đơn hàng của tôi
  getMyOrders() {
    return apiClient.get('/api/orders/my-history');
  },
  cancelOrder(orderId) {
    return apiClient.put(`/api/orders/${orderId}/cancel`);
  },
  getMyOrders(params) {
        // Gửi tham số qua query string (params = {page: 0, size: 5, status: 'PENDING'})
        return apiClient.get('/api/orders/my-history', { params: params }); 
  },
  confirmDelivery(orderId) {
        return apiClient.put(`/api/orders/${orderId}/confirm-delivery`);
    },
};