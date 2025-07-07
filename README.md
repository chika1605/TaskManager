# 📝 Тапшырмаларды Башкаруу Системасы

Бул долбоор — Spring Boot негизинде жазылган тапшырмаларды башкаруу системасы. Колдонуучулар ролдорго (ADMIN, MANAGER, USER) бөлүнөт жана ар бир ролдун өзүнүн уруксаттары бар. Система JWT авторизацияны, refresh токендерди, лог-аут механизмин, CRUD операцияларын, Swagger UI документтерин жана Docker колдоосун камтыйт.

---

## 🔧 Орнотуу нускамалары

### 1. Репозиторийди клондоо

git clone https://github.com/chika1605/TaskManager.git
cd TaskManager

### 2. Орнотуу талаптары
Java 17+

Maven 3.8+

Docker (эгер контейнер менен иштетүүнү кааласаң)

### 3. Конфигурация
src/main/resources/application.yml файлын текшерип, керектүү конфигурацияларды жөндө.

### 4. Долбоорду локалдуу ишке киргизүү
./mvnw spring-boot:run
### 5. Docker аркылуу ишке киргизүү
docker-compose up --build
🚀 Колдонуу
🔐 Аутентификация
Катталуу:
POST /api/auth/register

Кирүү (Login):
POST /api/auth/login

Токенди жаңылоо:
POST /api/auth/refresh

Чыгуу (Logout):
POST /api/auth/logout

✅ Тапшырмалар (Tasks)
POST /api/tasks — Тапшырма түзүү

GET /api/tasks — Тапшырмалар тизмеси

PUT /api/tasks/{id} — Тапшырманы жаңылоо

DELETE /api/tasks/{id} — Тапшырманы өчүрүү

👥 Колдонуучулар (Users)
GET /api/users — Бардык колдонуучулар (ADMIN жана MANAGER гана)

GET /api/users/{id} — Колдонуучунун маалыматы

PUT /api/users/{id} — Колдонуучуну жаңылоо

DELETE /api/users/{id} — Колдонуучуну өчүрүү (только ADMIN)

📚 API документация
Swagger UI (локалдуу иштетүүдө жеткиликтүү):
http://localhost:8080/swagger-ui/index.html

OpenAPI спецификациясы (JSON):
http://localhost:8080/v3/api-docs

⚠️ Эскертүүлөр
Проект учурда H2 in-memory база менен иштеп жатат. Продуктивдүү чөйрө үчүн PostgreSQL же башка базага оңой туташса болот.

Refresh токендер refresh_tokens таблицасында сакталат. Logout учурунда алар жараксыз болуп калат.

@PreAuthorize жана SecurityContextHolder аркылуу ролдорго жараша уруксат берилет.

Логдор logback аркылуу башкарылат.

База структурасы Liquibase аркылуу миграциялана турган файлдарда сакталат.

🐳 Docker колдонуу
Проектти контейнерде иштетүү үчүн:

docker-compose up --build
Эгер өзгөртүүлөр киргизсең, контейнерди кайра түз:

docker-compose down
docker-compose up --build

🧪 Тестирлөө
Юнит-тесттер JUnit жана Mockito менен жазылган

Интеграциялык тесттер @SpringBootTest, MockMvc жана H2 базасы менен жүргүзүлөт

Камтуу: 70%+

⚙️ База жана Миграциялар
Проект H2 же PostgreSQL базасын колдойт

Liquibase менен автоматтык таблица түзүү

Миграция файлдары: src/main/resources/db/changelog/

📁 Долбоордун Структурасы
src/
├── controller/       # REST API контроллерлору
├── dto/              # Request / Response класстар
├── entity/           # JPA энтитилери
├── repository/       # JPA репозиторийлери
├── security/         # JWT, фильтр, конфигурация
├── service/          # Бизнес логика
├── resources/
│   └── db/changelog/ # Liquibase миграциялары
└── application.yml   # Конфигурация
```bash
