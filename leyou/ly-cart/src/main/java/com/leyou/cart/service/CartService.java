package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX="cart:user:id:";

    public void addCart(Cart cart) {
        //首先获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //用userid作为redis的key加前缀
        String key=KEY_PREFIX+user.getId();
        //绑定一个key
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //取出hashkey
        String hashKey=cart.getSkuId().toString();
        //通过hashkey判断当前购物车商品是否存在
        if (operations.hasKey(hashKey)){//存在修改数量
            //将获得obj类型数据装换成json格式
            String json = operations.get(hashKey).toString();
            //再将json格式装换成cart对象
            Cart chenCart = JsonUtils.toBean(json, Cart.class);
            //修改数据
            chenCart.setNum(chenCart.getNum()+cart.getNum());
            System.out.println("商品存在修改数量");
            //再将chenCart装换成json，然后写回redis
            operations.put(hashKey,JsonUtils.toString(chenCart));

        }else {
            System.out.println("商品不存在添加商品");
            //不存在，添加
            operations.put(hashKey,JsonUtils.toString(cart));
        }
    }

    public List<Cart> queryListCart() {
        //首先获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //用userid作为redis的key加前缀
        String key=KEY_PREFIX+user.getId();
        //判断key为空返回404
        if(!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.NO_CART_FOUND);
        }
        //绑定一个key
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //拿到绑定key的所有value
        List<Cart> carts = operations.values().stream().map(o -> JsonUtils.toBean(o.toString(), Cart.class))
                .collect(Collectors.toList());
        return carts;
    }

    public void updateCartNum(Long skuId, Integer num) {
        //首先获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //用userid作为redis的key加前缀
        String key=KEY_PREFIX+user.getId();
        String hashKey=skuId.toString();
        //判断key为空返回404
        if(!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.NO_CART_FOUND);
        }
        //绑定一个key
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //判断是否存在
        if(!operations.hasKey(hashKey)){
            throw new LyException(ExceptionEnum.NO_CART_FOUND);
        }
        //查询购物车
        Cart cart = JsonUtils.toBean(operations.get(hashKey).toString(), Cart.class);
        cart.setNum(num);
        //写回redis
        operations.put(hashKey,JsonUtils.toString(cart));
    }

    public void deleteCart(Long skuId) {
        //首先获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //用userid作为redis的key加前缀
        String key=KEY_PREFIX+user.getId();
        //判断key为空返回404
        if(!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.NO_CART_FOUND);
        }
        redisTemplate.opsForHash().delete(key,skuId.toString());
    }
}
