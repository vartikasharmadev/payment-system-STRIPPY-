# STRIPPY

Small demo app: React UI + Spring Boot API, Stripe Checkout in INR. You enter an amount, get redirected to Stripe’s hosted page, then land back on the API success screen (which sends you to the SPA after a few seconds).

## What you need

- Java 17+, Maven, Node 18+
- MySQL with a database named `payment_system` (schema is created/updated by JPA)
- A Stripe test secret key and webhook signing secret

## Run the API

From the repo root:

```bash
mvn spring-boot:run
```

Put secrets in the environment (recommended) or in `src/main/resources/application.properties`:

| Variable | What it’s for |
|----------|----------------|
| `STRIPE_SECRET_KEY` | Stripe API |
| `STRIPE_WEBHOOK_SECRET` | Verifies webhook payloads |
| `MYSQL_PASSWORD` | DB password (defaults to `root` if unset) |
| `APP_PUBLIC_URL` | Public URL of this API — Stripe uses it for success/cancel redirects (default `http://localhost:8080`) |
| `APP_FRONTEND_URL` | Where the success page should send the user after checkout (default `http://localhost:5173/`) |
| `APP_CORS_PATTERNS` | Allowed browser origins for the API |

## Run the UI

```bash
cd frontend
npm install
npm run dev
```

Vite serves on port 5173 and proxies `/payment` to `http://localhost:8080`, so you usually don’t need to configure the API URL locally.

If the frontend is hosted somewhere else, set `VITE_API_BASE_URL` to your API origin (see `.env.example`). You can also set `VITE_HOME_URL` if you want the app to redirect somewhere specific after a successful payment when it’s polling in the background (defaults to `/`).
