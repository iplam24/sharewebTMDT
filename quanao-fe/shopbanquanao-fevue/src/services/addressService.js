import apiClient from './api.js';

export default {
  // Lấy danh sách địa chỉ
  getMyAddresses() {
    return apiClient.get('/api/addresses');
  },

  // Thêm địa chỉ mới
  addAddress(data) {
    // data = { street, city, state, phoneNumber, country }
    return apiClient.post('/api/addresses', data);
  }
};