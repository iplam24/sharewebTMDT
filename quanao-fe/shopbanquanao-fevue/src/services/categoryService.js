import apiClient from './api.js'; // Đảm bảo đường dẫn này đúng

export default {
    /**
     * Lấy tất cả danh mục công khai (Public Categories).
     * Endpoint: GET /api/categories
     * KHÔNG yêu cầu xác thực.
     * Dùng cho hiển thị ở trang khách hàng (ShopView, Header, Footer...).
     */
    getAllPublicCategories() {
        return apiClient.get('/api/categories');
    },

    /**
     * Lấy tất cả danh mục (dành cho Admin).
     * Endpoint: GET /api/admin/categories
     * Yêu cầu quyền ADMIN và JWT Token.
     */
    getAllCategories() {
        return apiClient.get('/api/admin/categories');
    },

    /**
     * Tạo danh mục mới (dành cho Admin).
     * Endpoint: POST /api/admin/categories
     * Yêu cầu quyền ADMIN và JWT Token.
     */
    createCategory(data) {
        return apiClient.post('/api/admin/categories', data);
    },

    /**
     * Xóa danh mục (dành cho Admin).
     * Endpoint: DELETE /api/admin/categories/{id}
     * Yêu cầu quyền ADMIN và JWT Token.
     */
    deleteCategory(id) {
        return apiClient.delete(`/api/admin/categories/${id}`);
    },

    // Bạn có thể thêm các phương thức khác nếu có, ví dụ update Category
    // updateCategory(id, data) {
    //     return apiClient.put(`/api/admin/categories/${id}`, data);
    // },
    // getAdminCategoryById(id) {
    //    return apiClient.get(`/api/admin/categories/${id}`);
    // }
};