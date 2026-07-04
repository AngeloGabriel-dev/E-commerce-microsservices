package com.Ecommerce.Auth.jwt;

import com.Ecommerce.Auth.entity.ServiceAccount;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.UUID;

public class ServiceAccountUserDetails extends User {
    private ServiceAccount serviceAccount;

    public ServiceAccountUserDetails(ServiceAccount serviceAccount){
        super(serviceAccount.getClientId(), serviceAccount.getClientSecretHash(), 
              AuthorityUtils.createAuthorityList(serviceAccount.getRole().name()));
        this.serviceAccount = serviceAccount;
    }

    public UUID getId(){
        return this.serviceAccount.getId();
    }

    public String getRole(){
        return this.serviceAccount.getRole().name();
    }
}