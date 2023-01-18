package com.zktony.web.repository;

import com.zktony.web.entity.LogData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LogDataRepository extends JpaRepository<LogData, String>, JpaSpecificationExecutor<LogData> {

}