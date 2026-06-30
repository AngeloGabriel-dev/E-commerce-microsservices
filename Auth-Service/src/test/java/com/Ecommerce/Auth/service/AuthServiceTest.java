package com.Ecommerce.Auth.service;

import com.Ecommerce.Auth.dto.UserCreateDto;
import com.Ecommerce.Auth.dto.UserLoginDto;
import com.Ecommerce.Auth.entity.User;
import com.Ecommerce.Auth.exception.EmailUniqueViolationException;
import com.Ecommerce.Auth.jwt.JwtToken;
import com.Ecommerce.Auth.jwt.JwtUserDetailsService;
import com.Ecommerce.Auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUserDetailsService detailsService;

    @Mock
    private RestTemplate restTemplate;

    private AuthService authService;

    private final String userServiceUrl = "http://user-service:8081";

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                detailsService,
                restTemplate
        );
        // Set the @Value field via reflection since we're not using Spring context
        setField(authService, "userServiceUrl", userServiceUrl);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private UserCreateDto createValidUserDto() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setName("Test User");
        dto.setRole(User.Role.ROLE_CLIENT);
        dto.setPhoneNumber("+5511999999999");
        dto.setCpf("12345678901");
        return dto;
    }

    private User createSavedUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(User.Role.ROLE_CLIENT);
        return user;
    }

    @Nested
    @DisplayName("save() - Register new user")
    class SaveUserTests {

        @Test
        @DisplayName("Should register user successfully when User-Service returns 201")
        void saveUser_Success() {
            // Arrange
            UserCreateDto dto = createValidUserDto();
            User savedUser = createSavedUser();

            when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(restTemplate.postForEntity(
                    eq(userServiceUrl + "/api/v1/users"),
                    any(),
                    eq(String.class)
            )).thenReturn(new ResponseEntity<>("Created", HttpStatus.CREATED));

            JwtToken expectedToken = new JwtToken("jwt-token-123");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(detailsService.getTokenAuthenticated(dto.getEmail())).thenReturn(expectedToken);

            // Act
            JwtToken result = authService.save(dto);

            // Assert
            assertThat(result).isEqualTo(expectedToken);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo(dto.getEmail());
            assertThat(capturedUser.getPassword()).isEqualTo("encodedPassword");
            assertThat(capturedUser.getRole()).isEqualTo(dto.getRole());

            ArgumentCaptor<org.springframework.http.HttpEntity> requestCaptor =
                    ArgumentCaptor.forClass(org.springframework.http.HttpEntity.class);
            verify(restTemplate).postForEntity(
                    eq(userServiceUrl + "/api/v1/users"),
                    requestCaptor.capture(),
                    eq(String.class)
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) requestCaptor.getValue().getBody();
            assertThat(body)
                    .containsEntry("id", savedUser.getId())
                    .containsEntry("name", dto.getName())
                    .containsEntry("phoneNumber", dto.getPhoneNumber())
                    .containsEntry("cpf", dto.getCpf())
                    .containsEntry("email", savedUser.getEmail());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(detailsService).getTokenAuthenticated(dto.getEmail());
        }

        @Test
        @DisplayName("Should throw EmailUniqueViolationException when email already exists")
        void saveUser_EmailAlreadyExists() {
            // Arrange
            UserCreateDto dto = createValidUserDto();

            when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenThrow(new DataIntegrityViolationException("Unique violation"));

            // Act & Assert
            assertThatThrownBy(() -> authService.save(dto))
                    .isInstanceOf(EmailUniqueViolationException.class)
                    .hasMessageContaining(dto.getEmail());

            verify(restTemplate, never()).postForEntity(anyString(), any(), any());
            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("Should abort registration when User-Service returns error status")
        void saveUser_UserServiceReturnsError() {
            // Arrange
            UserCreateDto dto = createValidUserDto();
            User savedUser = createSavedUser();

            when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(restTemplate.postForEntity(
                    eq(userServiceUrl + "/api/v1/users"),
                    any(),
                    eq(String.class)
            )).thenReturn(new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST));

            // Act & Assert
            assertThatThrownBy(() -> authService.save(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create user profile");

            verify(authenticationManager, never()).authenticate(any());
            verify(detailsService, never()).getTokenAuthenticated(anyString());
        }

        @Test
        @DisplayName("Should abort registration when User-Service is unreachable")
        void saveUser_UserServiceUnreachable() {
            // Arrange
            UserCreateDto dto = createValidUserDto();
            User savedUser = createSavedUser();

            when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(restTemplate.postForEntity(
                    eq(userServiceUrl + "/api/v1/users"),
                    any(),
                    eq(String.class)
            )).thenThrow(new RuntimeException("Connection refused"));

            // Act & Assert
            assertThatThrownBy(() -> authService.save(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create user profile");

            verify(authenticationManager, never()).authenticate(any());
            verify(detailsService, never()).getTokenAuthenticated(anyString());
        }
    }

    @Nested
    @DisplayName("authenticate() - User login")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate user successfully")
        void authenticate_Success() {
            // Arrange
            UserLoginDto dto = new UserLoginDto("test@example.com", "password123");
            JwtToken expectedToken = new JwtToken("jwt-token-123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(detailsService.getTokenAuthenticated(dto.getEmail())).thenReturn(expectedToken);

            // Act
            JwtToken result = authService.authenticate(dto);

            // Assert
            assertThat(result).isEqualTo(expectedToken);
            ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(authCaptor.capture());
            assertThat(authCaptor.getValue().getPrincipal()).isEqualTo(dto.getEmail());
            assertThat(authCaptor.getValue().getCredentials()).isEqualTo(dto.getPassword());
        }

        @Test
        @DisplayName("Should throw AuthenticationException when credentials are invalid")
        void authenticate_InvalidCredentials() {
            // Arrange
            UserLoginDto dto = new UserLoginDto("test@example.com", "wrongpassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.authenticate(dto))
                    .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);

            verify(detailsService, never()).getTokenAuthenticated(anyString());
        }
    }

    @Nested
    @DisplayName("findUserById() - Find user by ID")
    class FindUserByIdTests {

        @Test
        @DisplayName("Should return user when ID exists")
        void findUserById_Found() {
            // Arrange
            UUID id = UUID.randomUUID();
            User expectedUser = new User();
            expectedUser.setId(id);
            expectedUser.setEmail("test@example.com");

            when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));

            // Act
            User result = authService.findUserById(id);

            // Assert
            assertThat(result).isEqualTo(expectedUser);
            assertThat(result.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when ID does not exist")
        void findUserById_NotFound() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.findUserById(id))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    @Nested
    @DisplayName("findUserByEmail() - Find user by email")
    class FindUserByEmailTests {

        @Test
        @DisplayName("Should return user when email exists")
        void findUserByEmail_Found() {
            // Arrange
            String email = "test@example.com";
            User expectedUser = new User();
            expectedUser.setId(UUID.randomUUID());
            expectedUser.setEmail(email);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));

            // Act
            User result = authService.findUserByEmail(email);

            // Assert
            assertThat(result).isEqualTo(expectedUser);
            assertThat(result.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when email does not exist")
        void findUserByEmail_NotFound() {
            // Arrange
            String email = "notfound@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.findUserByEmail(email))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(email);
        }
    }

    @Nested
    @DisplayName("findAllUsersById() - Find multiple users by IDs")
    class FindAllUsersByIdTests {

        @Test
        @DisplayName("Should return set of users for existing IDs")
        void findAllUsersById_Found() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<UUID> ids = Arrays.asList(id1, id2);

            User user1 = new User();
            user1.setId(id1);
            User user2 = new User();
            user2.setId(id2);

            when(userRepository.findAllById(ids)).thenReturn(Arrays.asList(user1, user2));

            // Act
            Set<User> result = authService.findAllUsersById(ids);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(user1, user2);
        }

        @Test
        @DisplayName("Should return empty set when no IDs exist")
        void findAllUsersById_NotFound() {
            // Arrange
            List<UUID> ids = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
            when(userRepository.findAllById(ids)).thenReturn(Collections.emptyList());

            // Act
            Set<User> result = authService.findAllUsersById(ids);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteUser() - Delete user")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user locally and notify User-Service")
        void deleteUser_Success() {
            // Arrange
            String email = "test@example.com";
            UUID id = UUID.randomUUID();

            doNothing().when(userRepository).deleteById(id);
            doNothing().when(restTemplate).delete(anyString());

            // Act
            authService.deleteUser(email, id);

            // Assert
            verify(userRepository).deleteById(id);
            verify(restTemplate).delete(userServiceUrl + "/api/v1/users/" + id);
        }

        @Test
        @DisplayName("Should delete user locally even if User-Service is unreachable")
        void deleteUser_UserServiceUnreachable() {
            // Arrange
            String email = "test@example.com";
            UUID id = UUID.randomUUID();

            doNothing().when(userRepository).deleteById(id);
            doThrow(new RuntimeException("Connection refused"))
                    .when(restTemplate).delete(anyString());

            // Act - should not throw
            authService.deleteUser(email, id);

            // Assert - local delete should still happen
            verify(userRepository).deleteById(id);
            verify(restTemplate).delete(userServiceUrl + "/api/v1/users/" + id);
        }
    }
}