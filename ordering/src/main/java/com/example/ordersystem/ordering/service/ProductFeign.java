package com.example.ordersystem.ordering.service;

import com.example.ordersystem.ordering.dto.ProductDTO;
import com.example.ordersystem.ordering.dto.ProductUpdateStockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//name은 eureka에 등록된 호출할 서비스의 이름
//마치 controller에 매핑처럼 사용하면 됨 즉 유레카에 /localhost:8080/product-service를 질의
@FeignClient(name = "product-service")
public interface ProductFeign {
    @GetMapping("/product/{productId}")
    ProductDTO getProductById(@PathVariable("productId") Long productId, @RequestHeader("X-User-Id") String userId);

    @PutMapping("/product/updatestock")
    void updateProductStock(@RequestBody ProductUpdateStockDTO productUpdateStockDTO);
}
