package com.zcunsoft.clklog.manage.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "tbl_funnel")
@Data
public class TblFunnel {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 80)
    private String projectName;

    @Column(length = 120)
    private String name;

    @Column(length = 32)
    private String status;

    @Column(length = 16)
    private String measurement;

    @Column
    private Integer windowNumeral;

    @Column(length = 16)
    private String windowUnit;

    @Column
    private Boolean isOpenRelation;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String steps;

    @Column(columnDefinition = "LONGTEXT")
    private String queryJson;

    @Column
    private Timestamp createTime;

    @Column
    private Timestamp updateTime;
}
