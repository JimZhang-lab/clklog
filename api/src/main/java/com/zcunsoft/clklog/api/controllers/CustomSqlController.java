package com.zcunsoft.clklog.api.controllers;

import com.zcunsoft.clklog.api.models.ResponseBase;
import com.zcunsoft.clklog.api.models.customsql.CustomSqlRequest;
import com.zcunsoft.clklog.api.models.customsql.CustomSqlResponse;
import com.zcunsoft.clklog.api.services.CustomSqlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(path = "customsql")
@Tag(name = "自定义SQL查询", description = "自定义SQL查询")
public class CustomSqlController {

    @Resource
    private CustomSqlService customSqlService;

    @Operation(summary = "执行只读ClickHouse SQL")
    @RequestMapping(path = "/query", method = RequestMethod.POST)
    public ResponseBase<CustomSqlResponse> query(@RequestBody CustomSqlRequest request) {
        try {
            return ResponseBase.ok(customSqlService.query(request));
        } catch (IllegalArgumentException ex) {
            return ResponseBase.fail(ex.getMessage());
        } catch (Exception ex) {
            return ResponseBase.fail("查询执行失败，请检查SQL和字段名称");
        }
    }
}
