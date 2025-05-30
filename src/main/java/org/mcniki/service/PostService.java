package org.mcniki.service;

import io.ebean.Database;
import org.mcniki.domain.DPost;
import org.mcniki.domain.DUser;
import org.mcniki.domain.query.QDPost;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final Database database;

    public PostService(Database database) {
        this.database = database;
    }

    public void savePost(DPost post) {
        if (post == null) {
            logger.warn("savePost called with null post.");
            return;
        }

        database.save(post);
    }

    public DPost findPostById(long id) {
        return database.find(DPost.class, id);
    }

    public List<DPost> findAllPosts() {
        QDPost qdPost = new QDPost(database);

        try {
            if (qdPost.orderBy() == null || qdPost.orderBy().postDate == null) {
                logger.error("CRITICAL: QDPost.orderBy() or QDPost.orderBy().postDate is null. " +
                        "This indicates an issue with Ebean query bean generation or enhancement. " +
                        "Falling back to an unordered list of posts.");
                return qdPost.findList(); // Find without ordering
            }
        } catch (NullPointerException e) {
            logger.error("CRITICAL: NullPointerException during orderBy setup in findAllPosts. " +
                    "This indicates an issue with Ebean query bean generation or enhancement. " +
                    "Falling back to an unordered list of posts.", e);
            return qdPost.findList(); // Find without ordering
        }

        return qdPost
                .orderBy().postDate.desc() // Original line: PostService.java:27
                .findList();
    }

    public List<DPost> findPostsByUser(DUser user) {
        if (user == null) {
            logger.warn("findPostsByUser called with a null DUser object. Returning empty list.");
            return Collections.emptyList();
        }

        QDPost qdPost = new QDPost(database);


        if (qdPost.user == null) {
            logger.error("CRITICAL: QDPost.user is null. " +
                    "This indicates an issue with Ebean query bean generation or enhancement. " +
                    "Cannot query posts by user. Returning empty list.");
            return Collections.emptyList();
        }
        try {
            if (qdPost.orderBy() == null || qdPost.orderBy().postDate == null) {
                logger.error("CRITICAL: QDPost.orderBy() or QDPost.orderBy().postDate is null in findPostsByUser. " +
                        "Falling back to an unordered list of posts for the user.");
                return qdPost.user.equalTo(user).findList(); // Find without specific ordering
            }
        } catch (NullPointerException e) {
            logger.error("CRITICAL: NullPointerException during orderBy setup in findPostsByUser. " +
                    "Falling back to an unordered list of posts for the user.", e);
            return qdPost.user.equalTo(user).findList(); // Find without specific ordering
        }


        return qdPost
                .user.equalTo(user)
                .orderBy().postDate.desc()
                .findList();
    }

    public List<DPost> findPostsByUserId(long userId) {
        if (userId == 0) {
            logger.warn("findPostsByUserId called with user ID 0. Returning empty list.");
            return Collections.emptyList();
        }

        QDPost qdPost = new QDPost(database);
        if (qdPost.user == null || qdPost.user.id == null) {
            logger.error("CRITICAL: QDPost.user or QDPost.user.id is null. " +
                    "This indicates an issue with Ebean query bean generation or enhancement. " +
                    "Cannot query posts by user ID. Returning empty list.");
            return Collections.emptyList();
        }
        try {
            if (qdPost.orderBy() == null || qdPost.orderBy().postDate == null) {
                logger.error("CRITICAL: QDPost.orderBy() or QDPost.orderBy().postDate is null in findPostsByUserId. " +
                        "Falling back to an unordered list of posts for the user ID.");
                return qdPost.user.id.equalTo(userId).findList();
            }
        } catch (NullPointerException e) {
            logger.error("CRITICAL: NullPointerException during orderBy setup in findPostsByUserId. " +
                    "Falling back to an unordered list of posts for the user ID.", e);
            return qdPost.user.id.equalTo(userId).findList();
        }

        return qdPost
                .user.id.equalTo(userId)
                .orderBy().postDate.desc()
                .findList();
    }

    public boolean deletePostById(long id) {
        if (id == 0) {
            logger.warn("deletePostById called with ID 0.");
            return false;
        }
        return database.delete(DPost.class, id) > 0;
    }

    public void updatePost(DPost post) {
        if (post == null) {
            logger.warn("updatePost called with null post.");
            return;
        }
        database.update(post);
    }
}