package com.zcunsoft.clklog.manage.controllers;

import com.zcunsoft.clklog.manage.entity.mysql.TblCdpAsset;
import com.zcunsoft.clklog.manage.models.R;
import com.zcunsoft.clklog.manage.models.analytics.PageResult;
import com.zcunsoft.clklog.manage.repository.mysql.CdpAssetRepository;
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
@RequestMapping(path = "cdp")
@Tag(name = "CDP资产", description = "用户分群与群画像")
public class CdpAssetController {

    @Resource
    private CdpAssetRepository cdpAssetRepository;

    @Operation(summary = "分页获取CDP资产")
    @RequestMapping(path = "/getPageList", method = RequestMethod.POST)
    public R<PageResult<Map<String, Object>>> getPageList(@RequestBody Map<String, Object> params) {
        if (StringUtils.isBlank(RequestParamUtils.getString(params, "projectName"))) {
            return R.fail("项目不能为空");
        }
        int pageNum = Math.max(1, RequestParamUtils.getInt(params, "pageNum", 1));
        int pageSize = Math.max(1, RequestParamUtils.getInt(params, "pageSize", 20));
        Page<TblCdpAsset> page = cdpAssetRepository.findAll(buildSpec(params),
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime")));
        List<Map<String, Object>> rows = page.getContent().stream().map(this::view).collect(Collectors.toList());
        return R.ok(new PageResult<>(rows, page.getTotalElements()));
    }

    @Operation(summary = "获取CDP资产详情")
    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public R<Map<String, Object>> get(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        String projectName = RequestParamUtils.getString(params, "projectName");
        if (StringUtils.isAnyBlank(id, projectName)) {
            return R.fail("项目和CDP资产ID不能为空");
        }
        TblCdpAsset item = StringUtils.isBlank(id) ? null : cdpAssetRepository.findById(id).orElse(null);
        return item == null || !projectName.equals(item.getProjectName()) ? R.fail("CDP资产不存在") : R.ok(view(item));
    }

    @Operation(summary = "新增CDP资产")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public R<Map<String, Object>> add(@RequestBody Map<String, Object> params) {
        String projectName = RequestParamUtils.getString(params, "projectName");
        String assetType = normalizedType(params);
        String displayName = RequestParamUtils.getString(params, "displayName");
        String assetKey = assetKey(params);
        if (StringUtils.isAnyBlank(projectName, assetType, displayName, assetKey)) {
            return R.fail("项目、类型、名称和标识不能为空");
        }
        if (cdpAssetRepository.findByProjectNameAndAssetTypeAndAssetKey(projectName, assetType, assetKey).isPresent()) {
            return R.fail("标识已存在");
        }
        TblCdpAsset item = new TblCdpAsset();
        item.setId(UUID.randomUUID().toString());
        item.setCreateTime(now());
        fill(item, params);
        item.setAssetType(assetType);
        item.setAssetKey(assetKey);
        item.setUpdateTime(now());
        return R.ok(view(cdpAssetRepository.save(item)));
    }

    @Operation(summary = "编辑CDP资产")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public R<Map<String, Object>> edit(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        String projectName = RequestParamUtils.getString(params, "projectName");
        if (StringUtils.isAnyBlank(id, projectName)) {
            return R.fail("项目和CDP资产ID不能为空");
        }
        TblCdpAsset item = StringUtils.isBlank(id) ? null : cdpAssetRepository.findById(id).orElse(null);
        if (item == null || !projectName.equals(item.getProjectName())) {
            return R.fail("CDP资产不存在");
        }
        String assetType = StringUtils.defaultIfBlank(RequestParamUtils.getString(params, "assetType"), item.getAssetType());
        String key = assetKey(params);
        if (StringUtils.isNotBlank(key)) {
            Optional<TblCdpAsset> duplicate = cdpAssetRepository
                    .findByProjectNameAndAssetTypeAndAssetKey(projectName, assetType, key);
            if (duplicate.isPresent() && !duplicate.get().getId().equals(item.getId())) {
                return R.fail("标识已存在");
            }
            item.setAssetKey(key);
        }
        fill(item, params);
        item.setUpdateTime(now());
        return R.ok(view(cdpAssetRepository.save(item)));
    }

    @Operation(summary = "删除CDP资产")
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
        ids.forEach(itemId -> cdpAssetRepository.findById(itemId)
                .filter(item -> projectName.equals(item.getProjectName()))
                .ifPresent(cdpAssetRepository::delete));
        return R.ok(true);
    }

