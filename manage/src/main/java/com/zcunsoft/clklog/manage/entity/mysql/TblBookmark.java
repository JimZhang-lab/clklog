package com.zcunsoft.clklog.manage.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "tbl_bookmark")
@Data
public class TblBookmark {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 80)
    private String projectName;

    @Column(length = 160)
    private String name;

    @Column(length = 64)
    private String analysisType;

    @Column(length = 32)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String queryJson;

    @Column(length = 80)
    private String createUser;

    @Column
    private Timestamp createTime;

    @Column
    private Timestamp updateTime;
}
