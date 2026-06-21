package com.zcunsoft.clklog.manage.controllers;

import com.zcunsoft.clklog.manage.entity.mysql.TblBookmark;
import com.zcunsoft.clklog.manage.models.R;
import com.zcunsoft.clklog.manage.models.analytics.PageResult;
import com.zcunsoft.clklog.manage.repository.mysql.BookmarkRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "bookmark")
@Tag(name = "分析书签", description = "分析书签")
public class BookmarkController {

    @Resource
    private BookmarkRepository bookmarkRepository;

    @Operation(summary = "分页获取书签")
    @RequestMapping(path = "/getPageList", method = RequestMethod.POST)
    public R<PageResult<TblBookmark>> getPageList(@RequestBody Map<String, Object> params) {
        if (StringUtils.isBlank(RequestParamUtils.getString(params, "projectName"))) {
            return R.fail("项目不能为空");
        }
        int pageNum = Math.max(1, RequestParamUtils.getInt(params, "pageNum", 1));
        int pageSize = Math.max(1, RequestParamUtils.getInt(params, "pageSize", 20));
        Page<TblBookmark> page = bookmarkRepository.findAll(buildSpec(params),
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime")));
        return R.ok(new PageResult<>(page.getContent(), page.getTotalElements()));
    }

    @Operation(summary = "获取书签详情")
    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public R<TblBookmark> get(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        String projectName = RequestParamUtils.getString(params, "projectName");
        if (StringUtils.isAnyBlank(id, projectName)) {
            return R.fail("项目和书签ID不能为空");
        }
        TblBookmark item = StringUtils.isBlank(id) ? null : bookmarkRepository.findById(id).orElse(null);
        return item == null || !projectName.equals(item.getProjectName()) ? R.fail("书签不存在") : R.ok(item);
    }

    @Operation(summary = "新增书签")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public R<TblBookmark> add(@RequestBody Map<String, Object> params) {
        String projectName = RequestParamUtils.getString(params, "projectName");
        String name = RequestParamUtils.getString(params, "name");
        if (StringUtils.isAnyBlank(projectName, name)) {
            return R.fail("项目和书签名称不能为空");
        }
        TblBookmark item = new TblBookmark();
        item.setId(UUID.randomUUID().toString());
        item.setCreateTime(now());
        fill(item, params);
        item.setUpdateTime(now());
        return R.ok(bookmarkRepository.save(item));
    }

    @Operation(summary = "编辑书签")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public R<TblBookmark> edit(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        String projectName = RequestParamUtils.getString(params, "projectName");
        if (StringUtils.isAnyBlank(id, projectName)) {
            return R.fail("项目和书签ID不能为空");
        }
        TblBookmark item = StringUtils.isBlank(id) ? null : bookmarkRepository.findById(id).orElse(null);
        if (item == null || !projectName.equals(item.getProjectName())) {
            return R.fail("书签不存在");
        }
        fill(item, params);
        item.setUpdateTime(now());
        return R.ok(bookmarkRepository.save(item));
    }

    @Operation(summary = "删除书签")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        String projectName = RequestParamUtils.getString(params, "projectName");
        if (StringUtils.isBlank(projectName)) {
            return R.fail("项目不能为空");
        }
        List<String> ids = RequestParamUtils.getStringList(params, "ids");
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isNotBlank(id)) {
            ids.add(id);
        }
        ids.forEach(itemId -> bookmarkRepository.findById(itemId)
                .filter(item -> projectName.equals(item.getProjectName()))
                .ifPresent(bookmarkRepository::delete));
        return R.ok(true);
    }

    private Specification<TblBookmark> buildSpec(Map<String, Object> params) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String projectName = RequestParamUtils.getString(params, "projectName");
            if (StringUtils.isNotBlank(projectName)) {
                predicates.add(builder.equal(root.get("projectName"), projectName));
            }
            String keyword = RequestParamUtils.getString(params, "keyword");
            if (StringUtils.isNotBlank(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(builder.or(builder.like(root.get("name"), like),
                        builder.like(root.get("analysisType"), like)));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void fill(TblBookmark item, Map<String, Object> params) {
        setIfPresent(params, "projectName", item::setProjectName);
        setIfPresent(params, "name", item::setName);
        setIfPresent(params, "analysisType", item::setAnalysisType);
        setIfPresent(params, "status", item::setStatus);
        setIfPresent(params, "description", item::setDescription);
        setIfPresent(params, "queryJson", item::setQueryJson);
        setIfPresent(params, "createUser", item::setCreateUser);
        if (StringUtils.isBlank(item.getStatus())) {
            item.setStatus("enabled");
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
