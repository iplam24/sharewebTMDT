package com.shop.service;

import com.shop.entity.Order;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Async
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true = cho phép gửi HTML

            javaMailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email đến " + to + ": " + e.getMessage());
        }
    }

    @Async
    public void sendBulkEmailAsync(List<String> toList, String subject, String content) {
        for (String to : toList) {
            sendEmail(to, subject, content);
        }
    }

    @Async
    public void sendNewOrderNotificationToAdmins(List<String> adminEmails, Order order) {
        String subject = "Đơn hàng mới #" + order.getId() + " - " + order.getUser().getUsername();
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String totalPrice = currencyFormat.format(order.getTotalPrice());

        StringBuilder content = new StringBuilder();
        content.append("<h3>Có đơn hàng mới!</h3>");
        content.append("<p><b>Mã đơn hàng:</b> #").append(order.getId()).append("</p>");
        content.append("<p><b>Khách hàng:</b> ").append(order.getUser().getUsername()).append("</p>");
        content.append("<p><b>Số điện thoại:</b> ").append(order.getPhoneNumber()).append("</p>");
        content.append("<p><b>Tổng tiền:</b> <span style='color:red; font-weight:bold;'>").append(totalPrice).append("</span></p>");
        content.append("<p><b>Phương thức thanh toán:</b> ").append(order.getPaymentMethod()).append("</p>");
        content.append("<p><b>Địa chỉ giao hàng:</b> ").append(order.getShippingAddress()).append("</p>");
        content.append("<hr>");
        content.append("<p>Vui lòng đăng nhập vào trang quản trị để xử lý đơn hàng.</p>");

        sendBulkEmailAsync(adminEmails, subject, content.toString());
    }
}
