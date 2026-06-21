package com.zcunsoft.clklog.manage.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcunsoft.clklog.manage.entity.mysql.TblTagCategory;
import com.zcunsoft.clklog.manage.entity.mysql.TblUserTag;
import com.zcunsoft.clklog.manage.entity.mysql.TblUserTagAssignment;
import com.zcunsoft.clklog.manage.models.R;
import com.zcunsoft.clklog.manage.models.analytics.PageResult;
import com.zcunsoft.clklog.manage.repository.mysql.TagCategoryRepository;
import com.zcunsoft.clklog.manage.repository.mysql.UserTagAssignmentRepository;
import com.zcunsoft.clklog.manage.repository.mysql.UserTagRepository;
import com.zcunsoft.clklog.manage.utils.RequestParamUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "tag")
@Tag(name = "用户标签", description = "用户标签")
public class UserTagController {

    @Resource
    private UserTagRepository userTagRepository;

    @Resource
    private UserTagAssignmentRepository assignmentRepository;

    @Resource
    private TagCategoryRepository tagCategoryRepository;

    @Resource
    private ObjectMapper objectMapper;

    @Operation(summary = "分页获取用户标签")
    @RequestMapping(path = "/getPageList", method = RequestMethod.POST)
    public R<PageResult<Map<String, Object>>> getPageList(@RequestBody Map<String, Object> params) {
        int pageNum = Math.max(1, RequestParamUtils.getInt(params, "pageNum",
                RequestParamUtils.getInt(params, "pageIndex", 1)));
        int pageSize = Math.max(1, RequestParamUtils.getInt(params, "pageSize", 20));
        Page<TblUserTag> page = userTagRepository.findAll(buildSpec(params),
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime")));
        Map<String, String> categoryNames = categoryNameMap();
        List<Map<String, Object>> rows = page.getContent().stream()
                .map(item -> tagView(item, categoryNames))
                .collect(Collectors.toList());
        return R.ok(new PageResult<>(rows, page.getTotalElements()));
    }

    @Operation(summary = "获取用户标签详情")
    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public R<Map<String, Object>> get(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        TblUserTag tag = StringUtils.isBlank(id) ? null : userTagRepository.findById(id).orElse(null);
        return tag == null ? R.fail("用户标签不存在") : R.ok(tagView(tag, categoryNameMap()));
    }

    @Operation(summary = "新增用户标签")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public R<TblUserTag> add(@RequestBody Map<String, Object> params) {
        String projectName = RequestParamUtils.getString(params, "projectName");
        String displayName = RequestParamUtils.getString(params, "displayName");
        String tagKey = RequestParamUtils.getString(params, "tagKey");
        if (StringUtils.isAnyBlank(projectName, displayName, tagKey)) {
            return R.fail("项目、标签名称和标签标识不能为空");
        }
        if (userTagRepository.findByProjectNameAndTagKey(projectName, tagKey).isPresent()) {
            return R.fail("标签标识已存在");
        }
        TblUserTag tag = new TblUserTag();
        tag.setId(UUID.randomUUID().toString());
        tag.setCreateTime(now());
        fill(tag, params);
        tag.setUpdateTime(now());
        return R.ok(userTagRepository.save(tag));
    }

    @Operation(summary = "编辑用户标签")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public R<TblUserTag> edit(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        TblUserTag tag = StringUtils.isBlank(id) ? null : userTagRepository.findById(id).orElse(null);
        if (tag == null) {
            return R.fail("用户标签不存在");
        }
        String tagKey = RequestParamUtils.getString(params, "tagKey");
        if (StringUtils.isNotBlank(tagKey)) {
            Optional<TblUserTag> duplicate = userTagRepository.findByProjectNameAndTagKey(
                    StringUtils.defaultIfBlank(RequestParamUtils.getString(params, "projectName"),
                            tag.getProjectName()), tagKey);
            if (duplicate.isPresent() && !duplicate.get().getId().equals(tag.getId())) {
                return R.fail("标签标识已存在");
            }
        }
        fill(tag, params);
        tag.setUpdateTime(now());
        return R.ok(userTagRepository.save(tag));
    }

    @Operation(summary = "启停用户标签")
    @RequestMapping(path = "/status", method = RequestMethod.POST)
    public R<TblUserTag> status(@RequestBody Map<String, Object> params) {
        return edit(params);
    }

    @Transactional
    @Operation(summary = "删除用户标签")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        List<String> ids = RequestParamUtils.getStringList(params, "ids");
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isNotBlank(id)) {
            ids.add(id);
        }
        for (String tagId : ids) {
            assignmentRepository.deleteByTagId(tagId);
            userTagRepository.deleteById(tagId);
        }
        return R.ok(true);
    }

    @Transactional
    @Operation(summary = "为用户赋标签")
    @RequestMapping(path = "/assign", method = RequestMethod.POST)
    public R<List<TblUserTagAssignment>> assign(@RequestBody Map<String, Object> params) {
        String projectName = RequestParamUtils.getString(params, "projectName");
        String tagId = RequestParamUtils.getString(params, "tagId");
        List<String> distinctIds = RequestParamUtils.getStringList(params, "distinctIds");
        String distinctId = RequestParamUtils.getString(params, "distinctId");
        if (StringUtils.isNotBlank(distinctId)) {
            distinctIds.add(distinctId);
        }
        distinctIds = distinctIds.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (StringUtils.isAnyBlank(projectName, tagId) || distinctIds.isEmpty()) {
            return R.fail("项目、标签和用户ID不能为空");
        }
        TblUserTag tag = userTagRepository.findById(tagId).orElse(null);
        if (tag == null || !projectName.equals(tag.getProjectName())) {
            return R.fail("用户标签不存在");
        }
        if (!"enabled".equalsIgnoreCase(tag.getStatus())) {
            return R.fail("用户标签已停用");
        }
        String tagValue = StringUtils.defaultIfBlank(RequestParamUtils.getString(params, "tagValue"),
                tag.getDisplayName());
        List<TblUserTagAssignment> saved = new ArrayList<>();
        for (String userId : distinctIds) {
            TblUserTagAssignment assignment = assignmentRepository
                    .findByProjectNameAndTagIdAndDistinctId(projectName, tagId, userId)
                    .orElseGet(() -> {
                        TblUserTagAssignment item = new TblUserTagAssignment();
                        item.setId(UUID.randomUUID().toString());
                        item.setProjectName(projectName);
                        item.setTagId(tagId);
                        item.setDistinctId(userId);
                        item.setCreateTime(now());
                        return item;
                    });
            assignment.setTagValue(tagValue);
            assignment.setUpdateTime(now());
            saved.add(assignmentRepository.save(assignment));
        }
        refreshMatchCount(tag);
        return R.ok(saved);
    }

    @Transactional
    @Operation(summary = "移除用户标签")
    @RequestMapping(path = "/unassign", method = RequestMethod.POST)
    public R<Boolean> unassign(@RequestBody Map<String, Object> params) {
        String projectName = RequestParamUtils.getString(params, "projectName");
        String tagId = RequestParamUtils.getString(params, "tagId");
        String distinctId = RequestParamUtils.getString(params, "distinctId");
        assignmentRepository.deleteByProjectNameAndTagIdAndDistinctId(projectName, tagId, distinctId);
        userTagRepository.findById(tagId).ifPresent(this::refreshMatchCount);
        return R.ok(true);
    }

    @Operation(summary = "获取用户已分配标签")
    @RequestMapping(path = "/getUserTags", method = RequestMethod.POST)
    public R<List<Map<String, Object>>> getUserTags(@RequestBody Map<String, Object> params) {
        String projectName = RequestParamUtils.getString(params, "projectName");
        String distinctId = RequestParamUtils.getString(params, "distinctId");
        List<TblUserTagAssignment> assignments = assignmentRepository
                .findByProjectNameAndDistinctIdOrderByUpdateTimeDesc(projectName, distinctId);
        Map<String, TblUserTag> tags = new HashMap<>();
        userTagRepository.findAllById(assignments.stream()
                .map(TblUserTagAssignment::getTagId).collect(Collectors.toList()))
                .forEach(item -> tags.put(item.getId(), item));
        Map<String, String> categoryNames = categoryNameMap();
        List<Map<String, Object>> result = new ArrayList<>();
        for (TblUserTagAssignment assignment : assignments) {
            TblUserTag tag = tags.get(assignment.getTagId());
            if (tag == null) {
                continue;
            }
            Map<String, Object> item = tagView(tag, categoryNames);
            item.put("assignmentId", assignment.getId());
            item.put("distinctId", assignment.getDistinctId());
            item.put("tagValue", assignment.getTagValue());
            item.put("assignedTime", assignment.getUpdateTime());
            result.add(item);
        }
        return R.ok(result);
    }

    @Operation(summary = "获取标签覆盖用户")
    @RequestMapping(path = "/getUserPageList", method = RequestMethod.POST)
    public R<PageResult<TblUserTagAssignment>> getUserPageList(@RequestBody Map<String, Object> params) {
        String projectName = RequestParamUtils.getString(params, "projectName");
        String tagId = RequestParamUtils.getString(params, "id");
        if (StringUtils.isBlank(tagId)) {
            tagId = RequestParamUtils.getString(params, "tagId");
        }
        int pageNum = Math.max(1, RequestParamUtils.getInt(params, "pageNum",
                RequestParamUtils.getInt(params, "pageIndex", 1)));
        int pageSize = Math.max(1, RequestParamUtils.getInt(params, "pageSize", 20));
        List<TblUserTagAssignment> all = assignmentRepository
                .findByProjectNameAndTagIdOrderByUpdateTimeDesc(projectName, tagId);
        int from = Math.min((pageNum - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return R.ok(new PageResult<>(all.subList(from, to), all.size()));
    }

    @Operation(summary = "获取标签值分布")
    @RequestMapping(path = "/getUserDistributeStats", method = RequestMethod.POST)
    public R<List<Map<String, Object>>> getUserDistributeStats(@RequestBody Map<String, Object> params) {
        String projectName = RequestParamUtils.getString(params, "projectName");
        String tagId = StringUtils.defaultIfBlank(RequestParamUtils.getString(params, "id"),
                RequestParamUtils.getString(params, "tagId"));
        Map<String, Long> stats = assignmentRepository
                .findByProjectNameAndTagIdOrderByUpdateTimeDesc(projectName, tagId).stream()
                .collect(Collectors.groupingBy(item -> StringUtils.defaultString(item.getTagValue()),
                        LinkedHashMap::new, Collectors.counting()));
        List<Map<String, Object>> result = new ArrayList<>();
        stats.forEach((name, value) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("value", value);
            result.add(item);
        });
        return R.ok(result);
    }

    private Specification<TblUserTag> buildSpec(Map<String, Object> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String projectName = RequestParamUtils.getString(params, "projectName");
            if (StringUtils.isNotBlank(projectName)) {
                predicates.add(cb.equal(root.get("projectName"), projectName));
            }
            String categoryId = RequestParamUtils.getString(params, "categoryId");
            if (StringUtils.isNotBlank(categoryId)) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            String status = RequestParamUtils.getString(params, "status");
            if (StringUtils.isNotBlank(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            String updateMode = RequestParamUtils.getString(params, "updateMode");
            if (StringUtils.isNotBlank(updateMode)) {
                predicates.add(cb.equal(root.get("updateMode"), updateMode));
            }
            String createType = RequestParamUtils.getString(params, "createType");
            if (StringUtils.isNotBlank(createType)) {
                predicates.add(cb.equal(root.get("createType"), createType));
            }
            String keyword = RequestParamUtils.getString(params, "keyword");
            if (StringUtils.isNotBlank(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(cb.like(root.get("displayName"), like),
                        cb.like(root.get("tagKey"), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void fill(TblUserTag tag, Map<String, Object> params) {
        setIfPresent(params, "projectName", tag::setProjectName);
        setIfPresent(params, "categoryId", tag::setCategoryId);
        setIfPresent(params, "displayName", tag::setDisplayName);
        setIfPresent(params, "tagKey", tag::setTagKey);
        setIfPresent(params, "dataType", tag::setDataType);
        setIfPresent(params, "createType", tag::setCreateType);
        setIfPresent(params, "updateMode", tag::setUpdateMode);
        setIfPresent(params, "status", tag::setStatus);
        setIfPresent(params, "description", tag::setDescription);
        Object rule = params.containsKey("ruleJson") ? params.get("ruleJson") : params.get("rules");
        if (rule != null) {
            tag.setRuleJson(toJson(rule));
        }
        if (StringUtils.isBlank(tag.getDataType())) {
            tag.setDataType("string");
        }
        if (StringUtils.isBlank(tag.getCreateType())) {
            tag.setCreateType("custom");
        }
        if (StringUtils.isBlank(tag.getUpdateMode())) {
            tag.setUpdateMode("manual");
        }
        if (StringUtils.isBlank(tag.getStatus())) {
            tag.setStatus("enabled");
        }
        if (tag.getMatchUserCount() == null) {
            tag.setMatchUserCount(0L);
        }
    }

    private Map<String, Object> tagView(TblUserTag tag, Map<String, String> categoryNames) {
        Map<String, Object> item = objectMapper.convertValue(tag, Map.class);
        item.put("categoryName", categoryNames.getOrDefault(tag.getCategoryId(), ""));
        return item;
    }

    private Map<String, String> categoryNameMap() {
        Map<String, String> result = new HashMap<>();
        for (TblTagCategory item : tagCategoryRepository.findAll()) {
            result.put(item.getId(), item.getDisplayName());
        }
        return result;
    }

    private void refreshMatchCount(TblUserTag tag) {
        tag.setMatchUserCount(assignmentRepository.countByProjectNameAndTagId(
                tag.getProjectName(), tag.getId()));
        tag.setLastExecuteStatus("success");
        tag.setLastExecuteTime(now());
        tag.setUpdateTime(now());
        userTagRepository.save(tag);
    }

    private String toJson(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private void setIfPresent(Map<String, Object> params, String key, Consumer<String> setter) {
        String value = RequestParamUtils.getString(params, key);
        if (value != null) {
            setter.accept(value);
        }
    }

    private Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}
