package com.videoapp.service;

import com.videoapp.model.User;
import com.videoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OTPServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OTPService otpService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPhoneNumber("+911234567890");
        existingUser.setUsername("+911234567890");
    }

    @Test
    void generateOTP_returnsExactlySixDigits() {
        String otp = otpService.generateOTP();
        assertThat(otp).hasSize(6).matches("\\d{6}");
    }

    @Test
    void generateOTP_producesRandomValues() {
        String otp1 = otpService.generateOTP();
        String otp2 = otpService.generateOTP();
        // Extremely unlikely to be equal two in a row with 1,000,000 possibilities
        assertThat(otp1).isNotNull();
        assertThat(otp2).isNotNull();
    }

    @Test
    void sendOTP_existingUser_returnsFalse() {
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        boolean isNewUser = otpService.sendOTP("+911234567890");

        assertThat(isNewUser).isFalse();
        verify(userRepository).save(existingUser);
    }

    @Test
    void sendOTP_newUser_returnsTrue() {
        when(userRepository.findByPhoneNumber("+919999999999")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean isNewUser = otpService.sendOTP("+919999999999");

        assertThat(isNewUser).isTrue();
    }

    @Test
    void sendOTP_setsOtpAndExpiry() {
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        otpService.sendOTP("+911234567890");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getOtp()).hasSize(6);
        assertThat(saved.getOtpExpiry()).isAfter(LocalDateTime.now());
    }

    @Test
    void verifyOTP_validOtp_returnsTrue() {
        existingUser.setOtp("123456");
        existingUser.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        boolean result = otpService.verifyOTP("+911234567890", "123456");

        assertThat(result).isTrue();
    }

    @Test
    void verifyOTP_wrongOtp_returnsFalse() {
        existingUser.setOtp("123456");
        existingUser.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.of(existingUser));

        boolean result = otpService.verifyOTP("+911234567890", "999999");

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyOTP_expiredOtp_returnsFalse() {
        existingUser.setOtp("123456");
        existingUser.setOtpExpiry(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.of(existingUser));

        boolean result = otpService.verifyOTP("+911234567890", "123456");

        assertThat(result).isFalse();
    }

    @Test
    void verifyOTP_userNotFound_returnsFalse() {
        when(userRepository.findByPhoneNumber("+910000000000")).thenReturn(Optional.empty());

        boolean result = otpService.verifyOTP("+910000000000", "123456");

        assertThat(result).isFalse();
    }

    @Test
    void verifyOTP_nullOtpOnUser_returnsFalse() {
        existingUser.setOtp(null);
        existingUser.setOtpExpiry(null);
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.of(existingUser));

        boolean result = otpService.verifyOTP("+911234567890", "123456");

        assertThat(result).isFalse();
    }

    @Test
    void verifyOTP_success_clearsOtpFields() {
        existingUser.setOtp("123456");
        existingUser.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByPhoneNumber("+911234567890")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        otpService.verifyOTP("+911234567890", "123456");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getOtp()).isNull();
        assertThat(captor.getValue().getOtpExpiry()).isNull();
        assertThat(captor.getValue().getIsPhoneVerified()).isTrue();
    }
}
