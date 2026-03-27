package com.shop.service.payment;

import com.shop.utils.VNPayUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayService {

    private final VNPayUtils vnPayUtils;

    public String createPaymentUrl(Long orderId, long amount, String orderInfo, HttpServletRequest request) {
        try {
            return vnPayUtils.createVNPayPaymentUrl(orderId, amount, orderInfo, request);
        } catch (Exception e) {
            log.error("Error creating VNPay URL: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating VNPay URL");
        }
    }

    private Map<String, String> extractVNPayParams(HttpServletRequest request) {
        Map<String, String> vnp_Params = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();

        while (params.hasMoreElements()) {
            String name = params.nextElement();
            String value = request.getParameter(name);
            if (name != null && name.startsWith("vnp_") && value != null && !value.isEmpty()) {
                vnp_Params.put(name, value);
            }
        }
        return vnp_Params;
    }

    private boolean validateSignature(Map<String, String> vnp_Params) {
        String vnp_SecureHash = vnp_Params.remove("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHashType");

        String signData = vnPayUtils.hashAllFields(vnp_Params);
        String signedHash = vnPayUtils.hmacSHA512(signData);

        boolean ok = signedHash.equals(vnp_SecureHash);
        if (!ok) {
            log.warn("VNPay signature invalid. signData={}, expected={}, actual={}",
                    signData, vnp_SecureHash, signedHash);
        }
        return ok;
    }


    // IPN: VNPAY server gọi server mình
    public boolean processVNPayIpn(HttpServletRequest request) {
        Map<String, String> vnp_Params = extractVNPayParams(request);

        if (!validateSignature(vnp_Params)) {
            log.warn("IPN | Invalid VNPay signature");
            return false;
        }

        String responseCode = vnp_Params.get("vnp_ResponseCode");
        String transactionStatus = vnp_Params.get("vnp_TransactionStatus");
        String amount = vnp_Params.get("vnp_Amount");
        String orderId = vnp_Params.get("vnp_TxnRef");

        long realAmount = Long.parseLong(amount) / 100;
        log.info("IPN | orderId={} amount={} response={} txnStatus={}",
                orderId, realAmount, responseCode, transactionStatus);

        // TODO:
        // 1. Tìm order theo orderId
        // 2. So sánh số tiền
        // 3. Nếu status hiện tại chưa xử lý:
        //    - Nếu responseCode = "00" && transactionStatus = "00" => PAID/SUCCESS
        //    - Ngược lại => FAILED

        return "00".equals(responseCode) && "00".equals(transactionStatus);
    }
    public boolean processVNPayReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();

        // 1) Lấy toàn bộ params từ request
        request.getParameterMap().forEach((key, value) -> {
            if (key.startsWith("vnp_")) {
                fields.put(key, value[0]);
            }
        });

        // 2) Lấy secureHash từ VNPay
        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        // 3) Build lại hashData
        try {
            String hashData = vnPayUtils.hashAllFields(fields);
            String myHash = vnPayUtils.hmacSHA512(hashData);

            if (!myHash.equalsIgnoreCase(vnp_SecureHash)) {
                return false; // Sai chữ ký!
            }
        } catch (Exception e) {
            return false;
        }

        // 4) Check Response Code
        String responseCode = fields.get("vnp_ResponseCode");
        return "00".equals(responseCode);
    }


}
