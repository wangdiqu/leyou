package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 添加购物车
     * @param cart
     * @return
     */
     @PostMapping
     public ResponseEntity<Void> addCart(@RequestBody Cart cart){
         System.out.println("-------------------------[添加购物车]-----------------------------------------");
         cartService.addCart(cart);
         return ResponseEntity.status(HttpStatus.CREATED).build();
     }

    /**
     *查询购物车
     * @return
     */
     @GetMapping("list")
     public ResponseEntity<List<Cart>> queryListCart(){
         System.out.println("list");
         return ResponseEntity.ok(cartService.queryListCart());
     }

    /**
     * 修改购物车数量
     * @param skuId
     * @param num
     * @return
     */
     @PutMapping
     public ResponseEntity<Void> updateCartNum(@RequestParam("id") Long skuId,@RequestParam("num") Integer num){
         cartService.updateCartNum(skuId,num);
         return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
     }
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId")Long skuId){
        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

