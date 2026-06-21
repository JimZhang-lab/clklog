package com.zcunsoft.clklog.api.models.eventanalysis;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FunnelAnalysisResponseData {

    private long totalUser;

    private String measurement;

    private long windowSeconds;

    private List<FunnelStepResult> steps = new ArrayList<>();
}
