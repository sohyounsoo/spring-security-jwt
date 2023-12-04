package com.example.security.jwt.member.presentation;

import com.example.security.jwt.global.dto.CommonResponse;
import com.example.security.jwt.global.message.Message;
import com.example.security.jwt.member.facacde.MemberFacade;
import com.example.security.jwt.member.facacde.dto.RequestMemberFacade;
import com.example.security.jwt.member.facacde.dto.ResponseMemberFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;


@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberFacade memberFacade;

    @PostMapping
    public ResponseEntity<CommonResponse> signup(
            @Valid @RequestBody RequestMemberFacade.Register registerDto) {
        ResponseMemberFacade.Information userInfo = memberFacade.signup(registerDto);

        CommonResponse response = CommonResponse.builder()
                .success(true)
                .response(userInfo)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 계정 탈퇴
     */
    @DeleteMapping("/{userName}")
    @PreAuthorize("#userName == authentication.principal.username") //토큰값과 userName 검증
    public ResponseEntity<CommonResponse> deleteMember(@PathVariable String userName) {
        memberFacade.delete(userName);
        CommonResponse response = CommonResponse.builder()
                .success(true)
                .message(Message.ACCOUNT_DELETED_SUCCESSFULLY)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
