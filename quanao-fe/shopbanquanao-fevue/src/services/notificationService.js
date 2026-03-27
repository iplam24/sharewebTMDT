import apiClient from './api';

export default {
  getAllNotifications() {
    return apiClient.get('/api/admin/notifications');
  },
  createNotification(data) {
    return apiClient.post('/api/admin/notifications', data);
  }
};