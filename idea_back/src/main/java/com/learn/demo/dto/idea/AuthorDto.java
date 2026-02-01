package com.learn.demo.dto.idea;

import com.learn.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorDto {
    private Long id;
    private String username;
    private String avatar;

    public static AuthorDto fromUser(User user) {
        if (user == null) {
            return null;
        }
        return AuthorDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .avatar(user.getAvatar())
            .build();
    }
}
