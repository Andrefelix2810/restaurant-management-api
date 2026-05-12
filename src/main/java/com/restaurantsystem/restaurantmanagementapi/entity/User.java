package com.restaurantsystem.restaurantmanagementapi.entity;

import com.restaurantsystem.restaurantmanagementapi.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    private String name;

    private String email;

    private String login;

    private String password;

    private LocalDateTime lastModifiedDate;

    private Role role;

    private Address address;
}
