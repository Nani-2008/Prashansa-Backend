package com.prashansa.web;

import com.prashansa.dto.CreateUserRequest;
import com.prashansa.dto.RolePatchRequest;
import com.prashansa.dto.UserDto;
import com.prashansa.entity.AppUser;
import com.prashansa.service.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> list(@RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user) {
        requireAdmin(user);
        return userService.listAll().stream().map(UserDto::from).toList();
    }

    @PostMapping
    public UserDto create(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user, @RequestBody CreateUserRequest body) {
        requireAdmin(user);
        try {
            return UserDto.from(userService.createUser(body));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/{id}/role")
    public UserDto patchRole(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user,
            @RequestBody RolePatchRequest body) {
        requireAdmin(user);
        return UserDto.from(userService.updateRole(id, body.role()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user) {
        requireAdmin(user);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private static void requireAdmin(AppUser user) {
        if (!"admin".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
    }
}
