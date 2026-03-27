<template>
  <div class="container-fluid mt-4">
    <h2 class="h3 mb-4 text-gray-800">Quản lý Thông báo</h2>

    <!-- Form tạo thông báo -->
    <div class="card shadow mb-4">
      <div class="card-header py-3">
        <h6 class="m-0 fw-bold text-primary">Gửi thông báo mới</h6>
      </div>
      <div class="card-body">
        <form @submit.prevent="handleCreate">
          <div class="mb-3">
            <label class="form-label fw-bold">Tiêu đề</label>
            <input v-model="newNotification.title" type="text" class="form-control" required placeholder="Nhập tiêu đề thông báo..." />
          </div>
          <div class="mb-3">
            <label class="form-label fw-bold">Nội dung</label>
            <textarea v-model="newNotification.content" class="form-control" rows="3" required placeholder="Nhập nội dung thông báo..."></textarea>
          </div>
          <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
            <i class="fa-solid fa-paper-plane me-2"></i>
            {{ isSubmitting ? 'Đang gửi...' : 'Gửi thông báo' }}
          </button>
        </form>
      </div>
    </div>

    <!-- Danh sách thông báo -->
    <div class="card shadow mb-4">
      <div class="card-header py-3">
        <h6 class="m-0 fw-bold text-primary">Lịch sử thông báo</h6>
      </div>
      <div class="card-body p-0">
        <div v-if="loading" class="text-center py-4">
          <div class="spinner-border text-primary"></div>
          <p class="mt-2">Đang tải dữ liệu...</p>
        </div>
        
        <div v-else-if="notifications.length === 0" class="text-center py-4 text-muted">
          Chưa có thông báo nào được gửi.
        </div>

        <div v-else class="table-responsive">
          <table class="table table-hover table-striped mb-0 align-middle">
            <thead class="table-light">
              <tr>
                <th style="width: 60px;" class="text-center">ID</th>
                <th style="width: 250px;">Tiêu đề</th>
                <th>Nội dung</th>
                <th style="width: 180px;">Ngày tạo</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="notif in notifications" :key="notif.id">
                <td class="text-center">#{{ notif.id }}</td>
                <td class="fw-bold text-primary">{{ notif.title }}</td>
                <td>{{ notif.content }}</td>
                <td>{{ formatDate(notif.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import notificationService from '@/services/notificationService.js';

const notifications = ref([]);
const loading = ref(true);
const isSubmitting = ref(false);

const newNotification = ref({
  title: '',
  content: ''
});

const fetchNotifications = async () => {
  loading.value = true;
  try {
    const res = await notificationService.getAllNotifications();
    notifications.value = res.data || [];
    // Sắp xếp mới nhất lên đầu
    notifications.value.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } catch (err) {
    console.error("Lỗi tải thông báo:", err);
  } finally {
    loading.value = false;
  }
};

const handleCreate = async () => {
  if (!newNotification.value.title || !newNotification.value.content) return;
  
  isSubmitting.value = true;
  try {
    await notificationService.createNotification(newNotification.value);
    alert("Gửi thông báo thành công!");
    newNotification.value = { title: '', content: '' }; // Reset form
    fetchNotifications(); // Reload list
  } catch (err) {
    console.error(err);
    alert("Lỗi khi gửi thông báo: " + (err.response?.data?.message || err.message));
  } finally {
    isSubmitting.value = false;
  }
};

const formatDate = (dateString) => new Date(dateString).toLocaleString('vi-VN');

onMounted(fetchNotifications);
</script>