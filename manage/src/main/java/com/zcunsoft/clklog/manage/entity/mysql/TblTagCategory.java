package com.zcunsoft.clklog.manage.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "tbl_tag_category")
@Data
public class TblTagCategory {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 80)
    private String projectName;

    @Column(length = 36)
    private String parentId;

    @Column(length = 120)
    private String displayName;

    @Column
    private Integer sortOrder;

    @Column(length = 32)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Timestamp createTime;

    @Column
    private Timestamp updateTime;
}
