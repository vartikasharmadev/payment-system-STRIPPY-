const DASH = "—";

const inr = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 2
});

export function formatInr(value) {
  const n = Number(value);
  if (value == null || value === "" || Number.isNaN(n)) return DASH;
  return inr.format(n);
}

export function formatTime() {
  return new Date().toLocaleTimeString();
}

export function statusFromPayment(payment) {
  if (!payment || typeof payment !== "object") return "";
  const s = payment.status;
  if (s == null || s === "") return "";
  return String(s).trim();
}

export function badgeClass(label) {
  if (!label || label === DASH) return "status-badge--muted";
  const u = String(label).toUpperCase();
  if (u === "SUCCESS") return "status-badge--success";
  if (u === "FAILED") return "status-badge--failed";
  return "status-badge--pending";
}
