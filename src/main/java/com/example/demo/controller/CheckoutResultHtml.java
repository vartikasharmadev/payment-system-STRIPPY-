package com.example.demo.controller;

final class CheckoutResultHtml {

    private CheckoutResultHtml() {}

    private static final String SUCCESS_PAGE_TEMPLATE = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="utf-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>Payment successful — STRIPPY</title>
              <link rel="preconnect" href="https://fonts.googleapis.com"/>
              <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
              <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;600;700&display=swap" rel="stylesheet"/>
              <style>
                :root {
                  --bg1: #fffbeb;
                  --bg2: #ffedd5;
                  --accent: #ea580c;
                  --gold: #fbbf24;
                  --ink: #431407;
                  --muted: #9a3412;
                }
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                  min-height: 100vh;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  padding: 2rem;
                  font-family: "Outfit", system-ui, sans-serif;
                  background: radial-gradient(120% 90% at 50% 0%, #fde68a 0%, var(--bg2) 40%, var(--bg1) 100%);
                  color: var(--ink);
                }
                .card {
                  text-align: center;
                  max-width: 36rem;
                  animation: rise 0.85s cubic-bezier(0.22, 1, 0.36, 1) both;
                }
                @keyframes rise {
                  from { opacity: 0; transform: translateY(28px) scale(0.98); }
                  to { opacity: 1; transform: translateY(0) scale(1); }
                }
                .mark {
                  width: 5.5rem;
                  height: 5.5rem;
                  margin: 0 auto 1.75rem;
                  border-radius: 50%;
                  background: linear-gradient(180deg, #fef9c3 0%, #fde047 100%);
                  border: 2px solid #facc15;
                  display: grid;
                  place-items: center;
                  font-size: 2.85rem;
                  line-height: 1;
                  box-shadow: 0 12px 36px rgba(234, 88, 12, 0.25);
                }
                h1 {
                  font-weight: 700;
                  font-size: clamp(1.75rem, 6vw, 2.5rem);
                  line-height: 1.15;
                  margin-bottom: 0.5rem;
                  background: linear-gradient(115deg, #b45309 0%, var(--accent) 45%, #fb923c 100%);
                  -webkit-background-clip: text;
                  background-clip: text;
                  color: transparent;
                }
                .sub {
                  font-size: clamp(1rem, 2.5vw, 1.15rem);
                  font-weight: 400;
                  color: var(--muted);
                  line-height: 1.55;
                  margin-top: 1rem;
                }
                .countdown {
                  margin-top: 1.5rem;
                  font-size: 0.95rem;
                  font-weight: 600;
                  color: var(--accent);
                }
                .brand {
                  margin-top: 2rem;
                  font-weight: 600;
                  font-size: 0.9rem;
                  letter-spacing: 0.12em;
                  text-transform: uppercase;
                  color: var(--accent);
                  opacity: 0.9;
                }
              </style>
            </head>
            <body>
              <div class="card">
                <div class="mark" aria-hidden="true">&#x2705;</div>
                <h1>Payment successful</h1>
                <p class="sub">Thank you. Your payment went through.</p>
                <p class="countdown" id="redirect-msg">Taking you home in <span id="sec">4</span>s…</p>
                <p class="brand">STRIPPY</p>
              </div>
              <script>
                (function () {
                  var home = "__FRONTEND_HOME__";
                  var left = 4;
                  var secEl = document.getElementById("sec");
                  var iv = setInterval(function () {
                    left--;
                    if (secEl) secEl.textContent = String(Math.max(0, left));
                    if (left <= 0) clearInterval(iv);
                  }, 1000);
                  setTimeout(function () {
                    window.location.href = home;
                  }, 4000);
                })();
              </script>
            </body>
            </html>
            """;

    static final String CANCEL_PAGE = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="utf-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>Payment cancelled — STRIPPY</title>
              <link rel="preconnect" href="https://fonts.googleapis.com"/>
              <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
              <link href="https://fonts.googleapis.com/css2?family=DM+Serif+Display:ital@0;1&family=Outfit:wght@300;500;600&display=swap" rel="stylesheet"/>
              <style>
                :root {
                  --bg1: #fafaf9;
                  --bg2: #f5f5f4;
                  --ink: #292524;
                  --muted: #78716c;
                  --accent: #a8a29e;
                }
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                  min-height: 100vh;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  padding: 2rem;
                  font-family: "Outfit", system-ui, sans-serif;
                  background: radial-gradient(120% 80% at 50% 0%, var(--bg2) 0%, var(--bg1) 50%, #fff 100%);
                  color: var(--ink);
                }
                .card {
                  text-align: center;
                  max-width: 32rem;
                  animation: rise 0.75s ease both;
                }
                @keyframes rise {
                  from { opacity: 0; transform: translateY(20px); }
                  to { opacity: 1; transform: translateY(0); }
                }
                .mark {
                  width: 4.5rem;
                  height: 4.5rem;
                  margin: 0 auto 1.5rem;
                  border-radius: 50%;
                  background: linear-gradient(145deg, #e7e5e4 0%, #d6d3d1 100%);
                  display: grid;
                  place-items: center;
                  font-size: 2rem;
                  color: var(--muted);
                  box-shadow: inset 0 1px 0 rgba(255,255,255,0.8);
                }
                h1 {
                  font-family: "DM Serif Display", Georgia, serif;
                  font-weight: 400;
                  font-size: clamp(2.25rem, 8vw, 3.25rem);
                  line-height: 1.1;
                  letter-spacing: -0.02em;
                  color: var(--ink);
                  margin-bottom: 0.5rem;
                }
                .sub {
                  font-size: clamp(1rem, 2.5vw, 1.15rem);
                  font-weight: 300;
                  color: var(--muted);
                  line-height: 1.55;
                  margin-top: 0.75rem;
                }
                .brand {
                  margin-top: 2rem;
                  font-weight: 600;
                  font-size: 0.85rem;
                  letter-spacing: 0.1em;
                  text-transform: uppercase;
                  color: var(--accent);
                }
              </style>
            </head>
            <body>
              <div class="card">
                <div class="mark" aria-hidden="true">—</div>
                <h1>Payment cancelled</h1>
                <p class="sub">No charge was made. Close this tab whenever you like — you can try again from the app.</p>
                <p class="brand">STRIPPY</p>
              </div>
            </body>
            </html>
            """;

    static String successPage(String frontendHome) {
        String safe = frontendHome == null || frontendHome.isBlank()
                ? "http://localhost:5173/"
                : frontendHome.trim();
        if (!safe.endsWith("/")) {
            safe = safe + "/";
        }
        return SUCCESS_PAGE_TEMPLATE.replace("__FRONTEND_HOME__", escapeJsString(safe));
    }

    private static String escapeJsString(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r", "");
    }
}
