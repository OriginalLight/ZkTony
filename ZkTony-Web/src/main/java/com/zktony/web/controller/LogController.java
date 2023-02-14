package com.zktony.web.controller;

import com.zktony.web.model.Result;
import com.zktony.web.entity.LogData;
import com.zktony.web.entity.LogRecord;
import com.zktony.web.repository.LogDataRepository;
import com.zktony.web.repository.LogRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private LogRecordRepository recordRepository;

    @Autowired
    private LogDataRepository dataRepository;

    @PostMapping("/data")
    public Result<Void> insertData(@RequestBody List<LogData> logDataList) {
        var result = dataRepository.saveAll(logDataList);
        if (result.isEmpty()) {
            return Result.fail();
        } else {
            return Result.ok();
        }
    }

    @PostMapping("/record")
    public Result<Void> insertRecord(@RequestBody List<LogRecord> logRecordList) {
        var result = recordRepository.saveAll(logRecordList);
        if (result.isEmpty()) {
            return Result.fail();
        } else {
            return Result.ok();
        }
    }
}
