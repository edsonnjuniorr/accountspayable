package com.totvs.accounts.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.totvs.accounts.domain.entity.AccountsPayable;
import com.totvs.accounts.domain.repository.AccountsPayableRepository;

@ExtendWith(MockitoExtension.class)
public class AccountPayableServiceTest {

	@Mock
	private AccountsPayableRepository accountsPayableRepository;

	@InjectMocks
	private AccountPayableService accountPayableService;

	private AccountsPayable validAccount() {
		AccountsPayable account = new AccountsPayable();
		account.setDueDate(LocalDate.now().plusDays(10));
		account.setPaymentDate(LocalDate.now());
		account.setAmount(BigDecimal.valueOf(100));
		account.setDescription("Conta de Teste");
		account.setStatus("PENDENTE");
		return account;
	}

	@Test
	public void testSaveValidAccount() {
		AccountsPayable account = validAccount();
		when(accountsPayableRepository.save(account)).thenReturn(account);
		AccountsPayable result = accountPayableService.save(account);
		assertEquals(account.getAmount(), result.getAmount());
		verify(accountsPayableRepository, times(1)).save(account);
	}

	@Test
	public void testSaveAccountInvalidAmount() {
		AccountsPayable account = validAccount();
		account.setAmount(BigDecimal.ZERO);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> accountPayableService.save(account));
		assertEquals("O valor da conta deve ser maior que zero", exception.getMessage());
		verify(accountsPayableRepository, never()).save(any());
	}

	@Test
	public void testSaveAccountInvalidDescription() {
		AccountsPayable account = validAccount();
		account.setDescription("  ");
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> accountPayableService.save(account));
		assertEquals("A descrição da conta não pode estar vazia", exception.getMessage());
		verify(accountsPayableRepository, never()).save(any());
	}

	@Test
	public void testSaveListValidAccounts() {
		AccountsPayable account1 = validAccount();
		AccountsPayable account2 = validAccount();
		List<AccountsPayable> accounts = Arrays.asList(account1, account2);
		when(accountsPayableRepository.saveAll(accounts)).thenReturn(accounts);
		List<AccountsPayable> result = accountPayableService.save(accounts);
		assertEquals(2, result.size());
		verify(accountsPayableRepository, times(1)).saveAll(accounts);
	}

	@Test
	public void testSaveListWithInvalidAccount() {
		AccountsPayable account1 = validAccount();
		AccountsPayable account2 = validAccount();
		account2.setAmount(BigDecimal.valueOf(-50));
		List<AccountsPayable> accounts = Arrays.asList(account1, account2);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> accountPayableService.save(accounts));
		assertEquals("O valor da conta deve ser maior que zero", exception.getMessage());
		verify(accountsPayableRepository, never()).saveAll(any());
	}

	@Test
	public void testUpdateValidAccount() {
		Long id = 1L;
		AccountsPayable existingAccount = validAccount();
		existingAccount.setDescription("Original");
		when(accountsPayableRepository.findById(id)).thenReturn(Optional.of(existingAccount));
		AccountsPayable updatedAccount = validAccount();
		updatedAccount.setDueDate(LocalDate.now().plusDays(20));
		updatedAccount.setPaymentDate(LocalDate.now().plusDays(1));
		updatedAccount.setAmount(BigDecimal.valueOf(200));
		updatedAccount.setDescription("Atualizado");
		updatedAccount.setStatus("PAGO");
		when(accountsPayableRepository.save(any(AccountsPayable.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		AccountsPayable result = accountPayableService.update(id, updatedAccount);
		assertEquals(updatedAccount.getDueDate(), result.getDueDate());
		assertEquals(updatedAccount.getPaymentDate(), result.getPaymentDate());
		assertEquals(updatedAccount.getAmount(), result.getAmount());
		assertEquals(updatedAccount.getDescription(), result.getDescription());
		assertEquals(updatedAccount.getStatus(), result.getStatus());
		verify(accountsPayableRepository, times(1)).findById(id);
		verify(accountsPayableRepository, times(1)).save(existingAccount);
	}

	@Test
	public void testUpdateAccountNotFound() {
		Long id = 1L;
		when(accountsPayableRepository.findById(id)).thenReturn(Optional.empty());
		AccountsPayable updatedAccount = validAccount();
		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
				() -> accountPayableService.update(id, updatedAccount));
		assertEquals("Conta não encontrada", exception.getMessage());
		verify(accountsPayableRepository, times(1)).findById(id);
		verify(accountsPayableRepository, never()).save(any());
	}

	@Test
	public void testUpdateAccountInvalidUpdatedAccount() {
		Long id = 1L;
		AccountsPayable existingAccount = validAccount();
		when(accountsPayableRepository.findById(id)).thenReturn(Optional.of(existingAccount));
		AccountsPayable updatedAccount = validAccount();
		updatedAccount.setAmount(BigDecimal.ZERO);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> accountPayableService.update(id, updatedAccount));
		assertEquals("O valor da conta deve ser maior que zero", exception.getMessage());
		verify(accountsPayableRepository, times(1)).findById(id);
		verify(accountsPayableRepository, never()).save(any());
	}

	@Test
	public void testUpdateAccountsPayableStatusValid() {
		Long id = 1L;
		AccountsPayable existingAccount = validAccount();
		existingAccount.setStatus("PENDENTE");
		when(accountsPayableRepository.findById(id)).thenReturn(Optional.of(existingAccount));
		when(accountsPayableRepository.save(any(AccountsPayable.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		String newStatus = "PAGO";
		AccountsPayable result = accountPayableService.updateAccountsPayableStatus(id, newStatus);
		assertEquals(newStatus, result.getStatus());
		verify(accountsPayableRepository, times(1)).findById(id);
		verify(accountsPayableRepository, times(1)).save(existingAccount);
	}

	@Test
	public void testUpdateAccountsPayableStatusNotFound() {
		Long id = 1L;
		when(accountsPayableRepository.findById(id)).thenReturn(Optional.empty());
		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
				() -> accountPayableService.updateAccountsPayableStatus(id, "PAGO"));
		assertEquals("Conta não encontrada", exception.getMessage());
		verify(accountsPayableRepository, times(1)).findById(id);
		verify(accountsPayableRepository, never()).save(any());
	}

	@Test
	public void testGetAccountsPayableByDueDateAndDescription() {
		LocalDate dueDate = LocalDate.now();
		String description = "Conta";
		Pageable pageable = PageRequest.of(0, 10);
		AccountsPayable account = validAccount();
		Page<AccountsPayable> page = new PageImpl<>(Collections.singletonList(account));
		when(accountsPayableRepository.findAccountsPayableByDueDateAndDescriptionContaining(dueDate, description,
				pageable)).thenReturn(page);
		Page<AccountsPayable> result = accountPayableService.getAccountsPayable(dueDate, description, pageable);
		assertEquals(1, result.getTotalElements());
		verify(accountsPayableRepository, times(1)).findAccountsPayableByDueDateAndDescriptionContaining(dueDate,
				description, pageable);
	}

	@Test
	public void testGetAccountsPayableByDueDateOnly() {
		LocalDate dueDate = LocalDate.now();
		Pageable pageable = PageRequest.of(0, 10);
		AccountsPayable account = validAccount();
		Page<AccountsPayable> page = new PageImpl<>(Collections.singletonList(account));
		when(accountsPayableRepository.findAccountsPayableByDueDate(dueDate, pageable)).thenReturn(page);
		Page<AccountsPayable> result = accountPayableService.getAccountsPayable(dueDate, null, pageable);
		assertEquals(1, result.getTotalElements());
		verify(accountsPayableRepository, times(1)).findAccountsPayableByDueDate(dueDate, pageable);
	}

	@Test
	public void testGetAccountsPayableByDescriptionOnly() {
		String description = "Conta";
		Pageable pageable = PageRequest.of(0, 10);
		AccountsPayable account = validAccount();
		Page<AccountsPayable> page = new PageImpl<>(Collections.singletonList(account));
		when(accountsPayableRepository.findAccountsPayableByDescriptionContaining(description, pageable))
				.thenReturn(page);
		Page<AccountsPayable> result = accountPayableService.getAccountsPayable(null, description, pageable);
		assertEquals(1, result.getTotalElements());
		verify(accountsPayableRepository, times(1)).findAccountsPayableByDescriptionContaining(description, pageable);
	}

	@Test
	public void testGetAccountsPayableWithoutFilters() {
		Pageable pageable = PageRequest.of(0, 10);
		AccountsPayable account = validAccount();
		Page<AccountsPayable> page = new PageImpl<>(Collections.singletonList(account));
		when(accountsPayableRepository.findAll(pageable)).thenReturn(page);
		Page<AccountsPayable> result = accountPayableService.getAccountsPayable(null, null, pageable);
		assertEquals(1, result.getTotalElements());
		verify(accountsPayableRepository, times(1)).findAll(pageable);
	}

	@Test
	public void testGetAccountsPayableByIdValid() {
		Long id = 1L;
		AccountsPayable account = validAccount();
		when(accountsPayableRepository.findById(id)).thenReturn(Optional.of(account));
		AccountsPayable result = accountPayableService.getAccountsPayableById(id);
		assertEquals(account.getDescription(), result.getDescription());
		verify(accountsPayableRepository, times(1)).findById(id);
	}

	@Test
	public void testGetAccountsPayableByIdNotFound() {
		Long id = 1L;
		when(accountsPayableRepository.findById(id)).thenReturn(Optional.empty());
		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
				() -> accountPayableService.getAccountsPayableById(id));
		assertEquals("Conta de id (1) não foi encontrada!", exception.getMessage());
		verify(accountsPayableRepository, times(1)).findById(id);
	}

	@Test
	public void testGetAccountsPayableTotalPaidValid() {
		LocalDate startDate = LocalDate.now().minusDays(5);
		LocalDate endDate = LocalDate.now().plusDays(5);
		AccountsPayable account1 = validAccount();
		account1.setAmount(BigDecimal.valueOf(100));
		AccountsPayable account2 = validAccount();
		account2.setAmount(BigDecimal.valueOf(200));
		List<AccountsPayable> accounts = Arrays.asList(account1, account2);
		when(accountsPayableRepository.findByDueDateBetween(startDate, endDate)).thenReturn(accounts);
		BigDecimal total = accountPayableService.getAccountsPayableTotalPaid(startDate, endDate);
		assertEquals(BigDecimal.valueOf(300), total);
		verify(accountsPayableRepository, times(1)).findByDueDateBetween(startDate, endDate);
	}

	@Test
	public void testGetAccountsPayableTotalPaidNoAccounts() {
		LocalDate startDate = LocalDate.now().minusDays(5);
		LocalDate endDate = LocalDate.now().plusDays(5);
		when(accountsPayableRepository.findByDueDateBetween(startDate, endDate)).thenReturn(Collections.emptyList());
		BigDecimal total = accountPayableService.getAccountsPayableTotalPaid(startDate, endDate);
		assertEquals(BigDecimal.ZERO, total);
		verify(accountsPayableRepository, times(1)).findByDueDateBetween(startDate, endDate);
	}

	@Test
	public void testGetAccountsPayableTotalPaidInvalidDates() {
		LocalDate startDate = LocalDate.now().plusDays(5);
		LocalDate endDate = LocalDate.now();
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> accountPayableService.getAccountsPayableTotalPaid(startDate, endDate));
		assertEquals("Data inicial não pode ser maior que a data final", exception.getMessage());
		verify(accountsPayableRepository, never()).findByDueDateBetween(any(), any());
	}
}