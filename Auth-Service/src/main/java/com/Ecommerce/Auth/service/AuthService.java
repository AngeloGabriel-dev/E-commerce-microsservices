package com.Ecommerce.Auth.service;

import com.Ecommerce.Auth.dto.UserCreateDto;
import com.Ecommerce.Auth.dto.UserLoginDto;
import com.Ecommerce.Auth.entity.User;
import com.Ecommerce.Auth.exception.EmailUniqueViolationException;
import com.Ecommerce.Auth.jwt.JwtToken;
import com.Ecommerce.Auth.jwt.JwtUserDetailsService;
import com.Ecommerce.Auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailsService detailsService;
    private final RestTemplate restTemplate;

    @Value("${app.user-service.url:http://user-service:8081}")
    private String userServiceUrl;

    @Transactional
    public JwtToken save(UserCreateDto createDto){
        try {
            User user = new User();
            user.setEmail(createDto.getEmail());
            user.setPassword(passwordEncoder.encode(createDto.getPassword()));
            user.setRole(createDto.getRole());

            // Save auth user first to generate the ID
            User savedUser = userRepository.save(user);

            // Call User-Service via REST to create user profile
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("id", savedUser.getId());
            userProfile.put("name", createDto.getName());
            userProfile.put("phoneNumber", createDto.getPhoneNumber());
            userProfile.put("cpf", createDto.getCpf());
            userProfile.put("email", savedUser.getEmail());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userProfile, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    userServiceUrl + "/api/v1/users",
                    request,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to create user in User-Service");
            }

            return authenticate(new UserLoginDto(createDto.getEmail(),
                                            createDto.getPassword()));
        }
        catch (org.springframework.dao.DataIntegrityViolationException ex){
            throw new EmailUniqueViolationException(String.format("Email {%s} has already been registered.", createDto.getEmail()));
        }
        catch (Exception ex) {
            log.error("Error creating user in User-Service: {}", ex.getMessage());
            throw new RuntimeException("Failed to create user profile. Registration aborted.", ex);
        }
    }

    @Transactional(readOnly = true)
    public JwtToken authenticate(UserLoginDto dto){
        log.info("Processo de autenticação pelo login {}", dto.getEmail());
        System.out.println(dto);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                );

        authenticationManager.authenticate(authenticationToken);
        return detailsService.getTokenAuthenticated(dto.getEmail());
    }

    @Transactional(readOnly = true)
    public User findUserById(UUID id){
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id = %s not founded.", id))
        );
    }


    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with email = %s not founded.", email))
        );
    }

    @Transactional(readOnly = true)
    public Set<User> findAllUsersById(List<UUID> usersId) {
        return new HashSet<>(userRepository.findAllById(usersId));
    }

    public void deleteUser(String email, UUID id){
        userRepository.deleteById(id);

        try {
            restTemplate.delete(userServiceUrl + "/api/v1/users/" + id);
        } catch (Exception ex) {
            log.warn("Failed to notify User-Service about user deletion: {}", ex.getMessage());
        }
    }

    /*public User.Role findUserRoleByEmail(String email) {
        return userRepository.findRoleByEmail(email);
    }*/
}
