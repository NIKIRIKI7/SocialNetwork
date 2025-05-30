package org.mcniki.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "d_user")
public class DUser extends BaseModel {

    @NotNull
    @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String login;

    @NotNull
    @Size(max = 255)
    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @NotNull
    @Column(nullable = false)
    private boolean active = true; // По умолчанию активен

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<DPost> posts = new ArrayList<>();

    public DUser(String login, String email) {
        this.login = login;
        this.email = email;
    }

    // Getters
    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public List<DPost> getPosts() {
        return posts;
    }

    // Fluid Setters
    public DUser setLogin(String login) {
        this.login = login;
        return this;
    }

    public DUser setEmail(String email) {
        this.email = email;
        return this;
    }

    public DUser setActive(boolean active) {
        this.active = active;
        return this;
    }

    public DUser setPosts(List<DPost> posts) {
        this.posts = posts;
        return this;
    }

    public DUser addPost(DPost post) {
        this.posts.add(post);
        post.setUserInternal(this); // Важно для двунаправленной связи
        return this;
    }


    @Override
    public String toString() {
        // Не используем getters для избежания lazy loading в дебаггере
        return "DUser{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", version=" + version +
                '}';
    }
}