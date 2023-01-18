package com.zktony.web.service.impl;

import com.zktony.web.entity.LogRecord;
import com.zktony.web.mapper.LogRecordMapper;
import com.zktony.web.service.ILogRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 刘贺贺
 * @since 2022-09-23
 */
@Service
public class LogRecordServiceImpl extends ServiceImpl<LogRecordMapper, LogRecord> implements ILogRecordService {

}
