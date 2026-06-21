package com.zcunsoft.clklog.api.controllers;

import com.zcunsoft.clklog.api.models.useranalysis.UserAnalysisRequest;
import com.zcunsoft.clklog.api.models.useranalysis.UserAnalysisTrendResponse;
import com.zcunsoft.clklog.api.models.useranalysis.UserRetentionResponse;
import com.zcunsoft.clklog.api.services.UserAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "user")
@Tag(name = "用户分析", description = "用户分析")
public class UserAnalysisController {

    @Resource
    private UserAnalysisService userAnalysisService;

    @Operation(summary = "获取活跃用户趋势")
    @RequestMapping(path = "/getUserActiveTrend", method = RequestMethod.POST)
    public UserAnalysisTrendResponse getUserActiveTrend(@RequestBody UserAnalysisRequest request, HttpServletRequest httpRequest) {
        return userAnalysisService.getUserActiveTrend(request);
    }

    @Operation(summary = "获取留存用户趋势")
    @RequestMapping(path = "/getUserRemainTrend", method = RequestMethod.POST)
    public UserRetentionResponse getUserRemainTrend(@RequestBody UserAnalysisRequest request, HttpServletRequest httpRequest) {
        return userAnalysisService.getUserRemainTrend(request);
    }

    @Operation(summary = "获取流失用户趋势")
    @RequestMapping(path = "/getUserChurnTrend", method = RequestMethod.POST)
    public UserAnalysisTrendResponse getUserChurnTrend(@RequestBody UserAnalysisRequest request, HttpServletRequest httpRequest) {
        return userAnalysisService.getUserLifeTrend(request);
    }

    @Operation(summary = "获取回流与沉默用户趋势")
    @RequestMapping(path = "/getUserRevisitAndSilentTrend", method = RequestMethod.POST)
    public UserAnalysisTrendResponse getUserRevisitAndSilentTrend(@RequestBody UserAnalysisRequest request, HttpServletRequest httpRequest) {
        return userAnalysisService.getUserLifeTrend(request);
    }
}
