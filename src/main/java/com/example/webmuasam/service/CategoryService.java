package com.example.webmuasam.service;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Category;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) throws AppException{
        if(this.categoryRepository.existsByName(category.getName())) {
            throw new AppException("Name category da ton tai");
        }
        return this.categoryRepository.save(category);
    }
    public Category updateCategory(Category category) throws AppException{
        if(this.categoryRepository.existsByName(category.getName())) {
            throw new AppException("Name category da ton tai");
        }
        Category updateCategory = this.categoryRepository.findById(category.getId()).orElseThrow(()-> new AppException("Id category not found"));
        if(updateCategory !=null){
            updateCategory.setName(category.getName());
            return this.categoryRepository.save(updateCategory);
        }
        return null;
    }
    public Category getCategoryById(@PathVariable Long id) throws AppException {
        return this.categoryRepository.findById(id).orElseThrow(()-> new AppException("Id category not found"));
    }

    public void deleteCategory(long id) throws AppException{
        Category category = this.categoryRepository.findById(id).orElseThrow(()-> new AppException("Id category not found"));
        category.getProducts().forEach(product -> product.getCategories().remove(category));
        this.categoryRepository.delete(category);
    }

    public ResultPaginationDTO getAllCategories(Specification<Category> spec, Pageable pageable) {
        Page<Category> pageCategories = this.categoryRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());

        meta.setTotal(pageCategories.getTotalElements());
        meta.setPages(pageCategories.getTotalPages());
        resultPaginationDTO.setMeta(meta);

        resultPaginationDTO.setResult(pageCategories.getContent());
        return resultPaginationDTO;
    }
}
