package com.zcunsoft.clklog.manage.controllers;

import com.zcunsoft.clklog.manage.entity.mysql.TblApiKey;
import com.zcunsoft.clklog.manage.models.R;
import com.zcunsoft.clklog.manage.repository.mysql.ApiKeyRepository;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping(path = "apikey")
@Tag(name = "API密钥", description = "API密钥")
public class ApiKeyController {

    @Resource
    private ApiKeyRepository apiKeyRepository;

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @Operation(summary = "获取API密钥列表")
    @RequestMapping(path = "/list", method = RequestMethod.POST)
    public R<List<Map<String, Object>>> list(@RequestBody Map<String, Object> params) {
        if (StringUtils.isBlank(RequestParamUtils.getString(params, "projectName"))) {
            return R.fail("项目不能为空");
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        apiKeyRepository.findAll(buildSpec(params), Sort.by(Sort.Direction.DESC, "createdAt"))
                .forEach(item -> rows.add(view(item, false)));
        return R.ok(rows);
    }

    @Operation(summary = "获取API密钥详情")
    @RequestMapping(path = "/get", method = RequestMethod.POST)
    public R<Map<String, Object>> get(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        String projectName = RequestParamUtils.getString(params, "projectName");
        if (StringUtils.isAnyBlank(id, projectName)) {
            return R.fail("项目和密钥ID不能为空");
        }
        TblApiKey item = apiKeyRepository.findById(id).orElse(null);
        return item == null || !projectName.equals(item.getProjectName()) ? R.fail("API密钥不存在") : R.ok(view(item, false));
    }

    @Operation(summary = "新增API密钥")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public R<Map<String, Object>> add(@RequestBody Map<String, Object> params) {
        String projectName = RequestParamUtils.getString(params, "projectName");
        String displayName = RequestParamUtils.getString(params, "displayName");
        if (StringUtils.isAnyBlank(projectName, displayName)) {
            return R.fail("项目和显示名称不能为空");
        }
        String plainKey = generateKey();
        TblApiKey item = new TblApiKey();
        item.setId(UUID.randomUUID().toString());
        item.setProjectName(projectName);
        item.setDisplayName(displayName);
        item.setKeyPrefix(plainKey.substring(0, Math.min(10, plainKey.length())));
        item.setKeyMask(maskKey(plainKey));
        item.setKeyHash(sha256(plainKey));
        item.setStatus("启用");
        item.setCreateUser(StringUtils.defaultIfBlank(RequestParamUtils.getString(params, "createUser"), "clklog"));
        item.setExpiresAt(parseTime(RequestParamUtils.getString(params, "expiresAt")));
        Timestamp now = now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        Map<String, Object> result = view(apiKeyRepository.save(item), false);
        result.put("apiKey", plainKey);
        return R.ok(result);
    }

    @Operation(summary = "编辑API密钥")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public R<Map<String, Object>> edit(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        String projectName = RequestParamUtils.getString(params, "projectName");
        if (StringUtils.isAnyBlank(id, projectName)) {
            return R.fail("项目和密钥ID不能为空");
        }
        TblApiKey item = apiKeyRepository.findById(id).orElse(null);
        if (item == null || !projectName.equals(item.getProjectName())) {
            return R.fail("API密钥不存在");
        }
        String displayName = RequestParamUtils.getString(params, "displayName");
        if (StringUtils.isNotBlank(displayName)) {
            item.setDisplayName(displayName);
        }
        String status = RequestParamUtils.getString(params, "status");
        if (StringUtils.isNotBlank(status)) {
            item.setStatus(status);
        }
        item.setExpiresAt(parseTime(RequestParamUtils.getString(params, "expiresAt")));
        item.setUpdatedAt(now());
        return R.ok(view(apiKeyRepository.save(item), false));
    }

    @Operation(summary = "删除API密钥")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        String projectName = RequestParamUtils.getString(params, "projectName");
        if (StringUtils.isAnyBlank(id, projectName)) {
            return R.fail("项目和密钥ID不能为空");
        }
        apiKeyRepository.findById(id)
                .filter(item -> projectName.equals(item.getProjectName()))
                .ifPresent(apiKeyRepository::delete);
        return R.ok(true);
    }

    private Specification<TblApiKey> buildSpec(Map<String, Object> params) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String projectName = RequestParamUtils.getString(params, "projectName");
            if (StringUtils.isNotBlank(projectName)) {
                predicates.add(builder.equal(root.get("projectName"), projectName));
            }
            String keyword = RequestParamUtils.getString(params, "keyword");
            if (StringUtils.isNotBlank(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(builder.or(builder.like(root.get("displayName"), like),
                        builder.like(root.get("keyMask"), like)));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<String, Object> view(TblApiKey item, boolean includeHash) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", item.getId());
        result.put("projectName", item.getProjectName());
        result.put("displayName", item.getDisplayName());
        result.put("apiKey", item.getKeyMask());
        result.put("status", StringUtils.defaultIfBlank(item.getStatus(), "启用"));
        result.put("createdAt", format(item.getCreatedAt()));
        result.put("expiresAt", format(item.getExpiresAt()));
        result.put("updatedAt", format(item.getUpdatedAt()));
        result.put("createUser", item.getCreateUser());
        if (includeHash) {
            result.put("keyHash", item.getKeyHash());
        }
        return result;
    }

    private String generateKey() {
        return "clk_" + UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String maskKey(String value) {
        if (StringUtils.length(value) <= 12) {
            return "************";
        }
        return value.substring(0, 6) + "************************" + value.substring(value.length() - 4);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : encoded) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private Timestamp parseTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new Timestamp(DATE_FORMAT.get().parse(value).getTime());
        } catch (Exception ex) {
            return null;
        }
    }

    private String format(Timestamp value) {
        return value == null ? null : DATE_FORMAT.get().format(value);
    }

    private Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}
