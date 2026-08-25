const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim().replace(/\/$/, "") || "";

export async function subscribeToLaunchWaitlist({ email, privacyConsent, ageConfirmed }) {
  let response;
  try {
    response = await fetch(`${apiBaseUrl}/api/v1/waitlist`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, privacyConsent, ageConfirmed }),
    });
  } catch {
    throw new WaitlistRequestError("network");
  }

  if (!response.ok) {
    throw new WaitlistRequestError(response.status >= 500 ? "server" : "request");
  }

  const body = await response.json();
  const status = body?.data?.status;
  if (status !== "SUBSCRIBED" && status !== "ALREADY_SUBSCRIBED") {
    throw new WaitlistRequestError("response");
  }
  return status;
}

export class WaitlistRequestError extends Error {
  constructor(reason) {
    super("Launch waitlist request failed");
    this.name = "WaitlistRequestError";
    this.reason = reason;
  }
}
