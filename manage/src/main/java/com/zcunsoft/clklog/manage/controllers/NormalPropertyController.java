package com.zcunsoft.clklog.manage.controllers;

import com.zcunsoft.clklog.manage.entity.mysql.TblNormalProperty;
import com.zcunsoft.clklog.manage.models.R;
import com.zcunsoft.clklog.manage.models.analytics.PageResult;
import com.zcunsoft.clklog.manage.repository.mysql.NormalPropertyRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "normal/property")
@Tag(name = "属性管理", description = "属性管理")
public class NormalPropertyController {

    @Resource
    private NormalPropertyRepository normalPropertyRepository;

    @Operation(summary = "分页获取属性列表")
    @RequestMapping(path = "/getPageList", method = RequestMethod.POST)
    public R<PageResult<TblNormalProperty>> getPageList(@RequestBody Map<String, Object> params) {
        int pageNum = Math.max(1, RequestParamUtils.getInt(params, "pageNum",
                RequestParamUtils.getInt(params, "pageIndex", 1)));
        int pageSize = Math.max(1, RequestParamUtils.getInt(params, "pageSize", 20));
        Page<TblNormalProperty> page = normalPropertyRepository.findAll(buildSpec(params),
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime")));
        return R.ok(new PageResult<>(page.getContent(), page.getTotalElements()));
    }

    @Operation(summary = "获取属性详情")
    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public R<TblNormalProperty> get(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isBlank(id)) {
            return R.fail("缺少属性ID");
        }
        return R.ok(normalPropertyRepository.findById(id).orElse(null));
    }

    @Operation(summary = "新增属性")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public R<TblNormalProperty> add(@RequestBody Map<String, Object> params) {
        TblNormalProperty property = new TblNormalProperty();
        property.setId(UUID.randomUUID().toString());
        property.setCreateTime(now());
        fillProperty(property, params);
        property.setUpdateTime(now());
        return R.ok(normalPropertyRepository.save(property));
    }

    @Operation(summary = "编辑属性")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public R<TblNormalProperty> edit(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isBlank(id)) {
            return R.fail("缺少属性ID");
        }
        TblNormalProperty property = normalPropertyRepository.findById(id).orElse(null);
        if (property == null) {
            return R.fail("属性不存在");
        }
        fillProperty(property, params);
        property.setUpdateTime(now());
        return R.ok(normalPropertyRepository.save(property));
    }

    @Operation(summary = "删除属性")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        List<String> ids = RequestParamUtils.getStringList(params, "ids");
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isNotBlank(id)) {
            ids.add(id);
        }
        for (String item : ids) {
            normalPropertyRepository.deleteById(item);
        }
        return R.ok(true);
    }

    @Operation(summary = "获取属性列表")
    @RequestMapping(path = "/getListNoGroup", method = RequestMethod.POST)
    public R<List<TblNormalProperty>> getListNoGroup(@RequestBody Map<String, Object> params) {
        return R.ok(normalPropertyRepository.findAll(buildSpec(params)));
    }

    @Operation(summary = "获取属性分组列表")
    @RequestMapping(path = "/getList", method = RequestMethod.POST)
    public R<List<Map<String, Object>>> getList(@RequestBody Map<String, Object> params) {
        List<TblNormalProperty> properties = normalPropertyRepository.findAll(buildSpec(params));
        Map<String, List<TblNormalProperty>> grouped = properties.stream()
                .collect(Collectors.groupingBy(item -> StringUtils.defaultIfBlank(item.getGroupName(), "默认分组"),
                        LinkedHashMap::new, Collectors.toList()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<TblNormalProperty>> entry : grouped.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("groupName", entry.getKey());
            item.put("children", entry.getValue());
            result.add(item);
        }
        return R.ok(result);
    }

    @Operation(summary = "获取通用属性列表")
    @RequestMapping(path = "/getCommonPropertyList", method = RequestMethod.POST)
    public R<List<TblNormalProperty>> getCommonPropertyList(@RequestBody Map<String, Object> params) {
        Specification<TblNormalProperty> spec = buildSpec(params).and((root, query, cb) ->
                cb.or(cb.equal(root.get("commonProperty"), true), cb.isNull(root.get("commonProperty"))));
        return R.ok(normalPropertyRepository.findAll(spec));
    }

    @Operation(summary = "获取属性分组名列表")
    @RequestMapping(path = "/getGroupNameList", method = RequestMethod.POST)
    public R<List<String>> getGroupNameList(@RequestBody Map<String, Object> params) {
        return R.ok(normalPropertyRepository.findAll(buildSpec(params)).stream()
                .map(TblNormalProperty::getGroupName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
    }

    @Operation(summary = "获取属性标签列表")
    @RequestMapping(path = "/getTagList", method = RequestMethod.POST)
    public R<List<String>> getTagList(@RequestBody Map<String, Object> params) {
        return R.ok(normalPropertyRepository.findAll(buildSpec(params)).stream()
                .map(TblNormalProperty::getTag)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
    }

    @Operation(summary = "导入用户属性")
    @RequestMapping(path = "/import", method = RequestMethod.POST)
    public R<Boolean> importData() {
        return R.ok(true);
    }

    private Specification<TblNormalProperty> buildSpec(Map<String, Object> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String projectName = RequestParamUtils.getString(params, "projectName");
            if (StringUtils.isNotBlank(projectName)) {
                predicates.add(cb.equal(root.get("projectName"), projectName));
            }
            String keyword = RequestParamUtils.getString(params, "keyword");
            String name = RequestParamUtils.getString(params, "name");
            String propertyName = RequestParamUtils.getString(params, "propertyName");
            String search = StringUtils.defaultIfBlank(keyword, StringUtils.defaultIfBlank(name, propertyName));
            if (StringUtils.isNotBlank(search)) {
                String like = "%" + search + "%";
                predicates.add(cb.or(cb.like(root.get("propertyName"), like), cb.like(root.get("displayName"), like)));
            }
            String groupName = RequestParamUtils.getString(params, "groupName");
            if (StringUtils.isNotBlank(groupName)) {
                predicates.add(cb.equal(root.get("groupName"), groupName));
            }
            String tag = RequestParamUtils.getString(params, "tag");
            if (StringUtils.isNotBlank(tag)) {
                predicates.add(cb.equal(root.get("tag"), tag));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void fillProperty(TblNormalProperty property, Map<String, Object> params) {
        setIfPresent(params, "projectName", property::setProjectName);
        setIfPresent(params, "propertyName", property::setPropertyName);
        setIfPresent(params, "displayName", property::setDisplayName);
        setIfPresent(params, "groupName", property::setGroupName);
        setIfPresent(params, "tag", property::setTag);
        setIfPresent(params, "dataType", property::setDataType);
        setIfPresent(params, "status", property::setStatus);
        setIfPresent(params, "description", property::setDescription);
        if (params.containsKey("commonProperty") || params.containsKey("common")) {
            property.setCommonProperty(RequestParamUtils.getBoolean(params, "commonProperty",
                    RequestParamUtils.getBoolean(params, "common", false)));
        }
        if (StringUtils.isBlank(property.getStatus())) {
            property.setStatus("enabled");
        }
        if (StringUtils.isBlank(property.getDataType())) {
            property.setDataType("string");
        }
        if (StringUtils.isBlank(property.getDisplayName())) {
            property.setDisplayName(property.getPropertyName());
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
