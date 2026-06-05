package com.Ecommerce.Auth.service;

import com.Ecommerce.Auth.dto.UserCreateDto;
import com.Ecommerce.Auth.dto.UserLoginDto;
import com.Ecommerce.Auth.entity.User;
import com.Ecommerce.Auth.exception.EmailUniqueViolationException;
import com.Ecommerce.Auth.jwt.JwtToken;
import com.Ecommerce.Auth.jwt.JwtUserDetailsService;
import com.Ecommerce.Auth.kafka.producer.UserDeletedProducer;
import com.Ecommerce.common.kafka.event.user.UserCreatedEvent;
import com.Ecommerce.Auth.kafka.producer.UserCreatedProducer;
import com.Ecommerce.Auth.repository.UserRepository;
import com.Ecommerce.common.kafka.event.user.UserDeletedEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailsService detailsService;
    private final UserCreatedProducer userCreatedProducer;
    private final UserDeletedProducer userDeletedProducer;

    @Transactional
    public JwtToken save(UserCreateDto createDto){
        try {
            User user = new User();
            user.setEmail(createDto.getEmail());
            user.setPassword(passwordEncoder.encode(createDto.getPassword()));
            user.setRole(createDto.getRole());
            User savedUser = userRepository.save(user);
            userCreatedProducer.send(
                    new UserCreatedEvent(
                            savedUser.getId(),
                            createDto.getName(),
                            createDto.getPhoneNumber(),
                            createDto.getCpf(),
                            savedUser.getEmail(),
                            savedUser.getRole().toString()
                    )
            );
            return authenticate(new UserLoginDto(createDto.getEmail(),
                                            createDto.getPassword()));
        }
        catch (org.springframework.dao.DataIntegrityViolationException ex){
            throw new EmailUniqueViolationException(String.format("Email {%s} has already been registered.", createDto.getEmail()));
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
    public User findUserById(Long id){
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
    public Set<User> findAllUsersById(List<Long> usersId) {
        return new HashSet<>(userRepository.findAllById(usersId));
    }

    public void deleteUser(String email, Long id){
        userRepository.deleteById(id);
        userDeletedProducer.send(new UserDeletedEvent(id, email));
    }

    /*public User.Role findUserRoleByEmail(String email) {
        return userRepository.findRoleByEmail(email);
    }*/
}
