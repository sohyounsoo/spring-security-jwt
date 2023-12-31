package com.example.security.jwt.account.application;

import com.example.security.jwt.account.application.dto.RequestAccount;
import com.example.security.jwt.account.application.dto.ResponseAccount;

public interface AccountService {

    ResponseAccount.Token authenticate(String username, String password);

    ResponseAccount.Information registerMember(RequestAccount.RegisterMember registerMemberDto);

    ResponseAccount.Information registerAdmin(RequestAccount.RegisterAdmin registerAdminDto);

    ResponseAccount.Information getAccountWithAuthorities(String username);

    ResponseAccount.Token refreshToken(String toekn);

    ResponseAccount.Information getMyAccountWithAuthorities();

    void invalidateRefreshTokenByUsername(String userName);

    void deleteMember(String userName);
}
