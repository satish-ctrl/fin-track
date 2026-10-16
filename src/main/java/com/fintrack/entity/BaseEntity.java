package com.fintrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime created;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updated;

    @PrePersist
    protected void onCreate(){
        OffsetDateTime now = OffsetDateTime.now();
        this.created = now;
        this.updated = now;
    }

    @PreUpdate
    public void onUpdate(){
        this.updated = OffsetDateTime.now();
    }
}
