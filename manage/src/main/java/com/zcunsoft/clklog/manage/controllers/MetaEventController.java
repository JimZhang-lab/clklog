package com.zcunsoft.clklog.manage.controllers;

import com.zcunsoft.clklog.manage.entity.mysql.TblMetaEvent;
import com.zcunsoft.clklog.manage.entity.mysql.TblNormalProperty;
import com.zcunsoft.clklog.manage.models.R;
import com.zcunsoft.clklog.manage.models.analytics.PageResult;
import com.zcunsoft.clklog.manage.repository.mysql.MetaEventRepository;
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
@RequestMapping(path = "meta/event")
@Tag(name = "事件管理", description = "事件管理")
public class MetaEventController {

    @Resource
    private MetaEventRepository metaEventRepository;

    @Resource
    private NormalPropertyRepository normalPropertyRepository;

    @Operation(summary = "分页获取事件列表")
    @RequestMapping(path = "/getPageList", method = RequestMethod.POST)
    public R<PageResult<TblMetaEvent>> getPageList(@RequestBody Map<String, Object> params) {
        int pageNum = Math.max(1, RequestParamUtils.getInt(params, "pageNum",
                RequestParamUtils.getInt(params, "pageIndex", 1)));
        int pageSize = Math.max(1, RequestParamUtils.getInt(params, "pageSize", 20));
        Page<TblMetaEvent> page = metaEventRepository.findAll(buildSpec(params),
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime")));
        return R.ok(new PageResult<>(page.getContent(), page.getTotalElements()));
    }

    @Operation(summary = "获取事件详情")
    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public R<TblMetaEvent> get(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isBlank(id)) {
            return R.fail("缺少事件ID");
        }
        return R.ok(metaEventRepository.findById(id).orElse(null));
    }

    @Operation(summary = "新增事件")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public R<TblMetaEvent> add(@RequestBody Map<String, Object> params) {
        TblMetaEvent event = new TblMetaEvent();
        event.setId(UUID.randomUUID().toString());
        event.setCreateTime(now());
        fillEvent(event, params);
        event.setUpdateTime(now());
        return R.ok(metaEventRepository.save(event));
    }

    @Operation(summary = "编辑事件")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public R<TblMetaEvent> edit(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isBlank(id)) {
            return R.fail("缺少事件ID");
        }
        TblMetaEvent event = metaEventRepository.findById(id).orElse(null);
        if (event == null) {
            return R.fail("事件不存在");
        }
        fillEvent(event, params);
        event.setUpdateTime(now());
        return R.ok(metaEventRepository.save(event));
    }

    @Operation(summary = "删除事件")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        List<String> ids = RequestParamUtils.getStringList(params, "ids");
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isNotBlank(id)) {
            ids.add(id);
        }
        for (String item : ids) {
            metaEventRepository.deleteById(item);
        }
        return R.ok(true);
    }

    @Operation(summary = "获取事件分组列表")
    @RequestMapping(path = "/getGroupNameList", method = RequestMethod.POST)
    public R<List<String>> getGroupNameList(@RequestBody Map<String, Object> params) {
        return R.ok(metaEventRepository.findAll(buildSpec(params)).stream()
                .map(TblMetaEvent::getGroupName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
    }

    @Operation(summary = "获取事件标签列表")
    @RequestMapping(path = "/getTagList", method = RequestMethod.POST)
    public R<List<String>> getTagList(@RequestBody Map<String, Object> params) {
        return R.ok(metaEventRepository.findAll(buildSpec(params)).stream()
                .map(TblMetaEvent::getTag)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
    }

    @Operation(summary = "获取事件可用属性分组")
    @RequestMapping(path = "/getGroupedProperties", method = RequestMethod.POST)
    public R<List<Map<String, Object>>> getGroupedProperties(@RequestBody Map<String, Object> params) {
        List<TblNormalProperty> properties = normalPropertyRepository.findAll(propertySpec(params));
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

    @Operation(summary = "设置热门属性")
    @RequestMapping(path = "/setHotProp", method = RequestMethod.POST)
    public R<Boolean> setHotProp(@RequestBody Map<String, Object> params) {
        return R.ok(true);
    }

    @Operation(summary = "导入事件数据")
    @RequestMapping(path = "/importData", method = RequestMethod.POST)
    public R<Boolean> importData() {
        return R.ok(true);
    }

    private Specification<TblMetaEvent> buildSpec(Map<String, Object> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String projectName = RequestParamUtils.getString(params, "projectName");
            if (StringUtils.isNotBlank(projectName)) {
                predicates.add(cb.equal(root.get("projectName"), projectName));
            }
            String keyword = RequestParamUtils.getString(params, "keyword");
            String name = RequestParamUtils.getString(params, "name");
            String eventName = RequestParamUtils.getString(params, "eventName");
            String search = StringUtils.defaultIfBlank(keyword, StringUtils.defaultIfBlank(name, eventName));
            if (StringUtils.isNotBlank(search)) {
                String like = "%" + search + "%";
                predicates.add(cb.or(cb.like(root.get("eventName"), like), cb.like(root.get("displayName"), like)));
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

    private Specification<TblNormalProperty> propertySpec(Map<String, Object> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String projectName = RequestParamUtils.getString(params, "projectName");
            if (StringUtils.isNotBlank(projectName)) {
                predicates.add(cb.equal(root.get("projectName"), projectName));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void fillEvent(TblMetaEvent event, Map<String, Object> params) {
        setIfPresent(params, "projectName", event::setProjectName);
        setIfPresent(params, "eventName", event::setEventName);
        setIfPresent(params, "displayName", event::setDisplayName);
        setIfPresent(params, "groupName", event::setGroupName);
        setIfPresent(params, "tag", event::setTag);
        setIfPresent(params, "status", event::setStatus);
        setIfPresent(params, "description", event::setDescription);
        List<String> propertyIds = RequestParamUtils.getStringList(params, "propertyIds");
        if (!propertyIds.isEmpty()) {
            event.setPropertyIds(String.join(",", propertyIds));
        }
        if (StringUtils.isBlank(event.getStatus())) {
            event.setStatus("enabled");
        }
        if (StringUtils.isBlank(event.getDisplayName())) {
            event.setDisplayName(event.getEventName());
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
