package com.zktony.service.impl;

import com.zktony.entity.Version;
import com.zktony.mapper.VersionMapper;
import com.zktony.service.IVersionService;
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
public class VersionServiceImpl extends ServiceImpl<VersionMapper, Version> implements IVersionService {

}
