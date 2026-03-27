import apiClient from './api.js';

export default {
  getDailyRevenue() {
    return apiClient.get('/api/stats/daily-revenue');
  },
  
  getTopSellingProducts(limit = 5) {
    return apiClient.get(`/api/stats/top-selling?limit=${limit}`);
  },
  
  // Bạn có thể thêm các hàm tổng số lượng khác ở đây
};