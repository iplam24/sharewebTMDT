import { defineStore } from 'pinia';
import wishlistService from '@/services/wishlistService';

export const useWishlistStore = defineStore('wishlist', {
  state: () => ({
    items: [], // List<ProductDTO>
    loading: false,
  }),

  getters: {
    ids: (state) => state.items.map((p) => p.id),
  },

  actions: {
    async fetchWishlist() {
      this.loading = true;
      try {
        const res = await wishlistService.getMyWishlist();
        this.items = res.data || []; // BE trả về ProductDTO
      } catch (err) {
        console.error('Lỗi tải wishlist:', err);
      } finally {
        this.loading = false;
      }
    },

    isInWishlist(productId) {
      return this.ids.includes(productId);
    },

    async toggle(product) {
      if (!product?.id) return;
      const id = product.id;

      // nếu đã có → xoá
      if (this.isInWishlist(id)) {
        try {
          await wishlistService.remove(id);
          this.items = this.items.filter((p) => p.id !== id);
        } catch (err) {
          console.error('Lỗi xoá khỏi wishlist:', err);
        }
        return;
      }

      // chưa có → thêm
      try {
        await wishlistService.add(id);
        this.items.push(product); // Thêm ProductDTO vào store
      } catch (err) {
        console.error('Lỗi thêm vào wishlist:', err);
      }
    },
  },
});
