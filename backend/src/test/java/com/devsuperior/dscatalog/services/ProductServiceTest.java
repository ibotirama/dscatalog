package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository repository;

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private ProductService service;
	
	private long existingId;
	private long nonExisting;
	private long dependentId;
	private PageImpl<Product> page;
	private Product product;
	private ProductDTO productDto;

	private Category category;
	
	@BeforeEach
	void setUp() {
		existingId = 1L;
		nonExisting = 100L;
		dependentId = 4L;
		product = Factory.createProduct();
		category = Factory.createCategory();
		productDto = Factory.createProductDTO();
		page = new PageImpl<>(List.of(product));
		
		Mockito.when(repository.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(page);
		Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);
		
		Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
		Mockito.when(repository.findById(nonExisting)).thenReturn(Optional.empty());
		
		Mockito.when(repository.getOne(existingId)).thenReturn(product);
		Mockito.when(repository.getOne(nonExisting)).thenThrow(EntityNotFoundException.class);

		Mockito.when(categoryRepository.getOne(existingId)).thenReturn(category);
		Mockito.when(categoryRepository.getOne(nonExisting)).thenThrow(EntityNotFoundException.class);
		
		Mockito.doNothing().when(repository).deleteById(existingId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExisting);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
	}
	
	@Test
	void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExisting, productDto);
		});
	}
	
	@Test
	void updateShouldUpdateObjectIfTheIdExists() {
		
		productDto.setDescription("Updated description");
		ProductDTO productDtoUpdated = service.update(existingId, productDto);
		
		Assertions.assertEquals("Updated description", productDtoUpdated.getDescription());
	}
	
	@Test
	void findByIdShouldReturnProductDTOWhenIdExists() {
		ProductDTO productDTO = service.findById(existingId);
		
		Assertions.assertNotNull(productDTO);
		Assertions.assertEquals(existingId, productDTO.getId());
	}
	
	@Test
	void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExisting);
		});
		Mockito.verify(repository).findById(nonExisting);
	}
	
	@Test
	void findAllPagedShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		
		Page<ProductDTO> result = service.findAllPaged(pageable);
		
		Assertions.assertNotNull(result);
		Mockito.verify(repository).findAll(pageable);
	}
	
	@Test
	void deleteShouldDoNothingWhenIdExist() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		
		Mockito.verify(repository).deleteById(existingId);
	}

	@Test
	void deleteShouldTrowResourceNotFoundWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExisting);
		});
		
		Mockito.verify(repository).deleteById(nonExisting);
	}

	@Test
	void deleteShouldTrowDatabaseExceptionWhenThereIsRefIntegrity() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
		
		Mockito.verify(repository).deleteById(dependentId);
	}

}
