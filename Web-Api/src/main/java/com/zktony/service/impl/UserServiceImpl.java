package com.zktony.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zktony.entity.User;
import com.zktony.service.IUserService;
import com.zktony.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author liuhh
* @description 针对表【user】的数据库操作Service实现
* @createDate 2022-10-29 13:27:11
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements IUserService {

}




