package com.mertao.authenticationjwtexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mertao.authenticationjwtexample.config.Role;
import com.mertao.authenticationjwtexample.entity.User;
import com.mertao.authenticationjwtexample.repository.UserRepository;

@Service
public class UserService {
	@Autowired
	UserRepository userRepository;
	
	public User save(User user) {
		return userRepository.save(user);
	}
	
	public User create(User user) {
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new RuntimeException("Пользователь с таким именем уже существует");
		}
		
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new RuntimeException("Пользователь с таким email уже существует");
		}
		
		return userRepository.save(user);
	}
	
	public User getByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Пользователь " + username + " не найден."));
	}
	
	/**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
	public UserDetailsService userDetailsService() {
		return this::getByUsername;
	}
	
	/**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
	public User getCurrentUser() {
		var username = SecurityContextHolder.getContext().getAuthentication().getName();
		return getByUsername(username);
	}
	
	/**
     * Выдача прав администратора текущему пользователю
     * <p>
     * Нужен для демонстрации
     */
	@Deprecated
    public void getAdmin() {
        var user = getCurrentUser();
        user.getRoles().add(Role.ROLE_ADMIN);
        save(user);
    }
}
