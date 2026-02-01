package com.learn.demo.dto.user;

import com.learn.demo.entity.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .avatar(user.getAvatar())
            .role(user.getRole() == null ? null : user.getRole().name())
            .status(user.getStatus() == null ? null : user.getStatus().name())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
