package com.example.webmuasam.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Images;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.ImageRepository;

@Service
public class ImageService {
    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Images createImage(Images image) throws AppException {
        if (this.imageRepository.existsByBaseImage(image.getBaseImage())) {
            throw new AppException("ảnh đã tồn tại");
        }
        return this.imageRepository.save(image);
    }

    public Images updateImage(Images image) throws AppException {
        if (this.imageRepository.existsByBaseImage(image.getBaseImage())) {
            throw new AppException("Name Image da ton tai");
        }
        Images updateImage =
                this.imageRepository.findById(image.getId()).orElseThrow(() -> new AppException("Image not found"));
        if (updateImage != null) {
            updateImage.setBaseImage(updateImage.getBaseImage());
            return this.imageRepository.save(updateImage);
        }
        return null;
    }

    public void deleteImage(long id) throws AppException {
        Images images = this.imageRepository.findById(id).orElseThrow(() -> new AppException("Id images not found"));
        this.imageRepository.delete(images);
    }

    public ResultPaginationDTO getAllImages(Specification<Images> spec, Pageable pageable) throws AppException {
        Page<Images> pageImages = this.imageRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());

        meta.setTotal(pageImages.getTotalElements());
        meta.setPages(pageImages.getTotalPages());
        resultPaginationDTO.setMeta(meta);

        resultPaginationDTO.setResult(pageImages.getContent());
        return resultPaginationDTO;
    }
}
