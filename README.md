# PantryFlow — сервер лабораторной №1

PantryFlow — серверное приложение на Kotlin и Ktor для совместного учёта домашних продуктов и покупок. Проект реализован как модульный монолит с гексагональной архитектурой и одной базой PostgreSQL.

## Что реализовано

- Создание и получение пользователей; создание дома, вступление по коду приглашения и просмотр домов пользователя.
- Каталог продуктов дома, учёт отдельных партий с количеством, местом хранения и сроком годности, изменение остатков и список продуктов ниже заданного минимума.
- Общий список покупок: добавление и удаление позиций, проведение покупки. При покупке позиция получает статус `PURCHASED`, а запас пополняется новой партией в одной транзакции; повторное проведение отклоняется.
- REST API с проверкой входных данных, ответами `400`/`404`/`409` для ожидаемых ошибок и документацией OpenAPI в Swagger UI. Данные сохраняются в PostgreSQL между перезапусками контейнеров.

## Технологии и архитектура

- **Kotlin/JVM 17, Ktor и Netty** — HTTP-сервер; **kotlinx.serialization** — JSON.
- **PostgreSQL 16** — постоянное хранение; **Hibernate ORM/JPA** — работа с таблицами; **HikariCP** — пул соединений; **Flyway** — миграции схемы.
- **Gradle** — сборка; **Docker Compose** — совместный запуск сервера и БД; **Logback** — логирование; **JUnit 5 и Ktor Test Host** — тесты.
- Входные адаптеры Ktor вызывают сценарии через порты `application/port/in`, а сервисы обращаются к БД через `application/port/out`. Доменные сущности и правила не зависят от HTTP и JPA. Внешних сервисов и межсервисного взаимодействия нет.

## Запуск

Нужны Docker с Docker Compose. Откройте терминал в каталоге проекта:

```bash
cd /home/alex/KOTLIN/lab_1/pantryflow
cp .env.example .env
```

Если `.env` уже существует, повторно копировать шаблон не нужно. Откройте `.env` и замените `DB_PASSWORD` на свой пароль **для локальной PostgreSQL**. `DB_PORT=5433` — порт БД на вашем компьютере; если он занят, выберите другой. Файл `.env` не публикуйте в Git. Затем запустите оба контейнера:

```bash
docker compose up --build
```

