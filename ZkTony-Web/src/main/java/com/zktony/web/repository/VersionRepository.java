package com.zktony.web.repository;

import com.zktony.web.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VersionRepository extends JpaRepository<Version, Long>, JpaSpecificationExecutor<Version> {

}