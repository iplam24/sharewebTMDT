<template>
  <div class="profile-page">
    <h2>Tài khoản của tôi</h2>
    <div class="tabs">
      <button :class="{ active: tab === 'info' }" @click="tab = 'info'">Thông tin</button>
      <button :class="{ active: tab === 'orders' }" @click="tab = 'orders'">Đơn hàng</button>
      <button :class="{ active: tab === 'reviews' }" @click="tab = 'reviews'">Đánh giá của tôi</button>
    </div>

    <MyInfo v-if="tab === 'info'" />
    <MyOrders v-else-if="tab === 'orders'" />
    <MyReviews v-else-if="tab === 'reviews'" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import MyInfo from './profile/MyInfo.vue';
import MyOrders from './profile/MyOrders.vue';
import MyReviews from './profile/MyReviews.vue';

const tab = ref('info');
</script>