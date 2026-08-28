export default {
  async fetch(request, env) {
    const response = await env.ASSETS.fetch(request);
    const { pathname } = new URL(request.url);
    const acceptsHtml = request.headers.get("accept")?.includes("text/html");
    const isApiRequest = pathname === "/api" || pathname.startsWith("/api/");

    if (
      response.status !== 404
      || !acceptsHtml
      || isApiRequest
      || !["GET", "HEAD"].includes(request.method)
    ) {
      return response;
    }

    const indexUrl = new URL(request.url);
    indexUrl.pathname = "/index.html";
    indexUrl.search = "";
    return env.ASSETS.fetch(new Request(indexUrl, request));
  },
};
