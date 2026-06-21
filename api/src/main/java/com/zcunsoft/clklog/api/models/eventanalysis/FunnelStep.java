package com.zcunsoft.clklog.api.models.eventanalysis;

import lombok.Data;

@Data
public class FunnelStep {

    private String name;

    private String customName;

    private String event;

    private String eventName;

    private String relevanceField;

    private String relevanceFieldType;

    private Object filter;
}
