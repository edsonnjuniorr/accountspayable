package com.totvs.accounts.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AccountsPayableTotalPaidResponseDto {
	private BigDecimal totalPaid;
}
