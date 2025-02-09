# Documentação da API de Contas a Pagar

Esta API gerencia informações sobre contas a pagar. Todas as requisições precisam incluir no cabeçalho:

```
Authorization: ApiKey 123456789
```

## Endpoints

### 1. Obter Conta a Pagar por ID

**Endpoint:**
```
GET /accountspayable/{id}
```

**Parâmetros:**
- `id` (Path Variable, Long): ID da conta a pagar.

**Resposta:**
- `200 OK`: Retorna os detalhes da conta a pagar.

**Exemplo de requisição:**
```
GET /accountspayable/1
Authorization: ApiKey 123456789
```

---

### 2. Listar Contas a Pagar (com Paginação)

**Endpoint:**
```
GET /accountspayable
```

**Parâmetros:**
- `dueDate` (Query, LocalDate) - Opcional: Filtra pelo vencimento.
- `description` (Query, String) - Opcional: Filtra pela descrição.
- `page` (Query, Integer) - Opcional: Número da página (começando em 0). Valor padrão: `0`.
- `size` (Query, Integer) - Opcional: Quantidade de registros por página. Valor padrão: `10`.
- `sort` (Query, String) - Opcional: Campo para ordenação (exemplo: `dueDate,desc`).

**Resposta:**
- `200 OK`: Retorna uma página de contas a pagar no formato paginado.

**Exemplo de requisição:**
```
GET /accountspayable?page=0&size=10&sort=dueDate,desc
Authorization: ApiKey 123456789
```

**Exemplo de resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "description": "Aluguel",
      "amount": 1500.00,
      "dueDate": "2023-12-01"
    },
    {
      "id": 2,
      "description": "Energia",
      "amount": 500.00,
      "dueDate": "2023-12-10"
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0
}
```

---

### 3. Obter Total Pago entre Datas

**Endpoint:**
```
GET /accountspayable/total-paid
```

**Parâmetros:**
- `startDate` (Query, LocalDate): Data inicial.
- `endDate` (Query, LocalDate): Data final.

**Resposta:**
- `200 OK`: Retorna o total pago no período.

**Exemplo de requisição:**
```
GET /accountspayable/total-paid?startDate=2023-01-01&endDate=2023-12-31
Authorization: ApiKey 123456789
```

