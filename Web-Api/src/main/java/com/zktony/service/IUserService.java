package com.zktony.service;

import com.zktony.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author liuhh
* @description 针对表【user】的数据库操作Service
* @createDate 2022-10-29 13:27:11
*/
public interface IUserService extends IService<User> {

    User login(User user);
}
