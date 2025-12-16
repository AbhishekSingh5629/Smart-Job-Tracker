package com.example.JobTracker.Controller;

import com.example.JobTracker.CustomException.UnauthorizedAccessException;
import com.example.JobTracker.DTO.*;
import com.example.JobTracker.Service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @ModelAttribute
    public void verifyAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedAccessException("Admin access required");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginAdmin(@RequestBody LoginRequest dto) {
        LoginResponse response = adminService.loginAdmin(dto);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/admins")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        List<AdminResponse> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }


    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse user = adminService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{userId}/make-admin")
    public ResponseEntity<Map<String, Object>> makeUserAdmin(@PathVariable Long userId) {
        UserResponse user = adminService.makeUserAdmin(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User granted admin privileges");
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/revoke-admin")
    public ResponseEntity<Map<String, Object>> revokeAdminAccess(@PathVariable Long userId) {
        UserResponse user = adminService.revokeAdminAccess(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Admin privileges revoked");
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable Long userId) {
        UserResponse user = adminService.deactivateUser(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User account deactivated");
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable Long userId) {
        UserResponse user = adminService.activateUser(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User account activated");
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User deleted successfully");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/statistics")
    public ResponseEntity<AdminStatsResponse> getStatistics() {
        AdminStatsResponse stats = adminService.getAdminStatistics();
        return ResponseEntity.ok(stats);
    }



    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Admin service is running");
        return ResponseEntity.ok(response);
    }
}