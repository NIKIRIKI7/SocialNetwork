package org.mcniki.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.time.Instant;

@MappedSuperclass
public abstract class BaseModel extends Model {

    @Id
    long id;

    @Version
    long version;

    @WhenCreated
    Instant whenCreated;

    @WhenModified
    Instant whenModified;

    public long getId() {
        return id;
    }

    public BaseModel setId(long id) {
        this.id = id;
        return this;
    }

    public long getVersion() {
        return version;
    }

    public BaseModel setVersion(long version) {
        this.version = version;
        return this;
    }

    public Instant getWhenCreated() {
        return whenCreated;
    }

    public BaseModel setWhenCreated(Instant whenCreated) {
        this.whenCreated = whenCreated;
        return this;
    }

    public Instant getWhenModified() {
        return whenModified;
    }

    public BaseModel setWhenModified(Instant whenModified) {
        this.whenModified = whenModified;
        return this;
    }
}