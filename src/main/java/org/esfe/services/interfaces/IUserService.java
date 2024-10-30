package org.esfe.services.interfaces;

import org.esfe.models.Product;
import org.esfe.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IUserService {
    Page<User> findAll(Pageable pageable);
    User findById(Integer id);

    List<User> getAll();

    List<User> findAll();

    Optional<User> findOneById(Integer userId);

    User createOrEditOne(User user);

    void deleteOneById(Integer userId);

    List<User> getUsersByStatus(int status);

    Page<User> searchUsers(String name, String lastname,Integer rolId, Integer status, Pageable pageable);
}
