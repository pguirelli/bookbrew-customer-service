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
import com.bookbrew.customer.service.dto.CustomerSearchDTO;
import com.bookbrew.customer.service.dto.UserDTO;
import com.bookbrew.customer.service.dto.UserResponseDTO;
import com.bookbrew.customer.service.exception.DuplicateAddressException;
import com.bookbrew.customer.service.exception.ResourceNotFoundException;
import com.bookbrew.customer.service.model.Address;
import com.bookbrew.customer.service.model.Customer;
import com.bookbrew.customer.service.repository.AddressRepository;
import com.bookbrew.customer.service.repository.CustomerRepository;

import feign.FeignException;
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

    public List<CustomerSearchDTO> findAll() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerSearchDTO> customerDTOs = new ArrayList<>();

        if (customers.isEmpty()) {
            throw new ResourceNotFoundException("No customers found");
        }

        for (Customer customer : customers) {
            customerDTOs.add(convertToCustomerSearchDTO(customer));
        }

        return customerDTOs;
    }

    public CustomerSearchDTO findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        return convertToCustomerSearchDTO(customer);
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        UserResponseDTO createdUser = null;
        try {
            customerDTO.setProfile((long) 4);

            validateCustomerData(customerDTO);

            UserDTO userDTO = prepareUserDTO(customerDTO);
            createdUser = userClient.createUser(userDTO);

            Customer customer = new Customer();
            customer.setUserId(createdUser.getId());
            customer.setBirthDate(customerDTO.getBirthDate());

            List<Address> savedAddresses = saveAddresses(customerDTO.getAddresses());
            customer.setAddresses(savedAddresses);

            Customer savedCustomer = customerRepository.save(customer);
            return convertToCustomerDTO(savedCustomer, userDTO.getProfile().getId());

        } catch (Exception e) {
            if (createdUser != null) {
                try {
                    userClient.deleteUser(createdUser.getId());
                } catch (Exception compensationError) {
                    throw new RuntimeException("Failed to rollback user creation: " + e.getMessage());
                }
            }
            throw new RuntimeException("Failed to create customer: " + e.getMessage());
        }
    }

    @Transactional
    public CustomerSearchDTO update(Long id, CustomerDTO customerUpdateDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        UserResponseDTO currentUserDTO = userClient.getUserById(customer.getUserId());

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
        updateUserDTO.setCpf(currentUserDTO.getCpf());
        updateUserDTO.setProfile(userClient.getUserProfileById((long) 4));

        userClient.updateUser(customer.getUserId(), updateUserDTO);

        if (customerUpdateDTO.getBirthDate() != null) {
            customer.setBirthDate(customerUpdateDTO.getBirthDate());
        }

        if (customerUpdateDTO.getAddresses() != null) {
            List<Address> updatedAddresses = new ArrayList<>();

            List<Long> updatedAddressIds = customerUpdateDTO.getAddresses().stream()
                    .filter(addr -> addr.getId() != null)
                    .map(Address::getId)
                    .collect(Collectors.toList());

            customer.getAddresses().stream()
                    .filter(addr -> !updatedAddressIds.contains(addr.getId()))
                    .forEach(updatedAddresses::add);

            for (Address addressDTO : customerUpdateDTO.getAddresses()) {
                if (addressDTO.getId() != null) {
                    Address existingAddress = addressRepository.findById(addressDTO.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Address not found with id: " + addressDTO.getId()));

                    existingAddress.setZipCode(addressDTO.getZipCode());
                    existingAddress.setStreet(addressDTO.getStreet());
                    existingAddress.setNumber(addressDTO.getNumber());
                    existingAddress.setNeighborhood(addressDTO.getNeighborhood());
                    existingAddress.setType(addressDTO.getType());
                    existingAddress.setCity(addressDTO.getCity());
                    existingAddress.setState(addressDTO.getState());
                    existingAddress.setCountry(addressDTO.getCountry());
                    existingAddress.setComplement(addressDTO.getComplement());

                    updatedAddresses.add(existingAddress);
                } else {
                    Address newAddress = new Address();
                    newAddress.setZipCode(addressDTO.getZipCode());
                    newAddress.setStreet(addressDTO.getStreet());
                    newAddress.setNumber(addressDTO.getNumber());
                    newAddress.setNeighborhood(addressDTO.getNeighborhood());
                    newAddress.setType(addressDTO.getType());
                    newAddress.setCity(addressDTO.getCity());
                    newAddress.setState(addressDTO.getState());
                    newAddress.setCountry(addressDTO.getCountry());
                    newAddress.setComplement(addressDTO.getComplement());

                    updatedAddresses.add(newAddress);
                }
            }

            customer.getAddresses().clear();
            customer.getAddresses().addAll(updatedAddresses);
        }

        Customer savedCustomer = customerRepository.save(customer);
        return findById(savedCustomer.getId());
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        UserResponseDTO currentUserDTO = userClient.getUserById(customer.getUserId());

        customerRepository.deleteById(customerId);

        userClient.deleteUser(currentUserDTO.getId());
    }

    @Transactional(readOnly = true)
    public List<Address> getAddresses(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        return customer.getAddresses();
    }

    @Transactional(readOnly = true)
    public Address getAddressById(Long customerId, Long addressId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        return customer.getAddresses().stream()
                .filter(address -> address.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
    }

    @Transactional
    public Address addAddress(Long customerId, Address address) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        customer.getAddresses().add(address);
        Customer updatedCustomer = customerRepository.save(customer);
        return updatedCustomer.getAddresses().get(updatedCustomer.getAddresses().size() - 1);
    }

    @Transactional
    public Address updateAddress(Long customerId, Long addressId, AddressUpdateDTO newAddress) {
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

        return address;
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

        addressRepository.deleteById(addressId);

        customerRepository.save(customer);
    }

    private CustomerSearchDTO convertToCustomerSearchDTO(Customer customer) {
        try {
            CustomerSearchDTO customerDTO = modelMapper.map(customer, CustomerSearchDTO.class);
            UserResponseDTO userDTO = userClient.getUserById(customer.getUserId());
            List<Address> addresses = customer.getAddresses();
            List<Long> addressesId = new ArrayList<>();
            for (Address address : addresses) {
                addressesId.add(address.getId());
            }

            if (userDTO == null) {
                throw new ResourceNotFoundException("User not found with id: " + customer.getUserId());
            }

            customerDTO.setName(userDTO.getName());
            customerDTO.setLastName(userDTO.getLastName());
            customerDTO.setEmail(userDTO.getEmail());
            customerDTO.setCpf(userDTO.getCpf());
            customerDTO.setPhone(userDTO.getPhone());
            customerDTO.setStatus(userDTO.getStatus());
            customerDTO.setAddressesId(addressesId);
            customerDTO.setProfile(userDTO.getIdProfile());
            customerDTO.setPassword(userDTO.getPassword());
            customerDTO.setCreationDate(userDTO.getCreationDate());
            customerDTO.setUpdateDate(userDTO.getUpdateDate());
            customerDTO.setLastLoginDate(userDTO.getLastLoginDate());
            customerDTO.setPasswordUpdateDate(userDTO.getPasswordUpdateDate());

            return customerDTO;
        } catch (FeignException e) {
            throw new ResourceNotFoundException("Error fetching user with id: " + customer.getUserId());
        }
    }

    private CustomerDTO convertToCustomerDTO(Customer customer, Long idProfile) {
        try {
            CustomerDTO customerDTO = modelMapper.map(customer, CustomerDTO.class);
            UserResponseDTO userDTO = userClient.getUserById(customer.getUserId());

            if (userDTO == null) {
                throw new ResourceNotFoundException("User not found with id: " + customer.getUserId());
            }

            customerDTO.setName(userDTO.getName());
            customerDTO.setLastName(userDTO.getLastName());
            customerDTO.setEmail(userDTO.getEmail());
            customerDTO.setCpf(userDTO.getCpf());
            customerDTO.setPhone(userDTO.getPhone());
            customerDTO.setStatus(userDTO.getStatus());
            customerDTO.setProfile(idProfile);
            customerDTO.setPassword(userDTO.getPassword());
            customerDTO.setUpdateDate(userDTO.getUpdateDate());
            customerDTO.setPasswordUpdateDate(userDTO.getPasswordUpdateDate());
            customerDTO.setAddresses(customer.getAddresses());

            return customerDTO;
        } catch (FeignException e) {
            throw new ResourceNotFoundException("Error fetching user with id: " + customer.getUserId());
        }
    }

    private void validateCustomerData(CustomerDTO customerDTO) {
        Set<ConstraintViolation<CustomerDTO>> violations = validator.validate(customerDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        validateAddresses(customerDTO.getAddresses());
    }

    private List<Address> saveAddresses(List<Address> addresses) {
        Set<Address> uniqueAddresses = new HashSet<>(addresses);
        if (uniqueAddresses.size() < addresses.size()) {
            throw new DuplicateAddressException("Duplicate addresses are not allowed");
        }

        return addresses.stream()
                .map(addressRepository::save)
                .collect(Collectors.toList());
    }

    private void validateAddresses(List<Address> addresses) {
        for (Address address : addresses) {
            Set<ConstraintViolation<Address>> violationAddress = validator.validate(address);
            if (!violationAddress.isEmpty()) {
                throw new ConstraintViolationException(violationAddress);
            }
        }

        Set<Address> uniqueAddresses = new HashSet<>(addresses);
        if (uniqueAddresses.size() < addresses.size()) {
            throw new DuplicateAddressException("Duplicate addresses are not allowed");
        }
    }

    private UserDTO prepareUserDTO(CustomerDTO customerDTO) {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(customerDTO.getName());
        userDTO.setLastName(customerDTO.getLastName());
        userDTO.setEmail(customerDTO.getEmail());
        userDTO.setCpf(customerDTO.getCpf());
        userDTO.setPhone(customerDTO.getPhone());
        userDTO.setPassword(customerDTO.getPassword());
        userDTO.setStatus(customerDTO.getStatus());
        userDTO.setProfile(userClient.getUserProfileById((long) 4));

        return userDTO;
    }

}
