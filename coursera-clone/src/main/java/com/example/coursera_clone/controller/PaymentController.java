package com.example.coursera_clone.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Value("${razorpay.keyId}")
    private String keyId;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {
        try {
            int amount = ((Number) data.get("amount")).intValue();
            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount * 100); // amount in the smallest currency unit
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcptid_11");

            Order order = razorpayClient.orders.create(orderRequest);
            return ResponseEntity.ok(order.toString());
        } catch (RazorpayException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
