package com.zcunsoft.clklog.manage.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "tbl_api_key")
@Data
public class TblApiKey {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 80)
    private String projectName;

    @Column(length = 120)
    private String displayName;

    @Column(length = 32)
    private String keyPrefix;

    @Column(length = 160)
    private String keyMask;

    @Column(length = 128)
    private String keyHash;

    @Column(length = 32)
    private String status;

    @Column(length = 80)
    private String createUser;

    @Column
    private Timestamp expiresAt;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;
}
