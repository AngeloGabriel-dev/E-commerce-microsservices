package com.Ecommerce.User.service;

import com.Ecommerce.User.dto.SellerResponseDto;
import com.Ecommerce.User.dto.UserCreateDto;
import com.Ecommerce.User.entity.User;
import com.Ecommerce.User.repository.UserRepository;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserService userService;

    private UUID userId;
    private User user;
    private UserCreateDto createDto;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);

        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPhoneNumber("+5511999999999");
        user.setCpf("12345678901");

        createDto = new UserCreateDto();
        createDto.setId(userId);
        createDto.setName("Test User");
        createDto.setEmail("test@example.com");
        createDto.setPhoneNumber("+5511999999999");
        createDto.setCpf("12345678901");
    }

    private User createUser(UUID id, String name, String email) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPhoneNumber("+5511999999999");
        u.setCpf("12345678901");
        return u;
    }

    @Nested
    @DisplayName("saveUser() - Save user")
    class SaveUserTests {

        @Test
        @DisplayName("Should save user successfully when valid DTO is provided")
        void saveUser_Success() {
            // Arrange
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            User result = userService.saveUser(createDto);

            // Assert
            assertThat(result).isEqualTo(user);
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getName()).isEqualTo("Test User");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getPhoneNumber()).isEqualTo("+5511999999999");
            assertThat(result.getCpf()).isEqualTo("12345678901");

            verify(userRepository).save(userCaptor.capture());
            User captured = userCaptor.getValue();
            assertThat(captured.getId()).isEqualTo(createDto.getId());
            assertThat(captured.getName()).isEqualTo(createDto.getName());
            assertThat(captured.getPhoneNumber()).isEqualTo(createDto.getPhoneNumber());
            assertThat(captured.getCpf()).isEqualTo(createDto.getCpf());
            assertThat(captured.getEmail()).isEqualTo(createDto.getEmail());
        }

        @Test
        @DisplayName("Should persist all DTO fields into the User entity")
        void saveUser_PersistsAllFields() {
            // Arrange
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            userService.saveUser(createDto);

            // Assert
            verify(userRepository).save(userCaptor.capture());
            User captured = userCaptor.getValue();
            assertThat(captured.getId()).isEqualTo(createDto.getId());
            assertThat(captured.getName()).isEqualTo(createDto.getName());
            assertThat(captured.getPhoneNumber()).isEqualTo(createDto.getPhoneNumber());
            assertThat(captured.getCpf()).isEqualTo(createDto.getCpf());
            assertThat(captured.getEmail()).isEqualTo(createDto.getEmail());
        }

        @Test
        @DisplayName("Should throw DataIntegrityViolationException when CPF already exists")
        void saveUser_CpfAlreadyExists() {
            // Arrange
            when(userRepository.save(any(User.class)))
                    .thenThrow(new DataIntegrityViolationException("Unique index or primary key violation"));

            // Act & Assert
            assertThatThrownBy(() -> userService.saveUser(createDto))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("Unique index or primary key violation");

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should propagate repository exception on save failure")
        void saveUser_RepositoryFailure() {
            // Arrange
            when(userRepository.save(any(User.class)))
                    .thenThrow(new RuntimeException("Database connection lost"));

            // Act & Assert
            assertThatThrownBy(() -> userService.saveUser(createDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection lost");
        }
    }

    @Nested
    @DisplayName("deleteUser() - Delete user")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user by ID successfully")
        void deleteUser_Success() {
            // Arrange
            doNothing().when(userRepository).deleteById(userId);

            // Act
            userService.deleteUser(userId);

            // Assert
            verify(userRepository).deleteById(userId);
        }

        @Test
        @DisplayName("Should propagate exception when repository delete fails")
        void deleteUser_RepositoryFailure() {
            // Arrange
            doThrow(new RuntimeException("Delete failed"))
                    .when(userRepository).deleteById(userId);

            // Act & Assert
            assertThatThrownBy(() -> userService.deleteUser(userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Delete failed");

            verify(userRepository).deleteById(userId);
        }
    }

    @Nested
    @DisplayName("getById() - Find user by ID")
    class GetByIdTests {

        @Test
        @DisplayName("Should return user when ID exists")
        void getById_Found() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // Act
            User result = userService.getById(userId);

            // Assert
            assertThat(result).isEqualTo(user);
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getName()).isEqualTo("Test User");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when ID does not exist")
        void getById_NotFound() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getById(userId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(userId.toString());

            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException with correct message for different ID")
        void getById_NotFound_DifferentId() {
            // Arrange
            UUID anotherId = UUID.randomUUID();
            when(userRepository.findById(anotherId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getById(anotherId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(anotherId.toString());
        }
    }

    @Nested
    @DisplayName("getSellerById() - Find seller by ID")
    class GetSellerByIdTests {

        @Test
        @DisplayName("Should return SellerResponseDto when seller ID exists")
        void getSellerById_Found() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // Act
            SellerResponseDto result = userService.getSellerById(userId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId.toString());
            assertThat(result.name()).isEqualTo("Test User");
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.phoneNumber()).isEqualTo("+5511999999999");
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should map only public seller fields and exclude sensitive data")
        void getSellerById_MapsOnlyPublicFields() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // Act
            SellerResponseDto result = userService.getSellerById(userId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId.toString());
            assertThat(result.name()).isEqualTo("Test User");
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.phoneNumber()).isEqualTo("+5511999999999");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when seller ID does not exist")
        void getSellerById_NotFound() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getSellerById(userId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Seller with id = " + userId + " not founded.");

            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should include seller ID in exception message when not found")
        void getSellerById_NotFound_MessageContainsId() {
            // Arrange
            UUID sellerId = UUID.randomUUID();
            when(userRepository.findById(sellerId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getSellerById(sellerId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Seller with id = " + sellerId + " not founded.");
        }
    }
}
