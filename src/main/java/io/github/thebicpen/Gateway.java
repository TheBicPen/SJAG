package io.github.thebicpen;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Gateway implements HttpHandler {
  static HttpClient client = HttpClient.newBuilder().build();
  private Map<String, Endpoint> services;
  private int defaultPort;

  public Gateway(Map<String, Endpoint> services, int defaultPort) {
    this.services = services;
  }

  @Override
  public void handle(HttpExchange r) throws IOException {
    URI requestURI = r.getRequestURI();
    System.out.println("Gateway request: " + requestURI + " " + r.getRequestMethod());
    String requestPath = requestURI.getPath();
    String[] uriParts = requestPath.split("/");
    if (uriParts.length < 2) {
      this.sendError(r, 400, "No endpoint specified.");
      return;
    }
    Endpoint endpoint = services.getOrDefault(uriParts[1], new Endpoint(defaultPort, uriParts[1]));
    if (endpoint.hostname().equals(null)) {
      this.sendError(r, 400, "Invalid endpoint.");
      return;
    }
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(
              new URI(
                  "http",
                  null,
                  endpoint.hostname(),
                  endpoint.port(),
                  requestPath.substring(requestPath.indexOf("/") + 1),
                  requestURI.getQuery(),
                  requestURI.getFragment()))
          .method(r.getRequestMethod(), BodyPublishers.ofInputStream(() -> r.getRequestBody()))
          .build();
      CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, BodyHandlers.ofString());

      response
          .thenApply(HttpResponse::statusCode)
          .thenAcceptBothAsync(
              response.thenApply(HttpResponse::body),
              (code, body) -> {
                try {
                  r.sendResponseHeaders(code, 0);
                  OutputStream os = r.getResponseBody();
                  os.write(body.getBytes());
                  os.close();
                } catch (IOException e) {
                  r.close();
                }
              });
    } catch (URISyntaxException e) {
      e.printStackTrace();
      this.sendError(r, 400, "Unable to build URI to forward request to.");
    }
  }

  private void sendError(HttpExchange r, int code, String error) {
    try {
      r.sendResponseHeaders(code, 0);
      r.getResponseBody().write(("Error: " + error).getBytes());
    } catch (IOException e) {
      System.err.println("Unable to send error response");
      e.printStackTrace();
    } finally {
      r.close();
    }
  }
}
