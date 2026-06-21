package com.zcunsoft.clklog.manage.controllers;

import com.zcunsoft.clklog.manage.entity.mysql.TblTagCategory;
import com.zcunsoft.clklog.manage.models.R;
import com.zcunsoft.clklog.manage.repository.mysql.TagCategoryRepository;
import com.zcunsoft.clklog.manage.repository.mysql.UserTagRepository;
import com.zcunsoft.clklog.manage.utils.RequestParamUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "category")
@Tag(name = "标签分类", description = "标签分类")
public class TagCategoryController {

    @Resource
    private TagCategoryRepository tagCategoryRepository;

    @Resource
    private UserTagRepository userTagRepository;

    @Operation(summary = "获取标签分类树")
    @RequestMapping(path = "/getTreeList", method = RequestMethod.POST)
    public R<List<Map<String, Object>>> getTreeList(@RequestBody Map<String, Object> params) {
        List<TblTagCategory> rows = tagCategoryRepository.findAll(buildSpec(params),
                Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.ASC, "displayName")));
        return R.ok(buildTree(rows, ""));
    }

    @Operation(summary = "获取标签分类列表")
    @RequestMapping(path = "/getList", method = RequestMethod.POST)
    public R<List<TblTagCategory>> getList(@RequestBody Map<String, Object> params) {
        return R.ok(tagCategoryRepository.findAll(buildSpec(params),
                Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.ASC, "displayName"))));
    }

    @Operation(summary = "获取标签分类详情")
    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public R<TblTagCategory> get(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        return StringUtils.isBlank(id) ? R.fail("缺少分类ID")
                : R.ok(tagCategoryRepository.findById(id).orElse(null));
    }

    @Operation(summary = "新增标签分类")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public R<TblTagCategory> add(@RequestBody Map<String, Object> params) {
        String displayName = RequestParamUtils.getString(params, "displayName");
        if (StringUtils.isBlank(displayName)) {
            return R.fail("请输入分类显示名");
        }
        TblTagCategory category = new TblTagCategory();
        category.setId(UUID.randomUUID().toString());
        category.setCreateTime(now());
        fill(category, params);
        category.setUpdateTime(now());
        return R.ok(tagCategoryRepository.save(category));
    }

    @Operation(summary = "编辑标签分类")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public R<TblTagCategory> edit(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        TblTagCategory category = StringUtils.isBlank(id) ? null
                : tagCategoryRepository.findById(id).orElse(null);
        if (category == null) {
            return R.fail("标签分类不存在");
        }
        fill(category, params);
        category.setUpdateTime(now());
        return R.ok(tagCategoryRepository.save(category));
    }

    @Operation(summary = "拖动标签分类")
    @RequestMapping(path = "/drag", method = RequestMethod.POST)
    public R<TblTagCategory> drag(@RequestBody Map<String, Object> params) {
        return edit(params);
    }

    @Operation(summary = "删除标签分类")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isBlank(id)) {
            return R.fail("缺少分类ID");
        }
        String projectName = RequestParamUtils.getString(params, "projectName");
        TblTagCategory category = tagCategoryRepository.findById(id).orElse(null);
        if (category == null || (StringUtils.isNotBlank(projectName) && !projectName.equals(category.getProjectName()))) {
            return R.fail("标签分类不存在");
        }
        boolean hasChildren = false;
        for (TblTagCategory item : tagCategoryRepository.findAll()) {
            if (id.equals(item.getParentId()) && StringUtils.equals(category.getProjectName(), item.getProjectName())) {
                hasChildren = true;
                break;
            }
        }
        if (hasChildren) {
            return R.fail("请先删除子分类");
        }
        if (userTagRepository.countByCategoryIdAndProjectName(id, category.getProjectName()) > 0) {
            return R.fail("该分类下仍有用户标签");
        }
        tagCategoryRepository.delete(category);
        return R.ok(true);
    }

    private List<Map<String, Object>> buildTree(List<TblTagCategory> rows, String parentId) {
        return rows.stream()
                .filter(item -> StringUtils.equals(StringUtils.defaultString(item.getParentId()), parentId))
                .map(item -> {
                    Map<String, Object> node = new LinkedHashMap<>();
                    node.put("id", item.getId());
                    node.put("projectName", item.getProjectName());
                    node.put("parentId", item.getParentId());
                    node.put("displayName", item.getDisplayName());
                    node.put("label", item.getDisplayName());
                    node.put("sortOrder", item.getSortOrder());
                    node.put("status", item.getStatus());
                    node.put("description", item.getDescription());
                    node.put("children", buildTree(rows, item.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }

    private Specification<TblTagCategory> buildSpec(Map<String, Object> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String projectName = RequestParamUtils.getString(params, "projectName");
            if (StringUtils.isNotBlank(projectName)) {
                predicates.add(cb.equal(root.get("projectName"), projectName));
            }
            String status = RequestParamUtils.getString(params, "status");
            if (StringUtils.isNotBlank(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void fill(TblTagCategory category, Map<String, Object> params) {
        setIfPresent(params, "projectName", category::setProjectName);
        if (params.containsKey("parentId")) {
            category.setParentId(StringUtils.defaultString(
                    RequestParamUtils.getString(params, "parentId")));
        }
        setIfPresent(params, "displayName", category::setDisplayName);
        setIfPresent(params, "status", category::setStatus);
        setIfPresent(params, "description", category::setDescription);
        if (params.containsKey("sortOrder") || params.containsKey("orderNumber")) {
            category.setSortOrder(RequestParamUtils.getInt(params, "sortOrder",
                    RequestParamUtils.getInt(params, "orderNumber", 0)));
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (StringUtils.isBlank(category.getStatus())) {
            category.setStatus("enabled");
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
