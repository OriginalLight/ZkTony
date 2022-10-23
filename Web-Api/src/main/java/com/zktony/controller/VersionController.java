package com.zktony.controller;


import com.zktony.commom.model.Result;
import com.zktony.entity.Version;
import com.zktony.service.IVersionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 刘贺贺
 * @since 2022-09-23
 */
@RestController
@RequestMapping("/version")
public class VersionController {
    
    @Resource
    private IVersionService iVersionService;

    @GetMapping
    public Result<Version> getVersion(@PathParam("id") Long id) {
        Version version = iVersionService.getById(id);
        if(version != null) {
            return Result.ok(version);
        } else {
            return Result.fail();
        }
    }
}
