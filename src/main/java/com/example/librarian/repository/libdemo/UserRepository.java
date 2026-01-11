package com.example.librarian.repository.libdemo;

import com.example.librarian.entity.libdemo.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
