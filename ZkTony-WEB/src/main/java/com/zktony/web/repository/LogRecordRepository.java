package com.zktony.web.repository;

import com.zktony.web.entity.LogRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LogRecordRepository extends JpaRepository<LogRecord, String>, JpaSpecificationExecutor<LogRecord> {

}