const environment = import.meta.env ?? {};
const apiBaseUrl = environment.VITE_API_BASE_URL?.trim().replace(/\/$/, "") || "";
const REQUEST_TIMEOUT_MS = 10000;

export function parseWaitlistStatus(body) {
  const status = body?.data?.status;
  if (status !== "SUBSCRIBED" && status !== "ALREADY_SUBSCRIBED") {
    throw new WaitlistRequestError("response");
  }
  return status;
}

export async function subscribeToLaunchWaitlist({ email, privacyConsent, ageConfirmed }) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  let response;
  try {
    response = await fetch(`${apiBaseUrl}/api/v1/waitlist`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, privacyConsent, ageConfirmed }),
      signal: controller.signal,
    });
  } catch (error) {
    throw new WaitlistRequestError(error?.name === "AbortError" ? "timeout" : "network");
  } finally {
    clearTimeout(timeoutId);
  }

  if (!response.ok) {
    throw new WaitlistRequestError(response.status >= 500 ? "server" : "request");
  }

  let body;
  try {
    body = await response.json();
  } catch {
    throw new WaitlistRequestError("response");
  }
  return parseWaitlistStatus(body);
}

export class WaitlistRequestError extends Error {
  constructor(reason) {
    super("Launch waitlist request failed");
    this.name = "WaitlistRequestError";
    this.reason = reason;
  }
}
