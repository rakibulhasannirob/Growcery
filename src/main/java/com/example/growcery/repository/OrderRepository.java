package com.example.growcery.repository;

import com.example.growcery.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);
    List<Order> findAllByOrderByOrderDateDesc();
}