package com.colak.springtutorial.employee.controller;


import com.colak.springtutorial.config.CacheConfiguration;
import com.colak.springtutorial.employee.dto.EmployeeDTO;
import com.colak.springtutorial.employee.jpa.Employee;
import com.colak.springtutorial.employee.mapstruct.EmployeeMapper;
import com.colak.springtutorial.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/employee")

@CacheDefaults(cacheName = "employees")
public class EmployeeController {

    private final EmployeeService employeeService;


    @PostMapping("/update/{id}")
    @CachePut
    public EmployeeDTO update(@PathVariable @CacheKey Long id, @RequestBody @CacheValue EmployeeDTO employeeDTO) {
        log.info("update is called with : {}", employeeDTO);

        // saving employee into db
        Employee employee = EmployeeMapper.INSTANCE.dtoToEmployee(employeeDTO);

        employeeService.save(employee);

        return EmployeeMapper.INSTANCE.employeeToDto(employee);
    }

    // http://localhost:8080/api/employee/findById/1
    @GetMapping(path = "/findById/{id}")
    @CacheResult
    public EmployeeDTO findById(@PathVariable @CacheKey Long id) {
        log.info("findById is called with : {}", id);
        return employeeService.findById(id)
                .map(EmployeeMapper.INSTANCE::employeeToDto)
                // throws NoSuchElementException
                .orElseThrow();
    }

    // http://localhost:8080/api/employee/evictFromCache/1
    // Only evict from cache. Do not delete from database
    @GetMapping(path = "/evictFromCache/{id}")
    @CacheRemove(afterInvocation = false)
    public void evictFromCache(@PathVariable @CacheKey Long id) {
        log.warn("Evicted key: {} from cache", id);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException exception) {
        // Return 404
        return ResponseEntity.notFound().build();
    }
}
