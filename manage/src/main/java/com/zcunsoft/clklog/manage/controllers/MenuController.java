package com.zcunsoft.clklog.manage.controllers;

import com.zcunsoft.clklog.manage.entity.mysql.TblMenu;
import com.zcunsoft.clklog.manage.models.R;
import com.zcunsoft.clklog.manage.models.analytics.PageResult;
import com.zcunsoft.clklog.manage.repository.mysql.MenuRepository;
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
@RequestMapping(path = "menu")
@Tag(name = "菜单管理", description = "菜单管理")
public class MenuController {

    @Resource
    private MenuRepository menuRepository;

    @Operation(summary = "分页获取菜单")
    @RequestMapping(path = "/getPageList", method = RequestMethod.POST)
    public R<PageResult<TblMenu>> getPageList(@RequestBody Map<String, Object> params) {
        int pageNum = Math.max(1, RequestParamUtils.getInt(params, "pageNum", 1));
        int pageSize = Math.max(1, RequestParamUtils.getInt(params, "pageSize", 100));
        Page<TblMenu> page = menuRepository.findAll(buildSpec(params),
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.ASC, "sortOrder")));
        return R.ok(new PageResult<>(page.getContent(), page.getTotalElements()));
    }

    @Operation(summary = "新增菜单")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public R<TblMenu> add(@RequestBody Map<String, Object> params) {
        String title = RequestParamUtils.getString(params, "title");
        String path = RequestParamUtils.getString(params, "path");
        if (StringUtils.isAnyBlank(title, path)) {
            return R.fail("菜单名称和路由地址不能为空");
        }
        TblMenu menu = new TblMenu();
        menu.setId(UUID.randomUUID().toString());
        menu.setCreateTime(now());
        fill(menu, params);
        menu.setUpdateTime(now());
        return R.ok(menuRepository.save(menu));
    }

    @Operation(summary = "编辑菜单")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public R<TblMenu> edit(@RequestBody Map<String, Object> params) {
        String id = RequestParamUtils.getString(params, "id");
        TblMenu menu = StringUtils.isBlank(id) ? null : menuRepository.findById(id).orElse(null);
        if (menu == null) {
            return R.fail("菜单不存在");
        }
        fill(menu, params);
        menu.setUpdateTime(now());
        return R.ok(menuRepository.save(menu));
    }

    @Operation(summary = "删除菜单")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        List<String> ids = RequestParamUtils.getStringList(params, "ids");
        String id = RequestParamUtils.getString(params, "id");
        if (StringUtils.isNotBlank(id)) {
            ids.add(id);
        }
        ids.forEach(menuRepository::deleteById);
        return R.ok(true);
    }

    private Specification<TblMenu> buildSpec(Map<String, Object> params) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String keyword = RequestParamUtils.getString(params, "keyword");
            if (StringUtils.isNotBlank(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(builder.or(builder.like(root.get("title"), like),
                        builder.like(root.get("path"), like)));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void fill(TblMenu menu, Map<String, Object> params) {
        setIfPresent(params, "title", menu::setTitle);
        setIfPresent(params, "path", menu::setPath);
        setIfPresent(params, "component", menu::setComponent);
        setIfPresent(params, "permissions", menu::setPermissions);
        setIfPresent(params, "roles", menu::setRoles);
        setIfPresent(params, "status", menu::setStatus);
        menu.setSortOrder(RequestParamUtils.getInt(params, "sortOrder",
                menu.getSortOrder() == null ? 0 : menu.getSortOrder()));
        menu.setHidden(RequestParamUtils.getBoolean(params, "hidden",
                menu.getHidden() != null && menu.getHidden()));
        menu.setExternalWindow(RequestParamUtils.getBoolean(params, "externalWindow",
                menu.getExternalWindow() != null && menu.getExternalWindow()));
        if (StringUtils.isBlank(menu.getStatus())) {
            menu.setStatus("enabled");
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
