package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Request.LoginRequest;
import com.example.webmuasam.dto.Response.CreateUserResponse;
import com.example.webmuasam.dto.Response.LoginResponse;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.UserService;
import com.example.webmuasam.util.SecurityUtil;
import com.example.webmuasam.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    @Value("${jwt.refreshable-duration}")
    private long refreshToken_duration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
                          UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);


        SecurityContextHolder.getContext().setAuthentication(authentication);
        LoginResponse responseLogin = new LoginResponse();

        LoginResponse res = new LoginResponse();
        User currentUserDB = null;
        try {
            currentUserDB = this.userService.handleGetUserByUserName(loginRequest.getUsername());
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
        if (currentUserDB != null) {
            LoginResponse.UserLogin userLogin = new LoginResponse.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getUsername(),
                     currentUserDB.getRole());
            res.setUser(userLogin);
        }

        //create Token
        String access_token = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(access_token);
        String refreshToken = this.securityUtil.createRefreshToken(loginRequest.getUsername(), res);

        //update token
        this.userService.updateUserToken(refreshToken, loginRequest.getUsername());

        //set cookies
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshToken_duration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(res);
    }

    @GetMapping("/account")
    @ApiMessage("fetch account")
    public ResponseEntity<LoginResponse.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        User currentUserDB = null;
        try {
            currentUserDB = this.userService.handleGetUserByUserName(email);
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
        LoginResponse.UserLogin userLogin = new LoginResponse.UserLogin();
        LoginResponse.UserGetAccount userGetAccount = new LoginResponse.UserGetAccount();
        if(currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(currentUserDB.getEmail());
            userLogin.setName(currentUserDB.getUsername());
            userLogin.setRole(currentUserDB.getRole());
            userGetAccount.setUser(userLogin);
        }
        return ResponseEntity.ok(userGetAccount);
    }

    @GetMapping("/refresh")
    @ApiMessage("get user by refresh token")
    public ResponseEntity<LoginResponse> getRefreshToken(@CookieValue(name="refresh_token" ,defaultValue = "abc") String refreshToken)
    throws AppException {
        if(refreshToken.equals("abc")){
            throw new AppException("bạn không có refresh token ở cookie");
        }
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        User currentUserDB = this.userService.getUserByFreshTokenAndEmail(email, refreshToken);
        if(currentUserDB == null) {
            throw new AppException("Token không hợp lệ");
        }
        LoginResponse loginResponse = new LoginResponse();
        User currentUser = this.userService.handleGetUserByUserName(email);
        if(currentUser != null) {
            LoginResponse.UserLogin userLogin = new LoginResponse.UserLogin(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getUsername(),
                    currentUser.getRole()
            );
            loginResponse.setUser(userLogin);

        }
        String accessToken = this.securityUtil.createAccessToken(email, loginResponse);

        loginResponse.setAccessToken(accessToken);

        this.userService.updateUserToken(accessToken, email);

        ResponseCookie responseCookie = ResponseCookie.from("refresh_token",email)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshToken_duration)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,responseCookie.toString()).body(loginResponse);

    }
    @PostMapping("/logout")
    @ApiMessage("logout user")
    public ResponseEntity<Void> logout()
            throws AppException{
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ?
                SecurityUtil.getCurrentUserLogin().get() : "";
        if(email.equals("")){
            throw new AppException("Access token không hợp lệ");
        }

        this.userService.updateUserToken(null,email);
        ResponseCookie deleteRefreshToken = ResponseCookie.from("refresh_token",null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,deleteRefreshToken.toString()).build();
    }

    @PostMapping("/register")
    @ApiMessage("Register a new user")
    public ResponseEntity<CreateUserResponse> register(@Valid @RequestBody User user)throws AppException{

        return ResponseEntity.ok(this.userService.CreateUser(user));
    }


}