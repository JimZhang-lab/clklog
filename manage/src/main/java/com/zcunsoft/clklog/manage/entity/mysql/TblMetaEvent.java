package com.zcunsoft.clklog.manage.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "tbl_meta_event")
@Data
public class TblMetaEvent {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 80)
    private String projectName;

    @Column(length = 120)
    private String eventName;

    @Column(length = 120)
    private String displayName;

    @Column(length = 120)
    private String groupName;

    @Column(length = 120)
    private String tag;

    @Column(length = 32)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String propertyIds;

    @Column
    private Timestamp createTime;

    @Column
    private Timestamp updateTime;
}
