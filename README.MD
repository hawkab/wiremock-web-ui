# WireMock Web UI

Web-интерфейс для управления WireMock: просмотр логов запросов, редактирование маппингов

Проект состоит из двух частей:
- Backend: Spring Boot (Java)
- Frontend: React + Vite (TypeScript/TSX)

---

## Системные требования

### Backend
- Java `17+`
- Maven `3.9+`

Проверка:
```
java -version
mvn -version
```

### Frontend
- Node.js `18+`
- npm `9+` (или pnpm/yarn по вкусу)

Проверка:
```
node -v
npm -v
```

---

## Структура проекта

```
root
 ├─ backend/        # Spring Boot
 ├─ frontend/       # React + Vite
 ├─ wiremock/       # mappings + __files (если используются)
 └─ README.md
```

---

## Установка

### 1\. Клонирование репозитория
```
git clone git@github.com:hawkab/wiremock-web-ui.git
cd wiremock-web-ui
```

---

## Первичная настройка

### Backend конфигурация

Файл:
```
backend/src/main/resources/application.yml
```

Минимальный пример:

```
server:
  port: 8082

wiremock:
  embedded:
    enabled: true
    port: 8081
    options:
      port: 8081
      root-dir: "${user.home}/wiremock-data"
      global-response-templating: true
      disable-banner: true

  base-url: "http://127.0.0.1:${wiremock.embedded.port}"

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Что это значит:
- `embedded.enabled=true` — backend сам поднимет WireMock
- `root-dir` — папка с `mappings` и `__files`

Создай структуру (если её нет):
```
mkdir -p wiremock/mappings
mkdir -p wiremock/__files
```

---

### Frontend конфигурация

По умолчанию фронт ходит в backend по:
```
/api/*
```

Если backend не на `localhost:8080`, смотри:
```
frontend/vite.config.ts
```

---

## Сборка

### Backend
```
cd backend
mvn clean package
```

После сборки:
```
backend/target/*.jar
```

---

### Frontend
```
cd frontend
npm install
npm run build
```

Сборка попадёт в:
```
frontend/dist
```

---

## Запуск (DEV режим)

### 1\. Backend
```
cd backend
mvn spring-boot:run
```

Или:
```
java -jar target/*.jar
```

Backend поднимется:
```
http://localhost:8080
```

---

### 2\. Frontend (dev)
```
cd frontend
npm install
npm run dev
```

Открой:
```
http://localhost:5173
```

---

## Запуск (production)

### Backend
```
cd backend
mvn clean package
java -jar target/*.jar
```

### Frontend
```
cd frontend
npm run build
```

Скопируй содержимое:
```
frontend/dist
```
в:
```
backend/src/main/resources/static
```

Пересобери backend:
```
mvn clean package
```

Теперь UI будет доступен прямо с backend:
```
http://localhost:8080
```

---

## WireMock embedded

Если включён:
```
wiremock.embedded.enabled=true
```

то backend автоматически поднимет WireMock standalone.

Папка:
```
wiremock/
 ├─ mappings
 └─ __files
```

---

## Основные endpoints

### Backend API
```
GET  /api/requests
POST /api/requests/find
DELETE /api/requests

GET  /api/mappings
POST /api/mappings
PUT  /api/mappings/{id}
DELETE /api/mappings/{id}
POST /api/mappings/save
```

---

## Типичный запуск одной командой (DEV)

Backend:
```
cd backend && mvn spring-boot:run
```

Frontend:
```
cd frontend && npm run dev
```

---

## Автор

```
Author: Grigory Olshansky
```