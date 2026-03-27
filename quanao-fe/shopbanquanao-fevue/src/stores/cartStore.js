import { defineStore } from 'pinia';
import cartService from '@/services/cartService.js';

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartCount: 0, // Biến toàn cục lưu số lượng
  }),

  actions: {
    // Hàm gọi API và cập nhật số lượng
    async fetchCartCount() {
      try {
        const res = await cartService.getCart();
        if (res.data && res.data.items) {
          this.cartCount = res.data.items.reduce((sum, item) => sum + item.quantity, 0);
        } else {
          this.cartCount = 0;
        }
      } catch (err) {
        console.error("Lỗi tải giỏ hàng:", err);
        this.cartCount = 0;
      }
    },
    
    // Reset khi logout
    clearCart() {
      this.cartCount = 0;
    }
  }
});