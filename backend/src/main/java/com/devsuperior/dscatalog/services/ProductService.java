package com.devsuperior.dscatalog.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {

	@Autowired
	private ProductRepository repository;

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
		Page<Product> list = repository.findAll(pageRequest);
		return list.map(entity -> new ProductDTO(entity, entity.getCategories()));
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> optProduct = repository.findById(id);
		Product category = optProduct.orElseThrow(() -> new EntityNotFoundException("Entity not found"));
		return new ProductDTO(category, category.getCategories());

	}

	@Transactional
	public ProductDTO insert(ProductDTO categoryDTO) {
		Product category = new Product();
//		category.setName(categoryDTO.getName());
		category = repository.save(category);
		return new ProductDTO(category);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO categoryDTO) {
		try {
			Product entity = repository.getOne(id);
//			entity.setName(categoryDTO.getName());
			entity = repository.save(entity);
			return new ProductDTO(entity);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException emptyException) {
			throw new ResourceNotFoundException("Id not found : " + id);
		} catch (DataIntegrityViolationException dataIntegrity) {
			throw new DatabaseException("Integrity violation");
		}
	}

}
