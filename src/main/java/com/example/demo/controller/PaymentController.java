package com.example.demo.controller;

import com.example.demo.dto.CheckoutSessionDto;
import com.example.demo.model.Payment;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.StripeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private static final String CHECKOUT_COMPLETED = "checkout.session.completed";

    private final StripeService stripeService;
    private final PaymentRepository payments;
    private final String stripeWebhookSecret;
    private final String frontendHomeUrl;
    private final ObjectMapper json = new ObjectMapper();

    public PaymentController(
            StripeService stripeService,
            PaymentRepository payments,
            @Value("${stripe.webhook.secret}") String stripeWebhookSecret,
            @Value("${app.frontend-url:http://localhost:5173/}") String frontendHomeUrl) {
        this.stripeService = stripeService;
        this.payments = payments;
        this.stripeWebhookSecret = stripeWebhookSecret;
        this.frontendHomeUrl = frontendHomeUrl;
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<Payment> status(@PathVariable Long id) {
        return payments.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/success", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> success(@RequestParam("session_id") String sessionId) {
        markCheckoutPaid(sessionId);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(CheckoutResultHtml.successPage(frontendHomeUrl));
    }

    @GetMapping(value = "/cancel", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> cancel() {
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(CheckoutResultHtml.CANCEL_PAGE);
    }

    @PostMapping("/checkout")
    public CheckoutSessionDto checkout(@RequestParam Double amount) throws Exception {
        return stripeService.createCheckoutSession(amount);
    }

    @PostMapping("/webhook")
    public String webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);
            if (CHECKOUT_COMPLETED.equals(event.getType())) {
                String sessionId = extractCheckoutSessionId(payload);
                if (sessionId != null) {
                    markCheckoutPaid(sessionId);
                }
            }
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    private void markCheckoutPaid(String stripeSessionId) {
        Payment payment = payments.findByStripeSessionId(stripeSessionId);
        if (payment != null) {
            payment.setStatus("SUCCESS");
            payments.save(payment);
        }
    }

    private String extractCheckoutSessionId(String payload) throws Exception {
        JsonNode root = json.readTree(payload);
        JsonNode session = root.path("data").path("object");
        return session.hasNonNull("id") ? session.get("id").asText() : null;
    }
}
