# Money Transfer and Transactional Service(MTSS) - упрошенная реализация программы перевода денежных сумм между банковскими счетами.

## Обзор архитектуры.

# 💸 Money Transfer and Transactional Service (MTSS)

Упрощенная реализация сервиса перевода денежных сумм между банковскими картами.

<span style="background-color: #007bff; color: white; padding: 3px 8px; border-radius: 12px; font-size: 14px;">Java 17</span>
<span style="background-color: #6f42c1; color: white; padding: 3px 8px; border-radius: 12px; font-size: 14px;">Spring Boot 3.3.5</span>
<span style="background-color: #2496ed; color: white; padding: 3px 8px; border-radius: 12px; font-size: 14px;">Docker</span>
<span style="background-color: #28a745; color: white; padding: 3px 8px; border-radius: 12px; font-size: 14px;">Testcontainers</span>

Сервис перевода денег с карты на карту. Реализован на Spring Boot с упрощенным in-memory типом данных.  
Реализует двухэтапный перевод с подтверждением по коду и логирование всех операций.
Поддерживает расчет сложной комиссии на основе расширяемой архитектуры (абстрактный класс `Commission`, enum `CommissionType`).
В текущей реализации комиссия зафиксирована на уровне **1%** для соответствия демонстрационному фронтенду.
---
> Примечания

> Сервер не проверяет срок действия и CVV карт (только формат). Для успешного тестирования достаточно передать валидные по формату значения.

> Код подтверждения всегда 0000.

> Валюта зафиксирована как RUB (также принимается RUR).

> Комиссия установлена в размере 1% и согласована с предоставленным фронтендом.
## value — это копейки.
> **Примечание о суммах:** фронтенд отправляет сумму в **копейках** (целое число). Сервер преобразует копейки в рубли BigDecimal (из-за намерения работы с CURENCY, также установлен лимит) с двумя знаками после запятой для внутренних расчётов и хранения балансов.

---

## 🚀 Быстрый старт

### Локально (Maven)
```bash
./mvnw spring-boot:run
---

## 1. Изучение протокола взаимодействия

Сервис предоставляет REST API в соответствии со спецификацией OpenAPI (файл `MoneyTransferServiceSpecification.yaml`).

### Эндпоинты:
| Метод | URL | Описание |
|-------|-----|----------|
| POST | `/transfer` | Создание операции перевода. Возвращает `operationId`. |
| POST | `/confirmOperation` | Подтверждение операции кодом. Выполняет фактическое списание и зачисление средств. |
> **Примечание:** в текущей реализации сервер **не проверяет** срок действия и CVV карт, только их формат. Для успешного тестирования достаточно передать валидные по формату значения.  
> Код подтверждения всегда **0000**.CURRENCY_RUB — валюта фиксирована, её изменение потребует пересмотра фрагментов бизнес‑логики.Каммиссия захардкоржена Front-ендом.

### Формат запросов и ответов
Пример запроса на перевод:
```
📡 API
POST /transfer
```json
{
  "cardFromNumber": "1234567812345678",
  "cardFromValidTill": "12/26",
  "cardFromCVV": "123",
  "cardToNumber": "8765432187654321",
  "amount": {
    "value": 500,
    "currency": "RUB"
  }
}

POST /confirmOperation
json
{
  "operationId": "полученный_id",
  "code": "0000"
}
```
## 2. 🛠 Технологический стэк.

<span style="background-color: #007bff; color: white; padding: 3px 8px; border-radius: 12px;">Java 17</span>
<span style="background-color: #6f42c1; color: white; padding: 3px 8px; border-radius: 12px;">Spring Boot 3.3.5</span>
<span style="background-color: #2496ed; color: white; padding: 3px 8px; border-radius: 12px;">Docker</span>
<span style="background-color: #28a745; color: white; padding: 3px 8px; border-radius: 12px;">Maven</span>
<span style="background-color: #dc3545; color: white; padding: 3px 8px; border-radius: 12px;">JUnit 5</span>

### 3.⚖️ Критерии выбора. Смешенный подход к расположению констант,часть констант вынесена в application.yaml, а часть в классе ConstantContainer.
### 4. Покрытие тестами.

In‑memory БД (H2) vs. ConcurrentHashMap (Данный проект)

```mermaid
flowchart LR
    A[Пользователь] --> B[Демо-фронтенд<br/>React]
    B --> C[TransferController<br/>REST API]
    C --> D[TransferServiceImpl<br/>Бизнес-логика]
    D --> E[MixedCommission<br/>Расчёт комиссии 1%]
    D --> F[InMemoryTransferRepository<br/>Хранилище в памяти]
    D --> G[LoggingServiceImpl<br/>Запись логов]
    F --> H[ConcurrentHashMap<br/>Балансы и операции]
    G --> I[transfer.log]
```




