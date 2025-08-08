package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Product;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.ProductService;
import com.example.webmuasam.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @PostMapping
    @ApiMessage("add Product success")
    public ResponseEntity<Product> CreateProduct(@Valid @RequestBody Product product) {

        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(product));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get product success")
    public ResponseEntity<Product> GetProduct(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok(this.productService.getProduct(id));
    }

    @GetMapping
    @ApiMessage("Get All Product success")
    public ResponseEntity<ResultPaginationDTO> GetAllProduct(@RequestParam(required = false) String name,
                                                             @RequestParam(required = false) Double minPrice,
                                                             @RequestParam(required = false) Double maxPrice,
                                                             @RequestParam(required = false) Long categoryId,
                                                             Pageable pageable) {
        return ResponseEntity.ok(this.productService.getAllProduct(name,minPrice,maxPrice,categoryId,pageable));
    }

    @PutMapping
    @ApiMessage("Update product success")
    public ResponseEntity<Product> UpdateProduct(@Valid @RequestBody Product product) throws AppException {
        return ResponseEntity.ok(this.productService.updateProduct(product));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete user success")
    public ResponseEntity<String> DeleteProduct(@PathVariable Long id) throws AppException {
        this.productService.deleteProduct(id);
        return ResponseEntity.ok("success");
    }
}
