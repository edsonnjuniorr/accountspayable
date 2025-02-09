package com.totvs.accounts.application.factory;

import com.totvs.accounts.domain.entity.AccountsPayable;
import com.totvs.accounts.presentation.dto.AccountsPayableRequestDto;

public class AccountsPayableFactory {
    public static AccountsPayable buildAccountsPayable(AccountsPayableRequestDto request) {
        return AccountsPayable.builder().amount(request.getAmount()).description(request.getDescription())
                .dueDate(request.getDueDate()).paymentDate(request.getPaymentDate()).status(request.getStatus())
                .build();
    }

}
