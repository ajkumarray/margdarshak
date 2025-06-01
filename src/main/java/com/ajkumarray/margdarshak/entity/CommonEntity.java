package com.ajkumarray.margdarshak.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ajkumarray.margdarshak.util.LocalDateTimeConverter;

@Getter
@Setter
@MappedSuperclass
public class CommonEntity {

    @Column(name = "created_at")
    @CreationTimestamp
    @Convert(converter = LocalDateTimeConverter.class)
    @Schema(description = "createdAt: creation date with timestamp")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Convert(converter = LocalDateTimeConverter.class)
    @Schema(description = "updatedAt: update date with timestamp")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @Convert(converter = LocalDateTimeConverter.class)
    @Schema(description = "deletedAt: deletion date with timestamp")
    private LocalDateTime deletedAt;

    @Column(name = "deleted")
    @Schema(description = "deleted: deletion flag")
    private boolean deleted;

}
