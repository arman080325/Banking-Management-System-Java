<div align="center">

<img src="https://readme-typing-svg.demolab.com/?font=Space+Mono&size=30&duration=2600&pause=900&color=16B083&center=true&vCenter=true&width=720&lines=Silicon+Bank;Spring+Boot+%2B+Spring+Security+%2B+PostgreSQL;Accounts+%C2%B7+Transfers+%C2%B7+Double-Entry+Ledger;Java+Console+App+%E2%86%92+Cloud+Web+Bank" alt="Silicon Bank" />

### A session-authenticated banking service — rebuilt from a console/JDBC Java app into a production **Spring Boot** web application with a real double-entry ledger, row-locked transfers, and BigDecimal money.

<br/>

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-Sessions-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Render-2496ED?style=for-the-badge&logo=docker&logoColor=white)

<br/>

**[🚀 Live Demo](#)** &nbsp;·&nbsp; **[📖 API Docs](#-api-reference)** &nbsp;·&nbsp; **[⚙️ Run Locally](#-getting-started)** &nbsp;·&nbsp; **[🏗️ Architecture](#-architecture)**

</div>

<br/>

## 📌 About

**Silicon Bank** lets a user register, sign in, open accounts, deposit, withdraw, transfer money between accounts, and view a full transaction statement — all behind session authentication, with every account scoped to its owner.

It's a ground-up rebuild of a **console-based JDBC banking app**. The original ran in a `Scanner` loop against MySQL; this version is a real web service that adds the ledger, locking, and auth the original only gestured at:

| 🗿 Original Console App | ✨ This Rebuild |
|---|---|
| Terminal `Scanner` loop | Session-auth web app + REST API |
| Balances stored as `double` | `BigDecimal` money (no floating-point drift) |
| Passwords & PINs in plaintext | BCrypt-hashed passwords **and** PINs |
| Hardcoded DB credentials in source | Environment-driven config |
| No transaction history | Immutable **double-entry ledger** with running balance |
| Transfer never checked the receiver existed | Receiver verified; atomic two-leg transfer |
| No locking (concurrent transfers could corrupt a balance) | **Row-level pessimistic locks** in a deterministic order |
| "Login" just returned an email in a loop | Real Spring Security sessions; per-user account isolation |

<br/>

## ✨ Features

- 🔐 **Register / sign in** with Spring Security sessions; BCrypt-hashed credentials
- 🏦 **Multiple accounts** per user, each with its own security PIN
- 💸 **Deposit, withdraw, transfer** — transfers are atomic and row-locked
- 📜 **Double-entry ledger** — every movement recorded with a running balance and a shared reference linking transfer legs
- 🧾 **Paginated statement** per account with credit/debit columns
- 👤 **Ownership isolation** — you can only see and touch your own accounts (enforced server-side)
- 🌗 **Light / Dark / System** theme, persisted, applied pre-paint
- 🛡️ Hardened headers (CSP, `X-Frame-Options`, `nosniff`, `Referrer-Policy`), HTTP-only SameSite session cookie
- 📘 Swagger UI · 💓 Actuator health · 🧪 Integration tests · 🔁 GitHub Actions CI

<br/>

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| **Runtime** | Java 21 |
| **Framework** | Spring Boot 3.3.4 (Web, Data JPA, Validation, **Security**, Actuator) |
| **Database** | PostgreSQL (prod, via Neon) · H2 in-memory (dev) |
| **Auth** | Spring Security — session-based, BCrypt |
| **Docs** | springdoc-openapi (Swagger UI) |
| **Frontend** | Vanilla HTML / CSS / JavaScript — no build step |
| **Deploy** | Docker → Render · Neon Postgres · GitHub Actions CI |

<br/>

## 🏗️ Architecture

```
  HTTP  →  Security filter (session)  →  Controller  →  Service  →  Repository  →  Entity
                                              ↑            ↑
                                            DTOs     locking · ledger · money rules
```

**Key design decisions**

- 💰 **`BigDecimal` everywhere for money** — the original used `double`, which is unsafe for currency.
- 🔒 **Transfers lock both accounts** with `PESSIMISTIC_WRITE`, acquired lowest-account-number first so two opposing transfers can't deadlock — and can't corrupt a balance via a stale read.
- 🧾 **Double-entry ledger** — a transfer writes a `DEBIT` leg and a `CREDIT` leg under one shared reference; each entry stores the balance *after* it, so the statement always reconciles.
- 🧍 **Per-user isolation** — every account operation re-resolves the current user from the session and checks ownership; the account number in the URL is never trusted alone.
- 🔑 **No secrets in source** — credentials come from environment variables.

<br/>

## 🚀 Getting Started

**JDK 21** required. Maven ships via the wrapper; dev uses an in-memory database.

```bash
git clone <your-repo-url>
cd banking-system

./mvnw spring-boot:run      # macOS / Linux
.\mvnw spring-boot:run       # Windows PowerShell
```

Open `http://localhost:8080/` and sign in with the seeded demo login:

> **demo@bank.app** · password **demo12345** (two accounts, PIN **1234**)

| URL | What you'll find |
|---|---|
| `/` | 🏦 The banking dashboard |
| `/login.html` | 🔐 Sign in / create account |
| `/swagger-ui.html` | 📘 API documentation |
| `/actuator/health` | 💓 Health check |

<br/>

## ⚙️ Configuration

Production is fully environment-driven — see `.env.example`:

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` to use PostgreSQL |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>/<db>?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | DB credentials |
| `PORT` | HTTP port (auto-provided by Render) |

<br/>

## 📡 API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Create a user |
| `POST` | `/api/auth/login` | Start a session |
| `POST` | `/api/auth/logout` | End the session |
| `GET` | `/api/auth/me` | Current user |
| `GET` | `/api/accounts` | List **your** accounts |
| `POST` | `/api/accounts` | Open an account |
| `GET` | `/api/accounts/{number}` | Account detail (owner only) |
| `GET` | `/api/accounts/{number}/history` | Paginated statement |
| `POST` | `/api/accounts/credit` | Deposit |
| `POST` | `/api/accounts/debit` | Withdraw |
| `POST` | `/api/accounts/transfer` | Transfer between accounts |

All `/api/accounts/**` routes require an authenticated session. Full docs at `/swagger-ui.html`.

<br/>

## 🧪 Testing

```bash
./mvnw verify
```

`BankingIntegrationTest` covers register/login, account opening, a transfer that moves money **and** writes both ledger legs, insufficient-balance and wrong-PIN rejections, unauthenticated access (401), and cross-user access being forbidden (403). Runs in CI on every push.

<br/>

## ☁️ Deployment

Single Docker container on **Render**, backed by **Neon** PostgreSQL. Set the env vars above, health check `/actuator/health`, and deploy.

<br/>

## 👤 Author

**Arman Ahemad Khan** · [arman-portfolio.online](https://arman-portfolio.online) · [GitHub](https://github.com/arman080325)

## 📄 License

MIT
