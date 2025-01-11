package de.unibayreuth.se.taskboard.business.impl;

import de.unibayreuth.se.taskboard.business.domain.User;
import de.unibayreuth.se.taskboard.business.exceptions.MalformedRequestException;
import de.unibayreuth.se.taskboard.business.exceptions.UserNotFoundException;
import de.unibayreuth.se.taskboard.business.exceptions.DuplicateNameException;
import de.unibayreuth.se.taskboard.business.ports.UserPersistenceService;
import de.unibayreuth.se.taskboard.business.ports.UserService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserPersistenceService userPersistenceService;

    @Override
    public void clear() {
        userPersistenceService.clear();
    }                   

    @Override
    @NonNull
    public User create(@NonNull User user) throws MalformedRequestException {
        if (user.getId() != null) {
            throw new MalformedRequestException("User ID must not be set.");
        }
        return upsert(user);
    }

    @Override
    @NonNull
    public List<User> getAll() {
        return userPersistenceService.getAll();
    }

    @Override
    @NonNull
    public User getById(@NonNull UUID id) throws UserNotFoundException {
        return userPersistenceService.getById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " does not exist."));
    }

    @Override
    @NonNull
    public User upsert(@NonNull User user) throws UserNotFoundException, DuplicateNameException {
        if (user.getId() != null) {
            verifyUserExists(user.getId());
        }
        List<User> existingUsers = userPersistenceService.getAll();
        for (User existingUser : existingUsers) {
            if (existingUser.getName().equals(user.getName()) && 
                !existingUser.getId().equals(user.getId())) {
                throw new DuplicateNameException("User with name " + user.getName() + " already exists.");
            }
        }
        return userPersistenceService.upsert(user);
    }

    private void verifyUserExists(@NonNull UUID id) throws UserNotFoundException {
        userPersistenceService.getById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " does not exist."));
    }
}

