package com.example.security.jwt.account.presentation;

import com.example.security.jwt.account.application.AccountService;
import com.example.security.jwt.account.application.dto.RequestAccount;
import com.example.security.jwt.account.application.dto.ResponseAccount;
import com.example.security.jwt.global.dto.CommonResponse;
import com.example.security.jwt.global.security.CustomJwtFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;

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

        ResponseAccount.Token token = accountService.refreshToken(refreshDto.refreshToken());

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

    /**
     * Authorization : Bearer {AccessToken}
     * @AuthenticationPrincipal를 통해 JwtFilter에서 토큰을 검증하며 등록한 시큐리티 유저 객체를 꺼내옴
     * JwtFilter는 디비 조회 X
     * 토큰 유저 조회
     */
    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('MEMBER','ADMIN')") // USER, ADMIN 권한 둘 다 호출 허용
    public ResponseEntity<CommonResponse> getMyUserInfo(@AuthenticationPrincipal User user) {
        logger.info(user.getUsername() + " " + user.getAuthorities());
        ResponseAccount.Information information = accountService.getMyAccountWithAuthorities();

        // 응답
        CommonResponse response = CommonResponse.builder()
                .success(true)
                .response(information)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 해당 계정의 가중치를 1 올린다. -> 리플레쉬 토큰 무효
     * @param userName
     * @return
     */
    @DeleteMapping("/{userName}/token")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<CommonResponse> authorize(@PathVariable String userName) {
        accountService.invalidateRefreshTokenByUsername(userName);
        //응답
        CommonResponse response = CommonResponse.builder()
                .success(true)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
