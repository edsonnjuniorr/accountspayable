package com.totvs.accounts.presentation.controller;

import com.totvs.accounts.application.factory.AccountsPayableFactory;
import com.totvs.accounts.application.service.AccountPayableService;
import com.totvs.accounts.application.service.CsvParserService;
import com.totvs.accounts.domain.entity.AccountsPayable;
import com.totvs.accounts.presentation.dto.AccountsPayableRequestDto;
import com.totvs.accounts.presentation.dto.AccountsPayableTotalPaidResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accountspayable")
@RequiredArgsConstructor
@Slf4j
public class AccountsPayableController {

	private final AccountPayableService accountPayableService;

	@GetMapping("/{id}")
	public ResponseEntity<AccountsPayable> getAccountPayableById(@PathVariable Long id) {
		log.info("GET /accountspayable/{} chamado", id);
		AccountsPayable account = accountPayableService.getAccountsPayableById(id);
		return ResponseEntity.ok(account);
	}

	@GetMapping
	public ResponseEntity<Page<AccountsPayable>> getAccountsPayable(@RequestParam(required = false) LocalDate dueDate,
			@RequestParam(required = false) String description, Pageable pageable) {
		log.info("GET /accountspayable chamado com dueDate: {} e description: {}", dueDate, description);
		Page<AccountsPayable> accounts = accountPayableService.getAccountsPayable(dueDate, description, pageable);
		return ResponseEntity.ok(accounts);
	}

	@GetMapping("/total-paid")
	public ResponseEntity<AccountsPayableTotalPaidResponseDto> getAccountsPayableTotalPaid(
			@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
		log.info("GET /accountspayable/total-paid chamado com startDate: {} e endDate: {}", startDate, endDate);
		BigDecimal totalPaid = accountPayableService.getAccountsPayableTotalPaid(startDate, endDate);
		AccountsPayableTotalPaidResponseDto responseDto = new AccountsPayableTotalPaidResponseDto(totalPaid);
		return ResponseEntity.ok(responseDto);
	}

	@PostMapping
	public ResponseEntity<AccountsPayable> createAccountsPayable(
			@RequestBody @Valid AccountsPayableRequestDto accountsPayableRequestDto) {
		log.info("POST /accountspayable chamado com payload: {}", accountsPayableRequestDto);
		AccountsPayable accountsPayable = AccountsPayableFactory.buildAccountsPayable(accountsPayableRequestDto);
		AccountsPayable savedAccount = accountPayableService.save(accountsPayable);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
	}

	@PutMapping("/{id}")
	public ResponseEntity<AccountsPayable> updateAccountsPayable(@PathVariable Long id,
			@RequestBody @Valid AccountsPayableRequestDto accountsPayableRequestDto) {
		log.info("PUT /accountspayable/{} chamado com payload: {}", id, accountsPayableRequestDto);
		AccountsPayable accountsPayable = AccountsPayableFactory.buildAccountsPayable(accountsPayableRequestDto);
		AccountsPayable updatedAccount = accountPayableService.update(id, accountsPayable);
		return ResponseEntity.ok(updatedAccount);
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<AccountsPayable> updateAccountsPayableStatus(@PathVariable Long id,
			@RequestParam String status) {
		log.info("PATCH /accountspayable/{}/status chamado com status: {}", id, status);
		AccountsPayable updatedAccount = accountPayableService.updateAccountsPayableStatus(id, status);
		return ResponseEntity.ok(updatedAccount);
	}

	@PostMapping("/upload")
	public ResponseEntity<List<AccountsPayable>> uploadCsv(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(accountPayableService.save(new CsvParserService().parseCsv(file)));
	}

}
