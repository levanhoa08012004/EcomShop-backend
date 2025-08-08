package com.example.webmuasam.service;

import com.example.webmuasam.Specification.ProductSpecification;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Category;
import com.example.webmuasam.entity.Images;
import com.example.webmuasam.entity.Product;
import com.example.webmuasam.entity.ProductVariant;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.CategoryRepository;
import com.example.webmuasam.repository.ImageRepository;
import com.example.webmuasam.repository.ProductRepository;
import com.example.webmuasam.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ImageRepository imageRepository;
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,ProductVariantRepository productVariantRepository, ImageRepository imageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
        this.imageRepository = imageRepository;
    }

    public Product createProduct(Product product) {
        // Gán danh mục
        if(product.getCategories() != null) {
            List<Long> categoryIds = product.getCategories().stream().map(x -> x.getId()).collect(Collectors.toList());
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            product.setCategories(categories);
        }


        // Gán lại product cho images & variants
        if (product.getImages() != null) {
            for (Images image : product.getImages()) {
                image.setProduct(product);
            }
        }

        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                variant.setProduct(product);
            }
        }

        // Tổng quantity từ variants
        long totalQuantity = product.getVariants() != null
                ? product.getVariants().stream().mapToLong(ProductVariant::getStockQuantity).sum()
                : 0;
        product.setQuantity(totalQuantity);

        return productRepository.save(product);
    }

    public Product updateProduct(Product updatedProduct) throws AppException {
        Product existingProduct = productRepository.findById(updatedProduct.getId())
                .orElseThrow(() -> new AppException("Product not found"));

        // Cập nhật thông tin cơ bản
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setDescription(updatedProduct.getDescription());

        // Cập nhật danh mục
        if(updatedProduct.getCategories() != null) {
            List<Long> categoryIds = updatedProduct.getCategories().stream().map(x -> x.getId()).collect(Collectors.toList());
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            existingProduct.setCategories(categories);
        }


        // Xoá ảnh và thêm mới
        imageRepository.deleteAllByProduct(existingProduct);
        if (updatedProduct.getImages() != null) {
            for (Images img : updatedProduct.getImages()) {
                img.setProduct(existingProduct);
            }
            existingProduct.setImages(updatedProduct.getImages());
        }

        // Xoá variant cũ và thêm mới
        productVariantRepository.deleteAllByProduct(existingProduct);
        if (updatedProduct.getVariants() != null) {
            for (ProductVariant variant : updatedProduct.getVariants()) {
                variant.setProduct(existingProduct);
            }
            existingProduct.setVariants(updatedProduct.getVariants());
        }

        // Tính lại tổng quantity
        long totalQuantity = updatedProduct.getVariants() != null
                ? updatedProduct.getVariants().stream().mapToLong(ProductVariant::getStockQuantity).sum()
                : 0;
        existingProduct.setQuantity(totalQuantity);

        return productRepository.save(existingProduct);
    }

    public ResultPaginationDTO getAllProduct(String name, Double minPrice, Double maxPrice, Long categoryId, Pageable pageable) {
        Specification<Product> spec = Specification.where(ProductSpecification.hasName(name))
                .and(ProductSpecification.hasPriceBetween(minPrice, maxPrice))
                .and(ProductSpecification.hasCategoryId(categoryId));
        Page<Product> products = this.productRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(products.getTotalElements());
        meta.setPages(products.getTotalPages());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(products.getContent());
        return resultPaginationDTO;
    }

    public Product getProduct(Long id) throws AppException {
        return this.productRepository.findById(id)
                .orElseThrow(() -> new AppException("Product không tồn tại với id = " + id));
    }
    public void deleteProduct(Long id) throws AppException {
        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> new AppException("Product không tồn tại với id = " + id));

        // Xử lý xóa liên kết với category nếu cần
        product.setCategories(null);

        // Xóa các biến thể sản phẩm nếu có
        product.getVariants().forEach(variant -> variant.setProduct(null));
        product.getVariants().clear();

        // Xử lý nếu có ảnh hoặc liên kết khác (ví dụ productImages)
        product.getImages().forEach(img -> img.setProduct(null));
        product.getImages().clear();

        // Sau khi làm sạch quan hệ -> xóa
        this.productRepository.delete(product);
    }


}
