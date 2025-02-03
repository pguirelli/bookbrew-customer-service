package com.bookbrew.customer.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookbrew.customer.service.dto.AddressUpdateDTO;
import com.bookbrew.customer.service.dto.CustomerDTO;
import com.bookbrew.customer.service.dto.CustomerSearchDTO;
import com.bookbrew.customer.service.model.Address;
import com.bookbrew.customer.service.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerSearchDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerSearchDTO> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(customerDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerSearchDTO> updateCustomer(@PathVariable Long id,
            @Valid @RequestBody CustomerDTO customerUpdateDTO) {
        return ResponseEntity.ok(customerService.update(id, customerUpdateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Address> getCustomerAddressById(
            @PathVariable Long customerId,
            @PathVariable Long addressId) {
        return ResponseEntity.ok(customerService.getAddressById(customerId, addressId));
    }

    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<List<Address>> getCustomerAddresses(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getAddresses(customerId));
    }

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<Address> addCustomerAddress(
            @PathVariable Long customerId,
            @Valid @RequestBody Address address) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addAddress(customerId, address));
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Address> updateCustomerAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateDTO address) {
        return ResponseEntity.ok(customerService.updateAddress(customerId, addressId, address));
    }

    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteCustomerAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId) {
        customerService.deleteCustomerAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }

}
