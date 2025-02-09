package com.totvs.accounts.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import com.totvs.accounts.domain.entity.AccountsPayable;
import com.totvs.accounts.domain.repository.AccountsPayableRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountPayableService {

    private final AccountsPayableRepository accountsPayableRepository;

    public AccountsPayable save(AccountsPayable accountsPayable) {
        validateAccountsPayable(accountsPayable);
        return accountsPayableRepository.save(accountsPayable);
    }

    public List<AccountsPayable> save(List<AccountsPayable> accountsPayable) {
        //validateAccountsPayable(accountsPayable);
        return accountsPayableRepository.saveAll(accountsPayable);
    }

    public AccountsPayable update(Long id, AccountsPayable updatedAccountsPayable) {
        AccountsPayable existingAccountsPayable = accountsPayableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        validateAccountsPayable(updatedAccountsPayable);
        existingAccountsPayable.setDueDate(updatedAccountsPayable.getDueDate());
        existingAccountsPayable.setPaymentDate(updatedAccountsPayable.getPaymentDate());
        existingAccountsPayable.setAmount(updatedAccountsPayable.getAmount());
        existingAccountsPayable.setDescription(updatedAccountsPayable.getDescription());
        existingAccountsPayable.setStatus(updatedAccountsPayable.getStatus());

        return accountsPayableRepository.save(existingAccountsPayable);
    }

    public AccountsPayable updateAccountsPayableStatus(Long id, String status) {
        AccountsPayable existingAccountsPayable = accountsPayableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        existingAccountsPayable.setStatus(status);
        return accountsPayableRepository.save(existingAccountsPayable);
    }

    public List<AccountsPayable> getAccountsPayable(LocalDate dueDate, String description) {
        if (dueDate != null && description != null) {
            return accountsPayableRepository.findAccountsPayableByDueDateAndDescriptionContaining(dueDate, description);
        } else if (dueDate != null) {
            return accountsPayableRepository.findAccountsPayableByDueDate(dueDate);
        } else if (description != null) {
            return accountsPayableRepository.findAccountsPayableByDescriptionContaining(description);
        }
        return accountsPayableRepository.findAll();
    }

    public AccountsPayable getAccountsPayableById(Long id) throws EntityNotFoundException {
        return accountsPayableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta de id (" + id + ") não foi encontrada!"));
    }

    public BigDecimal getAccountsPayableTotalPaid(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data inicial não pode ser maior que a data final");
        }
        List<AccountsPayable> filteredAccountsPayables = accountsPayableRepository.findByDueDateBetween(startDate,
                endDate);
        return filteredAccountsPayables.stream().map(AccountsPayable::getAmount).reduce(BigDecimal.ZERO,
                BigDecimal::add);
    }

    private void validateAccountsPayable(AccountsPayable accountsPayable) {
        if (accountsPayable.getAmount() == null || accountsPayable.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da conta deve ser maior que zero");
        }
        if (accountsPayable.getDescription() == null || accountsPayable.getDescription().isBlank()) {
            throw new IllegalArgumentException("A descrição da conta não pode estar vazia");
        }
    }

}
