package com.gmao.service;

import com.gmao.dto.UserDTO;
import com.gmao.entity.User;
import com.gmao.enums.Role;
import com.gmao.exception.BusinessException;
import com.gmao.exception.ResourceNotFoundException;
import com.gmao.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getTechnicians() {
        return userRepository.findByRole(Role.TECHNICIAN).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toDTO(user);
    }

    public UserDTO createUser(UserDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new BusinessException("Email is required");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email already in use: " + dto.getEmail());
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessException("Password is required");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setAddress(dto.getAddress());
        user.setIdNumber(dto.getIdNumber());
        user.setProfilePicture(dto.getProfilePicture());

        User saved = userRepository.save(user);
        logger.info("Created user: {}", saved.getEmail());
        return toDTO(saved);
    }

    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (dto.getEmail() != null && !user.getEmail().equals(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email already in use: " + dto.getEmail());
        }

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getIdNumber() != null) user.setIdNumber(dto.getIdNumber());
        if (dto.getProfilePicture() != null) user.setProfilePicture(dto.getProfilePicture());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User saved = userRepository.save(user);
        logger.info("Updated user: {}", saved.getEmail());
        return toDTO(saved);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        logger.info("Deleted user with id: {}", id);
    }

    /**
     * Update own profile — field restrictions enforced per role:
     * - TECHNICIAN: only profilePicture + password
     * - RESPONSABLE: profilePicture + password (no idNumber, no email change)
     * - ADMIN: all fields including idNumber
     *
     * @param id       target user id (must be the same as the authenticated user unless ADMIN)
     * @param dto      fields to update
     * @param caller   the authenticated user making the request
     */
    public UserDTO updateProfile(Long id, UserDTO dto, User caller) {
        // Security: non-admin can only update their own profile
        if (caller.getRole() != Role.ADMIN && !caller.getId().equals(id)) {
            throw new BusinessException("You can only update your own profile");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        Role callerRole = caller.getRole();

        // Profile picture allowed for all
        if (dto.getProfilePicture() != null) {
            user.setProfilePicture(dto.getProfilePicture());
        }

        // Password allowed for all
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Name allowed for ADMIN and RESPONSABLE
        if ((callerRole == Role.ADMIN || callerRole == Role.RESPONSABLE) && dto.getName() != null) {
            user.setName(dto.getName());
        }

        // Address allowed for ADMIN and RESPONSABLE
        if ((callerRole == Role.ADMIN || callerRole == Role.RESPONSABLE) && dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }

        // idNumber: ADMIN only
        if (callerRole == Role.ADMIN && dto.getIdNumber() != null) {
            user.setIdNumber(dto.getIdNumber());
        }

        // Email: read-only for all (never updated via profile endpoint)

        return toDTO(userRepository.save(user));
    }

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setAddress(user.getAddress());
        dto.setIdNumber(user.getIdNumber());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setTwoFactorEnabled(user.isTwoFactorEnabled());
        return dto;
    }
}