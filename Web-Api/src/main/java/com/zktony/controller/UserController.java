package com.zktony.controller;


import com.zktony.commom.model.Result;
import com.zktony.entity.User;
import com.zktony.service.IUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService iUserService;

    /**
     * 添加用户
     * @param user  用户信息
     * @return  Result
     */
    @PostMapping
    public Result<Boolean> insert(@RequestBody User user) {
        return Result.ok(iUserService.save(user));
    }

    /**
     * 批量添加用户
     * @param userList  用户信息
     * @return  Result
     */
    @PostMapping("/batch")
    public Result<Boolean> insertBatch(@RequestBody List<User> userList) {
        return Result.ok(iUserService.saveBatch(userList));
    }

    /**
     * 更新用户
     * @param user  用户信息
     * @return  Result
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody User user) {
        return Result.ok(iUserService.updateById(user));
    }

    /**
     * 批量更新用户
     * @param userList  用户信息
     * @return  Result
     */
    @PutMapping("/batch")
    public Result<Boolean> updateBatch(@RequestBody List<User> userList) {
        return Result.ok(iUserService.updateBatchById(userList));
    }

    /**
     * 删除用户
     * @param id  用户id
     * @return  Result
     */
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("id") Long id) {
        return Result.ok(iUserService.removeById(id));
    }

    /**
     * 批量删除用户
     * @param idList  用户id列表
     * @return  Result
     */
    @DeleteMapping("/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<Long> idList) {
        return Result.ok(iUserService.removeByIds(idList));
    }

    /**
     * 获取用户
     * @param id  用户id
     * @return  Result
     */
    @GetMapping
    public Result<User> getUser(@PathParam("id") Long id) {
        User user = iUserService.getById(id);
        if(user != null) {
            return Result.ok(user);
        } else {
            return Result.fail();
        }
    }

    /**
     * 获取用户列表
     * @return  Result
     */
    @GetMapping("/list")
    public Result<List<User>> getUserList() {
        List<User> userList = iUserService.list();
        if(userList != null) {
            return Result.ok(userList);
        } else {
            return Result.fail();
        }
    }

    /**
     * 登陆
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user) {
        User loginUser = iUserService.login(user);
        if(loginUser != null) {
            return Result.ok(loginUser);
        } else {
            return Result.fail();
        }
    }
}
