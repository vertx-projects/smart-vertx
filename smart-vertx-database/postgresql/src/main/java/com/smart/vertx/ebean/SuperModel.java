package com.smart.vertx.ebean;

import com.alibaba.fastjson2.annotation.JSONField;
import io.ebean.Model;
import io.ebean.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.ebean
 * @date 2022/9/23 17:08
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public class SuperModel extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Version
    Long version;
    @SoftDelete
    boolean deleted;
    @Column(name = "create_time", nullable = false, updatable = false)
    @WhenCreated
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Column(name = "update_time", insertable = false)
    @WhenModified
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @WhoCreated
    private String createBy;
    @WhoModified
    private String updateBy;
}
