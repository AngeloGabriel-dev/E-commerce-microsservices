# Service Account Authentication

Este documento descreve como autenticar serviços no Auth-Service usando Service Accounts.

## Estrutura da Entidade ServiceAccount

```java
{
  "id": "UUID",
  "clientId": "string (único)",
  "clientSecretHash": "string (BCrypt hash)",
  "role": "Enum (ROLE_ORDER_SERVICE, ROLE_PAYMENT_SERVICE, etc)",
  "enabled": "boolean"
}
```

## Roles Disponíveis

Cada serviço tem sua própria role específica:

- `ROLE_ORDER_SERVICE` - Serviço de pedidos
- `ROLE_PAYMENT_SERVICE` - Serviço de pagamentos
- `ROLE_NOTIFICATION_SERVICE` - Serviço de notificações
- `ROLE_CATALOG_SERVICE` - Serviço de catálogo
- `ROLE_USER_SERVICE` - Serviço de usuários
- `ROLE_AUTH_SERVICE` - Serviço de autenticação
- `ROLE_SERVICE` - Role genérica para serviços

## Endpoints de Autenticação

### 1. Login de Usuário Normal
**POST** `/api/v1/auth/login`

Request body:
```json
{
  "email": "user@example.com",
  "password": "userpassword"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 2. Login de Serviço (Service Account)
**POST** `/api/v1/auth/service-account/login`

Request body:
```json
{
  "clientId": "order-service",
  "clientSecret": "secret123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## Como Usar o Token

Após obter o token, inclua-o no header Authorization das requisições:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Service Accounts Pré-configurados

Os seguintes service accounts estão disponíveis por padrão (senha: `secret123`):

| Client ID | Role | Descrição |
|-----------|------|-----------|
| order-service | ROLE_ORDER_SERVICE | Serviço de pedidos |
| payment-service | ROLE_PAYMENT_SERVICE | Serviço de pagamentos |
| notification-service | ROLE_NOTIFICATION_SERVICE | Serviço de notificações |
| catalog-service | ROLE_CATALOG_SERVICE | Serviço de catálogo |

## Criando Novos Service Accounts

Para criar novos service accounts, você pode:

1. **Via SQL**: Inserir diretamente na tabela `service_accounts`
2. **Via código**: Chamar o método `serviceAccountService.createServiceAccount()`

Exemplo de criação via código:
```java
@Service
public class ServiceAccountInitializer {
    private final ServiceAccountService serviceAccountService;

    public ServiceAccountInitializer(ServiceAccountService serviceAccountService) {
        this.serviceAccountService = serviceAccountService;
        
        // Criar service account com role específica
        serviceAccountService.createServiceAccount(
            "new-service",
            "my-secret-password",
            ServiceAccount.Role.ROLE_SERVICE
        );
    }
}
```

## Gerenciando Service Accounts

O `ServiceAccountService` fornece métodos para gerenciar service accounts:

- `authenticate(ServiceAccountLoginDto dto)` - Autentica e retorna JWT token
- `createServiceAccount(String clientId, String clientSecret, ServiceAccount.Role role)` - Cria novo service account
- `findByClientId(String clientId)` - Busca por client ID
- `findAll()` - Lista todos os service accounts
- `updateServiceAccount(UUID id, ServiceAccount.Role role, Boolean enabled)` - Atualiza role ou status
- `deleteServiceAccount(UUID id)` - Remove service account

## Segurança

- Os client secrets são armazenados como hash BCrypt
- Tokens JWT expiram em 20 minutos
- Service accounts podem ser desabilitados (enabled = false)
- Cada serviço tem sua própria role específica (enum)
- Roles seguem o padrão `ROLE_<SERVICE_NAME>_SERVICE`

## Exemplo de Uso com cURL

```bash
# Login de usuário
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Login de serviço
curl -X POST http://localhost:8080/api/v1/auth/service-account/login \
  -H "Content-Type: application/json" \
  -d '{"clientId":"order-service","clientSecret":"secret123"}'

# Usar o token
curl http://localhost:8080/api/v1/protected \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## Validação de Roles em Endpoints

Para proteger endpoints específicos por serviço, use a anotação `@PreAuthorize`:

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ORDER_SERVICE')")
    public ResponseEntity<List<Order>> getAllOrders() {
        // Apenas order-service pode acessar
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ORDER_SERVICE', 'ROLE_PAYMENT_SERVICE')")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        // order-service e payment-service podem acessar
    }
}