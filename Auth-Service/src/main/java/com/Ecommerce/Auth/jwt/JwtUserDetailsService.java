package com.Ecommerce.Auth.jwt;

import com.Ecommerce.Auth.entity.User;
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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with email = %s not founded.", email))
        );
        return new JwtUserDetails(user);
    }

    public JwtToken getTokenAuthenticated(String email){
        User.Role role = userRepository.findRoleByEmail(email);
        return JwtUtils.createToken(email, role.name().substring("ROLE_".length()));
    }
}
