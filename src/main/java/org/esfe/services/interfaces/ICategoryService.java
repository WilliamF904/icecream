package org.esfe.services.interfaces;

import org.esfe.models.Category;
import org.esfe.models.Rol;
import org.springframework.data.domain.Sort;


import java.util.List;
import java.util.Optional;

public interface ICategoryService {
    List<Category> findAll();

    List<Category> findAll(Sort sort);

    List<Category> getAll();

    Optional<Category> findOneById(Integer categoryId);

    Category createOrEditOne(Category category);

    void deleteOneById(Integer categoryId);
}
