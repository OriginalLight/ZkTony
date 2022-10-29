package com.zktony.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName user
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     *
     */
    @TableField(value = "name")
    private String name;

    /**
     *
     */
    @TableField(value = "age")
    private Integer age;

    /**
     *
     */
    @TableField(value = "sex")
    private Integer sex;

    /**
     *
     */
    @TableField(value = "address")
    private String address;

    /**
     *
     */
    @TableField(value = "phone")
    private String phone;

    /**
     *
     */
    @TableField(value = "email")
    private String email;

    /**
     *
     */
    @TableField(value = "password")
    private String password;

    /**
     *
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     *
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     *
     */
    @TableField(value = "create_by")
    private String createBy;

    /**
     *
     */
    @TableField(value = "update_by")
    private String updateBy;

}