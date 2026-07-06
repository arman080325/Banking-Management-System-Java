<div align="center">

<img src="https://readme-typing-svg.demolab.com/?font=Space+Mono&size=30&duration=2600&pause=900&color=16B083&center=true&vCenter=true&width=720&lines=IndusTrust+Bank;Spring+Boot+%2B+Spring+Security+%2B+PostgreSQL;Accounts+%C2%B7+Transfers+%C2%B7+Double-Entry+Ledger;Java+Console+App+%E2%86%92+Cloud+Web+Bank" alt="IndusTrust Bank" />

### A session-authenticated banking service — rebuilt from a console/JDBC Java app into a production **Spring Boot** web application with a real double-entry ledger, row-locked transfers, and `BigDecimal` money.

<br/>

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-Sessions-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![H2](https://img.shields.io/badge/H2-Dev%20DB-1E88E5?style=for-the-badge&logo=h2&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Render-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

<br/>

**[🚀 Live Demo](#)** &nbsp;·&nbsp; **[📖 API Reference](#-api-reference)** &nbsp;·&nbsp; **[⚙️ Run Locally](#-getting-started)** &nbsp;·&nbsp; **[🏗️ Architecture](#-architecture)** &nbsp;·&nbsp; **[🗺️ Roadmap](#-roadmap)**

<br/>

<img src="https://user-images.githubusercontent.com/74038190/212284100-561aa473-3905-4a80-b561-0d28506553ee.gif" width="70%">

</div>

<br/>

## 📌 About The Project

**IndusTrust Bank** is a session-authenticated banking web application: register, sign in, open one or more accounts, deposit, withdraw, transfer between accounts, save beneficiaries, pay bills, manage a debit card, and raise support tickets — all backed by a real **double-entry ledger** instead of a single mutable balance column.

It's a ground-up rebuild of a **console-based JDBC banking app** that used to run in a `Scanner` loop against MySQL. This version replaces every one of that original's structural weaknesses:

| 🗿 Original Console App | ✨ This Rebuild |
|---|---|
| Terminal `Scanner` loop | Session-authenticated web app + REST API |
| Balances stored as `double` | `BigDecimal` money — no floating-point drift |
| Passwords & PINs in plaintext | BCrypt-hashed passwords **and** transaction PINs |
| Hardcoded DB credentials in source | 100% environment-driven configuration |
| No transaction history | Immutable **double-entry ledger** with running balance |
| Transfer never checked the receiver existed | Receiver verified; atomic two-leg transfer |
| No locking — concurrent transfers could corrupt a balance | **Row-level pessimistic locks**, acquired in a deterministic order |
| "Login" just returned an email in a loop | Real Spring Security sessions; per-user account isolation |

<br/>

## ✨ Features

<table>
<tr>
<td width="50%" valign="top">

### 🔐 Accounts & Security
- Register / sign in via **Spring Security sessions**
- **BCrypt**-hashed passwords and per-account transaction PINs
- **Multiple accounts** per user, each with its own PIN
- **Ownership isolation** — every operation re-resolves the current user from the session; an account number in a URL is never trusted alone
- **Change password** from the Profile page, verified against the current hash

</td>
<td width="50%" valign="top">

### 💸 Money Movement
- **Deposit, withdraw, transfer** between accounts
- Transfers are **atomic and row-locked** — both accounts are locked lowest-account-number-first, so opposing transfers can't deadlock or corrupt a balance
- **Double-entry ledger** — every movement writes a `DEBIT` leg and a `CREDIT` leg under one shared reference, each storing the balance *after* it
- **Beneficiaries** — save payees for faster future transfers

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🧾 Statements & Bills
- **Paginated account statement**, credit/debit columns, running balance
- **Full CSV export** of a statement — not limited to the loaded page
- **Bill Pay / Recharge** — debits a real owned account through the same ledger and keeps a persisted history

</td>
<td width="50%" valign="top">

### 💳 Cards & Support
- **One card per account**, issued on first request
- **Freeze / unfreeze**, contactless and online-payment toggles, replacement requests — all persisted server-side
- **Support tickets** — messages are stored and listed back with a ticket number

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🎨 Frontend
- **Light / Dark / System** theme, persisted, applied pre-paint (no flash)
- Zero-build vanilla HTML / CSS / JavaScript — no bundler required

</td>
<td width="50%" valign="top">

### 🛡️ Ops & Developer Experience
- Hardened headers: **CSP**, `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`
- HTTP-only, `SameSite` session cookie
- 📘 Swagger UI · 💓 Actuator health · 🧪 Integration tests · 🔁 GitHub Actions CI

</td>
</tr>
</table>

<br/>

## 🧰 Tech Stack

<div align="center">

| Layer | Technology |
|---|---|
| **Runtime** | Java 21 |
| **Framework** | Spring Boot 3.3.4 (Web, Data JPA, Validation, **Security**, Actuator) |
| **Database** | PostgreSQL (production, via Neon) · H2 in-memory (local dev) |
| **Auth** | Spring Security — session-based, BCrypt password/PIN hashing |
| **API Docs** | springdoc-openapi (Swagger UI) |
| **Frontend** | Vanilla HTML / CSS / JavaScript — no build tooling required |
| **Build tool** | Maven (bundled wrapper — no local install needed) |
| **CI/CD** | GitHub Actions |
| **Deployment** | Docker container on Render + Neon PostgreSQL |

</div>

<br/>

## 🏗️ Architecture

```
                 ┌──────────────────┐     ┌─────────────┐     ┌──────────┐     ┌────────────┐     ┌─────────┐
  HTTP Request → │ Security Filter  │ →   │ Controller  │ →   │ Service  │ →   │ Repository │ →   │ Entity  │ → PostgreSQL / H2
                 │ (session cookie) │     │ (REST API)  │     │ (rules + │     │ (Spring    │     │  (JPA)  │
                 │                  │  ←  │             │  ←  │  DTOs)   │  ←  │  Data JPA) │  ←  │         │
                 └──────────────────┘     └─────────────┘     └──────────┘     └────────────┘     └─────────┘
                                                                     ↓
                                                    money rules · row locking · ledger entries
```

**Key design decisions**

- 💰 **`BigDecimal` everywhere for money** — the original used `double`, which is unsafe for currency arithmetic.
- 🔒 **Transfers lock both accounts** with `PESSIMISTIC_WRITE`, acquired **lowest-account-number first**, so two opposing transfers can never deadlock each other or read a stale balance.
- 🧾 **Double-entry ledger** — a transfer writes a `DEBIT` leg on the sender and a `CREDIT` leg on the receiver under one shared reference; each entry records the balance *after* it, so a statement always reconciles independently of the account's current balance column.
- 🧍 **Per-user isolation enforced server-side** — every account, card, beneficiary, and bill-pay lookup re-checks ownership against the session user; nothing is authorized by trusting an ID in the URL.
- 🔑 **No secrets in source** — all credentials and connection strings come from environment variables.
- 🎫 **Support and billing history are persisted**, not just fire-and-forget requests, so a user's past tickets and payments are always retrievable.

<br/>

## 📂 Project Structure

```
banking-system/
├── src/main/java/online/armanportfolio/bank/
│   ├── controller/     # AuthController, AccountController, BeneficiaryController,
│   │                   # BillPayController, CardController, SupportController
│   ├── service/        # AccountService, LedgerService, BillPayService, CardService…
│   ├── repository/     # Spring Data JPA repositories for each entity
│   ├── model/          # User, Account, LedgerEntry, Beneficiary, Card, BillPayment, SupportTicket
│   ├── dto/            # Request/response DTOs isolating entities from the wire format
│   ├── security/        # Spring Security session config, BCrypt config
│   ├── exception/       # GlobalExceptionHandler + domain exceptions
│   └── config/          # DataSeeder, SecurityHeadersFilter, OpenApiConfig
├── src/main/resources/
│   ├── static/          # login.html, index.html, css/, js/ — the console UI
│   ├── application.properties        # dev profile (H2)
│   └── application-prod.properties   # prod profile (PostgreSQL)
├── src/test/java/.../BankingIntegrationTest.java
├── .github/workflows/ci.yml
├── .env.example
├── Dockerfile
└── pom.xml
```

<br/>

## 🚀 Getting Started

### Prerequisites
- **JDK 21** — that's it. Maven ships via the bundled wrapper, and local dev runs against a zero-setup in-memory H2 database.

### Run it

```bash
git clone <your-repo-url>
cd banking-system

# macOS / Linux
./mvnw spring-boot:run

# Windows PowerShell
.\mvnw spring-boot:run
```

Open `http://localhost:8080/` and sign in with the seeded demo login:

> **demo@bank.app** · password **demo12345** — two accounts, PIN **1234**

| URL | What you'll find |
|---|---|
| `http://localhost:8080/` | 🏦 The banking dashboard |
| `http://localhost:8080/login.html` | 🔐 Sign in / create account |
| `http://localhost:8080/swagger-ui.html` | 📘 Interactive API documentation |
| `http://localhost:8080/actuator/health` | 💓 Health check endpoint |
| `http://localhost:8080/h2-console` | 🗄️ H2 database console *(dev only)* |

> 💡 If port 8080 is already taken, run with `-Dspring-boot.run.arguments=--server.port=8081` or free the port first.

<br/>

## ⚙️ Configuration

Production configuration is 100% environment-variable driven — see `.env.example`:

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Set to `prod` to switch to PostgreSQL |
| `SPRING_DATASOURCE_URL` | JDBC URL, e.g. `jdbc:postgresql://<host>/<db>?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `PORT` | HTTP port (auto-provided by Render) |

<br/>

## 📡 API Reference

All `/api/**` routes other than register/login require an authenticated session.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Create a user |
| `POST` | `/api/auth/login` | Start a session |
| `POST` | `/api/auth/logout` | End the session |
| `GET` | `/api/auth/me` | Current signed-in user |
| `POST` | `/api/auth/change-password` | Change the signed-in user's password |
| `GET` | `/api/accounts` | List **your** accounts |
| `POST` | `/api/accounts` | Open an account |
| `GET` | `/api/accounts/{number}` | Account detail (owner only) |
| `GET` | `/api/accounts/{number}/history` | Paginated statement |
| `GET` | `/api/accounts/{number}/statement.csv` | Full statement export (CSV) |
| `POST` | `/api/accounts/credit` | Deposit |
| `POST` | `/api/accounts/debit` | Withdraw |
| `POST` | `/api/accounts/transfer` | Transfer between accounts (PIN-verified, row-locked) |
| `GET` | `/api/beneficiaries` | List your saved beneficiaries |
| `POST` | `/api/beneficiaries` | Save a beneficiary |
| `DELETE` | `/api/beneficiaries/{id}` | Remove a beneficiary |
| `GET` | `/api/billpay/history` | Paginated bill payment history |
| `POST` | `/api/billpay` | Pay a bill / recharge (debits an owned account) |
| `GET` | `/api/cards` | List your cards (issued lazily, one per account) |
| `PATCH` | `/api/cards/{accountNumber}` | Update freeze / contactless / online toggles |
| `POST` | `/api/cards/{accountNumber}/request-replacement` | Request a replacement card |
| `GET` | `/api/support/tickets` | List your support tickets |
| `POST` | `/api/support/tickets` | Raise a new support ticket |

📘 **Full interactive documentation, request/response schemas, and a try-it-out console are available at `/swagger-ui.html`.**

<br/>

## 🧪 Testing

```bash
./mvnw verify
```

`BankingIntegrationTest` is a full-context integration suite (Spring Boot + MockMvc) covering:

- ✅ Register and login flows
- ✅ Account opening
- ✅ A transfer that moves money **and** writes both ledger legs correctly
- ✅ Insufficient-balance and wrong-PIN rejections
- ✅ Unauthenticated access is rejected (**401**)
- ✅ Cross-user access to another user's account is forbidden (**403**)

The identical suite runs automatically on every push and pull request via **GitHub Actions** (`.github/workflows/ci.yml`).

<br/>

## 🔐 Security

- 🔒 Session-based authentication via Spring Security; no tokens to leak in client storage
- 🧂 **BCrypt** hashing for both account passwords and transaction PINs
- 🔢 **Pessimistic row locking** on transfers, acquired in a fixed order to prevent deadlocks and double-spend races
- 🧍 **Ownership checks on every request** — accounts, cards, beneficiaries, and bills are always re-verified against the session user
- 🧱 Hardened response headers: `Content-Security-Policy`, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`
- 🍪 HTTP-only, `SameSite` session cookie
- 🙈 No credentials ever committed — configuration is fully environment-driven

<br/>

## ☁️ Deployment

Deployed as a single Docker container on **[Render](https://render.com)**, backed by a **[Neon](https://neon.tech)** serverless PostgreSQL database.

```
GitHub push → GitHub Actions CI (build + test) → Render (Docker build) → Neon PostgreSQL
```

Set the environment variables from the [Configuration](#️-configuration) section on your Render service, point `SPRING_DATASOURCE_URL` at your Neon connection string, configure the health check to `/actuator/health`, and deploy.

<br/>

## 🗺️ Roadmap

- [ ] Two-factor authentication for login and high-value transfers
- [ ] Scheduled / recurring transfers
- [ ] Admin console for support-ticket triage
- [ ] Rate limiting on authentication and transfer endpoints
- [ ] Downloadable PDF statements alongside the existing CSV export
- [ ] Multi-currency accounts

<br/>

## 🤝 Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.

```bash
git checkout -b feature/your-feature
git commit -m "Add your feature"
git push origin feature/your-feature
# open a PR
```

<br/>

## 📄 License

Distributed under the **MIT License**. See `LICENSE` for details.

<br/>

## 👤 Author

<div align="center">

**Arman Ahemad Khan**

[![Portfolio](https://img.shields.io/badge/Portfolio-arman--portfolio.online-16B083?style=for-the-badge&logo=googlechrome&logoColor=white)](https://arman-portfolio.online)
[![GitHub](https://img.shields.io/badge/GitHub-arman080325-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/arman080325)

<br/>

⭐ **If this project helped you, consider giving it a star!** ⭐

<img src="https://user-images.githubusercontent.com/74038190/212284158-e840e285-664b-44d7-b79b-e264b5e54825.gif" width="100%">

</div>