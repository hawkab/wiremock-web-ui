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

### Frontend
- Node.js `18+`
- npm `9+`


---

## Структура проекта

```
root
 ├─ backend/        # Spring Boot
 ├─ frontend/       # React + Vite
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

## Сборка

### Backend
```
cd backend
mvn clean package
```
---

### Frontend
```
cd frontend
npm install
npm run build
```
---

## Запуск

### 1\. Backend
```
cd backend
mvn spring-boot:run
```

Или:
```
java -jar target/*.jar
```
---

### 2\. Frontend
```
cd frontend
npm install
npm run dev
```
---

## WireMock embedded
При необходимости можно выключить встроенный wiremock-standalone и использовать внешний wiremock-сервер:
```
wiremock.embedded.enabled=false
```

---

## Автор

```
Author: Grigory Olshansky
```
