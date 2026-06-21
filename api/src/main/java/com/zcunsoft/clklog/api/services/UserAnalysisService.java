package com.zcunsoft.clklog.api.services;

import com.zcunsoft.clklog.api.cfg.ClklogApiSetting;
import com.zcunsoft.clklog.api.models.enums.LibType;
import com.zcunsoft.clklog.api.models.useranalysis.UserAnalysisRequest;
import com.zcunsoft.clklog.api.models.useranalysis.UserAnalysisTrendResponse;
import com.zcunsoft.clklog.api.models.useranalysis.UserAnalysisTrendRow;
import com.zcunsoft.clklog.api.models.useranalysis.UserRetentionResponse;
import com.zcunsoft.clklog.api.models.useranalysis.UserRetentionRow;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class UserAnalysisService {

    private final NamedParameterJdbcTemplate clickHouseJdbcTemplate;
    private final ClklogApiSetting clklogApiSetting;
    private final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    private final ThreadLocal<DecimalFormat> rateFormat = ThreadLocal.withInitial(() -> new DecimalFormat("0.00"));

    public UserAnalysisService(NamedParameterJdbcTemplate clickHouseJdbcTemplate, ClklogApiSetting clklogApiSetting) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
        this.clklogApiSetting = clklogApiSetting;
    }

    public UserAnalysisTrendResponse getUserActiveTrend(UserAnalysisRequest request) {
        MapSqlParameterSource paramMap = new MapSqlParameterSource();
        String where = buildAggregateWhere(request, paramMap, false, false);
        String sql = "select " + periodLabel(request.getTimeType()) + " as statTime,"
                + " sum(t.uv) as userCount,"
                + " sum(t.uv) as activeUserCount,"
                + " sum(t.new_uv) as newUserCount,"
                + " sum(t.uv) - sum(t.new_uv) as oldUserCount"
                + " from visitor_detail_bydate t"
                + where
                + " group by " + periodLabel(request.getTimeType()) + ", " + periodStart(request.getTimeType())
                + " order by " + periodStart(request.getTimeType());

        List<UserAnalysisTrendRow> rows = clickHouseJdbcTemplate.query(sql, paramMap,
                new BeanPropertyRowMapper<UserAnalysisTrendRow>(UserAnalysisTrendRow.class));
        UserAnalysisTrendResponse response = new UserAnalysisTrendResponse();
        response.setData(rows);
        return response;
    }

    public UserAnalysisTrendResponse getUserLifeTrend(UserAnalysisRequest request) {
        MapSqlParameterSource paramMap = new MapSqlParameterSource();
        String where = buildAggregateWhere(request, paramMap, false, false);
        String sql = "select " + periodLabel(request.getTimeType()) + " as statTime,"
                + " sum(t.new_uv) as newUserCount,"
                + " sum(t.continuous_active_uv) as retainedUserCount,"
                + " sum(t.revisit_uv) as revisitUserCount,"
                + " sum(t.silent_uv) as silentUserCount,"
                + " sum(t.churn_uv) as churnUserCount"
                + " from visitor_life_bydate t"
                + where
                + " group by " + periodLabel(request.getTimeType()) + ", " + periodStart(request.getTimeType())
                + " order by " + periodStart(request.getTimeType());

        List<UserAnalysisTrendRow> rows = clickHouseJdbcTemplate.query(sql, paramMap,
                new BeanPropertyRowMapper<UserAnalysisTrendRow>(UserAnalysisTrendRow.class));
        int cumulativeUserCount = 0;
        for (UserAnalysisTrendRow row : rows) {
            int oldUserCount = row.getRetainedUserCount() + row.getRevisitUserCount();
            int userCount = row.getNewUserCount() + oldUserCount;
            cumulativeUserCount += userCount;
            row.setOldUserCount(oldUserCount);
            row.setUserCount(userCount);
            row.setActiveUserCount(userCount);
            row.setCumulativeUserCount(cumulativeUserCount);
        }
        UserAnalysisTrendResponse response = new UserAnalysisTrendResponse();
        response.setData(rows);
        return response;
    }

    public UserRetentionResponse getUserRemainTrend(UserAnalysisRequest request) {
        MapSqlParameterSource paramMap = new MapSqlParameterSource();
        String baseWhere = buildVisitorInfoWhere(request, paramMap, true);
        String joinWhere = buildVisitorInfoWhere(request, paramMap, false);
        String activitySql = "select distinct t.stat_date, t.distinct_id from visitor_detail_byinfo t";
        String baseSql = activitySql + baseWhere;
        String joinSql = activitySql + joinWhere;
        String sql = "select toString(base.stat_date) as statTime,"
                + " countDistinct(base.distinct_id) as userCount,"
                + " uniqExactIf(d1.distinct_id, d1.distinct_id != '') as day1Count,"
                + " uniqExactIf(d2.distinct_id, d2.distinct_id != '') as day2Count,"
                + " uniqExactIf(d3.distinct_id, d3.distinct_id != '') as day3Count,"
                + " uniqExactIf(d4.distinct_id, d4.distinct_id != '') as day4Count,"
                + " uniqExactIf(d5.distinct_id, d5.distinct_id != '') as day5Count,"
                + " uniqExactIf(d6.distinct_id, d6.distinct_id != '') as day6Count,"
                + " uniqExactIf(d7.distinct_id, d7.distinct_id != '') as day7Count"
                + " from (" + baseSql + ") base"
                + " left join (" + joinSql + ") d1 on d1.distinct_id = base.distinct_id and d1.stat_date = addDays(base.stat_date, 1)"
                + " left join (" + joinSql + ") d2 on d2.distinct_id = base.distinct_id and d2.stat_date = addDays(base.stat_date, 2)"
                + " left join (" + joinSql + ") d3 on d3.distinct_id = base.distinct_id and d3.stat_date = addDays(base.stat_date, 3)"
                + " left join (" + joinSql + ") d4 on d4.distinct_id = base.distinct_id and d4.stat_date = addDays(base.stat_date, 4)"
                + " left join (" + joinSql + ") d5 on d5.distinct_id = base.distinct_id and d5.stat_date = addDays(base.stat_date, 5)"
                + " left join (" + joinSql + ") d6 on d6.distinct_id = base.distinct_id and d6.stat_date = addDays(base.stat_date, 6)"
                + " left join (" + joinSql + ") d7 on d7.distinct_id = base.distinct_id and d7.stat_date = addDays(base.stat_date, 7)"
                + " group by base.stat_date order by base.stat_date";

        List<UserRetentionRow> rows = clickHouseJdbcTemplate.query(sql, paramMap,
                new BeanPropertyRowMapper<UserRetentionRow>(UserRetentionRow.class));
        for (UserRetentionRow row : rows) {
            row.setDay1Rate(rate(row.getUserCount(), row.getDay1Count()));
            row.setDay2Rate(rate(row.getUserCount(), row.getDay2Count()));
            row.setDay3Rate(rate(row.getUserCount(), row.getDay3Count()));
            row.setDay4Rate(rate(row.getUserCount(), row.getDay4Count()));
            row.setDay5Rate(rate(row.getUserCount(), row.getDay5Count()));
            row.setDay6Rate(rate(row.getUserCount(), row.getDay6Count()));
            row.setDay7Rate(rate(row.getUserCount(), row.getDay7Count()));
        }
        UserRetentionResponse response = new UserRetentionResponse();
        response.setData(rows);
        return response;
    }

    private String buildAggregateWhere(UserAnalysisRequest request, MapSqlParameterSource paramMap, boolean includeVisitorType, boolean visitorTypeByAll) {
        String where = " where 1 = 1";
        where += dateWhere(request, paramMap);
        where += projectWhere(request, paramMap);
        where += channelWhere(request, paramMap, true);
        where += countryWhere(request, paramMap, true);
        where += provinceWhere(request, paramMap, true);
        if (includeVisitorType) {
            where += visitorTypeWhere(request, paramMap, visitorTypeByAll);
        }
        return where;
    }

    private String buildVisitorInfoWhere(UserAnalysisRequest request, MapSqlParameterSource paramMap, boolean includeDate) {
        String where = " where 1 = 1";
        if (includeDate) {
            where += dateWhere(request, paramMap);
        }
        where += projectWhere(request, paramMap);
        where += channelWhere(request, paramMap, false);
        where += countryWhere(request, paramMap, false);
        where += provinceWhere(request, paramMap, false);
        where += visitorTypeWhere(request, paramMap, true);
        where += " and t.distinct_id <> ''";
        return where;
    }

    private String dateWhere(UserAnalysisRequest request, MapSqlParameterSource paramMap) {
        String startTime = StringUtils.defaultIfBlank(request.getStartTime(), defaultStartTime());
        String endTime = StringUtils.defaultIfBlank(request.getEndTime(), defaultEndTime());
        paramMap.addValue("starttime", startTime);
        paramMap.addValue("endtime", endTime);
        return " and t.stat_date >= :starttime and t.stat_date <= :endtime";
    }

    private String projectWhere(UserAnalysisRequest request, MapSqlParameterSource paramMap) {
        String projectName = StringUtils.defaultIfBlank(request.getProjectName(), clklogApiSetting.getProjectName());
        paramMap.addValue("project", projectName);
        return " and t.project_name = :project";
    }

    private String channelWhere(UserAnalysisRequest request, MapSqlParameterSource paramMap, boolean aggregateTable) {
        List<String> channelList = transChannelFilter(request.getChannel());
        if (aggregateTable || !channelList.isEmpty()) {
            if (aggregateTable && channelList.isEmpty()) {
                channelList.add("all");
            }
            paramMap.addValue("channel", channelList);
            return " and t.lib in (:channel)";
        }
        return "";
    }

    private String countryWhere(UserAnalysisRequest request, MapSqlParameterSource paramMap, boolean aggregateTable) {
        List<String> country = request.getCountry();
        if (country == null || country.isEmpty()) {
            if (!aggregateTable) {
                return "";
            }
            country = new ArrayList<>();
            country.add("all");
        }
        paramMap.addValue("country", country);
        return " and t.country in (:country)";
    }

    private String provinceWhere(UserAnalysisRequest request, MapSqlParameterSource paramMap, boolean aggregateTable) {
        List<String> province = request.getProvince();
        if (province == null || province.isEmpty()) {
            if (!aggregateTable) {
                return "";
            }
            province = new ArrayList<>();
            province.add("all");
        }
        paramMap.addValue("province", province);
        return " and t.province in (:province)";
    }

    private String visitorTypeWhere(UserAnalysisRequest request, MapSqlParameterSource paramMap, boolean byAll) {
        String visitorType = request.getVisitorType();
        if (StringUtils.isBlank(visitorType)) {
            if (byAll) {
                return "";
            }
            visitorType = "all";
        } else if ("老访客".equalsIgnoreCase(visitorType)) {
            visitorType = "false";
        } else if ("新访客".equalsIgnoreCase(visitorType)) {
            visitorType = "true";
        }
        paramMap.addValue("is_first_day", visitorType);
        return " and t.is_first_day = :is_first_day";
    }

    private List<String> transChannelFilter(List<String> channels) {
        List<String> channelList = new ArrayList<>();
        if (channels != null && !channels.isEmpty()) {
            for (String channel : channels) {
                LibType libType = LibType.parse(channel);
                if (libType != null && !"all".equalsIgnoreCase(libType.getValue())) {
                    channelList.add(libType.getValue());
                }
            }
        }
        return channelList;
    }

    private String periodLabel(String timeType) {
        if ("week".equalsIgnoreCase(timeType)) {
            return "concat(toString(toMonday(t.stat_date)), ' - ', toString(addDays(toMonday(t.stat_date), 6)))";
        }
        if ("month".equalsIgnoreCase(timeType)) {
            return "substring(toString(t.stat_date), 1, 7)";
        }
        return "toString(t.stat_date)";
    }

    private String periodStart(String timeType) {
        if ("week".equalsIgnoreCase(timeType)) {
            return "toMonday(t.stat_date)";
        }
        if ("month".equalsIgnoreCase(timeType)) {
            return "toStartOfMonth(t.stat_date)";
        }
        return "t.stat_date";
    }

    private float rate(int total, int value) {
        if (total <= 0) {
            return 0;
        }
        return Float.parseFloat(rateFormat.get().format(value * 100.0f / total));
    }

    private String defaultEndTime() {
        return dateFormat.get().format(new Date());
    }

    private String defaultStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        return dateFormat.get().format(calendar.getTime());
    }
}
