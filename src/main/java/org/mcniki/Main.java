package org.mcniki;

import io.ebean.DB;
import io.ebean.Database;
import org.mcniki.domain.DPost;
import org.mcniki.domain.DUser;
import org.mcniki.service.PostService;
import org.mcniki.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Database server = DB.getDefault();
        logger.info("Ebean server initialized: {}", server.name());

        UserService userService = new UserService(server);
        PostService postService = new PostService(server);

        // Создание пользователей
        DUser user1 = new DUser("john_doe", "john.doe@mcniki.com");
        user1.setActive(true);
        userService.saveUser(user1);
        logger.info("Saved user: {}", user1);


        DUser user2 = new DUser("jane_smith", "jane.smith@mcniki.com");
        userService.saveUser(user2);
        logger.info("Saved user: {}", user2);

        // Создание постов
        // Использование reference bean для user1
        DPost post1 = new DPost(DB.reference(DUser.class, user1.getId()), "First post by John!", OffsetDateTime.now());
        post1.setPublished(true);
        // postService.savePost(post1); // Можно так, если нет каскадного сохранения от user

        DPost post2 = new DPost(DB.reference(DUser.class, user1.getId()), "Another post from John.", OffsetDateTime.now().plusHours(1));
        // postService.savePost(post2);

        // Добавление постов пользователю и сохранение пользователя (с каскадом для постов)
        user1.addPost(post1);
        user1.addPost(post2);
        userService.updateUser(user1); // userService.saveUser(user1) если это новое сохранение


        DPost post3 = new DPost(DB.reference(DUser.class, user2.getId()), "Jane's first contribution.", OffsetDateTime.now().plusMinutes(30));
        post3.setPublished(true);
        user2.addPost(post3);
        userService.updateUser(user2);
        // postService.savePost(post3);

        logger.info("Saved post1 for user1: {}", post1);
        logger.info("Saved post2 for user1: {}", post2);
        logger.info("Saved post3 for user2: {}", post3);


        // Поиск пользователя
        DUser foundUser = userService.findUserById(user1.getId());
        if (foundUser != null) {
            logger.info("Found user by ID {}: {}", user1.getId(), foundUser.getLogin());
            logger.info("Posts for {}:", foundUser.getLogin());
            // Lazy loading сработает здесь, если посты не были загружены ранее
            List<DPost> userPosts = postService.findPostsByUser(foundUser); // или foundUser.getPosts()
            userPosts.forEach(p -> logger.info(" - {}", p.getPostText()));
        }

        // Поиск по части логина
        logger.info("Users with 'john' in login:");
        List<DUser> johns = userService.findUsersByLoginContains("john");
        johns.forEach(u -> logger.info(" - User: {}", u.getLogin()));

        // Поиск всех постов
        logger.info("All posts:");
        List<DPost> allPosts = postService.findAllPosts();
        allPosts.forEach(p -> logger.info(" - Post by {}: {}", p.getUser().getLogin(), p.getPostText()));

        // Обновление
        if (foundUser != null) {
            foundUser.setEmail("john.doe.updated@mcniki.com");
            userService.updateUser(foundUser);
            logger.info("Updated user: {}", userService.findUserById(foundUser.getId()));
        }


        logger.info("Application finished.");
    }
}
