package org.esfe.repository;

import org.esfe.models.Pay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PayRepository extends JpaRepository<Pay, Integer>, JpaSpecificationExecutor<Pay> {

}
