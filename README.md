# Librarian Book Service

This repository contains a minimal Spring Boot 'book-service' that manages books, loans, and an outbox for events.

Run
1. Configure MySQL credentials in `src/main/resources/application.properties`.
2. Apply schema: `mysql -u root -p < src/main/resources/schema.sql`
3. Run: `mvn spring-boot:run`

Endpoints
- POST /books/checkout
  Body: {"userId":"u_1","bookId":"b_100"}
  Response: {"loanId": 1}

- POST /books/return
  Body: {"userId":"u_1","bookId":"b_100"}
  Response: {"loanId": 1}

Notes
- Authentication/authorization is assumed to be handled upstream.
- Services receive trusted headers; no token validation is performed here.
- The outbox table `libbook.book_event_outbox` is written to transactionally; a separate dispatcher should read/process it and mark records processed.

