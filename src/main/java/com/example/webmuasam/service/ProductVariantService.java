package com.example.webmuasam.service;

import com.example.webmuasam.dto.Response.ProductVariantResponse;
import com.example.webmuasam.entity.Product;
import com.example.webmuasam.entity.ProductVariant;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.ProductRepository;
import com.example.webmuasam.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductVariantService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    public ProductVariantService(ProductVariantRepository productVariantRepository,ProductRepository productRepository) {
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
    }

    public ProductVariant createProductVariant(Long productId,ProductVariant productVariant) throws AppException {
        Product product = this.productRepository.findById(productId).orElseThrow(() -> new AppException("Product not found"));
        productVariant.setProduct(product);
        if(this.productVariantRepository.existsByProduct_IdAndSizeAndColor(productId,productVariant.getSize(), productVariant.getColor())) {
            throw new AppException("Product variant already exists");
        }
        return this.productVariantRepository.save(productVariant);
    }

    public List<ProductVariantResponse> getAllByProductID(Long productId) throws AppException {
        if(!this.productRepository.existsById(productId)) {
            throw new AppException("sản phẩm không tồn tại");
        }
        List<ProductVariant> list=this.productVariantRepository.findAllByProduct_Id(productId);
        return list.stream().map(variant ->{
            ProductVariantResponse response = new ProductVariantResponse();
            response.setId(variant.getId());
            response.setSize(variant.getSize());
            response.setColor(variant.getColor());
            response.setStockQuantity(variant.getStockQuantity());

            ProductVariantResponse.Product product = new ProductVariantResponse.Product();
            product.setProductId(variant.getProduct().getId());
            product.setProductName(variant.getProduct().getName());
            response.setProduct(product);
            return response;
        }).collect(Collectors.toList());

    }

    public ProductVariantResponse getProductVariant(Long id) throws AppException {
        ProductVariant productVariant = this.productVariantRepository.findById(id).orElseThrow(() -> new AppException("Product variant not found"));
        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(productVariant.getId());
        response.setSize(productVariant.getSize());
        response.setColor(productVariant.getColor());
        response.setStockQuantity(productVariant.getStockQuantity());
        ProductVariantResponse.Product product = new ProductVariantResponse.Product();
        product.setProductId(productVariant.getProduct().getId());
        product.setProductName(productVariant.getProduct().getName());
        response.setProduct(product);
        return response;
    }


    public ProductVariant updateProductVariant(ProductVariant updated) throws AppException {
        ProductVariant productVariant = this.productVariantRepository.findById(updated.getId()).orElseThrow(()->new AppException("ProductVariant không tồn tại")) ;
        productVariant.setColor(updated.getColor());
        productVariant.setSize(updated.getSize());
        productVariant.setStockQuantity(updated.getStockQuantity());
        productVariant.setProduct(updated.getProduct());
        return productVariantRepository.save(productVariant);
    }

    public void deleteproductVariant(Long productVariantId) throws AppException {
        if (!productVariantRepository.existsById(productVariantId)) {
            throw new AppException("ProductVariant not found with id: " + productVariantId);
        }
        productVariantRepository.deleteById(productVariantId);
    }


    @Transactional
    public void decreaseStock(Long variantId, int quantity) throws AppException {
        ProductVariant variant = this.productVariantRepository.findById(variantId).orElseThrow(()->new AppException("productVariant không tồn tại"));
        if (variant.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock for variant ID: " + variantId);
        }
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        productVariantRepository.save(variant);
    }

    @Transactional
    public void increaseStock(Long variantId, int quantity) throws AppException {
        ProductVariant variant = this.productVariantRepository.findById(variantId).orElseThrow(()->new AppException("productVariant không tồn tại"));
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        productVariantRepository.save(variant);
    }
}
