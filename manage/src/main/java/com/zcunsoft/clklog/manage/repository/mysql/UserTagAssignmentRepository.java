package com.zcunsoft.clklog.manage.repository.mysql;

import com.zcunsoft.clklog.manage.entity.mysql.TblUserTagAssignment;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTagAssignmentRepository extends PagingAndSortingRepository<TblUserTagAssignment, String> {

    Optional<TblUserTagAssignment> findByProjectNameAndTagIdAndDistinctId(
            String projectName, String tagId, String distinctId);

    List<TblUserTagAssignment> findByProjectNameAndDistinctIdOrderByUpdateTimeDesc(
            String projectName, String distinctId);

    List<TblUserTagAssignment> findByProjectNameAndTagIdOrderByUpdateTimeDesc(
            String projectName, String tagId);

    long countByProjectNameAndTagId(String projectName, String tagId);

    void deleteByTagId(String tagId);

    void deleteByProjectNameAndTagIdAndDistinctId(String projectName, String tagId, String distinctId);
}
