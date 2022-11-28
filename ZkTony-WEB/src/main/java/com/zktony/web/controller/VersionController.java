package com.zktony.web.controller;


import com.zktony.web.commom.model.Result;
import com.zktony.web.entity.Version;
import com.zktony.web.repository.VersionRepository;
import jakarta.annotation.Resource;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 刘贺贺
 * @since 2022-09-23
 */
@RestController
@RequestMapping("/version")
public class VersionController {

    @Autowired
    private VersionRepository repository;

    @GetMapping
    public Result<Version> getVersion(@RequestParam("id") Long id) {
        var version = repository.findById(id).orElse(null);
        if (version != null) {
            return Result.ok(version);
        } else {
            return Result.fail();
        }
    }
}
