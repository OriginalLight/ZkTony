package com.zktony.controller;


import com.zktony.commom.model.Result;
import com.zktony.entity.LogData;
import com.zktony.entity.LogRecord;
import com.zktony.service.ILogDataService;
import com.zktony.service.ILogRecordService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 刘贺贺
 * @since 2022-09-23
 */
@RestController
@RequestMapping("/log")
public class LogController {

    @Resource
    private ILogDataService iLogDataService;

    @Resource
    private ILogRecordService iLogRecordService;

    @PostMapping("/data")
    public Result<Void> insertData(@RequestBody List<LogData> logDataList) {
        iLogDataService.saveOrUpdateBatch(logDataList);
        return Result.ok();
    }

    @PostMapping("/record")
    public Result<Void> insertRecord(@RequestBody List<LogRecord> logRecordList) {
        iLogRecordService.saveOrUpdateBatch(logRecordList);
        return Result.ok();
    }
}
