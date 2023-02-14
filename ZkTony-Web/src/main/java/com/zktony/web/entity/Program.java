package com.zktony.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "program")
public class Program implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "motor")
    private Integer motor;

    @Column(name = "voltage")
    private Float voltage;

    @Column(name = "time")
    private Float time;

    @Column(name = "count")
    private Integer count;

    @Column(name = "thickness")
    private String thickness;

    @Column(name = "glue_type")
    private Integer glueType;

    @Column(name = "glue_concentration")
    private Float glueConcentration;

    @Column(name = "glue_max_concentration")
    private Float glueMaxConcentration;

    @Column(name = "glue_min_concentration")
    private Float glueMinConcentration;

    @Column(name = "protein_max_size")
    private Float proteinMaxSize;

    @Column(name = "protein_min_size")
    private Float proteinMinSize;

    @Column(name = "protein_name")
    private String proteinName;

    @Column(name = "buffer_type")
    private String bufferType;

    @Column(name = "model")
    private Integer model;

    @Column(name = "status")
    private Integer status;

    @Column(name = "def")
    private Integer def;

    @Column(name = "upload")
    private Integer upload;

    @Column(name = "create_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8", locale = "zh")
    private Date createTime;

}
