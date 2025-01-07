package com.bookbrew.customer.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookbrew.customer.service.dto.AddressUpdateDTO;
import com.bookbrew.customer.service.dto.CustomerDTO;
import com.bookbrew.customer.service.dto.CustomerUpdateDTO;
import com.bookbrew.customer.service.model.Customer;
import com.bookbrew.customer.service.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.findAll();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        CustomerDTO customer = customerService.findById(id);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody CustomerDTO customerDTO) {
        Customer createdCustomer = customerService.createCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id,
            @Valid @RequestBody CustomerUpdateDTO customerUpdateDTO) {
        return ResponseEntity.ok(customerService.update(id, customerUpdateDTO));
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<CustomerDTO> updateCustomerAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateDTO address) {
        CustomerDTO updatedCustomer = customerService.updateAddress(customerId, addressId, address);
        return ResponseEntity.ok(updatedCustomer);
    }

}
