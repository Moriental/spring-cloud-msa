package com.example.ordersystem.product.controller;

import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResponseDTO;
import com.example.ordersystem.product.dto.ProductUpdateStockDTO;
import com.example.ordersystem.product.service.ProductService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> productCreate(ProductRegisterDto dto,@RequestHeader("X-User-Id") String userId){
        Product product = productService.productCreate(dto,userId);
        return new ResponseEntity<>(product.getId(), HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> productDetail(@PathVariable("id") Long id){
        ProductResponseDTO dto = productService.productDetail(id);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(@RequestBody ProductUpdateStockDTO dto){
        Product product= productService.updateStockQuantity(dto);
        return new ResponseEntity<>(product.getId(), HttpStatus.OK);
    }
}
