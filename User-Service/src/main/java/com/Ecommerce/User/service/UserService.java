package com.Ecommerce.User.service;

import com.Ecommerce.User.entity.User;
import com.Ecommerce.common.kafka.event.user.*;
import com.Ecommerce.User.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public User getById(Long id){
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("User with id = %s not founded.", id))
        );
    }
}
