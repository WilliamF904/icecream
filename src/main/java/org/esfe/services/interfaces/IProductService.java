package org.esfe.services.interfaces;

import org.esfe.models.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IProductService {
    Page<Product> findAll(Pageable pageable);

    List<Product> getAll();

    List<Product> findAll();

    Optional<Product> findOneById(Integer productId);

    Product createOrEditOne(Product product);

    void deleteOneById(Integer productId);

    Page<Product> searchProducts(String description, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer status, Pageable pageable);

    byte[] generatePdfReport(Integer productId);
    byte[] generateAllProductsPdfReport();
    byte[] generateProductsPdfReportByStatus(Integer status);
}
