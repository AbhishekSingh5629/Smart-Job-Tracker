package com.example.JobTracker.Service;


import com.example.JobTracker.DTO.*;
import com.example.JobTracker.CustomException.*;
import com.example.JobTracker.Mapper.AdminMapper;
import com.example.JobTracker.Mapper.UserMapper;
import com.example.JobTracker.Model.Admin;
import com.example.JobTracker.Model.User;
import com.example.JobTracker.Repository.AdminRepository;
import com.example.JobTracker.Repository.UserRepository;
import com.example.JobTracker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


//import com.example.JobTracker.util.JwtUtil;
@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Validate registration data
    private void validateRegistration(RegisterRequest dto) {
        Map<String, String> errors = new HashMap<>();

        // Validate email
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            errors.put("email", "Email is required");
        } else if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.put("email", "Invalid email format");
        } else if (adminRepository.existsByEmail(dto.getEmail())) {
            errors.put("email", "Email already exists");
        }

        // Validate name
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            errors.put("name", "Name is required");
        } else if (dto.getName().length() < 2) {
            errors.put("name", "Name must be at least 2 characters");
        }

        // Validate password
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            errors.put("password", "Password is required");
        } else if (dto.getPassword().length() < 8) {
            errors.put("password", "Password must be at least 8 characters");
        } else if (!dto.getPassword().matches(".*\\d.*")) {
            errors.put("password", "Password must include a number");
        } else if (!dto.getPassword().matches(".*[A-Z].*")) {
            errors.put("password", "Password must include an uppercase letter");
        }


        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }


    public LoginResponse loginAdmin(LoginRequest dto) {
        Admin admin = adminRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));

        if (!admin.getPassword().equals(dto.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (!admin.getIsActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        admin.setLastLogin(LocalDateTime.now());
        adminRepository.save(admin);

        String token = jwtUtil.generateToken(admin.getId(), admin.getEmail(), admin.getRole());
//          String token="dummy-token-for-testing";

        AdminResponse adminResponse = adminMapper.toResponse(admin);
        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setToken(token);
        // Convert AdminResponse to UserResponse for LoginResponse
        UserResponse userResponse = new UserResponse();
        userResponse.setId(adminResponse.getId());
        userResponse.setName(adminResponse.getName());
        userResponse.setEmail(adminResponse.getEmail());
        userResponse.setRole(adminResponse.getRole());
        response.setUser(userResponse);

        return response;
    }

    public List<AdminResponse> getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        return admins.stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    public AdminResponse getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with id: " + id));
        return adminMapper.toResponse(admin);
    }

    public void deleteAdmin(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new UserNotFoundException("Admin not found with id: " + id);
        }
        adminRepository.deleteById(id);
    }
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toresponse)
                .collect(Collectors.toList());
    }

    // Get user by ID (Admin power)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toresponse(user);
    }

    // Make user an admin
    public UserResponse makeUserAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setRole("ADMIN");
        User updatedUser = userRepository.save(user);
        return userMapper.toresponse(updatedUser);
    }

    // Revoke admin access from user
    public UserResponse revokeAdminAccess(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setRole("USER");
        User updatedUser = userRepository.save(user);
        return userMapper.toresponse(updatedUser);
    }

    // Deactivate user account
    public UserResponse deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return userMapper.toresponse(updatedUser);
    }

    // Activate user account
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return userMapper.toresponse(updatedUser);
    }

    // Delete user (Admin power)
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }

    // Get admin statistics
    public AdminStatsResponse getAdminStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .count();
        long inactiveUsers = totalUsers - activeUsers;
        long adminUsers = userRepository.findAll().stream()
                .filter(user -> "ADMIN".equals(user.getRole()))
                .count();

        AdminStatsResponse stats = new AdminStatsResponse();
        stats.setTotalUsers(totalUsers);
        stats.setActiveUsers(activeUsers);
        stats.setInactiveUsers(inactiveUsers);
        stats.setAdminUsers(adminUsers);

        return stats;
    }
}
