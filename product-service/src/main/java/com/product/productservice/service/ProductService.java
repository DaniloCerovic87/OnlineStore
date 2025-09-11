package com.product.productservice.service;


import com.product.productservice.dto.CreateProductRequest;
import com.product.productservice.dto.CreateProductResponse;
import com.product.productservice.model.Product;
import com.product.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public CreateProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .skuCode(request.skuCode())
                .description(request.description())
                .price(request.price())
                .build();
        productRepository.save(product);
        log.info("Product created successfully: {}", product );
        return new CreateProductResponse(product.getId(), product.getName(), product.getSkuCode(), product.getDescription(), product.getPrice());
    }

    public List<CreateProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(p -> new CreateProductResponse(p.getId(), p.getName(), p.getSkuCode(), p.getDescription(), p.getPrice()))
                .toList();
    }

}
