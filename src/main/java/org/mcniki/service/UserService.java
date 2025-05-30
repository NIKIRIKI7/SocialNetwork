package org.mcniki.service;

import io.ebean.Database;
import org.mcniki.domain.DUser;
import org.mcniki.domain.query.QDUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final Database database;

    public UserService(Database database) {
        if (database == null) {
            logger.error("UserService initialized with a null Database instance!");
            throw new IllegalArgumentException("Database cannot be null for UserService");
        }
        this.database = database;
    }

    public void saveUser(DUser user) {
        if (user == null) {
            logger.warn("saveUser called with a null DUser object.");
            return;
        }
        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            logger.warn("Attempted to save user with null or empty login: {}", user);
            return;
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            logger.warn("Attempted to save user with null or empty email: {}", user);
            return;
        }
        database.save(user);
        logger.debug("Saved user: {}", user.getLogin());
    }

    public DUser findUserById(long id) {
        if (id <= 0) {
            logger.warn("findUserById called with invalid ID: {}", id);
            return null;
        }
        return database.find(DUser.class, id);
    }

    public List<DUser> findAllUsers() {
        try {
            return new QDUser(database)
                    .findList();
        } catch (Exception e) {
            logger.error("Error in findAllUsers: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<DUser> findUsersByLoginContains(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.warn("findUsersByLoginContains called with null or empty searchTerm. Returning all users instead.");
            return findAllUsers();
        }

        QDUser qdUser = new QDUser(database);

        try {
            if (qdUser.login == null) {
                logger.error("CRITICAL: QDUser.login is null. This indicates an issue with Ebean query bean generation or enhancement. Cannot search by login.");
                return Collections.emptyList();
            }
            if (qdUser.orderBy() == null || qdUser.orderBy().login == null) {
                logger.error("CRITICAL: QDUser.orderBy() or QDUser.orderBy().login is null. This indicates an issue with Ebean query bean generation or enhancement. Returning unordered results.");
                return qdUser.login.icontains(searchTerm).findList();
            }
        } catch (NullPointerException e) {
            logger.error("CRITICAL: NullPointerException during query setup in findUsersByLoginContains. This indicates an issue with Ebean query bean generation or enhancement.", e);
            return Collections.emptyList(); // Or attempt a simpler query
        }


        return qdUser
                .login.icontains(searchTerm)
                .orderBy().login.asc()
                .findList();
    }

    public boolean deleteUserById(long id) {
        if (id <= 0) {
            logger.warn("deleteUserById called with invalid ID: {}", id);
            return false;
        }

        boolean deleted = database.delete(DUser.class, id) > 0;
        if (deleted) {
            logger.debug("Deleted user with ID: {}", id);
        } else {
            logger.warn("Failed to delete user with ID: {} (user might not exist)", id);
        }
        return deleted;
    }

    public void updateUser(DUser user) {
        if (user == null) {
            logger.warn("updateUser called with a null DUser object.");
            return;
        }
        if (user.getId() <= 0) { // User must have an ID to be updated
            logger.warn("updateUser called for a DUser object with invalid ID: {}", user);
            return;
        }
        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            logger.warn("Attempted to update user (ID: {}) with null or empty login.", user.getId());
            return;
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            logger.warn("Attempted to update user (ID: {}) with null or empty email.", user.getId());
            return;
        }
        database.update(user);
        logger.debug("Updated user: {}", user.getLogin());
    }
}