package com.zcunsoft.clklog.manage.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "tbl_user_tag_assignment")
@Data
public class TblUserTagAssignment {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 80)
    private String projectName;

    @Column(length = 36)
    private String tagId;

    @Column(length = 160)
    private String distinctId;

    @Column(length = 500)
    private String tagValue;

    @Column
    private Timestamp createTime;

    @Column
    private Timestamp updateTime;
}
