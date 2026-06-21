package com.zcunsoft.clklog.manage.repository.mysql;

import com.zcunsoft.clklog.manage.entity.mysql.TblTagCategory;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagCategoryRepository extends PagingAndSortingRepository<TblTagCategory, String>,
        JpaSpecificationExecutor<TblTagCategory> {
}
