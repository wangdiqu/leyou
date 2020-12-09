package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.internal.NotNull;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Date;
@Data
@Table(name = "tb_user")
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long Id;
    @NotNull
    @Pattern(regexp = "^[0-9a-zA-Z_]{1,26}$",message = "用户名格式不正确")
    private String username;//用户名
    @JsonIgnore
    @NotNull
    @Pattern(regexp = "^[0-9a-zA-Z_]{1,26}$",message = "密码格式不正确")
    private String password;//密码
    @Pattern(regexp = "^1[345789]\\d{9}$",message = "手机号不合法")
    private String phone;//手机号
    private Date created;//创建时间
    @JsonIgnore
    private String salt;//密码的盐值
}
