package com.Ecommerce.User.service;

import com.Ecommerce.User.dto.SellerResponseDto;
import com.Ecommerce.User.entity.User;
import com.Ecommerce.common.kafka.event.user.*;
import com.Ecommerce.User.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User saveUser(UserCreatedEvent event){
        User user = new User();

        user.setId(event.userId());
        user.setName(event.name());
        user.setPhoneNumber(event.phoneNumber());
        user.setCpf(event.cpf());
        user.setEmail(event.email());

        return userRepository.save(user);
    }

    public void deleteUser(UserDeletedEvent event){
        userRepository.deleteById(event.userId());
    }

    @Transactional(readOnly = true)
    public User getById(UUID id){
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id = %s not founded.", id))
        );
    }

    @Transactional(readOnly = true)
    public SellerResponseDto getSellerById(UUID id){
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Seller with id = %s not founded.", id))
        );

        log.info("Seller found: {}", user.getId());

        return new SellerResponseDto(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }
}
