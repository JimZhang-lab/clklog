package com.zcunsoft.clklog.api.services;

import com.zcunsoft.clklog.api.models.eventanalysis.*;
import com.zcunsoft.clklog.api.models.enums.LibType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventAnalysisService {

    private final NamedParameterJdbcTemplate clickHouseJdbcTemplate;

    private static final ThreadLocal<DecimalFormat> RATE_FORMAT =
            ThreadLocal.withInitial(() -> new DecimalFormat("0.####"));

    public EventAnalysisService(NamedParameterJdbcTemplate clickHouseJdbcTemplate) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
    }

    public FunnelAnalysisResponseData funnelAnalysis(FunnelAnalysisRequest request) {
        FunnelAnalysisResponseData responseData = new FunnelAnalysisResponseData();
        List<FunnelStep> steps = normalizeSteps(request.getSteps());
        responseData.setMeasurement(normalizeMeasurement(request.getMeasurement()));
        responseData.setWindowSeconds(windowSeconds(request));
        long firstStepCount = 0L;
        long previousCount = 0L;
        for (int i = 0; i < steps.size(); i++) {
            long currentCount = countFunnelStep(request, steps, i);
            if (i == 0) {
                firstStepCount = currentCount;
            }
            FunnelStep step = steps.get(i);
            FunnelStepResult result = new FunnelStepResult();
            result.setName(StringUtils.defaultIfBlank(step.getName(),
                    StringUtils.defaultIfBlank(step.getCustomName(), eventName(step))));
            result.setEvent(eventName(step));
            result.setUserCount(currentCount);
            result.setStepRate(i == 0 ? 1F : rate(currentCount, previousCount));
            result.setTotalRate(i == 0 ? 1F : rate(currentCount, firstStepCount));
            responseData.getSteps().add(result);
            previousCount = currentCount;
        }
        responseData.setTotalUser(firstStepCount);
        return responseData;
    }

    public List<Map<String, Object>> customAnalysis(CustomAnalysisRequest request) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("select t.event as event, count(1) as eventCount, countDistinct(t.distinct_id) as userCount from log_analysis t");
        String where = baseWhere(request.getProjectName(), request.getStartTime(), request.getEndTime(), request.getChannel(), params);
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            where += " and t.event in (:events)";
            params.addValue("events", request.getEvents());
        }
        if (StringUtils.isNotBlank(where)) {
            sql.append(" where ").append(where.substring(5));
        }
        sql.append(" group by t.event order by eventCount desc limit 100");
        return clickHouseJdbcTemplate.queryForList(sql.toString(), params);
    }

    private long countFunnelStep(FunnelAnalysisRequest request, List<FunnelStep> steps, int endIndex) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> conditions = new ArrayList<>();
        List<String> events = new ArrayList<>();
        for (int i = 0; i <= endIndex; i++) {
            String eventParam = "event" + i;
            String event = eventName(steps.get(i));
            params.addValue(eventParam, event);
            conditions.add("t.event = :" + eventParam);
            events.add(event);
        }
        params.addValue("funnelEvents", events);
        params.addValue("requiredStep", endIndex + 1);
        String subject = "UV".equals(normalizeMeasurement(request.getMeasurement()))
                ? "t.distinct_id"
                : "if(t.event_session_id = '', t.distinct_id, concat(t.distinct_id, '#', t.event_session_id))";
        String where = baseWhere(request.getProjectName(), effectiveStartTime(request),
                effectiveEndTime(request), request.getChannel(), params);
        where += " and t.distinct_id <> '' and t.event in (:funnelEvents)";
        StringBuilder sql = new StringBuilder("select countIf(funnel_level >= :requiredStep) from (");
        sql.append("select ").append(subject).append(" as subject_id, ")
                .append("windowFunnel(").append(windowSeconds(request)).append(")")
                .append("(toUInt32(t.log_time), ")
                .append(String.join(", ", conditions))
                .append(") as funnel_level from log_analysis t where ")
                .append(where.substring(5))
                .append(" group by subject_id)");
        Number value = clickHouseJdbcTemplate.queryForObject(sql.toString(), params, Number.class);
        return value == null ? 0L : value.longValue();
    }

    private String baseWhere(String projectName, String startTime, String endTime, List<String> channel, MapSqlParameterSource params) {
        String where = "";
        if (StringUtils.isNotBlank(projectName)) {
            where += " and t.project_name = :projectName";
            params.addValue("projectName", projectName);
        }
        if (StringUtils.isNotBlank(startTime)) {
            where += " and t.stat_date >= :startTime";
            params.addValue("startTime", startTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            where += " and t.stat_date <= :endTime";
            params.addValue("endTime", endTime);
        }
        List<String> libs = normalizeChannels(channel);
        if (!libs.isEmpty()) {
            where += " and t.lib in (:channel)";
            params.addValue("channel", libs);
        }
        return where;
    }

    private List<String> normalizeChannels(List<String> channel) {
        if (channel == null) {
            return Collections.emptyList();
        }
        return channel.stream()
                .map(item -> {
                    LibType libType = LibType.parse(item);
                    return libType == null ? item : libType.getValue();
                })
                .filter(item -> StringUtils.isNotBlank(item) && !"all".equalsIgnoreCase(item) && !"全部".equals(item))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<FunnelStep> normalizeSteps(List<FunnelStep> steps) {
        if (steps == null) {
            return Collections.emptyList();
        }
        return steps.stream()
                .filter(step -> StringUtils.isNotBlank(eventName(step)))
                .collect(Collectors.toList());
    }

    private String eventName(FunnelStep step) {
        return StringUtils.defaultIfBlank(step.getEvent(), step.getEventName());
    }

    private String effectiveStartTime(FunnelAnalysisRequest request) {
        return StringUtils.defaultIfBlank(request.getStartTime(), request.getStartDate());
    }

    private String effectiveEndTime(FunnelAnalysisRequest request) {
        return StringUtils.defaultIfBlank(request.getEndTime(), request.getEndDate());
    }

    private String normalizeMeasurement(String measurement) {
        return "PV".equalsIgnoreCase(measurement) ? "PV" : "UV";
    }

    private long windowSeconds(FunnelAnalysisRequest request) {
        int numeral = request.getWindowNumeral() == null ? 24 : request.getWindowNumeral();
        String unit = request.getWindowUnit();
        if (request.getWindow() != null) {
            if (request.getWindow().getNumeral() != null) {
                numeral = request.getWindow().getNumeral();
            }
            unit = StringUtils.defaultIfBlank(request.getWindow().getUnit(), unit);
        }
        numeral = Math.max(1, numeral);
        String normalizedUnit = StringUtils.defaultIfBlank(unit, "HOUR").toUpperCase(Locale.ROOT);
        long multiplier;
        switch (normalizedUnit) {
            case "MINUTE":
            case "MINUTES":
                multiplier = 60L;
                break;
            case "DAY":
            case "DAYS":
                multiplier = 86400L;
                break;
            case "WEEK":
            case "WEEKS":
                multiplier = 604800L;
                break;
            case "HOUR":
            case "HOURS":
            default:
                multiplier = 3600L;
                break;
        }
        return Math.min(numeral * multiplier, 31536000L);
    }

    private float rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0F;
        }
        return Float.parseFloat(RATE_FORMAT.get().format(numerator * 1.0F / denominator));
    }
}
