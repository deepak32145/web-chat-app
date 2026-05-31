package com.videoapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true)
    private String email;

    @Column
    private String password;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "profile_picture")
    private String profilePicture;
    
    @Column(unique = true, nullable = false)
    private String phoneNumber;
    
    @Column(name = "otp")
    private String otp;
    
    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;
    
    @Column(name = "is_phone_verified")
    private Boolean isPhoneVerified = false;
    
    @Column(length = 500)
    private String bio;
    
    @Column(name = "status")
    private String status = "offline"; // Online, offline, away
    
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> sentMessages;
    
    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private List<Conversation> conversations;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
