package com.ssafy.a107.api.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "인증 API")
@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private static PasswordEncoder passwordEncoder;

    @GetMapping("/naver")
    public ResponseEntity<?> naverCallBack(@RequestParam String code, @RequestParam String state) throws Exception {

        System.out.println("OAuth code: " + code);
        System.out.println("OAuth state: " + state);

        // for test
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}