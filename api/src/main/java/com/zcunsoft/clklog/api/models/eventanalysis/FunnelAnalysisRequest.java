package com.zcunsoft.clklog.api.models.eventanalysis;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FunnelAnalysisRequest {

    private String projectName;

    private String startTime;

    private String endTime;

    private String startDate;

    private String endDate;

    private String measurement = "UV";

    private Integer windowNumeral = 24;

    private String windowUnit = "HOUR";

    private FunnelWindow window;

    private List<String> channel = new ArrayList<>();

    private List<FunnelStep> steps = new ArrayList<>();
}
