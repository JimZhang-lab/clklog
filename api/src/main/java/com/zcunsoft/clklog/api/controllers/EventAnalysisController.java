package com.zcunsoft.clklog.api.controllers;

import com.zcunsoft.clklog.api.models.ResponseBase;
import com.zcunsoft.clklog.api.models.eventanalysis.CustomAnalysisRequest;
import com.zcunsoft.clklog.api.models.eventanalysis.FunnelAnalysisRequest;
import com.zcunsoft.clklog.api.models.eventanalysis.FunnelAnalysisResponseData;
import com.zcunsoft.clklog.api.services.EventAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "eventanalysis")
@Tag(name = "事件分析", description = "事件分析")
public class EventAnalysisController {

    @Resource
    private EventAnalysisService eventAnalysisService;

    @Operation(summary = "自定义事件分析")
    @RequestMapping(path = "/customAnalysis", method = RequestMethod.POST)
    public ResponseBase<List<Map<String, Object>>> customAnalysis(@RequestBody CustomAnalysisRequest request) {
        return ResponseBase.ok(eventAnalysisService.customAnalysis(request));
    }

    @Operation(summary = "漏斗分析")
    @RequestMapping(path = "/funnelAnalysis", method = RequestMethod.POST)
    public ResponseBase<FunnelAnalysisResponseData> funnelAnalysis(@RequestBody FunnelAnalysisRequest request) {
        if (request.getSteps() == null || request.getSteps().isEmpty()) {
            return ResponseBase.fail("请至少选择一个漏斗步骤");
        }
        return ResponseBase.ok(eventAnalysisService.funnelAnalysis(request));
    }
}
