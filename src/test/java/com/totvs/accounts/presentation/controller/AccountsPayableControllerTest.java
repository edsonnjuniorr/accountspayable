package com.totvs.accounts.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.totvs.accounts.application.factory.AccountsPayableFactory;
import com.totvs.accounts.application.service.AccountPayableService;
import com.totvs.accounts.application.service.CsvParserService;
import com.totvs.accounts.domain.entity.AccountsPayable;
import com.totvs.accounts.presentation.dto.AccountsPayableRequestDto;
import com.totvs.accounts.presentation.dto.AccountsPayableTotalPaidResponseDto;

@ExtendWith(MockitoExtension.class)
public class AccountsPayableControllerTest {

	@InjectMocks
	private AccountsPayableController controller;

	@Mock
	private AccountPayableService accountPayableService;

	private <T> void assertResponse(HttpStatus expectedStatus, T expectedBody, ResponseEntity<T> response) {
		assertAll("Response assertions", () -> assertNotNull(response, "Response should not be null"),
				() -> assertEquals(expectedStatus, response.getStatusCode(), "Unexpected HTTP status"),
				() -> assertEquals(expectedBody, response.getBody(), "Unexpected response body"));
	}

	private Page<AccountsPayable> buildPage(Pageable pageable, AccountsPayable... accounts) {
		return new PageImpl<>(Arrays.asList(accounts), pageable, accounts.length);
	}

	private AccountsPayable createTestAccount() {
		return new AccountsPayable();
	}

	@ParameterizedTest(name = "dueDate={0}, description={1}, pageable={2}")
	@MethodSource("provideGetAccountsPayableParameters")
	@DisplayName("Test getAccountsPayable with various parameters")
	public void testGetAccountsPayable_VariousParameters(LocalDate dueDate, String description, Pageable pageable) {
		AccountsPayable account = createTestAccount();
		Page<AccountsPayable> page = buildPage(pageable, account);
		when(accountPayableService.getAccountsPayable(dueDate, description, pageable)).thenReturn(page);
		ResponseEntity<Page<AccountsPayable>> response = controller.getAccountsPayable(dueDate, description, pageable);
		assertResponse(HttpStatus.OK, page, response);
		verify(accountPayableService, times(1)).getAccountsPayable(dueDate, description, pageable);
	}

	private static Stream<Arguments> provideGetAccountsPayableParameters() {
		Pageable pageable1 = PageRequest.of(0, 10, Sort.unsorted());
		Pageable pageable2 = PageRequest.of(1, 5, Sort.unsorted());
		return Stream.of(Arguments.of(LocalDate.of(2025, 2, 10), "Test description", pageable1),
				Arguments.of(null, null, pageable2), Arguments.of(LocalDate.of(2025, 2, 10), null, pageable1),
				Arguments.of(null, "Only Description", pageable1));
	}

