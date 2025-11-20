/*
 * @ {#} BaseDocument.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/*
 * @description: Base document class for MongoDB entities
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Getter
@Setter
public abstract class BaseDocument {
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @CreatedBy
    @Field("created_by")
    private String createdBy;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Field("updated_by")
    private String updatedBy;

    @Field("is_deleted")
    private boolean isDeleted;

    @Field("deleted_at")
    private LocalDateTime deletedAt;
}
