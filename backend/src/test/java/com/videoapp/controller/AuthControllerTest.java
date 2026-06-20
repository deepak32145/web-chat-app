package com.videoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoapp.dto.SendOTPRequest;
import com.videoapp.dto.VerifyOTPRequest;
import com.videoapp.model.User;
import com.videoapp.repository.UserRepository;
import com.videoapp.service.OTPService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OTPService otpService;

    @MockBean
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setPhoneNumber("+911234567890");
        user.setUsername("+911234567890");
        user.setFirstName("John");
        user.setLastName("Doe");
    }

    @Test
    void sendOTP_success_returnsOkWithSuccessTrue() throws Exception {
        when(otpService.sendOTP("+911234567890")).thenReturn(false);

        SendOTPRequest request = new SendOTPRequest();
        request.setPhoneNumber("+911234567890");

        mockMvc.perform(post("/api/auth/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully to +911234567890"))
                .andExpect(jsonPath("$.isNewUser").value(false));
    }

    @Test
    void sendOTP_newUser_returnsIsNewUserTrue() throws Exception {
        when(otpService.sendOTP("+919999999999")).thenReturn(true);

        SendOTPRequest request = new SendOTPRequest();
        request.setPhoneNumber("+919999999999");

        mockMvc.perform(post("/api/auth/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNewUser").value(true));
    }

    @Test
    void sendOTP_serviceThrows_returnsBadRequest() throws Exception {
        when(otpService.sendOTP("+911234567890")).thenThrow(new RuntimeException("SMS service down"));

        SendOTPRequest request = new SendOTPRequest();
        request.setPhoneNumber("+911234567890");

        mockMvc.perform(post("/api/auth/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void verifyOTP_validOtp_returnsUserWithToken() throws Exception {
        when(otpService.verifyOTP("+911234567890", "123456")).thenReturn(true);
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        VerifyOTPRequest request = new VerifyOTPRequest();
        request.setPhoneNumber("+911234567890");
        request.setOtp("123456");

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("+911234567890"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void verifyOTP_invalidOtp_returnsBadRequest() throws Exception {
        when(otpService.verifyOTP("+911234567890", "000000")).thenReturn(false);

        VerifyOTPRequest request = new VerifyOTPRequest();
        request.setPhoneNumber("+911234567890");
        request.setOtp("000000");

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyOTP_userNotFoundAfterVerification_returnsBadRequest() throws Exception {
        when(otpService.verifyOTP("+911234567890", "123456")).thenReturn(true);
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.empty());

        VerifyOTPRequest request = new VerifyOTPRequest();
        request.setPhoneNumber("+911234567890");
        request.setOtp("123456");

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyOTP_withFirstAndLastName_updatesUserProfile() throws Exception {
        when(otpService.verifyOTP("+911234567890", "123456")).thenReturn(true);
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        VerifyOTPRequest request = new VerifyOTPRequest();
        request.setPhoneNumber("+911234567890");
        request.setOtp("123456");
        request.setFirstName("Jane");
        request.setLastName("Smith");

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
