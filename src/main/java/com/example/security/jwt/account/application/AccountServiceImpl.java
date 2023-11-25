package com.example.security.jwt.account.application;

import com.example.security.jwt.account.application.dto.RequestAccount;
import com.example.security.jwt.account.application.dto.ResponseAccount;
import com.example.security.jwt.account.domain.AccountErrorCode;
import com.example.security.jwt.account.domain.AccountRepository;
import com.example.security.jwt.account.domain.entity.Account;
import com.example.security.jwt.account.domain.entity.AccountAdapter;
import com.example.security.jwt.account.domain.entity.Authority;
import com.example.security.jwt.global.exception.ApplicationException;
import com.example.security.jwt.global.exception.CommonErrorCode;
import com.example.security.jwt.global.security.RefreshTokenProvider;
import com.example.security.jwt.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenProvider refreshTokenProvider;


    // username과 password로 사용자를 인증하여 액세스토큰과 리프레시 토큰을 반환한다.
    @Override
    public ResponseAccount.Token authenticate(String username, String password) {
        // 받아온 유저네임과 패스워드를 이용해 UsernamePasswordAuthenticationToken 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        //authenticationToken 객체를 통해 Authentication 객체 생성
        //이 과정에서 UserDetailsService 에서 우리가 재정의한 loadUserByUsername 매서드 호출
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증 정보를 기준으로 jwt access 토큰 생성
        String accessToken = tokenProvider.createToken(authentication);
        Date expiredTime = tokenProvider.getExpiredTime(accessToken); // 토큰 정보에서 만료 정보를 가져옴

        // 위에서 loadUserByUsername를 호출하였으므로 AccountAdapter가 시큐리티 컨텍스트에 저장되어 Account 엔티티 정보를 우리는 알 수 있음
        // 유저 정보에서 중치를 꺼내 리프레시 토큰 가중치에 할당, 나중에 액세스토큰 재발급 시도 시 유저정보 가중치 > 리프레시 토큰이라면 실패
        Long tokenWeight = ((AccountAdapter)authentication.getPrincipal()).getAccount().getTokenWeight();
        String refreshToken =  refreshTokenProvider.createToken(authentication, tokenWeight);


        return ResponseAccount.Token.builder()
                .accessToken(accessToken)
                .expiredTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expiredTime))
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    @Override
    public ResponseAccount.Information registerMember(RequestAccount.RegisterMember registerMemberDto) {
        Optional<Account> accountOptional = accountRepository.findOneWithAuthoritiesByUsername(registerMemberDto.username());

        if(accountOptional.isPresent()) {
            throw new ApplicationException(CommonErrorCode.CONFLICT, "이미 가입되어있는 유저");
        }

        // 이 유저는 권한이 ROLE_MEMBER
        // 이건 부팅 시 data.sql에서 INSERT로 디비에 반영해야 한다. 즉 디비에 존재하는 값이여야함
        Authority authority = Authority.builder()
                .authorityName("ROLE_MEMBER")
                .build();

        Account user = Account.builder()
                .username(registerMemberDto.username())
                .password(passwordEncoder.encode(registerMemberDto.password()))
                .nickname(registerMemberDto.nickname())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        // DB에 저장하고 그걸 DTO로 변환해서 반환, 예제라서 비번까지 다 보낸다.??
        return ResponseAccount.Information.of(accountRepository.save(user));
    }

    @Override
    public ResponseAccount.Information registerAdmin(RequestAccount.RegisterAdmin registerAdminDto) {
        Optional<Account> accountOptional = accountRepository.findOneWithAuthoritiesByUsername(registerAdminDto.username());

        if(accountOptional.isPresent()) {
            throw new ApplicationException(CommonErrorCode.CONFLICT, "이미 가입되어있는 유저");
        }

        //이건 부팅 시 data.sql에서 INSERT로 디비에 반영
        Authority authority = Authority.builder()
                .authorityName("ROLE_ADMIN")
                .build();

        Account user = Account.builder()
                .username(registerAdminDto.username())
                .password(passwordEncoder.encode(registerAdminDto.password()))
                .nickname(registerAdminDto.nickname())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        return ResponseAccount.Information.of(accountRepository.save(user));
    }

    @Transactional(readOnly = true)
    @Override
    public ResponseAccount.Information getAccountWithAuthorities(String username) {
        Account account = accountRepository.findOneWithAuthoritiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + "-> 찾을 수 없습니다."));

        return ResponseAccount.Information.of(account);
    }

    @Override
    public ResponseAccount.Token refreshToken(String refreshToken) {
        // 먼저 리프레시 토큰을 검증한다.
        if(!refreshTokenProvider.validateToken(refreshToken)) throw new ApplicationException(AccountErrorCode.INVALID_REFRESH_TOKEN);

        // 리프레시 토큰 값을 이용해 사용자를 꺼낸다.
        // refreshTokenProvider과 TokenProvider는 다른 서명키를 가지고 있기에 refreshTokenProvider를 써야한다.
        Authentication authentication = refreshTokenProvider.getAuthentication(refreshToken);
        Account account = accountRepository.findOneWithAuthoritiesByUsername(authentication.getName())
                .orElseThrow(()-> new UsernameNotFoundException(authentication.getName() + "을 찾을 수 없습니다."));

        //사용자 디비 값에 있는 것과 가중치 비교, 디비 가중치가 더 크다면 유효하지 않음
        if(account.getTokenWeight() > refreshTokenProvider.getTokenWeight(refreshToken)) throw new ApplicationException(AccountErrorCode.INVALID_REFRESH_TOKEN);

        // 리프레시 토큰에 담긴 값을 그대로 액세스 토큰 생성에 활용한다.
        String accessToken = tokenProvider.createToken(authentication);
        Date expiredTime = tokenProvider.getExpiredTime(accessToken); // 토큰 정보에서 만료 정보를 가져옴

        return ResponseAccount.Token.builder()
                .accessToken(accessToken)
                .expiredTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expiredTime))
                .refreshToken(refreshToken)
                .build();
    }

    // 현재 시큐리티 컨텍스트에 저장된 username에 해당하는 정보를 가져온다.


}
