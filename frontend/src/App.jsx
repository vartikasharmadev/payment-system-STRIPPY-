import { useEffect, useMemo, useState } from "react";
import { createStripeCheckout, getPayment } from "./api/paymentApi";
import { useCheckoutPolling } from "./hooks/useCheckoutPolling";
import { badgeClass, formatInr, formatTime, statusFromPayment } from "./utils/paymentHelpers";

const SK_CHECKOUT_PAYMENT_ID = "strippy_checkout_payment_id";
const SK_ACTIVITY_LOGS = "strippy_activity_logs";

function readStoredLogs() {
  try {
    const raw = sessionStorage.getItem(SK_ACTIVITY_LOGS);
    if (!raw) return [];
    const arr = JSON.parse(raw);
    return Array.isArray(arr) ? arr.filter(Boolean) : [];
  } catch {
    return [];
  }
}

function prependStoredLog(line) {
  try {
    const next = [line, ...readStoredLogs()].slice(0, 45);
    sessionStorage.setItem(SK_ACTIVITY_LOGS, JSON.stringify(next));
  } catch {
    // sessionStorage unavailable
  }
}

function App() {
  const [amount, setAmount] = useState("100.00");
  const [state, setState] = useState({
    loading: false,
    error: null,
    checkoutPayment: null,
    logs: [],
    successCount: 0,
    failCount: 0
  });
  const [pollingPaymentId, setPollingPaymentId] = useState(null);
  const [theme, setTheme] = useState(() => {
    if (typeof window === "undefined") return "light";
    return localStorage.getItem("strippy-theme") || "light";
  });

  useCheckoutPolling(pollingPaymentId, setState, setPollingPaymentId);

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("strippy-theme", theme);
  }, [theme]);

  useEffect(() => {
    const rawId = sessionStorage.getItem(SK_CHECKOUT_PAYMENT_ID);
    if (!rawId) return;
    const pid = Number(rawId);
    if (!Number.isFinite(pid)) return;

    let cancelled = false;

    (async () => {
      try {
        const row = await getPayment(pid);
        if (cancelled || !row) return;

        const st = String(statusFromPayment(row) || "").toUpperCase();
        const storedLogs = readStoredLogs();
        const restoreLine = `${formatTime()} — Welcome back · ${st || "unknown"} · ${formatInr(row.amount)}`;

        setState((prev) => {
          if (prev.checkoutPayment?.id === pid) {
            return prev;
          }
          return {
            ...prev,
            checkoutPayment: row,
            logs: [restoreLine, ...storedLogs, ...prev.logs].filter(Boolean).slice(0, 50),
            successCount: st === "SUCCESS" ? prev.successCount + 1 : prev.successCount
          };
        });

        if (st !== "SUCCESS" && st !== "FAILED") {
          setPollingPaymentId(pid);
        }
      } catch {
        if (!cancelled) {
          setState((prev) => ({
            ...prev,
            logs: [
              `${formatTime()} — Could not reload checkout status (try again)`,
              ...readStoredLogs(),
              ...prev.logs
            ].slice(0, 50)
          }));
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  const amountDisplay = useMemo(() => formatInr(Number(amount) || 0), [amount]);

  function pushLog(msg) {
    setState((prev) => ({ ...prev, logs: [`${formatTime()} — ${msg}`, ...prev.logs] }));
  }

  async function handleStripeCheckout() {
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const res = await createStripeCheckout(amount);
      const url = res?.url;
      if (!url) throw new Error("No redirect URL from server");
      const id = res?.paymentId;
      if (id == null) throw new Error("No payment id from server");

      sessionStorage.setItem(SK_CHECKOUT_PAYMENT_ID, String(id));
      const openLine = `${formatTime()} — Opening Checkout · ${formatInr(Number(amount) || 0)}`;
      prependStoredLog(openLine);

      setPollingPaymentId(id);
      window.location.assign(url);
    } catch (e) {
      setState((prev) => ({ ...prev, error: String(e.message || e), loading: false }));
      pushLog(`Checkout failed (${formatInr(Number(amount) || 0)}): ${e.message || e}`);
    }
  }

  const checkoutStatus = state.checkoutPayment ? statusFromPayment(state.checkoutPayment) : "";

  return (
    <div className="stripe-app">
      <header className="stripe-nav">
        <div className="stripe-nav__inner stripe-nav__header-grid">
          <div className="nav-side nav-side--left" aria-hidden="true" />
          <div className="brand-lockup-center">
            <h1 className="brand__logo-title">STRIPPY</h1>
            <p className="brand__tag brand__tag--logo">Honey-gold payments · INR</p>
          </div>
          <div className="nav-side nav-side--right">
            <button
              type="button"
              className="theme-toggle"
              onClick={() => setTheme((t) => (t === "light" ? "dark" : "light"))}
              aria-label="Toggle theme"
            >
              {theme === "light" ? "🌙" : "☀️"}
            </button>
          </div>
        </div>
      </header>

      <main className="stripe-main layout">
        <section className="card card--hero">
          <div className="hero-grid">
            <div className="hero-copy">
              <p className="eyebrow">Payments demo</p>
              <h2 className="hero-title">Amount &amp; Checkout</h2>
              <p className="lede">
                Set an amount in INR and use <strong>Checkout</strong> for the secure hosted payment page.
              </p>
            </div>
            <div className="hero-controls">
              <div className="field-row">
                <label className="field">
                  <span>Amount (INR)</span>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                  />
                </label>
                <div className="amount-meta">
                  <div className="pill pill--muted pill--amount">{amountDisplay}</div>
                </div>
              </div>
              <div className="actions actions--hero">
                <button
                  type="button"
                  className="btn btn--checkout"
                  onClick={handleStripeCheckout}
                  disabled={state.loading}
                  aria-label="Open Stripe Checkout hosted payment page"
                >
                  <span className="btn--checkout__shine" aria-hidden />
                  <span className="btn--checkout__inner">
                    <span className="btn--checkout__label">Checkout</span>
                    <span className="btn--checkout__hint">Secure hosted page · cards &amp; wallets</span>
                  </span>
                  <span className="btn--checkout__arrow" aria-hidden>
                    →
                  </span>
                </button>
              </div>
            </div>
          </div>
          {state.error && <div className="alert">{state.error}</div>}
        </section>

        <section className="card">
          <div className="card__head">
            <h2>Checkout status</h2>
            {pollingPaymentId != null && <span className="pill pill--live">Polling…</span>}
          </div>
          {!state.checkoutPayment && (
            <p className="muted">
              After you pay on Stripe and return here, status updates automatically. On success you may be
              sent home after 4 seconds.
            </p>
          )}
          {state.checkoutPayment && (
            <dl className="kv">
              <div>
                <dt>Amount</dt>
                <dd>{formatInr(state.checkoutPayment.amount)}</dd>
              </div>
              <div>
                <dt>Status</dt>
                <dd>
                  <span className={`status-badge ${badgeClass(checkoutStatus)}`}>
                    {checkoutStatus || "—"}
                  </span>
                </dd>
              </div>
            </dl>
          )}
        </section>

        <section className="card">
          <div className="card__head">
            <h2>Payment logs</h2>
            <div className="stats">
              <span className="pill pill--ok">OK {state.successCount}</span>
              <span className="pill pill--bad">Fail {state.failCount}</span>
            </div>
          </div>
          <ul className="log">
            {state.logs.length === 0 && <li className="muted">No events yet.</li>}
            {state.logs.map((line, i) => (
              <li key={i}>{line}</li>
            ))}
          </ul>
        </section>
      </main>

      <footer className="stripe-footer-wrap">
        <div className="stripe-nav__inner footer">
          <span>
            <strong>STRIPPY</strong> · Spring Boot · React · Stripe Checkout
          </span>
        </div>
      </footer>
    </div>
  );
}

export default App;
