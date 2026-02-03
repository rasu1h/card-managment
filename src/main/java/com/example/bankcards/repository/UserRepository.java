package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(@NotBlank(message = "Имя пользователя обязательно") @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов") String username);

    boolean existsByPhoneNumber(@NotBlank(message = "Номер телефона обязателен") @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Некорректный формат номера телефона") String phoneNumber);

   }

