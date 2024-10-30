package org.esfe.services.interfaces;

import org.esfe.models.Pay;
import org.esfe.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface IPayService {
    Page<Pay> findAll(Pageable pageable);

    List<Pay> getAll();

    Optional<Pay> findOneById(Integer PayId);

    Pay createOrEditOne(Pay pay);

    void deleteOneById(Integer payId);

        boolean existsByUserId(Integer userId);

    Page<Pay> searchPays(String nameclient, Date startDate, Date endDate, Pageable pageable);
}
