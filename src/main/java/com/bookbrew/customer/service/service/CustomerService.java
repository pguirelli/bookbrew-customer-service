package com.bookbrew.customer.service.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookbrew.customer.service.client.UserClient;
import com.bookbrew.customer.service.dto.AddressUpdateDTO;
import com.bookbrew.customer.service.dto.CustomerDTO;
import com.bookbrew.customer.service.dto.CustomerUpdateDTO;
import com.bookbrew.customer.service.dto.UserDTO;
import com.bookbrew.customer.service.dto.UserProfileDTO;
import com.bookbrew.customer.service.exception.DuplicateAddressException;
import com.bookbrew.customer.service.exception.ResourceNotFoundException;
import com.bookbrew.customer.service.model.Address;
import com.bookbrew.customer.service.model.Customer;
import com.bookbrew.customer.service.repository.AddressRepository;
import com.bookbrew.customer.service.repository.CustomerRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

@Service
public class CustomerService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private Validator validator;

    public List<CustomerDTO> findAll() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(customer -> {
                    CustomerDTO customerDTO = modelMapper.map(customer, CustomerDTO.class);
                    UserDTO userDTO = userClient.getUserById(customer.getUserId());
                    customerDTO.setName(userDTO.getName());
                    customerDTO.setLastName(userDTO.getLastName());
                    customerDTO.setEmail(userDTO.getEmail());
                    customerDTO.setCpf(userDTO.getCpf());
                    customerDTO.setPhone(userDTO.getPhone());
                    customerDTO.setStatus(userDTO.getStatus());
                    customerDTO.setProfileId(userDTO.getProfile().getId());
                    customerDTO.setPassword(userDTO.getPassword());
                    customerDTO.setCreationDate(userDTO.getCreationDate());
                    customerDTO.setUpdateDate(userDTO.getUpdateDate());
                    customerDTO.setLastLoginDate(userDTO.getLastLoginDate());
                    customerDTO.setPasswordUpdateDate(userDTO.getPasswordUpdateDate());
                    return customerDTO;
                })
                .collect(Collectors.toList());
    }

    public CustomerDTO findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        CustomerDTO customerDTO = modelMapper.map(customer, CustomerDTO.class);
        UserDTO userDTO = userClient.getUserById(customer.getUserId());

        customerDTO.setName(userDTO.getName());
        customerDTO.setLastName(userDTO.getLastName());
        customerDTO.setEmail(userDTO.getEmail());
        customerDTO.setCpf(userDTO.getCpf());
        customerDTO.setPhone(userDTO.getPhone());
        customerDTO.setStatus(userDTO.getStatus());
        customerDTO.setProfileId(userDTO.getProfile().getId());
        customerDTO.setPassword(userDTO.getPassword());
        customerDTO.setCreationDate(userDTO.getCreationDate());
        customerDTO.setUpdateDate(userDTO.getUpdateDate());
        customerDTO.setLastLoginDate(userDTO.getLastLoginDate());
        customerDTO.setPasswordUpdateDate(userDTO.getPasswordUpdateDate());

        return customerDTO;
    }

    @Transactional
    public Customer createCustomer(CustomerDTO customerDTO) {
        List<Address> addresses = new ArrayList<>();

        UserDTO userDTO = new UserDTO();
        userDTO.setName(customerDTO.getName());
        userDTO.setLastName(customerDTO.getLastName());
        userDTO.setEmail(customerDTO.getEmail());
        userDTO.setCpf(customerDTO.getCpf());
        userDTO.setPhone(customerDTO.getPhone());
        userDTO.setPassword(customerDTO.getPassword());
        userDTO.setStatus(customerDTO.getStatus());
        userDTO.setProfile(new UserProfileDTO());
        userDTO.getProfile().setId(customerDTO.getProfileId());

        Set<ConstraintViolation<CustomerDTO>> violations = validator.validate(customerDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        for (Address address : customerDTO.getAddresses()) {
            Set<ConstraintViolation<Address>> violationAddress = validator.validate(address);
            if (!violationAddress.isEmpty()) {
                throw new ConstraintViolationException(violationAddress);
            }
        }

        Set<Address> uniqueAddresses = new HashSet<>(customerDTO.getAddresses());
        if (uniqueAddresses.size() < customerDTO.getAddresses().size()) {
            throw new DuplicateAddressException("Duplicate addresses are not allowed");
        }

        UserDTO createdUser = userClient.createUser(userDTO);

        Customer customer = new Customer();
        customer.setId(null);
        customer.setUserId(createdUser.getId());

        for (Address address : customerDTO.getAddresses()) {
            Address savedAddress = addressRepository.save(address);
            addresses.add(savedAddress);
        }

        customer.setAddresses(addresses);
        customer.setBirthDate(customerDTO.getBirthDate());

        Customer savedCustomer = customerRepository.save(customer);
        UserDTO savedUser = userClient.getUserById(savedCustomer.getUserId());
        savedCustomer.setUserDTO(savedUser);

        return savedCustomer;
    }

    @Transactional
    public CustomerDTO update(Long id, CustomerUpdateDTO customerUpdateDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        UserDTO currentUserDTO = userClient.getUserById(customer.getUserId());

        UserDTO updateUserDTO = new UserDTO();
        updateUserDTO.setId(currentUserDTO.getId());

        if (customerUpdateDTO.getName() != null)
            updateUserDTO.setName(customerUpdateDTO.getName());
        if (customerUpdateDTO.getLastName() != null)
            updateUserDTO.setLastName(customerUpdateDTO.getLastName());
        if (customerUpdateDTO.getEmail() != null)
            updateUserDTO.setEmail(customerUpdateDTO.getEmail());
        if (customerUpdateDTO.getPhone() != null)
            updateUserDTO.setPhone(customerUpdateDTO.getPhone());
        if (customerUpdateDTO.getPassword() != null)
            updateUserDTO.setPassword(customerUpdateDTO.getPassword());
        if (customerUpdateDTO.getStatus() != null)
            updateUserDTO.setStatus(customerUpdateDTO.getStatus());
        updateUserDTO.setProfile(currentUserDTO.getProfile());
        updateUserDTO.setCpf(currentUserDTO.getCpf());

        userClient.updateUser(customer.getUserId(), updateUserDTO);

        if (customerUpdateDTO.getBirthDate() != null) {
            customer.setBirthDate(customerUpdateDTO.getBirthDate());
        }

        if (customerUpdateDTO.getAddresses() != null && !customerUpdateDTO.getAddresses().isEmpty()) {
            customer.setAddresses(customerUpdateDTO.getAddresses());
            for (Address address : customer.getAddresses()) {
                addressRepository.save(address);
            }
        }

        Customer savedCustomer = customerRepository.save(customer);
        return findById(savedCustomer.getId());
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        UserDTO currentUserDTO = userClient.getUserById(customer.getUserId());

        customerRepository.deleteById(customerId);

        userClient.deleteUser(currentUserDTO.getId());
    }

    @Transactional
    public CustomerDTO updateAddress(Long customerId, Long addressId, AddressUpdateDTO newAddress) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (newAddress.getZipCode() != null)
            address.setZipCode(newAddress.getZipCode());
        if (newAddress.getStreet() != null)
            address.setStreet(newAddress.getStreet());
        if (newAddress.getNumber() != null)
            address.setNumber(newAddress.getNumber());
        if (newAddress.getNeighborhood() != null)
            address.setNeighborhood(newAddress.getNeighborhood());
        if (newAddress.getType() != null)
            address.setType(newAddress.getType());
        if (newAddress.getCity() != null)
            address.setCity(newAddress.getCity());
        if (newAddress.getState() != null)
            address.setState(newAddress.getState());
        if (newAddress.getCountry() != null)
            address.setCountry(newAddress.getCountry());

        addressRepository.save(address);

        return findById(customer.getId());
    }

    @Transactional
    public void deleteCustomerAddress(Long customerId, Long addressId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Address addressToDelete = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!customer.getAddresses().contains(addressToDelete)) {
            throw new ResourceNotFoundException("Address does not belong to this customer");
        }

        customer.getAddresses().remove(addressToDelete);

        customerRepository.save(customer);
    }

}