    private Specification<TblCdpAsset> buildSpec(Map<String, Object> params) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String projectName = RequestParamUtils.getString(params, "projectName");
            if (StringUtils.isNotBlank(projectName)) {
                predicates.add(builder.equal(root.get("projectName"), projectName));
            }
            String assetType = normalizedType(params);
            if (StringUtils.isNotBlank(assetType)) {
                predicates.add(builder.equal(root.get("assetType"), assetType));
            }
            String status = RequestParamUtils.getString(params, "status");
            if (StringUtils.isNotBlank(status)) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            String createType = RequestParamUtils.getString(params, "createType");
            if (StringUtils.isNotBlank(createType)) {
                predicates.add(builder.equal(root.get("createType"), createType));
            }
            String updateMode = RequestParamUtils.getString(params, "updateMode");
            if (StringUtils.isNotBlank(updateMode)) {
                predicates.add(builder.equal(root.get("updateMode"), updateMode));
            }
            String executeStatus = RequestParamUtils.getString(params, "lastExecuteStatus");
            if (StringUtils.isNotBlank(executeStatus)) {
                predicates.add(builder.equal(root.get("lastExecuteStatus"), executeStatus));
            }
            String executeStart = RequestParamUtils.getString(params, "lastExecuteStartTime");
            if (StringUtils.isNotBlank(executeStart)) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("lastExecuteTime"),
                        Timestamp.valueOf(executeStart + " 00:00:00")));
            }
            String executeEnd = RequestParamUtils.getString(params, "lastExecuteEndTime");
            if (StringUtils.isNotBlank(executeEnd)) {
                predicates.add(builder.lessThanOrEqualTo(root.get("lastExecuteTime"),
                        Timestamp.valueOf(executeEnd + " 23:59:59")));
            }
            String keyword = RequestParamUtils.getString(params, "keyword");
            if (StringUtils.isNotBlank(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(builder.or(builder.like(root.get("displayName"), like),
                        builder.like(root.get("assetKey"), like)));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void fill(TblCdpAsset item, Map<String, Object> params) {
        setIfPresent(params, "projectName", item::setProjectName);
        setIfPresent(params, "displayName", item::setDisplayName);
        setIfPresent(params, "status", item::setStatus);
        setIfPresent(params, "createType", item::setCreateType);
        setIfPresent(params, "updateMode", item::setUpdateMode);
        setIfPresent(params, "ruleJson", item::setRuleJson);
        setIfPresent(params, "description", item::setDescription);
        setIfPresent(params, "createUser", item::setCreateUser);
        if (params.containsKey("distinctIds")) {
            List<String> distinctIds = RequestParamUtils.getStringList(params, "distinctIds");
            item.setDistinctIds(distinctIds.stream().distinct().collect(Collectors.joining(",")));
            item.setMatchUserCount((long) distinctIds.stream().distinct().count());
        }
        if (item.getMatchUserCount() == null) {
            item.setMatchUserCount(0L);
        }
        if (StringUtils.isBlank(item.getStatus())) {
            item.setStatus("enabled");
        }
        if (StringUtils.isBlank(item.getCreateType())) {
            item.setCreateType("custom");
        }
        if (StringUtils.isBlank(item.getUpdateMode())) {
            item.setUpdateMode("manual");
        }
        if (StringUtils.isBlank(item.getCreateUser())) {
            item.setCreateUser("clklog");
        }
        item.setLastExecuteStatus("success");
        item.setLastExecuteTime(now());
    }

    private Map<String, Object> view(TblCdpAsset item) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", item.getId());
        result.put("projectName", item.getProjectName());
        result.put("assetType", item.getAssetType());
        result.put("displayName", item.getDisplayName());
        result.put("assetKey", item.getAssetKey());
        result.put("key", item.getAssetKey());
        result.put("status", item.getStatus());
        result.put("statusText", "enabled".equalsIgnoreCase(item.getStatus()) ? "启用" : "停用");
        result.put("createType", "custom".equalsIgnoreCase(item.getCreateType()) ? "自定义" : item.getCreateType());
        result.put("createTypeCode", item.getCreateType());
        result.put("updateMode", "manual".equalsIgnoreCase(item.getUpdateMode()) ? "手动" : item.getUpdateMode());
        result.put("updateModeCode", item.getUpdateMode());
        result.put("description", item.getDescription());
        result.put("matchUserCount", item.getMatchUserCount());
        result.put("lastExecuteStatus", item.getLastExecuteStatus());
        result.put("lastExecuteTime", item.getLastExecuteTime());
        result.put("createUser", StringUtils.defaultIfBlank(item.getCreateUser(), "clklog"));
        result.put("createTime", item.getCreateTime());
        result.put("updateTime", item.getUpdateTime());
        result.put("distinctIds", distinctIds(item.getDistinctIds()));
        return result;
    }

    private List<String> distinctIds(String value) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(",")).map(String::trim)
                .filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
    }

    private String normalizedType(Map<String, Object> params) {
        String value = StringUtils.defaultIfBlank(RequestParamUtils.getString(params, "assetType"),
                RequestParamUtils.getString(params, "type"));
        return StringUtils.defaultIfBlank(value, "group");
    }

    private String assetKey(Map<String, Object> params) {
        return StringUtils.defaultIfBlank(RequestParamUtils.getString(params, "assetKey"),
                RequestParamUtils.getString(params, "key"));
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
