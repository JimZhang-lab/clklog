package com.zcunsoft.clklog.manage.controllers;

import com.zcunsoft.clklog.manage.entity.mysql.TblRole;
import com.zcunsoft.clklog.manage.models.R;
import com.zcunsoft.clklog.manage.models.analytics.PageResult;
import com.zcunsoft.clklog.manage.repository.mysql.RoleRepository;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "role")
@Tag(name = "角色管理", description = "角色管理")
public class RoleController {

    @Resource
    private RoleRepository roleRepository;

    @Operation(summary = "分页获取角色")
    @RequestMapping(path = "/getPageList", method = RequestMethod.POST)
    public R<PageResult<TblRole>> getPageList(@RequestBody Map<String, Object> params) {
        int pageNum = Math.max(1, RequestParamUtils.getInt(params, "pageNum", 1));
        int pageSize = Math.max(1, RequestParamUtils.getInt(params, "pageSize", 20));
        Page<TblRole> page = roleRepository.findAll(buildSpec(params),
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.ASC, "sortOrder")));
        return R.ok(new PageResult<>(page.getContent(), page.getTotalElements()));
    }

    @Operation(summary = "新增角色")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public R<TblRole> add(@RequestBody Map<String, Object> params) {
        String roleName = RequestParamUtils.getString(params, "roleName");
        if (StringUtils.isBlank(roleName)) {
            return R.fail("角色名不能为空");
        }
        if (roleRepository.findByRoleName(roleName).isPresent()) {
            return R.fail("角色名已存在");
        }
        TblRole role = new TblRole();
        role.setId(UUID.randomUUID().toString());
        role.setCreateTime(now());
        fill(role, params);
        role.setUpdateTime(now());
        return R.ok(roleRepository.save(role));
    }

    @Operation(summary = "编辑角色")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public R<TblRole> edit(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        TblRole role = StringUtils.isBlank(id) ? null : roleRepository.findById(id).orElse(null);
        if (role == null) {
            return R.fail("角色不存在");
        }
        String roleName = RequestParamUtils.getString(params, "roleName");
        if (StringUtils.isNotBlank(roleName)) {
            Optional<TblRole> duplicate = roleRepository.findByRoleName(roleName);
            if (duplicate.isPresent() && !duplicate.get().getId().equals(role.getId())) {
                return R.fail("角色名已存在");
            }
        }
        fill(role, params);
        role.setUpdateTime(now());
        return R.ok(roleRepository.save(role));
    }

    @Operation(summary = "删除角色")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        List<String> ids = RequestParamUtils.getStringList(params, "ids");
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isNotBlank(id)) {
            ids.add(id);
        }
        ids.forEach(roleRepository::deleteById);
        return R.ok(true);
    }

    private Specification<TblRole> buildSpec(Map<String, Object> params) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String keyword = RequestParamUtils.getString(params, "keyword");
            if (StringUtils.isNotBlank(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(builder.or(builder.like(root.get("roleName"), like),
                        builder.like(root.get("displayName"), like)));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void fill(TblRole role, Map<String, Object> params) {
        setIfPresent(params, "roleName", role::setRoleName);
        setIfPresent(params, "displayName", role::setDisplayName);
        setIfPresent(params, "roleType", role::setRoleType);
        setIfPresent(params, "status", role::setStatus);
        role.setSortOrder(RequestParamUtils.getInt(params, "sortOrder",
                role.getSortOrder() == null ? 0 : role.getSortOrder()));
        if (StringUtils.isBlank(role.getStatus())) {
            role.setStatus("enabled");
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
