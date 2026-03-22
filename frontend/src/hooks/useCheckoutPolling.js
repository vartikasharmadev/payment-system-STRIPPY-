import { useEffect, useRef } from "react";
import { getPayment } from "../api/paymentApi";
import { formatInr, formatTime, statusFromPayment } from "../utils/paymentHelpers";

const HOME_URL = (import.meta.env.VITE_HOME_URL || "/").trim() || "/";
const REDIRECT_MS = 4000;

export function useCheckoutPolling(pollingPaymentId, setState, setPollingPaymentId) {
  const lastStatus = useRef(null);
  const redirectTimerRef = useRef(null);

  useEffect(() => {
    if (pollingPaymentId == null) return;

    lastStatus.current = null;
    if (redirectTimerRef.current) {
      clearTimeout(redirectTimerRef.current);
      redirectTimerRef.current = null;
    }

    const id = pollingPaymentId;
    let intervalTimer = null;
    let busy = false;

    async function tick() {
      if (busy) return;
      busy = true;
      try {
        const row = await getPayment(id);
        const st = statusFromPayment(row).toUpperCase();

        setState((prev) => {
          const seen = lastStatus.current;
          let ok = prev.successCount;
          let bad = prev.failCount;
          if (st === "SUCCESS" && seen !== "SUCCESS") ok++;
          if (st === "FAILED" && seen !== "FAILED") bad++;
          lastStatus.current = st || seen;
          return { ...prev, checkoutPayment: row, successCount: ok, failCount: bad };
        });

        if (st === "SUCCESS" || st === "FAILED") {
          if (intervalTimer) clearInterval(intervalTimer);
          intervalTimer = null;
          setPollingPaymentId(null);
          const amt = formatInr(row?.amount);
          setState((prev) => ({
            ...prev,
            logs: [
              `${formatTime()} — Payment ${st.toLowerCase()} · ${amt}`,
              ...prev.logs
            ]
          }));

          if (st === "SUCCESS") {
            redirectTimerRef.current = setTimeout(() => {
              window.location.assign(HOME_URL);
            }, REDIRECT_MS);
          }
        }
      } catch {
        // transient network errors while polling
      } finally {
        busy = false;
      }
    }

    intervalTimer = setInterval(tick, 2000);
    tick();

    return () => {
      if (intervalTimer) clearInterval(intervalTimer);
      if (redirectTimerRef.current) {
        clearTimeout(redirectTimerRef.current);
        redirectTimerRef.current = null;
      }
    };
  }, [pollingPaymentId, setState, setPollingPaymentId]);
}
