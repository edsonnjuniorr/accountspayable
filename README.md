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

### 2. Listar Contas a Pagar

**Endpoint:**
```
GET /accountspayable
```

**Parâmetros:**
- `dueDate` (Query, LocalDate) - Opcional: Filtra pelo vencimento.
- `description` (Query, String) - Opcional: Filtra pela descrição.

**Resposta:**
- `200 OK`: Retorna uma lista de contas a pagar.

**Exemplo de requisição:**
```
GET /accountspayable?dueDate=2023-12-01&description=Aluguel
Authorization: ApiKey 123456789
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

---

### 4. Criar Conta a Pagar

**Endpoint:**
```
POST /accountspayable
```

**Corpo da requisição (JSON):**
```json
{
  "description": "Aluguel",
  "amount": 1500.00,
  "dueDate": "2023-12-01"
}
```

**Resposta:**
- `201 Created`: Retorna a conta criada.

**Exemplo de requisição:**
```
POST /accountspayable
Authorization: ApiKey 123456789
Content-Type: application/json

{
  "description": "Aluguel",
  "amount": 1500.00,
  "dueDate": "2023-12-01"
}
```

---

### 5. Atualizar Conta a Pagar

**Endpoint:**
```
PUT /accountspayable/{id}
```

**Parâmetros:**
- `id` (Path Variable, Long): ID da conta a pagar.

**Corpo da requisição (JSON):**
```json
{
  "description": "Energia",
  "amount": 500.00,
  "dueDate": "2023-12-10"
}
```

**Resposta:**
- `200 OK`: Retorna a conta atualizada.

**Exemplo de requisição:**
```
PUT /accountspayable/1
Authorization: ApiKey 123456789
Content-Type: application/json

{
  "description": "Energia",
  "amount": 500.00,
  "dueDate": "2023-12-10"
}
```

---

### 6. Atualizar Status da Conta

**Endpoint:**
```
PATCH /accountspayable/{id}/status
```

**Parâmetros:**
- `id` (Path Variable, Long): ID da conta a pagar.
- `status` (Query, String): Novo status da conta.

**Resposta:**
- `200 OK`: Retorna a conta com o status atualizado.

**Exemplo de requisição:**
```
PATCH /accountspayable/1/status?status=PAID
Authorization: ApiKey 123456789
```

---

### 7. Upload de CSV

**Endpoint:**
```
POST /accountspayable/upload
```

**Parâmetros:**
- `file` (FormData, MultipartFile): Arquivo CSV contendo contas a pagar.

**Resposta:**
- `200 OK`: Retorna uma lista das contas inseridas.

**Exemplo de requisição:**
```
POST /accountspayable/upload
Authorization: ApiKey 123456789
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="file"; filename="contas.csv"
Content-Type: text/csv

(dados do arquivo CSV)

------WebKitFormBoundary--
