package com.samatov.security.webflux.project.service;

import com.samatov.security.webflux.project.enums.Role;
import com.samatov.security.webflux.project.enums.Status;
import com.samatov.security.webflux.project.exception.NotFoundException;
import com.samatov.security.webflux.project.exception.UserException;
import com.samatov.security.webflux.project.model.User;
import com.samatov.security.webflux.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> getUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь с таким айди не найден")))
                .doOnSuccess(user -> log.info("Получение пользователя по id: {}", id))
                .doOnError(user -> log.error("Пользователь с таким id: {} не найден", id, user));
    }

    public Flux<User> getAllUser() {
        return userRepository.findAll()
                .collectList()
                .flatMapMany(users -> {
                    if (users.isEmpty()) {
                        log.error("Пользователи не найдены");
                        return Flux.error(new UserException("Ошибка запроса пользователи не найдены"));
                    } else {
                        log.info("Получение всех пользователей {}", users);
                        return Flux.fromIterable(users);
                    }
                });
    }

    public Mono<User> updateUser(User user) {
        return userRepository.findById(user.getId())
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь с таким айди не найден")))
                .flatMap(existingUser -> {

                    existingUser.setUsername(user.getUsername());
                    existingUser.setRoles(user.getRoles());
                    existingUser.setStatus(user.getStatus());

                    if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                    }

                    return userRepository.save(existingUser)
                            .switchIfEmpty(Mono.error(new UserException("Ошибка запроса пользователь не сохранен")));
                })
                .doOnSuccess(updatedUser -> log.info("Пользователь с айди: {} успешно обновлен", user.getId()))
                .doOnError(error -> log.error("Не удалось обновить пользователя {} ", user.getId(), error));
    }


    public Mono<Void> deleteUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь с таким айди не найден")))
                .flatMap(user -> userRepository.deleteById(id))
                .doOnSuccess(unused -> log.info("Пользователь с айди id: {} удален", id))
                .doOnError(error -> log.error("Не удалось удалить пользователя с айди: {}", id, error));
    }

    public Mono<User> registerUser(User user) {
        return userRepository
                .save(
                        user.toBuilder()
                                .password(passwordEncoder.encode(user.getPassword()))
                                .roles(Role.USER)
                                .status(Status.ACTIVE)
                                .build())
                .doOnSuccess(user1 -> log.info("Пользователь сохранен {}", user1))
                .doOnError(error -> log.error("Не удалось сохранить пользователя {}", error));
    }

    public Mono<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserException("Пользователь с таким именем не найден")))
                .doOnSuccess(user -> log.info("Получения пользователя с именем {}", username))
                .doOnError(user -> log.error("Не удалось получить пользователя с именем {}", username));
    }
}
