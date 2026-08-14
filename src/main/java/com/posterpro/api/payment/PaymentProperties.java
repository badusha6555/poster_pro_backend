package com.posterpro.api.payment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentProperties {

    private String activeProvider = "razorpay";
    private Razorpay razorpay = new Razorpay();

    @Getter
    @Setter
    public static class Razorpay {
        private String keyId;
        private String keySecret;
    }
}
