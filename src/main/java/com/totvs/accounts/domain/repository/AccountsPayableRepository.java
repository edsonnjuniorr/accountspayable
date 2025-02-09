package com.totvs.accounts.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.totvs.accounts.domain.entity.AccountsPayable;

@Repository
public interface AccountsPayableRepository extends JpaRepository<AccountsPayable, Long> {

    List<AccountsPayable> findAccountsPayableByDueDate(LocalDate dueDate);

    List<AccountsPayable> findAccountsPayableByDescriptionContaining(String description);

    List<AccountsPayable> findAccountsPayableByDueDateAndDescriptionContaining(LocalDate dueDate, String description);

    List<AccountsPayable> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
}