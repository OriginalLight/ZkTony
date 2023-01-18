package com.zktony.web.repository;

import com.zktony.web.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProgramRepository extends JpaRepository<Program, String>, JpaSpecificationExecutor<Program> {

}