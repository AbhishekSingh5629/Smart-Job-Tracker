package com.example.JobTracker.Service;

import com.example.JobTracker.DTO.*;
import com.example.JobTracker.CustomException.*;
import com.example.JobTracker.Mapper.UserMapper;
import com.example.JobTracker.Model.User;
import com.example.JobTracker.Repository.UserRepository;
//import com.example.JobTracker.util.JwtUtil;
import com.example.JobTracker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;


    @Autowired
    private JwtUtil jwtUtil;

    public UserResponse registerUser(RegisterRequest dto) {
        validateRegistration(dto);
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole("USER");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return  userMapper.toresponse(savedUser);
    }
    private void validateRegistration(RegisterRequest dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            errors.put("email", "Email is required");
        } else if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.put("email", "Invalid email format");
        } else if (userRepository.existsByEmail(dto.getEmail())) {
            errors.put("email", "Email already exists");
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            errors.put("name", "Name is required");
        } else if (dto.getName().length() < 2) {
            errors.put("name", "Name must be at least 2 characters");
        }

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

    public LoginResponse loginUser(LoginRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
//        String token="dummy-token-for-testing";

        UserResponse userResponse = userMapper.toresponse(user);
        return new LoginResponse(true, "Login successful", token, userResponse);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toresponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toresponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return userMapper.toresponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return userMapper.toresponse(updatedUser);
    }

    public void changePassword(Long id, ChangePasswordRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getPassword().equals(dto.getOldPassword())) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        user.setPassword(dto.getNewPassword());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return userMapper.toresponse(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}