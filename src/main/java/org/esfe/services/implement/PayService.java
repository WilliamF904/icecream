package org.esfe.services.implement;

import jakarta.persistence.criteria.Predicate;
import org.esfe.models.Pay;

import org.esfe.repository.PayRepository;

import org.esfe.services.interfaces.IPayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PayService implements IPayService {
    @Autowired
        private PayRepository payRepository;

    @Override
    public Page<Pay> findAll(Pageable pageable) {
        return payRepository.findAll(pageable);
    }


    @Override
    public List<Pay> getAll() {
        return payRepository.findAll();
    }

    @Override
    public Optional<Pay> findOneById(Integer payId) {
        return payRepository.findById(payId);
    }

    @Override
    public Pay createOrEditOne(Pay pay) {
        return payRepository.save(pay);
    }

    @Override
    public void deleteOneById(Integer payId) {
        payRepository.deleteById(payId);
    }

    @Override
    public boolean existsByUserId(Integer userId) {
        return false;
    }


    public Page<Pay> searchPays(String nameclient, Date startDate, Date endDate, Pageable pageable) {
        return payRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nameclient != null && !nameclient.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("nameclient"), "%" + nameclient + "%"));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("paydate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("paydate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

}
