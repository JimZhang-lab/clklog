package com.zcunsoft.clklog.manage.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "tbl_user_tag")
@Data
public class TblUserTag {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 80)
    private String projectName;

    @Column(length = 36)
    private String categoryId;

    @Column(length = 120)
    private String displayName;

    @Column(length = 160)
    private String tagKey;

    @Column(length = 32)
    private String dataType;

    @Column(length = 32)
    private String createType;

    @Column(length = 32)
    private String updateMode;

    @Column(length = 32)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String ruleJson;

    @Column
    private Long matchUserCount;

    @Column(length = 32)
    private String lastExecuteStatus;

    @Column
    private Timestamp lastExecuteTime;

    @Column
    private Timestamp createTime;

    @Column
    private Timestamp updateTime;
}
