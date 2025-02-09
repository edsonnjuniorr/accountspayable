package com.totvs.accounts.domain.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.totvs.accounts.domain.entity.AccountsPayable;

@Repository
public interface AccountsPayableRepository extends JpaRepository<AccountsPayable, Long> {

    Page<AccountsPayable> findAccountsPayableByDueDate(LocalDate dueDate, Pageable pageable);

    Page<AccountsPayable> findAccountsPayableByDescriptionContaining(String description, Pageable pageable);

    Page<AccountsPayable> findAccountsPayableByDueDateAndDescriptionContaining(LocalDate dueDate, String description, Pageable pageable);

    List<AccountsPayable> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
}