package com.example.security.jwt.member.presentation;

import com.example.security.jwt.global.dto.CommonResponse;
import com.example.security.jwt.member.facacde.MemberFacade;
import com.example.security.jwt.member.facacde.dto.RequestMemberFacade;
import com.example.security.jwt.member.facacde.dto.ResponseMemberFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MemberController {

    private final MemberFacade memberFacade;

    @PostMapping("/members")
    public ResponseEntity<CommonResponse> signup(
            @Valid @RequestBody RequestMemberFacade.Register registerDto) {
        ResponseMemberFacade.Information userInfo = memberFacade.signup(registerDto);

        CommonResponse response = CommonResponse.builder()
                .success(true)
                .response(userInfo)
                .build();

        return ResponseEntity.ok(response);
    }
}
