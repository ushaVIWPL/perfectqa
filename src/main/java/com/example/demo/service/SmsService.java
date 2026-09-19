package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class SmsService {

    @Value("${msg91.authkey}")
    private String authKey;

    @Value("${msg91.otp.template_id}")
    private String templateId;

    public boolean sendOTP(String mobileNumber, String otp) {
        try {
            if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
                System.out.println("Mobile number is empty. Cannot send SMS OTP.");
                return false;
            }

            // Clean number and ensure country code format
            String cleanNumber = mobileNumber.replaceAll("[^0-9]", "");
            String formattedNumber = cleanNumber.startsWith("91") 
                ? cleanNumber 
                : "91" + cleanNumber;

            // MSG91 OTP API Endpoint
            String url = String.format(
                "https://api.msg91.com/api/v5/otp?template_id=%s&mobile=%s&authkey=%s&otp=%s",
                URLEncoder.encode(templateId, StandardCharsets.UTF_8),
                URLEncoder.encode(formattedNumber, StandardCharsets.UTF_8),
                URLEncoder.encode(authKey, StandardCharsets.UTF_8),
                URLEncoder.encode(otp, StandardCharsets.UTF_8)
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = 
                client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("SMS OTP Response: " + response.body());
            
            // Check if successful (typically MSG91 returns 200 OK and type: success or success response)
            return response.statusCode() == 200 && response.body().contains("success");

        } catch (Exception e) {
            System.err.println("SMS OTP Error: " + e.getMessage());
            return false;
        }
    }
}
