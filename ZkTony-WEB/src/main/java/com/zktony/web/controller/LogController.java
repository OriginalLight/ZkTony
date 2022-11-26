package com.zktony.web.controller;

import com.zktony.web.commom.model.Result;
import com.zktony.web.entity.LogData;
import com.zktony.web.entity.LogRecord;
import com.zktony.web.repository.LogDataRepository;
import com.zktony.web.repository.LogRecordRepository;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 刘贺贺
 * @since 2022-09-23
 */
@RestController
@RequestMapping("/log")
public class LogController {

    @Resource
    private LogRecordRepository logRecordRepository;

    @Resource
    private LogDataRepository logDataRepository;

    @PostMapping("/data")
    public Result<Void> insertData(@RequestBody List<LogData> logDataList) {
        logDataRepository.saveAll(logDataList);
        return Result.ok();
    }

    @PostMapping("/record")
    public Result<Void> insertRecord(@RequestBody List<LogRecord> logRecordList) {
        logRecordRepository.saveAll(logRecordList);
        return Result.ok();
    }
}
