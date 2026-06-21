package com.zcunsoft.clklog.api.controllers;

import com.zcunsoft.clklog.api.models.ResponseBase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "task")
@Tag(name = "异步任务", description = "异步任务")
public class TaskController {

    @Operation(summary = "查询任务状态")
    @RequestMapping(path = "/query", method = RequestMethod.POST)
    public ResponseBase<Map<String, Object>> query(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "done");
        data.put("finished", true);
        data.put("taskId", request == null ? null : request.get("taskId"));
        return ResponseBase.ok(data);
    }

    @Operation(summary = "获取任务结果")
    @RequestMapping(path = "/fetch", method = RequestMethod.POST)
    public ResponseBase<Map<String, Object>> fetch(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "done");
        data.put("result", null);
        data.put("taskId", request == null ? null : request.get("taskId"));
        return ResponseBase.ok(data);
    }
}
