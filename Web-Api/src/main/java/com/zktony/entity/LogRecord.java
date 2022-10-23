package com.zktony.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author 刘贺贺
 * @since 2022-09-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("log_record")
public class LogRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("program_id")
    private String programId;

    @TableField("motor")
    private Integer motor;

    @TableField("voltage")
    private Float voltage;

    @TableField("time")
    private Float time;

    @TableField("model")
    private Integer model;

    @TableField("upload")
    private Integer upload;

    @TableField("create_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8", locale = "zh")
    private Date createTime;


}
