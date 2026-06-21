package com.zcunsoft.clklog.api.services;

import com.zcunsoft.clklog.api.models.customsql.CustomSqlRequest;
import com.zcunsoft.clklog.api.models.customsql.CustomSqlResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class CustomSqlService {

    private final NamedParameterJdbcTemplate clickHouseJdbcTemplate;

    private static final Pattern ALLOWED_START = Pattern.compile("^(select|show|describe|desc|explain)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern BLOCKED_WORDS = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|truncate|create|replace|rename|attach|detach|optimize|system|kill|grant|revoke|set|use)\\b",
            Pattern.CASE_INSENSITIVE);

    public CustomSqlService(NamedParameterJdbcTemplate clickHouseJdbcTemplate) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
    }

    public CustomSqlResponse query(CustomSqlRequest request) {
        if (request == null || StringUtils.isBlank(request.getProjectName())) {
            throw new IllegalArgumentException("项目不能为空");
        }
        String sql = normalizeSql(request == null ? null : request.getSql());
        validate(sql);
        int limit = normalizeLimit(request == null ? null : request.getPageSize());
        String executedSql = withLimit(sql, limit);
        long start = System.currentTimeMillis();
        List<Map<String, Object>> rows = clickHouseJdbcTemplate.queryForList(executedSql, new MapSqlParameterSource());
        CustomSqlResponse response = new CustomSqlResponse();
        response.setRows(rows);
        response.setColumns(columns(rows));
        response.setRowCount(rows.size());
        response.setElapsedMs(System.currentTimeMillis() - start);
        response.setExecutedSql(executedSql);
        return response;
    }

    private String normalizeSql(String sql) {
        String value = StringUtils.trimToEmpty(sql);
        while (value.endsWith(";")) {
            value = value.substring(0, value.length() - 1).trim();
        }
        return value;
    }

    private void validate(String sql) {
        if (StringUtils.isBlank(sql)) {
            throw new IllegalArgumentException("SQL不能为空");
        }
        if (sql.length() > 10000) {
            throw new IllegalArgumentException("SQL长度不能超过10000个字符");
        }
        String stripped = stripComments(sql);
        if (stripped.contains(";")) {
            throw new IllegalArgumentException("一次只能执行一条SQL");
        }
        if (!ALLOWED_START.matcher(stripped).find()) {
            throw new IllegalArgumentException("仅支持 SELECT / SHOW / DESCRIBE / EXPLAIN 查询");
        }
        if (BLOCKED_WORDS.matcher(stripped).find()) {
            throw new IllegalArgumentException("仅支持只读查询，禁止修改数据库结构或数据");
        }
    }

    private String stripComments(String sql) {
        String withoutBlock = sql.replaceAll("(?s)/\\*.*?\\*/", " ");
        return withoutBlock.replaceAll("(?m)--.*$", " ").trim();
    }

    private int normalizeLimit(Integer pageSize) {
        if (pageSize == null) {
            return 100;
        }
        return Math.max(1, Math.min(pageSize, 1000));
    }

    private String withLimit(String sql, int limit) {
        String lower = sql.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("select") || lower.matches("(?s).*\\blimit\\s+\\d+.*")) {
            return sql;
        }
        return sql + " limit " + limit;
    }

    private List<String> columns(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(rows.get(0).keySet());
    }
}
