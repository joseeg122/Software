package com.fintrack.repository;

import com.fintrack.entity.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByPersonIdOrderById(Long personId);

    List<Account> findByPersonIdAndBankId(Long personId, Long bankId);
}