	@Test
	@DisplayName("Test getAccountPayableById with invalid id (negative)")
	public void testGetAccountPayableById_InvalidId() {
		Long invalidId = -1L;
		when(accountPayableService.getAccountsPayableById(invalidId))
				.thenThrow(new IllegalArgumentException("Invalid id"));
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.getAccountPayableById(invalidId);
		});
		assertEquals("Invalid id", exception.getMessage());
		verify(accountPayableService, times(1)).getAccountsPayableById(invalidId);
	}

	@Test
	@DisplayName("Test updateAccountsPayableStatus with invalid status (empty)")
	public void testUpdateAccountsPayableStatus_InvalidStatus() {
		Long id = 1L;
		String invalidStatus = "";
		when(accountPayableService.updateAccountsPayableStatus(id, invalidStatus))
				.thenThrow(new IllegalArgumentException("Invalid status"));
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.updateAccountsPayableStatus(id, invalidStatus);
		});
		assertEquals("Invalid status", exception.getMessage());
		verify(accountPayableService, times(1)).updateAccountsPayableStatus(id, invalidStatus);
	}

	@Test
	@DisplayName("Test createAccountsPayable with null request")
	public void testCreateAccountsPayable_NullRequest() {
		AccountsPayableRequestDto nullRequest = null;
		try (MockedStatic<AccountsPayableFactory> factoryMock = mockStatic(AccountsPayableFactory.class)) {
			factoryMock.when(() -> AccountsPayableFactory.buildAccountsPayable(nullRequest))
					.thenThrow(new IllegalArgumentException("Request cannot be null"));
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
				controller.createAccountsPayable(nullRequest);
			});
			assertEquals("Request cannot be null", exception.getMessage());
			factoryMock.verify(() -> AccountsPayableFactory.buildAccountsPayable(nullRequest), times(1));
			verifyNoInteractions(accountPayableService);
		}
	}

	@Test
	public void testGetAccountPayableById_Success() {
		Long id = 1L;
		AccountsPayable account = createTestAccount();
		when(accountPayableService.getAccountsPayableById(id)).thenReturn(account);
		ResponseEntity<AccountsPayable> response = controller.getAccountPayableById(id);
		assertAll("GetAccountPayableById Success", () -> assertNotNull(response, "Response should not be null"),
				() -> assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status should be OK"),
				() -> assertEquals(account, response.getBody(), "Returned account does not match expected account"));
		verify(accountPayableService, times(1)).getAccountsPayableById(id);
	}

	@Test
	public void testGetAccountPayableById_NullAccount() {
		Long id = 1L;
		when(accountPayableService.getAccountsPayableById(id)).thenReturn(null);
		ResponseEntity<AccountsPayable> response = controller.getAccountPayableById(id);
		assertAll("GetAccountPayableById Null Account", () -> assertNotNull(response, "Response should not be null"),
				() -> assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status should be OK"),
				() -> assertNull(response.getBody(), "Response body should be null"));
		verify(accountPayableService, times(1)).getAccountsPayableById(id);
	}

	@Test
	public void testGetAccountPayableById_ServiceThrowsException() {
		Long id = 1L;
		when(accountPayableService.getAccountsPayableById(id)).thenThrow(new RuntimeException("Service exception"));
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			controller.getAccountPayableById(id);
		}, "Expected exception was not thrown");
		assertAll("Service Exception for GetAccountPayableById",
				() -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
				() -> assertEquals("Service exception", exception.getMessage(), "Exception message does not match"));
		verify(accountPayableService, times(1)).getAccountsPayableById(id);
	}

	@Test
	public void testGetAccountsPayable_ServiceThrowsException() {
		LocalDate dueDate = LocalDate.now();
		String description = "Error case";
		Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
		when(accountPayableService.getAccountsPayable(dueDate, description, pageable))
				.thenThrow(new RuntimeException("Service exception"));
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			controller.getAccountsPayable(dueDate, description, pageable);
		}, "Expected exception was not thrown");
		assertAll("Service Exception for GetAccountsPayable",
				() -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
				() -> assertEquals("Service exception", exception.getMessage(), "Exception message does not match"));
		verify(accountPayableService, times(1)).getAccountsPayable(dueDate, description, pageable);
	}

	@Test
	public void testGetAccountsPayableTotalPaid_Success() {
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		LocalDate endDate = LocalDate.of(2025, 1, 31);
		BigDecimal totalPaid = new BigDecimal("100.00");
		when(accountPayableService.getAccountsPayableTotalPaid(startDate, endDate)).thenReturn(totalPaid);
		ResponseEntity<AccountsPayableTotalPaidResponseDto> response = controller.getAccountsPayableTotalPaid(startDate,
				endDate);
		AccountsPayableTotalPaidResponseDto dto = response.getBody();
		assertAll("GetAccountsPayableTotalPaid Success", () -> assertNotNull(response, "Response should not be null"),
				() -> assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status should be OK"),
				() -> assertNotNull(dto, "DTO should not be null"),
				() -> assertEquals(totalPaid, dto.getTotalPaid(), "Total paid does not match expected value"));
		verify(accountPayableService, times(1)).getAccountsPayableTotalPaid(startDate, endDate);
	}

	@Test
	public void testGetAccountsPayableTotalPaid_NullTotal() {
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		LocalDate endDate = LocalDate.of(2025, 1, 31);
		when(accountPayableService.getAccountsPayableTotalPaid(startDate, endDate)).thenReturn(null);
		ResponseEntity<AccountsPayableTotalPaidResponseDto> response = controller.getAccountsPayableTotalPaid(startDate,
				endDate);
		AccountsPayableTotalPaidResponseDto dto = response.getBody();
		assertAll("GetAccountsPayableTotalPaid Null Total",
				() -> assertNotNull(response, "Response should not be null"),
				() -> assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status should be OK"),
				() -> assertNotNull(dto, "DTO should not be null"),
				() -> assertNull(dto.getTotalPaid(), "Total paid should be null"));
		verify(accountPayableService, times(1)).getAccountsPayableTotalPaid(startDate, endDate);
	}

	@Test
	public void testGetAccountsPayableTotalPaid_ServiceThrowsException() {
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		LocalDate endDate = LocalDate.of(2025, 1, 31);
		when(accountPayableService.getAccountsPayableTotalPaid(startDate, endDate))
				.thenThrow(new RuntimeException("Service exception"));
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			controller.getAccountsPayableTotalPaid(startDate, endDate);
		}, "Expected exception was not thrown");
		assertAll("Service Exception for GetAccountsPayableTotalPaid",
				() -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
				() -> assertEquals("Service exception", exception.getMessage(), "Exception message does not match"));
		verify(accountPayableService, times(1)).getAccountsPayableTotalPaid(startDate, endDate);
	}

	@Test
	public void testCreateAccountsPayable_Success() {
		AccountsPayableRequestDto requestDto = new AccountsPayableRequestDto();
		AccountsPayable builtAccount = createTestAccount();
		AccountsPayable savedAccount = createTestAccount();
		try (MockedStatic<AccountsPayableFactory> factoryMock = mockStatic(AccountsPayableFactory.class)) {
			factoryMock.when(() -> AccountsPayableFactory.buildAccountsPayable(requestDto)).thenReturn(builtAccount);
			when(accountPayableService.save(builtAccount)).thenReturn(savedAccount);
			ResponseEntity<AccountsPayable> response = controller.createAccountsPayable(requestDto);
			assertAll("CreateAccountsPayable Success", () -> assertNotNull(response, "Response should not be null"),
					() -> assertEquals(HttpStatus.CREATED, response.getStatusCode(), "HTTP Status should be CREATED"),
					() -> assertEquals(savedAccount, response.getBody(),
							"Saved account does not match expected account"));
			factoryMock.verify(() -> AccountsPayableFactory.buildAccountsPayable(requestDto), times(1));
			verify(accountPayableService, times(1)).save(builtAccount);
		}
	}

	@Test
	public void testCreateAccountsPayable_ServiceThrowsException() {
		AccountsPayableRequestDto requestDto = new AccountsPayableRequestDto();
		AccountsPayable builtAccount = createTestAccount();
		try (MockedStatic<AccountsPayableFactory> factoryMock = mockStatic(AccountsPayableFactory.class)) {
			factoryMock.when(() -> AccountsPayableFactory.buildAccountsPayable(requestDto)).thenReturn(builtAccount);
			when(accountPayableService.save(builtAccount)).thenThrow(new RuntimeException("Service error"));
			RuntimeException exception = assertThrows(RuntimeException.class, () -> {
				controller.createAccountsPayable(requestDto);
			}, "Expected exception was not thrown");
			assertAll("Service Exception for CreateAccountsPayable",
					() -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
					() -> assertEquals("Service error", exception.getMessage(), "Exception message does not match"));
			factoryMock.verify(() -> AccountsPayableFactory.buildAccountsPayable(requestDto), times(1));
			verify(accountPayableService, times(1)).save(builtAccount);
		}
	}

	@Test
	public void testUpdateAccountsPayable_Success() {
		Long id = 1L;
		AccountsPayableRequestDto requestDto = new AccountsPayableRequestDto();
		AccountsPayable builtAccount = createTestAccount();
		AccountsPayable updatedAccount = createTestAccount();
		try (MockedStatic<AccountsPayableFactory> factoryMock = mockStatic(AccountsPayableFactory.class)) {
			factoryMock.when(() -> AccountsPayableFactory.buildAccountsPayable(requestDto)).thenReturn(builtAccount);
			when(accountPayableService.update(id, builtAccount)).thenReturn(updatedAccount);
			ResponseEntity<AccountsPayable> response = controller.updateAccountsPayable(id, requestDto);
			assertAll("UpdateAccountsPayable Success", () -> assertNotNull(response, "Response should not be null"),
					() -> assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status should be OK"),
					() -> assertEquals(updatedAccount, response.getBody(),
							"Updated account does not match expected account"));
			factoryMock.verify(() -> AccountsPayableFactory.buildAccountsPayable(requestDto), times(1));
			verify(accountPayableService, times(1)).update(id, builtAccount);
		}
	}

	@Test
	public void testUpdateAccountsPayable_ServiceThrowsException() {
		Long id = 1L;
		AccountsPayableRequestDto requestDto = new AccountsPayableRequestDto();
		AccountsPayable builtAccount = createTestAccount();
		try (MockedStatic<AccountsPayableFactory> factoryMock = mockStatic(AccountsPayableFactory.class)) {
			factoryMock.when(() -> AccountsPayableFactory.buildAccountsPayable(requestDto)).thenReturn(builtAccount);
			when(accountPayableService.update(id, builtAccount)).thenThrow(new RuntimeException("Update failed"));
			RuntimeException exception = assertThrows(RuntimeException.class, () -> {
				controller.updateAccountsPayable(id, requestDto);
			}, "Expected exception was not thrown");
			assertAll("Service Exception for UpdateAccountsPayable",
					() -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
					() -> assertEquals("Update failed", exception.getMessage(), "Exception message does not match"));
			factoryMock.verify(() -> AccountsPayableFactory.buildAccountsPayable(requestDto), times(1));
			verify(accountPayableService, times(1)).update(id, builtAccount);
		}
	}

	@Test
	public void testUpdateAccountsPayableStatus_Success() {
		Long id = 1L;
		String status = "PAID";
		AccountsPayable updatedAccount = createTestAccount();
		when(accountPayableService.updateAccountsPayableStatus(id, status)).thenReturn(updatedAccount);
		ResponseEntity<AccountsPayable> response = controller.updateAccountsPayableStatus(id, status);
		assertAll("UpdateAccountsPayableStatus Success", () -> assertNotNull(response, "Response should not be null"),
				() -> assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status should be OK"),
				() -> assertEquals(updatedAccount, response.getBody(),
						"Updated account does not match expected account"));
		verify(accountPayableService, times(1)).updateAccountsPayableStatus(id, status);
	}

	@Test
	public void testUpdateAccountsPayableStatus_ServiceThrowsException() {
		Long id = 1L;
		String status = "PAID";
		when(accountPayableService.updateAccountsPayableStatus(id, status))
				.thenThrow(new RuntimeException("Status update failed"));
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			controller.updateAccountsPayableStatus(id, status);
		}, "Expected exception was not thrown");
		assertAll("Service Exception for UpdateAccountsPayableStatus",
				() -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
				() -> assertEquals("Status update failed", exception.getMessage(), "Exception message does not match"));
		verify(accountPayableService, times(1)).updateAccountsPayableStatus(id, status);
	}

	@Test
	public void testUploadCsv_Success() {
		MultipartFile file = mock(MultipartFile.class);
		List<AccountsPayable> parsedList = Collections.singletonList(createTestAccount());
		List<AccountsPayable> savedList = Collections.singletonList(createTestAccount());
		try (MockedConstruction<CsvParserService> mocked = mockConstruction(CsvParserService.class,
				(mock, context) -> when(mock.parseCsv(file)).thenReturn(parsedList))) {
			when(accountPayableService.save(parsedList)).thenReturn(savedList);
			ResponseEntity<List<AccountsPayable>> response = controller.uploadCsv(file);
			assertAll("UploadCsv Success", () -> assertNotNull(response, "Response should not be null"),
					() -> assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status should be OK"),
					() -> assertEquals(savedList, response.getBody(), "Saved list does not match expected list"));
			verify(accountPayableService, times(1)).save(parsedList);
		}
	}

	@Test
	public void testUploadCsv_CsvParserThrowsException() {
		MultipartFile file = mock(MultipartFile.class);
		try (MockedConstruction<CsvParserService> mocked = mockConstruction(CsvParserService.class,
				(mock, context) -> when(mock.parseCsv(file)).thenThrow(new RuntimeException("Parsing error")))) {
			RuntimeException exception = assertThrows(RuntimeException.class, () -> controller.uploadCsv(file),
					"Expected exception was not thrown");
			assertAll("CsvParser Exception",
					() -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
					() -> assertEquals("Parsing error", exception.getMessage(), "Exception message does not match"));
			verifyNoInteractions(accountPayableService);
		}
	}

	@Test
	public void testUploadCsv_ServiceThrowsException() {
		MultipartFile file = mock(MultipartFile.class);
		List<AccountsPayable> parsedList = Collections.singletonList(createTestAccount());
		try (MockedConstruction<CsvParserService> mocked = mockConstruction(CsvParserService.class,
				(mock, context) -> when(mock.parseCsv(file)).thenReturn(parsedList))) {
			when(accountPayableService.save(parsedList)).thenThrow(new RuntimeException("Service error"));
			RuntimeException exception = assertThrows(RuntimeException.class, () -> controller.uploadCsv(file),
					"Expected exception was not thrown");
			assertAll("Service Exception for UploadCsv",
					() -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
					() -> assertEquals("Service error", exception.getMessage(), "Exception message does not match"));
			verify(accountPayableService, times(1)).save(parsedList);
		}
	}

	@Test
	public void testUploadCsv_EmptyFile() {
		MultipartFile file = mock(MultipartFile.class);
		List<AccountsPayable> parsedList = Collections.emptyList();
		List<AccountsPayable> savedList = Collections.emptyList();
		try (MockedConstruction<CsvParserService> mocked = mockConstruction(CsvParserService.class,
				(mock, context) -> when(mock.parseCsv(file)).thenReturn(parsedList))) {
			when(accountPayableService.save(parsedList)).thenReturn(savedList);
			ResponseEntity<List<AccountsPayable>> response = controller.uploadCsv(file);
			assertAll("UploadCsv with Empty File", () -> assertNotNull(response, "Response should not be null"),
					() -> assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status should be OK"),
					() -> assertEquals(savedList, response.getBody(), "Response body should be an empty list"));
			verify(accountPayableService, times(1)).save(parsedList);
		}
	}
}