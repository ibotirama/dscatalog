package com.devsuperior.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.tests.Factory;

@DataJpaTest
class ProductRepositoryTest {
	
	@Autowired
	private ProductRepository repository;
	private long countTotalRecords;
	private long existingId;
	private long nonExistingId;
	
	@BeforeEach
	void setUp() {
		countTotalRecords = 25L;
		existingId = 1L;
		nonExistingId = 1000L;
	}
	

	@Test
	void deleteShouldDeleteObjectWhenIdExists() {
		repository.deleteById(existingId);
		
		Optional<Product> result = repository.findById(existingId);
		
		Assertions.assertFalse(result.isPresent());
	}
	
	@Test
	public void deleteShouldThrowEmptyResultNotFoundExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			repository.deleteById(nonExistingId);
		});
	}
	
	@Test
	public void saveShouldPersistWithAutoincrementWhenIdIsNull() {
		Product product = Factory.createProduct();
		product.setId(null);
		
		product = repository.save(product);
		
		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalRecords + 1L, product.getId());
	}
	
	@Test
	public void findByIdShouldReturnEmptyOptionalWhenIdDoesNotExists() {
		Optional<Product> optProduct = repository.findById(nonExistingId);
		
		Assertions.assertTrue(optProduct.isEmpty());
	}
	
	@Test
	public void findByIdShouldReturnNotEmptyOptionalWhenIdExists() {
		Optional<Product> optProduct = repository.findById(existingId);
		
		Assertions.assertTrue(optProduct.isPresent());
	}

}
