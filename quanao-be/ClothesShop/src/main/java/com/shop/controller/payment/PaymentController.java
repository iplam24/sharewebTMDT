package com.shop.controller.payment;

import com.shop.dto.payment.VNPayPaymentRequest;
import com.shop.dto.payment.VNPayPaymentResponse;
import com.shop.enums.PaymentStatus;
import com.shop.service.order.OrderService;
import com.shop.service.payment.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VNPayService vnPayService;
    private final OrderService orderService;
    @PostMapping("/create-payment")
    public ResponseEntity<VNPayPaymentResponse> createPayment(
            @Valid @RequestBody VNPayPaymentRequest requestDto,
            HttpServletRequest request) {

        try {
            String paymentUrl = vnPayService.createPaymentUrl(
                    requestDto.getOrderId(),
                    requestDto.getAmount(),
                    requestDto.getOrderInfo(),
                    request
            );

            return ResponseEntity.ok(new VNPayPaymentResponse(paymentUrl, "Success"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VNPayPaymentResponse(null, "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/payment-return")
    public RedirectView paymentReturn(HttpServletRequest request) {

        boolean valid = vnPayService.processVNPayReturn(request);

        // Lấy orderId CHUẨN: chính là OrderID anh gửi lúc tạo hóa đơn
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String realOrderId = orderInfo.substring(orderInfo.lastIndexOf(":") + 2);
        Long orderId = Long.valueOf(realOrderId);
        if (valid) {

            orderService.confirmPayment(orderId, PaymentStatus.PAID);
            return new RedirectView("http://localhost:5173/payment-success?orderId=" + realOrderId);
        } else {
            return new RedirectView("http://localhost:5173/payment-fail");
        }
    }


    // ====== IPN URL (VNPAY gọi server-to-server) ======
    @PostMapping("/ipn")
    public ResponseEntity<Map<String, String>> vnpIpn(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
            boolean success = vnPayService.processVNPayIpn(request);

            if (success) {
                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");
            } else {
                response.put("RspCode", "01");
                response.put("Message", "Confirm Failed");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing VNPay IPN: {}", e.getMessage());
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
