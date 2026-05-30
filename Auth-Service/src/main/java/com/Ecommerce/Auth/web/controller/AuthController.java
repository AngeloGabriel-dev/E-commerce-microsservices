package com.Ecommerce.Auth.web.controller;

import com.Ecommerce.Auth.dto.UserCreateDto;
import com.Ecommerce.Auth.dto.UserLoginDto;
import com.Ecommerce.Auth.dto.mapper.UserMapper;
import com.Ecommerce.Auth.entity.User;
import com.Ecommerce.Auth.jwt.JwtToken;
import com.Ecommerce.Auth.jwt.JwtUserDetailsService;
import com.Ecommerce.Auth.service.AuthService;
import com.Ecommerce.Auth.web.exceptions.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<JwtToken> authenticate(@RequestBody @Valid UserLoginDto dto, HttpServletRequest request){
        JwtToken token = authService.authenticate(dto);
        return ResponseEntity.ok(token);
    }


    @Operation(summary = "Criar um novo usuário", description = "Recurso para criar um novo usuário",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserLoginDto.class))),
                    @ApiResponse(responseCode = "409", description = "Usuário e-mail já cadastrado no sistema",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada invalidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping("/register")
    public ResponseEntity<JwtToken> register(@RequestBody @Valid UserCreateDto dto, HttpServletRequest request){
        authService.save(dto);
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(dto.getEmail());
        loginDto.setPassword(dto.getPassword());
        JwtToken token = authService.authenticate(loginDto);
        return ResponseEntity.ok(token);
    }
}
