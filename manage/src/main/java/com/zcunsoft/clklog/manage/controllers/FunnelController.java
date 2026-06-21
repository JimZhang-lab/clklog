package com.zcunsoft.clklog.manage.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcunsoft.clklog.manage.entity.mysql.TblFunnel;
import com.zcunsoft.clklog.manage.models.R;
import com.zcunsoft.clklog.manage.models.analytics.PageResult;
import com.zcunsoft.clklog.manage.repository.mysql.FunnelRepository;
import com.zcunsoft.clklog.manage.utils.RequestParamUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping(path = "funnel")
@Tag(name = "漏斗配置", description = "漏斗配置")
public class FunnelController {

    @Resource
    private FunnelRepository funnelRepository;

    @Resource
    private ObjectMapper objectMapper;

    @Operation(summary = "分页获取漏斗列表")
    @RequestMapping(path = "/getPageList", method = RequestMethod.POST)
    public R<PageResult<TblFunnel>> getPageList(@RequestBody Map<String, Object> params) {
        int pageNum = Math.max(1, RequestParamUtils.getInt(params, "pageNum",
                RequestParamUtils.getInt(params, "pageIndex", 1)));
        int pageSize = Math.max(1, RequestParamUtils.getInt(params, "pageSize", 20));
        Page<TblFunnel> page = funnelRepository.findAll(buildSpec(params),
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime")));
        return R.ok(new PageResult<>(page.getContent(), page.getTotalElements()));
    }

    @Operation(summary = "获取漏斗详情")
    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public R<TblFunnel> get(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isBlank(id)) {
            return R.fail("缺少漏斗ID");
        }
        return R.ok(funnelRepository.findById(id).orElse(null));
    }

    @Operation(summary = "新增漏斗")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public R<TblFunnel> add(@RequestBody Map<String, Object> params) {
        TblFunnel funnel = new TblFunnel();
        funnel.setId(UUID.randomUUID().toString());
        funnel.setCreateTime(now());
        fillFunnel(funnel, params);
        funnel.setUpdateTime(now());
        return R.ok(funnelRepository.save(funnel));
    }

    @Operation(summary = "编辑漏斗")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public R<TblFunnel> edit(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isBlank(id)) {
            return R.fail("缺少漏斗ID");
        }
        TblFunnel funnel = funnelRepository.findById(id).orElse(null);
        if (funnel == null) {
            return R.fail("漏斗不存在");
        }
        fillFunnel(funnel, params);
        funnel.setUpdateTime(now());
        return R.ok(funnelRepository.save(funnel));
    }

    @Operation(summary = "删除漏斗")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        List<String> ids = RequestParamUtils.getStringList(params, "ids");
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isNotBlank(id)) {
            ids.add(id);
        }
        for (String item : ids) {
            funnelRepository.deleteById(item);
        }
        return R.ok(true);
    }

    private Specification<TblFunnel> buildSpec(Map<String, Object> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String projectName = RequestParamUtils.getString(params, "projectName");
            if (StringUtils.isNotBlank(projectName)) {
                predicates.add(cb.equal(root.get("projectName"), projectName));
            }
            String keyword = RequestParamUtils.getString(params, "keyword");
            String name = RequestParamUtils.getString(params, "name");
            String search = StringUtils.defaultIfBlank(keyword, name);
            if (StringUtils.isNotBlank(search)) {
                predicates.add(cb.like(root.get("name"), "%" + search + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void fillFunnel(TblFunnel funnel, Map<String, Object> params) {
        setIfPresent(params, "projectName", funnel::setProjectName);
        setIfPresent(params, "name", funnel::setName);
        setIfPresent(params, "status", funnel::setStatus);
        setIfPresent(params, "measurement", funnel::setMeasurement);
        setIfPresent(params, "windowUnit", funnel::setWindowUnit);
        setIfPresent(params, "description", funnel::setDescription);
        if (params.containsKey("windowNumeral")) {
            funnel.setWindowNumeral(RequestParamUtils.getInt(params, "windowNumeral", 24));
        }
        if (params.containsKey("isOpenRelation")) {
            funnel.setIsOpenRelation(RequestParamUtils.getBoolean(params, "isOpenRelation", false));
        }
        Object steps = params.get("steps");
        if (steps == null) {
            steps = params.get("stepList");
        }
        if (steps == null) {
            steps = params.get("events");
        }
        if (steps != null) {
            funnel.setSteps(toJson(steps));
        }
        Object query = params.get("query");
        if (query == null) {
            query = params.get("queryJson");
        }
        if (query != null) {
            funnel.setQueryJson(toJson(query));
        }
        if (StringUtils.isBlank(funnel.getStatus())) {
            funnel.setStatus("enabled");
        }
        if (StringUtils.isBlank(funnel.getMeasurement())) {
            funnel.setMeasurement("UV");
        }
        if (funnel.getWindowNumeral() == null || funnel.getWindowNumeral() <= 0) {
            funnel.setWindowNumeral(24);
        }
        if (StringUtils.isBlank(funnel.getWindowUnit())) {
            funnel.setWindowUnit("HOUR");
        }
        if (funnel.getIsOpenRelation() == null) {
            funnel.setIsOpenRelation(false);
        }
    }

    private String toJson(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private void setIfPresent(Map<String, Object> params, String key, java.util.function.Consumer<String> setter) {
        String value = RequestParamUtils.getString(params, key);
        if (value != null) {
            setter.accept(value);
        }
    }

    private Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}
