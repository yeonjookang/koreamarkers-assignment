package org.example.assignment.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.assignment.domain.user.dto.request.SignInRequest;
import org.example.assignment.domain.user.dto.request.SignUpRequest;
import org.example.assignment.domain.user.dto.response.SignInResponse;
import org.example.assignment.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<SignInResponse> signIn(@Valid @RequestBody SignInRequest request, HttpServletResponse httpServletResponse) {
        SignInResponse response = userService.signIn(request, httpServletResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout() {
        userService.logout();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<SignInResponse> reissue(HttpServletRequest request, HttpServletResponse response) {
        SignInResponse signInResponse = userService.reissue(request, response);
        return ResponseEntity.ok(signInResponse);
    }

}
