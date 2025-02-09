package com.totvs.accounts.application.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.totvs.accounts.domain.entity.AccountsPayable;

public class CsvParserServiceTest {

	private final CsvParserService service = new CsvParserService();

	@Test
	public void testParseCsv_NullFile() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.parseCsv(null));
		assertEquals("O arquivo está vazio.", exception.getMessage());
	}

	@Test
	public void testParseCsv_EmptyFile() {
		MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.parseCsv(file));
		assertEquals("O arquivo está vazio.", exception.getMessage());
	}

	@Test
	public void testParseCsv_InvalidFormat() {
		MultipartFile file = new MockMultipartFile("file", "test.json", "application/json",
				"data".getBytes(StandardCharsets.UTF_8));
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.parseCsv(file));
		assertEquals("Formato de arquivo inválido. Por favor, envie um arquivo CSV.", exception.getMessage());
	}

	@Test
	public void testParseCsv_MissingRequiredColumns() {
		String csvContent = "amount,description,status\n" + "100,Test,PENDENTE\n";
		MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
				csvContent.getBytes(StandardCharsets.UTF_8));
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.parseCsv(file));
		assertEquals(
				"O arquivo CSV deve conter as colunas: amount, description, duedate, status (e opcionalmente paymentdate).",
				exception.getMessage());
	}

	@Test
	public void testParseCsv_ValidFileWithoutPaymentDate() {
		String csvContent = "amount,description,duedate,status\n" + "100.50,Test description,2025-01-01,PENDENTE\n";
		MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
				csvContent.getBytes(StandardCharsets.UTF_8));
		List<AccountsPayable> list = service.parseCsv(file);
		assertEquals(1, list.size());
		AccountsPayable account = list.get(0);
		assertEquals(new BigDecimal("100.50"), account.getAmount());
		assertEquals("Test description", account.getDescription());
		assertEquals(LocalDate.parse("2025-01-01"), account.getDueDate());
		assertEquals("PENDENTE", account.getStatus());
		assertNull(account.getPaymentDate());
	}

	@Test
	public void testParseCsv_ValidFileWithPaymentDate() {
		String csvContent = "amount,description,duedate,status,paymentdate\n"
				+ "200.75,Another test,2025-02-02,PAGO,2025-02-10\n";
		MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
				csvContent.getBytes(StandardCharsets.UTF_8));
		List<AccountsPayable> list = service.parseCsv(file);
		assertEquals(1, list.size());
		AccountsPayable account = list.get(0);
		assertEquals(new BigDecimal("200.75"), account.getAmount());
		assertEquals("Another test", account.getDescription());
		assertEquals(LocalDate.parse("2025-02-02"), account.getDueDate());
		assertEquals("PAGO", account.getStatus());
		assertEquals(LocalDate.parse("2025-02-10"), account.getPaymentDate());
	}

	@Test
	public void testParseCsv_RecordWithInvalidAmount() {
		String csvContent = "amount,description,duedate,status\n" + "100,Valid,2025-01-01,PENDENTE\n"
				+ "abc,Invalid,2025-01-01,PENDENTE\n";
		MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
				csvContent.getBytes(StandardCharsets.UTF_8));
		List<AccountsPayable> list = service.parseCsv(file);
		assertEquals(1, list.size());
		AccountsPayable account = list.get(0);
		assertEquals(new BigDecimal("100"), account.getAmount());
		assertEquals("Valid", account.getDescription());
	}

	@Test
	public void testParseCsv_RecordWithInvalidDate() {
		String csvContent = "amount,description,duedate,status\n" + "100,Invalid Date,invalid-date,PENDENTE\n";
		MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
				csvContent.getBytes(StandardCharsets.UTF_8));
		List<AccountsPayable> list = service.parseCsv(file);
		assertTrue(list.isEmpty());
	}

	@Test
	public void testParseCsv_IOException() {
		MultipartFile file = new MultipartFile() {
			@Override
			public String getName() {
				return "test.csv";
			}

			@Override
			public String getOriginalFilename() {
				return "test.csv";
			}

			@Override
			public String getContentType() {
				return "text/csv";
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public long getSize() {
				return 1;
			}

			@Override
			public byte[] getBytes() throws IOException {
				throw new IOException("Forced IOException");
			}

			@Override
			public InputStream getInputStream() throws IOException {
				throw new IOException("Forced IOException");
			}

			@Override
			public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
			}
		};

		RuntimeException exception = assertThrows(RuntimeException.class, () -> service.parseCsv(file));
		assertTrue(exception.getMessage().contains("Erro ao processar o arquivo:"));
	}

	@Test
	public void testIsCsvFile_ValidByContentType() {
		MultipartFile file = new MockMultipartFile("file", "test.txt", "text/csv",
				"data".getBytes(StandardCharsets.UTF_8));
		assertTrue(service.isCsvFile(file));
	}

	@Test
	public void testIsCsvFile_ValidByExtension() {
		MultipartFile file = new MockMultipartFile("file", "test.csv", "application/octet-stream",
				"data".getBytes(StandardCharsets.UTF_8));
		assertTrue(service.isCsvFile(file));
	}

	@Test
	public void testIsCsvFile_Invalid() {
		MultipartFile file = new MockMultipartFile("file", "test.txt", "application/json",
				"data".getBytes(StandardCharsets.UTF_8));
		assertFalse(service.isCsvFile(file));
	}
}
