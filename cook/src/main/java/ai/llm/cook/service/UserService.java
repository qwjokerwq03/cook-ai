package ai.llm.cook.service;

import ai.llm.cook.exception.CustomException;
import ai.llm.cook.model.User;
import ai.llm.cook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new CustomException("Email already in use", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found with id: " + id, HttpStatus.NOT_FOUND));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found with email: " + email, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);

        if (userDetails.getFullName() != null) {
            user.setFullName(userDetails.getFullName());
        }

        if (userDetails.getEmail() != null && !user.getEmail().equals(userDetails.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new CustomException("Email already in use", HttpStatus.BAD_REQUEST);
            }
            user.setEmail(userDetails.getEmail());
        }

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    public boolean authenticateUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);

        return user.filter(value -> passwordEncoder.matches(password, value.getPassword())).isPresent();

    }
}