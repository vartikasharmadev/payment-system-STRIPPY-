const root = (import.meta.env.VITE_API_URL || "").replace(/\/$/, "");

async function callBackend(path, options = {}) {
  const method = options.method || "GET";

  const res = await fetch(`${root}${path}`, {
    method,
    ...options,
  });

  if (!res.ok) {
    const body = await res.text();
    throw new Error(body || `HTTP ${res.status}`);
  }

  const raw = await res.text();
  try {
    return JSON.parse(raw);
  } catch {
    return raw;
  }
}

export function createStripeCheckout(amount) {
  return callBackend(`/payment/checkout?amount=${encodeURIComponent(amount)}`, {
    method: "POST",
  });
}

export function getPayment(id) {
  return callBackend(`/payment/status/${id}`, {
    method: "GET",
  });
}
