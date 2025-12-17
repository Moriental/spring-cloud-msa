package com.example.ordersystem.ordering.service;

import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.dto.ProductDTO;
import com.example.ordersystem.ordering.dto.ProductUpdateStockDTO;
import com.example.ordersystem.ordering.service.ProductFeign;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    //bean으로 restTemplate 등록
    private final RestTemplate restTemplate;
    private final ProductFeign productFeign;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    public OrderingService(OrderingRepository orderingRepository, RestTemplate restTemplatem, RestTemplate restTemplate, ProductFeign productFeign, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderingRepository = orderingRepository;
        this.restTemplate = restTemplate;
        this.productFeign = productFeign;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Ordering orderCreate(OrderCreateDto orderDto,String userId){
        //product get 요청
        String productGetUrl = "http://product-service/product/" + orderDto.getProductId();

        //requestEntity 부분에는 만약 우리가 apigateway에서 x-user-id값을 받아왔을 떄 product와 통신을 할 때 그 x-user-id값을 담아줘야함
        //커스텀 객체라서 넘겨줄 때 사라짐(그래서 명시해줘야함)

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-User-Id",userId);
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<ProductDTO> response = restTemplate.exchange(productGetUrl, HttpMethod.GET, httpEntity,ProductDTO.class);
        //get 요청이므로 응답받을 클래스 명시
        ProductDTO productDTO = response.getBody(); // response에서 받아온 객체에 productDTO를 꺼냄
        int quantity = orderDto.getProductCount();
        if(productDTO.getStockQuantity() < quantity){
            throw new IllegalArgumentException("재고 부족");
        }else {
            //product put 요청
            String productPutUrl = "http://product-service/product/updatestock";
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductUpdateStockDTO> updateEntity = new HttpEntity(
                    //body
                ProductUpdateStockDTO.builder()
                        .productId(orderDto.getProductId())
                        .productQuantity(orderDto.getProductCount())
                        .build()
                    //header
                    , httpHeaders
            );
            //응답을 받을게 없으면 put mapping이므로 Void class 선언
            restTemplate.exchange(productPutUrl,HttpMethod.PUT,updateEntity, Void.class);
        }
        Ordering ordering = Ordering.builder()
                .memberId(Long.parseLong(userId))
                .productId(orderDto.getProductId())
                .quantity(orderDto.getProductCount())
                .build();
        orderingRepository.save(ordering);
        return  ordering;
    }
    public Ordering orderFeignKafkaCreate(OrderCreateDto orderDto,String userId){
        ProductDTO productDTO= productFeign.getProductById(orderDto.getProductId(),userId);

        int quantity = orderDto.getProductCount();
        if(productDTO.getStockQuantity() < quantity){
            throw new IllegalArgumentException("재고 부족");
        }else {
//            productFeign.updateProductStock(ProductUpdateStockDTO.builder()
//                    .productId(orderDto.getProductId())
//                    .productQuantity(orderDto.getProductCount())
//                    .build());
            kafkaTemplate.send("update-stock-topic",ProductUpdateStockDTO.builder()
                   .productId(orderDto.getProductId())
                   .productQuantity(orderDto.getProductCount())
                   .build());
        }
        Ordering ordering = Ordering.builder()
                .memberId(Long.parseLong(userId))
                .productId(orderDto.getProductId())
                .quantity(orderDto.getProductCount())
                .build();
        orderingRepository.save(ordering);
        return  ordering;
    }
}
