package com.zcunsoft.clklog.manage.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "tbl_menu")
@Data
public class TblMenu {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 160)
    private String title;

    @Column(length = 240)
    private String path;

    @Column(length = 240)
    private String component;

    @Column(columnDefinition = "TEXT")
    private String permissions;

    @Column(columnDefinition = "TEXT")
    private String roles;

    @Column(length = 32)
    private String status;

    @Column
    private Integer sortOrder;

    @Column
    private Boolean hidden;

    @Column
    private Boolean externalWindow;

    @Column
    private Timestamp createTime;

    @Column
    private Timestamp updateTime;
}
