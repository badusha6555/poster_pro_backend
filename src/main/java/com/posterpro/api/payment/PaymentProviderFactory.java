package com.posterpro.api.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProviderFactory {

    private final Map<String, PaymentProvider> providers;
    private final PaymentProperties properties;

    public PaymentProvider getActiveProvider() {
        String key = properties.getActiveProvider();
        PaymentProvider provider = providers.get(key);
        if (provider == null) {
            throw new IllegalStateException("No payment provider registered under name: " + key);
        }
        return provider;
    }
}
