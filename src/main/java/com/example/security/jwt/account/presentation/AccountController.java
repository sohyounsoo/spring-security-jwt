package com.example.security.jwt.account.presentation;

import com.example.security.jwt.account.application.AccountService;
import com.example.security.jwt.account.application.dto.RequestAccount;
import com.example.security.jwt.account.application.dto.ResponseAccount;
import com.example.security.jwt.global.dto.CommonResponse;
import com.example.security.jwt.global.security.CustomJwtFilter;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    // 생성자 주입
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/token") // Acount 인증 API
    public ResponseEntity<CommonResponse> authorize(@Valid @RequestBody RequestAccount.Login loginDto) {

        ResponseAccount.Token token = accountService.authenticate(loginDto.username(), loginDto.password());

        //response header에도 넣고 응답 객체에도 저장
        HttpHeaders headers = new HttpHeaders();
        headers.add(CustomJwtFilter.AUTHORIZATION_HEADER, "Bearer " + token.accessToken());

        // 응답
        CommonResponse response = CommonResponse.builder()
                .success(true)
                .response(token)
                .build();

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @PutMapping("/token") // 리프레시 토큰을 활용한 액세스 토큰 갱신
    public ResponseEntity<CommonResponse> refreshToken(@Valid @RequestBody RequestAccount.Refresh refreshDto) {

        ResponseAccount.Token token = accountService.refreshToken(refreshDto.token());

        // response header 에도 넣고 응답 객체에도 넣는다.
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CustomJwtFilter.AUTHORIZATION_HEADER, "Bearer " + token.accessToken());

        // 응답
        CommonResponse response = CommonResponse.builder()
                .success(true)
                .response(token)
                .build();

        return new ResponseEntity<>(response, httpHeaders, HttpStatus.OK);
    }
}
