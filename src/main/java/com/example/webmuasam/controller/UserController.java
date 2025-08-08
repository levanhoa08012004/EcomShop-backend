package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Response.CreateUserResponse;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.dto.Response.UpdateUserResponse;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.UserRepository;
import com.example.webmuasam.service.UserService;
import com.example.webmuasam.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping
    @ApiMessage("add user success")
    public ResponseEntity<CreateUserResponse> CreateUser(@Valid @RequestBody User user)throws AppException {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.CreateUser(user));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get user success")
    public ResponseEntity<CreateUserResponse> GetUser(@PathVariable Long id) throws AppException {
        User user = this.userService.getUserById(id);
        CreateUserResponse userResponse = this.userService.convertToCreateUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping
    @ApiMessage("Get All user success")
    public ResponseEntity<ResultPaginationDTO> GetAllUser(@Filter Specification<User> spec , Pageable pageable) {
        return ResponseEntity.ok(this.userService.getAllUsers(spec,pageable));
    }

    @PutMapping
    @ApiMessage("Update user success")
    public ResponseEntity<UpdateUserResponse> UpdateUser(@Valid @RequestBody User user) throws AppException {
        return ResponseEntity.ok(this.userService.updateUser(user));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete user success")
    public ResponseEntity<String> DeleteUser(@PathVariable Long id) throws AppException {
        this.userService.deleteUser(id);
        return ResponseEntity.ok("success");
    }
}
