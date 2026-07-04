package com.Ecommerce.Auth.jwt;

import com.Ecommerce.Auth.entity.ServiceAccount;
import com.Ecommerce.Auth.entity.User;
import com.Ecommerce.Auth.repository.ServiceAccountRepository;
import com.Ecommerce.Auth.repository.UserRepository;
import com.Ecommerce.Auth.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ServiceAccountRepository serviceAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to load as regular user first (by email)
        if (userRepository.findByEmail(username).isPresent()) {
            User user = userRepository.findByEmail(username).orElseThrow(
                    () -> new EntityNotFoundException(String.format("User with email = %s not founded.", username))
            );
            return new JwtUserDetails(user);
        }
        
        // Try to load as service account (by clientId)
        if (serviceAccountRepository.findByClientId(username).isPresent()) {
            ServiceAccount serviceAccount = serviceAccountRepository.findByClientId(username).orElseThrow(
                    () -> new EntityNotFoundException(String.format("Service Account with clientId = %s not founded.", username))
            );
            return new ServiceAccountUserDetails(serviceAccount);
        }
        
        throw new UsernameNotFoundException(String.format("User or Service Account with identifier = %s not founded.", username));
    }

    public JwtToken getTokenAuthenticated(String email){
        User.Role role = userRepository.findRoleByEmail(email);
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with email = %s not founded.", email))
        );
        return JwtUtils.createToken(email, role.name().substring("ROLE_".length()), user.getId());
    }
}
