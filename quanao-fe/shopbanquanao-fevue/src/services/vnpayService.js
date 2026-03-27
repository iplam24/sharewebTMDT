import apiClient from './api.js';

export default {
    /**
     * Gửi yêu cầu đến backend để tạo URL thanh toán VNPay.
     * @param {Object} paymentData - Dữ liệu yêu cầu thanh toán VNPay.
     * @param {number} paymentData.orderId - ID của đơn hàng đã được tạo trên hệ thống của bạn.
     * @param {number} paymentData.amount - Tổng số tiền của đơn hàng (VND).
     * @param {string} paymentData.orderInfo - Mô tả đơn hàng (ví dụ: "Thanh toan don hang #123").
     * @returns {Promise<AxiosResponse>} - Response từ API, chứa paymentUrl.
     */
    createPayment(paymentData) {
        return apiClient.post('/api/vnpay/create-payment', paymentData);
    }
};