package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.Md5Utils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String KEY_VERIFY="user:verify:phone:";

    /**
     * 校验数据
     * @param data
     * @param type
     * @return
     */
    public Boolean checkData(String data, Integer type) {
        User user=new User();
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.USER_DATA_TYPE);
        }
         return userMapper.selectCount(user)==0;
    }

    /**
     * 发送短信
     * @param phone
     */
    public void sendCode(String phone) {
        //生成key
        String key=KEY_VERIFY+phone;
        Map<String,String> msg=new HashMap<>();
        String code= NumberUtils.generateCode(6);
        msg.put("phone",phone);
        msg.put("code",code);
        //发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
        //保存验证码
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);//时长为5分钟
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    public Boolean registerUser(User user,String code) {
        //短信验证
        String key=KEY_VERIFY+user.getPhone();
        String keyRedis = redisTemplate.opsForValue().get(key);
        if (!keyRedis.equals(code) || StringUtils.isBlank(code)) {
            return false;
        }
        user.setId(null);
        //密码加密,先获取盐
        String salt = Md5Utils.generate();
        user.setSalt(salt);
        //用盐给密码加密
        String md5Password = Md5Utils.encryptPassword(user.getPassword(), salt);
        user.setPassword(md5Password);
        user.setCreated(new Date(System.currentTimeMillis()));
        //数据插入数据库
        Boolean result=userMapper.insertSelective(user)==1;
        //注册成功删除redis中的数据
        if (result) {
            try {
                redisTemplate.delete(keyRedis);
            }catch (Exception e){
                log.error("redis数据删除失败！,key:{}",keyRedis);
            }
        }
        return result;
    }

    /**
     * 登录验证
     * @param username
     * @param password
     * @return
     */
    public User queryUser(String username, String password) {
    // 查询
    User record = new User();
        record.setUsername(username);
    User user = this.userMapper.selectOne(record);
    // 校验用户名
        if (user == null) {
            return null;
         }
    // 校验密码
        if (!user.getPassword().equals(Md5Utils.encryptPassword(password, user.getSalt()))) {
        return null;
    }
    // 用户名密码都正确
        return user;
    }
}
