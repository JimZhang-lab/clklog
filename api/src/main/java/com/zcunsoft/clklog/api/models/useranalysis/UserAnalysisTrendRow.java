package com.zcunsoft.clklog.api.models.useranalysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户趋势数据")
@Data
public class UserAnalysisTrendRow {

    @Schema(description = "统计时间")
    private String statTime;

    @Schema(description = "用户数")
    private int userCount;

    @Schema(description = "累计用户数")
    private int cumulativeUserCount;

    @Schema(description = "活跃用户数")
    private int activeUserCount;

    @Schema(description = "留存用户数")
    private int retainedUserCount;

    @Schema(description = "流失用户数")
    private int churnUserCount;

    @Schema(description = "回流用户数")
    private int revisitUserCount;

    @Schema(description = "沉默用户数")
    private int silentUserCount;

    @Schema(description = "老用户数")
    private int oldUserCount;

    @Schema(description = "新用户数")
    private int newUserCount;
}
