package com.zcunsoft.clklog.api.models.useranalysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "用户分析请求")
@Data
public class UserAnalysisRequest {

    @Schema(description = "时间粒度", example = "day")
    private String timeType = "day";

    @Schema(description = "渠道")
    private List<String> channel = new ArrayList<>();

    @Schema(description = "国家或地区")
    private List<String> country;

    @Schema(description = "地域")
    private List<String> province;

    @Schema(description = "访客类型")
    private String visitorType;

    @Schema(description = "开始时间", example = "2026-06-15")
    private String startTime;

    @Schema(description = "结束时间", example = "2026-06-21")
    private String endTime;

    @Schema(description = "应用名")
    private String projectName;
}
