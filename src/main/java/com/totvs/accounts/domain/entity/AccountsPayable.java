package com.totvs.accounts.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsPayable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "valor", nullable = false)
    private BigDecimal amount;

    @Column(name = "descricao", nullable = false, length = 255)
    private String description;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dueDate;

    @Column(name = "data_pagamento")
    private LocalDate paymentDate;

    @Column(name = "situacao", nullable = false, length = 50)
    private String status;

    @Builder
    public AccountsPayable(BigDecimal amount, String description, LocalDate dueDate, LocalDate paymentDate,
            String status) {
        this.amount = amount;
        this.description = description;
        this.dueDate = dueDate;
        this.paymentDate = paymentDate;
        this.status = status;
    }
}