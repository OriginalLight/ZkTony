package com.zktony.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zktony.entity.User;
import com.zktony.mapper.UserMapper;
import com.zktony.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * @author liuhh
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2022-10-29 13:27:11
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements IUserService {

    @Override
    public User login(User user) {
        // 返回email和password相同的用户
        return this.getOne(new QueryWrapper<User>().eq("email", user.getEmail()).eq("password", user.getPassword()));
    }
}




