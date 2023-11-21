package com.example.security.jwt.admin.presentation;

import com.example.security.jwt.admin.facade.AdminFacade;
import com.example.security.jwt.admin.facade.dto.RequestAdminFacade;
import com.example.security.jwt.admin.facade.dto.ResponseAdminFacade;
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
public class AdminController {

    private final AdminFacade adminFacade;

    @PostMapping("/admin/members")
    public ResponseEntity<CommonResponse> signup(@Valid @RequestBody RequestAdminFacade.Register AdminRegisterDto) {
        ResponseAdminFacade.Information userInfo = adminFacade.signup(AdminRegisterDto);

        CommonResponse response = CommonResponse.builder()
                .success(true)
                .response(userInfo)
                .build();

        return ResponseEntity.ok(response);
    }
}
