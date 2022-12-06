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
@Table(name = "log_data")
public class LogData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "log_id")
    private String logId;

    @Column(name = "motor")
    private Integer motor;

    @Column(name = "voltage")
    private Float voltage;

    @Column(name = "current")
    private Float current;

    @Column(name = "time")
    private Integer time;

    @Column(name = "upload")
    private Integer upload;

    @Column(name = "create_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8", locale = "zh")
    private Date createTime;

}
