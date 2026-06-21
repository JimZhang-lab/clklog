package com.zcunsoft.clklog.manage.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "tbl_cdp_asset")
@Data
public class TblCdpAsset {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 80)
    private String projectName;

    @Column(length = 32)
    private String assetType;

    @Column(length = 160)
    private String displayName;

    @Column(length = 180)
    private String assetKey;

    @Column(length = 32)
    private String status;

    @Column(length = 32)
    private String createType;

    @Column(length = 32)
    private String updateMode;

    @Column(columnDefinition = "TEXT")
    private String distinctIds;

    @Column(columnDefinition = "LONGTEXT")
    private String ruleJson;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Long matchUserCount;

    @Column(length = 32)
    private String lastExecuteStatus;

    @Column
    private Timestamp lastExecuteTime;

    @Column(length = 80)
    private String createUser;

    @Column
    private Timestamp createTime;

    @Column
    private Timestamp updateTime;
}
