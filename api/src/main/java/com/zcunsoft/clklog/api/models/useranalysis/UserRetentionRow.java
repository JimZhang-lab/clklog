package com.zcunsoft.clklog.api.models.useranalysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户留存数据")
@Data
public class UserRetentionRow {

    @Schema(description = "统计时间")
    private String statTime;

    @Schema(description = "用户数")
    private int userCount;

    private int day1Count;
    private int day2Count;
    private int day3Count;
    private int day4Count;
    private int day5Count;
    private int day6Count;
    private int day7Count;

    private float day1Rate;
    private float day2Rate;
    private float day3Rate;
    private float day4Rate;
    private float day5Rate;
    private float day6Rate;
    private float day7Rate;
}
