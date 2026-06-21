package com.zcunsoft.clklog.api.models.eventanalysis;

import lombok.Data;

@Data
public class FunnelStepResult {

    private String name;

    private String event;

    private long userCount;

    private float stepRate;

    private float totalRate;
}
