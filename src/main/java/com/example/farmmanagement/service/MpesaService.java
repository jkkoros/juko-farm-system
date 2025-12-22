package com.example.farmmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class MpesaService {

    @Value("${mpesa.consumer-key}")
    private String consumerKey;

    @Value("${mpesa.consumer-secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String shortcode;

    @Value("${mpesa.initiator-name}")
    private String initiatorName;

    @Value("${mpesa.security-credential}")
    private String securityCredential;

    @Value("${mpesa.environment}")
    private String environment;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private String getBaseUrl() {
        return "sandbox".equals(environment)
                ? "https://sandbox.safaricom.co.ke"
                : "https://api.safaricom.co.ke";
    }

    private String getAccessToken() throws IOException {
        String credentials = Base64.getEncoder().encodeToString((consumerKey + ":" + consumerSecret).getBytes());

        Request request = new Request.Builder()
                .url(getBaseUrl() + "/oauth/v1/generate?grant_type=client_credentials")
                .get()
                .addHeader("Authorization", "Basic " + credentials)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get access token: " + response.body().string());
            }
            String responseBody = response.body().string();
            @SuppressWarnings("unchecked")
            Map<String, Object> json = mapper.readValue(responseBody, Map.class);
            return (String) json.get("access_token");
        }
    }

    /**
     * Send B2C payment to farmer
     */
    public Map<String, Object> disbursePayment(String phoneNumber, double amount, String reference) throws Exception {
        String accessToken = getAccessToken();

        Map<String, Object> body = new HashMap<>();
        body.put("InitiatorName", initiatorName);
        body.put("SecurityCredential", securityCredential);
        body.put("CommandID", "BusinessPayment");
        body.put("Amount", (int) Math.round(amount)); // M-Pesa expects whole number
        body.put("PartyA", shortcode);
        body.put("PartyB", formatPhoneNumber(phoneNumber));
        body.put("Remarks", "Farmer payment - " + reference);
        body.put("QueueTimeOutURL", "https://unlettered-twila-pseudoapologetically.ngrok-free.dev/mpesa/timeout");
        body.put("ResultURL", "https://unlettered-twila-pseudoapologetically.ngrok-free.dev/mpesa/result");
        //body.put("QueueTimeOutURL", "https://yourdomain.com/mpesa/timeout");
        //body.put("ResultURL", "https://yourdomain.com/mpesa/result");
        body.put("Occassion", "");

        String json = mapper.writeValueAsString(body);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(getBaseUrl() + "/mpesa/b2c/v1/paymentrequest")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("B2C request failed: " + response.body().string());
            }
            return mapper.readValue(response.body().string(), Map.class);
        }
    }

    private String formatPhoneNumber(String phone) {
        phone = phone.replaceAll("\\s+", ""); // remove spaces
        if (phone.startsWith("0")) {
            return "254" + phone.substring(1);
        } else if (phone.startsWith("+254")) {
            return phone.substring(1);
        } else if (phone.startsWith("254")) {
            return phone;
        }
        return phone; // fallback
    }
}
