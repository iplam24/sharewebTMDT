package com.shop.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtils {

    public static String toSlug(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // 1. Chuyển tiếng Việt có dấu thành không dấu
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");

        // 2. Chuyển chữ hoa thành chữ thường, thay khoảng trắng bằng gạch ngang
        slug = slug.toLowerCase();
        slug = slug.replaceAll("[^\\w\\s-]", ""); // Xóa ký tự không phải chữ/số/gạch ngang/khoảng trắng
        slug = slug.replaceAll("[\\s]+", "-");    // Thay khoảng trắng bằng gạch ngang
        slug = slug.replaceAll("[-]+", "-");     // Xóa gạch ngang kép

        return slug.trim(); // Xóa khoảng trắng đầu cuối
    }
}