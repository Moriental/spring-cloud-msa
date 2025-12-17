package com.example.ordersystem.product.service;

import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResponseDTO;
import com.example.ordersystem.product.dto.ProductUpdateStockDTO;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product productCreate(ProductRegisterDto dto,String userId){

        Product product = productRepository.save(dto.toEntity(Long.parseLong(userId)));
        return product;
    }

    public ProductResponseDTO productDetail(Long id) {
        Product product = productRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("없는 상품입니다."));
        ProductResponseDTO dto = ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();

        return dto;
    }

    public Product updateStockQuantity(ProductUpdateStockDTO dto) {
        Product product =productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("없는 상품입니다."));
        product.updateStockQuantity(dto.getProductQuantity());
        return product;
    }
}
