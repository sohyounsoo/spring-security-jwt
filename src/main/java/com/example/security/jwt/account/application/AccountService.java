package com.example.security.jwt.account.application;

import com.example.security.jwt.account.application.dto.ResponseAccount;

public interface AccountService {

    ResponseAccount.Token authenticate(String username, String password);

}
