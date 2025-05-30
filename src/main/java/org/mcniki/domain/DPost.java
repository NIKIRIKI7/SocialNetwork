package org.mcniki.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime; // Или Instant, если нет нужды в таймзоне

@Entity
@Table(name = "d_post")
public class DPost extends BaseModel {

    @NotNull
    @ManyToOne(optional = false) // Ebean автоматически создаст user_id
    private DUser user;

    @NotNull
    @Lob // Для длинного текста
    @Column(nullable = false)
    private String postText;

    @NotNull
    @Column(nullable = false)
    private OffsetDateTime postDate;

    @NotNull
    @Column(nullable = false)
    private boolean published = false; // По умолчанию не опубликован

    public DPost(DUser user, String postText, OffsetDateTime postDate) {
        this.user = user;
        this.postText = postText;
        this.postDate = postDate;
    }

    // Getters
    public DUser getUser() {
        return user;
    }

    public String getPostText() {
        return postText;
    }

    public OffsetDateTime getPostDate() {
        return postDate;
    }

    public boolean isPublished() {
        return published;
    }

    // Fluid Setters
    public DPost setUser(DUser user) {
        this.user = user;
        return this;
    }

    // Пакетный доступ для установки user из DUser, чтобы избежать зацикливания
    void setUserInternal(DUser user) {
        this.user = user;
    }


    public DPost setPostText(String postText) {
        this.postText = postText;
        return this;
    }

    public DPost setPostDate(OffsetDateTime postDate) {
        this.postDate = postDate;
        return this;
    }

    public DPost setPublished(boolean published) {
        this.published = published;
        return this;
    }

    @Override
    public String toString() {
        return "DPost{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") + // Избегаем user.toString()
                ", postText='" + (postText != null ? postText.substring(0, Math.min(postText.length(), 20)) + "..." : "null") + '\'' +
                ", postDate=" + postDate +
                ", published=" + published +
                ", version=" + version +
                '}';
    }
}