package org.mcniki.service;

import io.ebean.Database;
import org.mcniki.domain.DPost;
import org.mcniki.domain.DUser;
import org.mcniki.domain.query.QDPost; // Сгенерируется

import java.util.List;

public class PostService {
    private final Database database;

    public PostService(Database database) {
        this.database = database;
    }

    public void savePost(DPost post) {
        database.save(post);
    }

    public DPost findPostById(long id) {
        return database.find(DPost.class, id);
    }

    public List<DPost> findAllPosts() {
        return new QDPost(database)
                .orderBy().postDate.desc()
                .findList();
    }

    public List<DPost> findPostsByUser(DUser user) {
        return new QDPost(database)
                .user.equalTo(user) // или .user.id.equalTo(user.getId())
                .orderBy().postDate.desc()
                .findList();
    }

    public List<DPost> findPostsByUserId(long userId) {
        return new QDPost(database)
                .user.id.equalTo(userId)
                .orderBy().postDate.desc()
                .findList();
    }


    public boolean deletePostById(long id) {
        return database.delete(DPost.class, id) > 0;
    }

    public void updatePost(DPost post) {
        database.update(post);
    }
}