package com.preview.preview.domain.web.controller;

import com.preview.preview.domain.service.authority.AuthorityServiceImpl;
import com.preview.preview.domain.web.dto.authority.AuthorityResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthorityController {

    private final AuthorityServiceImpl authorityService;

    public AuthorityController(AuthorityServiceImpl authorityService) {
        this.authorityService = authorityService;
    }

    @PostMapping("/authority/{kakaoId}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<AuthorityResponseDto> enroll(@PathVariable String kakaoId){
        AuthorityResponseDto authorityResponseDto = authorityService.enrollAuthority(kakaoId);
        return ResponseEntity.ok(authorityResponseDto);
    }
}
