package com.pfnd.UserService.model.dto;



import com.pfnd.UserService.model.postgresql.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
public class UserDto {
    private Integer id;
    private String email;
    private Date createdAt;
    private Date modifiedAt;
    private boolean isActive;

    public UserDto(User user) {
        id = user.getId();
        email = user.getEmail();
        createdAt = user.getCreatedAt();
        modifiedAt = user.getModifiedAt();
        isActive = user.isActive();
    }
}
