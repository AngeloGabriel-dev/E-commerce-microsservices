package com.Ecommerce.Auth.jwt;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public class JwtUserDetails extends User {
    private com.Ecommerce.Auth.entity.User user;
    public JwtUserDetails(com.Ecommerce.Auth.entity.User user){
        super(user.getEmail(), user.getPassword(), AuthorityUtils.createAuthorityList(user.getRole().name()));
        this.user = user;
        System.out.println(user.getId());
    }
    public Long getId(){
        return this.user.getId();
    }

    public String getRole(){
        return this.user.getRole().name();
    }
}
