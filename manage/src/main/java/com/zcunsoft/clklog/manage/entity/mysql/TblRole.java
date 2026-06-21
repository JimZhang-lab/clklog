package com.zcunsoft.clklog.manage.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "tbl_role")
@Data
public class TblRole {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 80)
    private String roleName;

    @Column(length = 120)
    private String displayName;

    @Column(length = 64)
    private String roleType;

    @Column(length = 32)
    private String status;

    @Column
    private Integer sortOrder;

    @Column
    private Timestamp createTime;

    @Column
    private Timestamp updateTime;
}
