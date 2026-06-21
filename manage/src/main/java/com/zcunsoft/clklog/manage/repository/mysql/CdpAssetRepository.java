package com.zcunsoft.clklog.manage.repository.mysql;

import com.zcunsoft.clklog.manage.entity.mysql.TblCdpAsset;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CdpAssetRepository extends PagingAndSortingRepository<TblCdpAsset, String>,
        JpaSpecificationExecutor<TblCdpAsset> {

    Optional<TblCdpAsset> findByProjectNameAndAssetTypeAndAssetKey(String projectName, String assetType, String assetKey);
}
