package com.Securegate.Securegate.helper;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class OTPstore {

    private final Map<String, String> otpMap = new HashMap<>();
    private final Map<String, Long> expiryMap = new HashMap<>();

    public void saveOtp(String email, String otp) {
        otpMap.put(email, otp);
        expiryMap.put(email, System.currentTimeMillis() + 10 * 60 * 1000); // 10 min
    }

    public boolean validateOtp(String email, String otp) {
        if (!otpMap.containsKey(email)) return false;
        if (System.currentTimeMillis() > expiryMap.get(email)) {
            otpMap.remove(email);
            expiryMap.remove(email);
            return false;
        }
        return otpMap.get(email).equals(otp);
    }

    public void clearOtp(String email) {
        otpMap.remove(email);
        expiryMap.remove(email);
    }
}