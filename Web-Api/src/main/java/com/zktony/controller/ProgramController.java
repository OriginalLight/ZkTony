package com.zktony.controller;


import com.zktony.commom.model.Result;
import com.zktony.entity.Program;
import com.zktony.service.IProgramService;
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
@RequestMapping("/program")
public class ProgramController {

    @Resource
    private IProgramService iProgramService;

    @PostMapping
    public Result<Void> insert(@RequestBody List<Program> programList) {
        iProgramService.saveOrUpdateBatch(programList);
        return Result.ok();
    }
}
