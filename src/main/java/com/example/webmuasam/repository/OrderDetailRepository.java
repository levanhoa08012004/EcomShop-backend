package com.example.webmuasam.repository;

import com.example.webmuasam.entity.Order;
import com.example.webmuasam.entity.OrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> , JpaSpecificationExecutor<OrderDetail> {
    Page<OrderDetail> findAllByOrderId(Long orderId, Pageable pageable);
    void deleteByOrderId(Long orderId);
    List<OrderDetail> findByOrderId(Long orderId);
}
