package com.zcunsoft.clklog.manage.models.analytics;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> rows;

    private long total;

    public PageResult(List<T> rows, long total) {
        this.rows = rows;
        this.total = total;
    }
}
