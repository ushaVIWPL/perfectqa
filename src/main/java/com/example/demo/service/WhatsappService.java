package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class WhatsappService {

    @Value("${msg91.authkey}")
    private String authKey;

    @Value("${msg91.whatsapp.integrated_number}")
    private String integratedNumber;

    @Value("${msg91.whatsapp.template_name}")
    private String templateName;

    public boolean sendOTP(String whatsappNumber, String otp) {
        try {
            // Format number with country code
            String formattedNumber = whatsappNumber.startsWith("91") 
                ? whatsappNumber 
                : "91" + whatsappNumber;

            String payload = "{"
                + "\"integrated_number\": \"" + integratedNumber + "\","
                + "\"content_type\": \"template\","
                + "\"payload\": {"
                + "  \"messaging_product\": \"whatsapp\","
                + "  \"type\": \"template\","
                + "  \"template\": {"
                + "    \"name\": \"" + templateName + "\","
                + "    \"language\": {"
                + "      \"code\": \"en\","
                + "      \"policy\": \"deterministic\""
                + "    },"
                + "    \"namespace\": null,"
                + "    \"to_and_components\": ["
                + "      {"
                + "        \"to\": [\"" + formattedNumber + "\"],"
                + "        \"components\": {"
                + "          \"body_1\": {"
                + "            \"type\": \"text\","
                + "            \"value\": \"" + otp + "\""
                + "          }"
                + "        }"
                + "      }"
                + "    ]"
                + "  }"
                + "}"
                + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                    "https://api.msg91.com/api/v5/whatsapp/whatsapp-outbound-message/bulk/"))
                .header("Content-Type", "application/json")
                .header("authkey", authKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

            HttpResponse<String> response = 
                client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("WhatsApp OTP Response: " + response.body());
            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println("WhatsApp OTP Error: " + e.getMessage());
            return false;
        }
    }
}