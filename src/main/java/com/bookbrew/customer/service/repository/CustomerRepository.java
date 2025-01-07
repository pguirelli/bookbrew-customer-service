package com.bookbrew.customer.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookbrew.customer.service.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

}