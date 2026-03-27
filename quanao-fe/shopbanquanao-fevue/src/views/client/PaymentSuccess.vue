<template>
  <div class="payment-result success">
    <h1>Thanh toán thành công</h1>
    <p>Đơn hàng #{{ orderId }} đã được thanh toán thành công.</p>

    <div v-if="order">
      <p><b>Tổng tiền:</b> {{ order.totalAmount.toLocaleString() }} ₫</p>
      <p><b>Trạng thái:</b> {{ order.status }}</p>
    </div>

    <router-link to="/my-orders" class="btn">Xem danh sách đơn hàng</router-link>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import apiClient from '@/services/api';

const route = useRoute();
const orderId = computed(() => route.query.orderId);
const order = ref(null);

onMounted(async () => {
  if (!orderId.value) return;
  try {
    const res = await apiClient.get(`/api/orders/${orderId.value}`);
    order.value = res.data;
  } catch (e) {
    console.error('Load order failed', e);
  }
});
</script>


<style scoped>
.payment-result {
  max-width: 600px;
  margin: 40px auto;
  text-align: center;
}
.success h1 { color: #16a34a; }
.btn {
  margin-top: 20px;
  padding: 10px 18px;
  border-radius: 6px;
  background: #3b82f6;
  color: #fff;
  text-decoration: none;
}
</style>