При первом запуске Docker скачает образы и зависимости Gradle; это может занять несколько минут. Дождитесь запуска сервера. В другом терминале проверьте `curl http://localhost:8080/health` — ответ `ok`. Откройте **Swagger UI: [http://localhost:8080/swagger](http://localhost:8080/swagger)**. Сам OpenAPI-контракт доступен по [http://localhost:8080/openapi.yaml](http://localhost:8080/openapi.yaml). В Swagger можно раскрыть операцию, нажать **Try it out**, заполнить JSON и выполнить запрос. Маршруты указаны относительно текущего сервера, поэтому менять адрес в Swagger не нужно.

PostgreSQL хранит данные в Docker-томе `pantryflow_pgdata`; `docker compose down` останавливает контейнеры, но сохраняет данные. Если том уже создан, оставьте в `.env` пароль, с которым БД запускалась впервые: изменение `DB_PASSWORD` не меняет пароль внутри существующей PostgreSQL. Flyway применяет миграции при старте. Для локального запуска без Docker нужен JDK 17, PostgreSQL и переменные `DB_URL`, `DB_USER`, `DB_PASSWORD`; команда: `cd server && ./gradlew run`. Тесты: `cd server && ./gradlew test`.

### Как проверить полный сценарий в Swagger

1. `POST /api/v1/users`: создайте пользователя с `email` и `displayName`; скопируйте его `id`.
2. `POST /api/v1/households`: передайте этот `id` как `ownerId` и укажите `name`; скопируйте `id` дома и `inviteCode`.
3. `POST /api/v1/households/{householdId}/products`: подставьте `id` дома, создайте продукт, например `name: Milk`, `unit: MILLILITER`, `minimumQuantity: "1000"`; скопируйте `id` продукта.
4. `POST /api/v1/households/{householdId}/shopping-items`: укажите `productId` и `quantity: "1500"`; скопируйте `id` позиции.
5. `POST /api/v1/households/{householdId}/shopping-items/{itemId}/purchase`: укажите `storage: FRIDGE`. Ответ покажет позицию со статусом `PURCHASED` и новую партию.
6. `GET /api/v1/households/{householdId}/products` и `GET /api/v1/households/{householdId}/products/{productId}/batches`: проверьте остаток и сохранённую партию. Повторная покупка той же позиции вернёт `409 Conflict`.

### Как посмотреть PostgreSQL

Из каталога проекта откройте `psql` внутри контейнера — устанавливать PostgreSQL на компьютер для этого не нужно:

```bash
docker compose exec db psql -U pantryflow -d pantryflow
```

В `psql` выполните:

```sql
\dt
SELECT id, email, display_name FROM users;
SELECT id, name, invite_code FROM households;
SELECT id, name, minimum_quantity FROM products;
SELECT product_id, quantity, storage FROM product_batches;
SELECT id, status, quantity FROM shopping_items;
```

Команда `\q` выходит из `psql`. Для DBeaver или DataGrip используйте PostgreSQL: **host** `localhost`, **port** `5433` (или ваш `DB_PORT`), **database** `pantryflow`, **user** `pantryflow`, **password** из `.env`. Порт открыт только на `127.0.0.1`; сервер внутри Compose подключается к БД по `db:5432`.

В этой версии **нет входа, паролей, токенов и проверки прав доступа**. `userId` в запросах — идентификатор участника для создания или присоединения к дому. API пока предназначено для локальной лабораторной проверки, а не для публикации в интернете.

## API

Полный машиночитаемый контракт с телами запросов, ответами и ошибками находится в [OpenAPI YAML](server/src/main/resources/openapi/pantryflow.yaml). Swagger UI отображает его при запущенном сервере.

Все пути, кроме `/health`, начинаются с `/api/v1`. Тело POST/PATCH — JSON с `Content-Type: application/json`.

| Метод и путь | Тело / результат |
| --- | --- |
| `POST /users` | `email`, `displayName` → пользователь с `id` |
| `GET /users/{userId}` | Пользователь |
| `POST /households` | `ownerId`, `name` → дом с `id` и `inviteCode` |
| `POST /households/join` | `userId`, `inviteCode` → дом |
| `GET /users/{userId}/households` | Дома пользователя |
| `POST /households/{householdId}/products` | `name`, `category?`, `unit`, `minimumQuantity` |
| `GET /households/{householdId}/products` | Продукты с текущим остатком |
| `GET /households/{householdId}/products/low-stock` | Продукты ниже минимума |
| `POST /households/{householdId}/products/{productId}/batches` | `quantity`, `storage`, `purchasedOn?`, `expiresOn?` |
| `GET /households/{householdId}/products/{productId}/batches` | Партии продукта |
| `PATCH /households/{householdId}/batches/{batchId}/quantity` | `delta`, например `"-2"` |
| `POST /households/{householdId}/shopping-items` | `productId`, `quantity` |
| `GET /households/{householdId}/shopping-items` | Общий список покупок |
| `POST /households/{householdId}/shopping-items/{itemId}/purchase` | `storage`, `purchasedOn?`, `expiresOn?` |
| `DELETE /households/{householdId}/shopping-items/{itemId}` | Удалить ещё не купленную позицию |

`unit`: `PIECE`, `GRAM`, `MILLILITER`. `storage`: `FRIDGE`, `FREEZER`, `CABINET`, `PANTRY`. Количества передаются строками с максимум тремя знаками после точки, даты — `YYYY-MM-DD`. Ошибки возвращаются как `{"code":"...","message":"..."}` со статусами 400, 404, 409 или 500.

### Быстрая проверка

```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"alex@example.com","displayName":"Alex"}'
```

Возьмите `id` пользователя из ответа и подставьте его вместо `USER_ID`:

```bash
curl -s -X POST http://localhost:8080/api/v1/households \
  -H 'Content-Type: application/json' \
  -d '{"ownerId":"USER_ID","name":"Our home"}'
```

Из ответа возьмите `id` дома (`HOUSEHOLD_ID`), затем создайте продукт:

```bash
curl -s -X POST http://localhost:8080/api/v1/households/HOUSEHOLD_ID/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Milk","unit":"MILLILITER","minimumQuantity":"1000"}'
```

## Структура

```text
server/src/main/kotlin/com/pantryflow/
├── domain/
│   ├── model/              # сущности: User, Household, Product и другие
│   ├── enums/              # роли, единицы измерения, хранение, статус
│   ├── valueobject/        # типы с нормализацией и проверкой инвариантов
│   └── exception/          # доменные ошибки
├── application/
│   ├── dto/                # команды и результаты сценариев
│   ├── mapper/             # доменная модель → результат сценария
│   ├── port/in/            # интерфейсы сценариев для входных адаптеров
│   ├── port/out/           # интерфейсы хранилищ и транзакций
│   └── service/            # бизнес-сценарии
├── adapters/
│   ├── in/web/             # Ktor-контроллеры, HTTP DTO и маппер
│   └── out/jpa/            # JPA-сущности, маппер и репозитории
└── bootstrap/              # конфигурация и соединение реализаций
```

Поток запроса: `controller → port/in → service → port/out → JPA repository → PostgreSQL`. Domain и application не импортируют Ktor, Hibernate или JDBC. Hibernate использует JPA-сущности только в выходном адаптере; SQL-схему управляет Flyway, а Hibernate её проверяет. Условные SQL-обновления защищают остаток партии от отрицательных значений и повторную покупку от двойного проведения.

На этом этапе не реализованы сканирование штрихкодов, внешние каталоги, рецепты, уведомления и фоновые задания.
