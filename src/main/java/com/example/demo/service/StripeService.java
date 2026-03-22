package com.example.demo.service;

import com.example.demo.dto.CheckoutSessionDto;
import com.example.demo.model.Payment;
import com.example.demo.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    private final String secretKey;
    private final String publicBaseUrl;
    private final PaymentRepository payments;

    public StripeService(
            @Value("${stripe.secret.key}") String secretKey,
            @Value("${app.public-url:http://localhost:8080}") String publicBaseUrl,
            PaymentRepository payments) {
        this.secretKey = secretKey;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/$", "");
        this.payments = payments;
    }

    public CheckoutSessionDto createCheckoutSession(Double amountInRupees) throws Exception {

    if (amountInRupees == null || amountInRupees <= 0) {
        throw new IllegalArgumentException("Invalid amount");
    }

    Stripe.apiKey = secretKey;
       

        Payment payment = new Payment();
        payment.setAmount(amountInRupees);
        payment.setStatus("CREATED");
        payments.save(payment);

        long paise = Math.round(amountInRupees * 100);

        var product = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName("Payment")
                .build();

        var price = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("inr")
                .setUnitAmount(paise)
                .setProductData(product)
                .build();

        var line = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(price)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(publicBaseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(publicBaseUrl + "/payment/cancel")
                .addLineItem(line)
                .build();

        Session session = Session.create(params);

        payment.setStripeSessionId(session.getId());
        payments.save(payment);

        return new CheckoutSessionDto(session.getUrl(), payment.getId());
    }
}
