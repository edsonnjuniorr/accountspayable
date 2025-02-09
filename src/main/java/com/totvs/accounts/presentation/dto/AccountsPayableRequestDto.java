package com.totvs.accounts.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AccountsPayableRequestDto {

    @NotNull(message = "O valor do pagamento é obrigatório.")
    @DecimalMin(value = "0.0", inclusive = false, message = "O valor do pagamento deve ser maior que zero.")
    private BigDecimal amount;

    @NotBlank(message = "A descrição é obrigatória.")
    private String description;

    @NotNull(message = "A data de vencimento é obrigatória.")
    private LocalDate dueDate;

    @NotBlank(message = "O status é obrigatório.")
    private String status;

    private LocalDate paymentDate;

}
