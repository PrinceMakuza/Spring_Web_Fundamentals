package com.ecommerce.service;

import com.ecommerce.dto.UserDTO;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> getAllUsers(int page, int size, String sortBy, String sortDir, String name) {
        // Map 'date' to 'createdAt' for sorting
        String sortField = sortBy.equalsIgnoreCase("date") ? "createdAt" : sortBy;
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (name != null && !name.isEmpty()) {
            return userRepository.findAll((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"), pageable);
        }
        return userRepository.findAll(pageable);
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.name());
        user.setEmail(userDTO.email());
        user.setRole(userDTO.role());
        user.setPassword(userDTO.password()); // In a real app, hash this
        user.setLocation(userDTO.location());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(int id, UserDTO userDTO) {
        return userRepository.findById(id).map(user -> {
            user.setName(userDTO.name());
            user.setEmail(userDTO.email());
            user.setRole(userDTO.role());
            if (userDTO.password() != null) user.setPassword(userDTO.password());
            user.setLocation(userDTO.location());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }
}
