package com.videoapp.service;

import com.videoapp.model.User;
import com.videoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OTPService {
    
    @Autowired
    private UserRepository userRepository;
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 5;
    
    public String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    public boolean sendOTP(String phoneNumber) {
        String otp = generateOTP();
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);

        boolean isNewUser = userOptional.isEmpty();
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = new User();
            user.setPhoneNumber(phoneNumber);
            user.setUsername(phoneNumber);
        }

        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        userRepository.save(user);

        // TODO: Integrate with SMS service (Twilio, AWS SNS, etc.) to send OTP
        System.out.println("OTP for " + phoneNumber + " is: " + otp);
        return isNewUser;
    }
    
    public boolean verifyOTP(String phoneNumber, String otp) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        
        if (userOptional.isEmpty()) {
            return false;
        }
        
        User user = userOptional.get();
        
        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            return false; // OTP expired
        }
        
        if (!user.getOtp().equals(otp)) {
            return false;
        }
        
        user.setIsPhoneVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        
        return true;
    }
}
