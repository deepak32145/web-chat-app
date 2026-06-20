package com.videoapp.controller;

import com.videoapp.dto.AuthResponse;
import com.videoapp.dto.SendOTPRequest;
import com.videoapp.dto.SendOTPResponse;
import com.videoapp.dto.VerifyOTPRequest;
import com.videoapp.model.User;
import com.videoapp.repository.UserRepository;
import com.videoapp.service.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private OTPService otpService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOTP(@RequestBody SendOTPRequest request) {
        try {
            boolean isNewUser = otpService.sendOTP(request.getPhoneNumber());
            SendOTPResponse response = new SendOTPResponse();
            response.setMessage("OTP sent successfully to " + request.getPhoneNumber());
            response.setSuccess(true);
            response.setIsNewUser(isNewUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SendOTPResponse response = new SendOTPResponse();
            response.setMessage("Failed to send OTP: " + e.getMessage());
            response.setSuccess(false);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody VerifyOTPRequest request) {
        try {
            boolean isVerified = otpService.verifyOTP(request.getPhoneNumber(), request.getOtp());
            
            if (!isVerified) {
                return ResponseEntity.badRequest().body(new AuthResponse());
            }
            
            Optional<User> userOptional = userRepository.findByPhoneNumber(request.getPhoneNumber());
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new AuthResponse());
            }
            
            User user = userOptional.orElseThrow();
            
            // Update user profile if provided (only for new users or explicit non-blank values)
            if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
                user.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null && !request.getLastName().isBlank()) {
                user.setLastName(request.getLastName());
            }
            
            userRepository.save(user);
            
            // TODO: Generate JWT token
            AuthResponse response = new AuthResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setToken("jwt_token_here_" + user.getId()); // Placeholder
            response.setType("Bearer");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse());
        }
    }
}
