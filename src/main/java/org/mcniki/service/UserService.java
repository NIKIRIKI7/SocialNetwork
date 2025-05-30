package org.mcniki.service;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import org.mcniki.domain.DUser;
import org.mcniki.domain.query.QDUser; // Сгенерируется после первой компиляции

import java.util.List;

public class UserService {

    private final Database database;

    public UserService(Database database) {
        this.database = database;
    }

    public void saveUser(DUser user) {
        database.save(user);
    }

    public DUser findUserById(long id) {
        return database.find(DUser.class, id);
    }

    public List<DUser> findAllUsers() {
        return new QDUser(database)
                .findList();
    }

    public List<DUser> findUsersByLoginContains(String searchTerm) {
        return new QDUser(database)
                .login.icontains(searchTerm)
                .orderBy().login.asc()
                .findList();
    }

    public boolean deleteUserById(long id) {
        return database.delete(DUser.class, id) > 0;
    }

    public void updateUser(DUser user) {
        database.update(user);
    }
}