package com.totvs.accounts.application.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import com.totvs.accounts.domain.entity.AccountsPayable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvParserService {

	private static final String DATE_PATTERN = "yyyy-MM-dd";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
	private static final String[] REQUIRED_COLUMNS = { "amount", "description", "duedate", "status" };

	public List<AccountsPayable> parseCsv(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("O arquivo está vazio.");
		}
		if (!isCsvFile(file)) {
			throw new IllegalArgumentException("Formato de arquivo inválido. Por favor, envie um arquivo CSV.");
		}

		List<AccountsPayable> accountsList = new ArrayList<>();

		CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader().setIgnoreEmptyLines(true).setTrim(true).build();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
				CSVParser csvParser = new CSVParser(reader, csvFormat)) {

			Map<String, String> headerMapping = new HashMap<>();
			for (String header : csvParser.getHeaderMap().keySet()) {
				headerMapping.put(header.toLowerCase(), header);
			}
			for (String requiredColumn : REQUIRED_COLUMNS) {
				if (!headerMapping.containsKey(requiredColumn)) {
					throw new IllegalArgumentException(
							"O arquivo CSV deve conter as colunas: amount, description, duedate, status (e opcionalmente paymentdate).");
				}
			}
			for (CSVRecord record : csvParser) {
				try {
					AccountsPayable account = parseRecord(record, headerMapping);
					accountsList.add(account);
				} catch (Exception e) {
					log.warn("Erro ao processar a linha {}: {}", record.getRecordNumber(), e.getMessage());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Erro ao processar o arquivo: " + e.getMessage(), e);
		}
		return accountsList;
	}

	public boolean isCsvFile(MultipartFile file) {
		String contentType = file.getContentType();
		String fileName = file.getOriginalFilename();

		boolean isCsvContentType = contentType != null
				&& (contentType.equals("text/csv") || contentType.equals("application/vnd.ms-excel"));
		boolean isCsvExtension = fileName != null && fileName.toLowerCase().endsWith(".csv");

		return isCsvContentType || isCsvExtension;
	}

	private AccountsPayable parseRecord(CSVRecord record, Map<String, String> headerMapping) {
		String amountStr = record.get(headerMapping.get("amount"));
		BigDecimal amount = parseAmount(amountStr);

		String description = record.get(headerMapping.get("description"));
		String dueDateStr = record.get(headerMapping.get("duedate"));
		LocalDate dueDate = parseLocalDate(dueDateStr);

		String status = record.get(headerMapping.get("status"));

		LocalDate paymentDate = null;
		if (headerMapping.containsKey("paymentdate") && record.isMapped(headerMapping.get("paymentdate"))) {
			String paymentDateStr = record.get(headerMapping.get("paymentdate"));
			if (paymentDateStr != null && !paymentDateStr.isEmpty()) {
				paymentDate = parseLocalDate(paymentDateStr);
			}
		}
		return AccountsPayable.builder().amount(amount).description(description).dueDate(dueDate)
				.status(status).paymentDate(paymentDate).build();
	}

	private BigDecimal parseAmount(String amountStr) {
		try {
			return new BigDecimal(amountStr);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Valor de 'amount' inválido: " + amountStr, e);
		}
	}

	private LocalDate parseLocalDate(String dateStr) {
		try {
			return LocalDate.parse(dateStr, DATE_FORMATTER);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Data inválida: " + dateStr, e);
		}
	}
}
