package com.zcunsoft.clklog.api.models.eventanalysis;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CustomAnalysisRequest {

    private String projectName;

    private String startTime;

    private String endTime;

    private List<String> channel = new ArrayList<>();

    private List<String> events = new ArrayList<>();
}
