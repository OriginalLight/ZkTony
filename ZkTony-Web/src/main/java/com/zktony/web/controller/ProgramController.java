package com.zktony.web.controller;


import com.zktony.web.model.Result;
import com.zktony.web.entity.Program;
import com.zktony.web.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 刘贺贺
 * @since 2022-09-23
 */
@RestController
@RequestMapping("/program")
public class ProgramController {

    @Autowired
    private ProgramRepository repository;

    @PostMapping
    public Result<Void> insert(@RequestBody List<Program> programList) {
        var result = repository.saveAll(programList);
        if (result.isEmpty()) {
            return Result.fail();
        } else {
            return Result.ok();
        }
    }
}
