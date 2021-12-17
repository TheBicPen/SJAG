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
  private Map<String, String> services;
  private boolean autoRoute = false;

  public Gateway(Map<String, String> services) {
    this.services = services;
    this.autoRoute = false;
  }

  public Gateway() {
    this.autoRoute = true;
  }

  @Override
  public void handle(HttpExchange r) throws IOException {
    URI requestURI = r.getRequestURI();
    System.out.println("Gateway request: " + requestURI + " " + r.getRequestMethod());
    String requestPath = requestURI.getPath();
    String[] uriParts = requestPath.split("/");
    if (uriParts.length < 2) {
      this.sendError(r, 400, "no endpoint specified");
      return;
    }
    String host = this.autoRoute ? uriParts[1] : services.getOrDefault(null, uriParts[1]);
    if (host.equals(null)) {
      this.sendError(r, 400, "invalid endpoint");
      return;
    }
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(
              new URI(
                  "http",
                  null,
                  host,
                  8000,
                  requestURI.getRawPath(),
                  requestURI.getRawQuery(),
                  requestURI.getRawFragment()))
          .method(r.getRequestMethod(), BodyPublishers.ofInputStream(() -> r.getRequestBody()))
          .header("Request-Timeout", "5") // 5 second timeout
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
      this.sendError(r, 400);
    } catch (Exception e) {
      e.printStackTrace();
      this.sendError(r, 500);
    }
  }

  private void sendError(HttpExchange r, int code) {
    try {
      r.sendResponseHeaders(code, 0);

    } catch (IOException e) {
      System.err.println("Unable to send error response");
      e.printStackTrace();
    } finally {
      r.close();
    }
  }

  private void sendError(HttpExchange r, int code, String error) {
    try {
      r.sendResponseHeaders(code, 0);
      r.getResponseBody().write(error.getBytes());
    } catch (IOException e) {
      System.err.println("Unable to send error response");
      e.printStackTrace();
    } finally {
      r.close();
    }
  }
}