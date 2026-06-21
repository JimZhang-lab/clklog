package com.zcunsoft.clklog.manage.repository.mysql;

import com.zcunsoft.clklog.manage.entity.mysql.TblUserTag;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTagRepository extends PagingAndSortingRepository<TblUserTag, String>,
        JpaSpecificationExecutor<TblUserTag> {

    Optional<TblUserTag> findByProjectNameAndTagKey(String projectName, String tagKey);

    long countByCategoryId(String categoryId);
}
