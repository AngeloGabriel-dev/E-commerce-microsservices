package com.Ecommerce.Auth.dto.mapper;

import com.Ecommerce.Auth.dto.UserCreateDto;
import com.Ecommerce.Auth.dto.UserLoginDto;
import com.Ecommerce.Auth.entity.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class UserMapper {
    public static User toUser(UserCreateDto dto){
        return new ModelMapper().map(dto, User.class);
    }
}
