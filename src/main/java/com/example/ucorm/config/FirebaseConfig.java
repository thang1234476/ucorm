package com.example.ucorm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount;

                // 1. Kiểm tra xem có biến môi trường trên Render không
                String firebaseEnv = System.getenv("FIREBASE_CREDENTIALS");

                if (firebaseEnv != null && !firebaseEnv.trim().isEmpty()) {
                    // Nếu có (chạy trên Render), chuyển chuỗi JSON thành InputStream
                    serviceAccount = new ByteArrayInputStream(firebaseEnv.getBytes(StandardCharsets.UTF_8));
                    System.out.println("🔥 Initializing Firebase using Environment Variable on Render...");
                } else {
                    // Nếu không có (chạy ở Local), đọc file từ thư mục resources
                    serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
                    System.out.println("💻 Initializing Firebase using local serviceAccountKey.json...");
                }

                // 2. Kiểm tra nếu cả 2 nguồn đều không có dữ liệu
                if (serviceAccount == null) {
                    throw new RuntimeException("Firebase credentials account file or variable not found!");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized successfully!");
            }
        } catch (Exception e) {
            System.err.println("❌ Firebase initialization failed: " + e.getMessage());
            e.printStackTrace();
            // Ném lỗi ra để Spring Boot dừng lại nếu cấu hình sai hoàn toàn
            throw new RuntimeException(e);
        }
    }
}