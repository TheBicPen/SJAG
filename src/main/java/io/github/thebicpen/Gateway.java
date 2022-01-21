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
    this.defaultPort = defaultPort;
  }

  @Override
  public void handle(HttpExchange r) throws IOException {
    URI requestURI = r.getRequestURI();
    System.out.println("Gateway request: " + requestURI + " " + r.getRequestMethod());
    String requestPath = requestURI.getPath();
    String[] uriParts = requestPath.split("/");
    if (uriParts.length < 2) {
      this.sendResponse(r, 400, "Error: No endpoint specified.");
      return;
    }
    Endpoint endpoint = services.getOrDefault(uriParts[1], new Endpoint(defaultPort, uriParts[1]));
    if (endpoint.hostname().equals(null)) {
      this.sendResponse(r, 400, "Error: Invalid endpoint.");
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
                  requestPath.substring(
                      requestPath.indexOf('/', 1) == -1 ? requestPath.length() : requestPath.indexOf('/', 1)),
                  requestURI.getQuery(),
                  requestURI.getFragment()))
          .method(r.getRequestMethod(), BodyPublishers.ofInputStream(() -> r.getRequestBody()))
          .build();
      CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, BodyHandlers.ofString());

      response
          .exceptionallyAsync(ex -> {
            sendResponse(r, 400, "Error: " + ex.getMessage());
            return null;
          })
          .thenApplyAsync(HttpResponse::statusCode)
          .thenAcceptBothAsync(
              response.thenApply(HttpResponse::body),
              (code, body) -> {
                sendResponse(r, code, body);
              });
    } catch (URISyntaxException e) {
      String error = "Error: Invalid URI: " + e.getMessage();
      System.err.println(error);
      this.sendResponse(r, 400, error);
    } catch (Exception e) {
      e.printStackTrace();
      String error = "Gateway internal error: " + e.getMessage();
      System.err.println(error);
      this.sendResponse(r, 500, error);
    }
  }

  private void sendResponse(HttpExchange r, int code, String response) {
    try {
      r.sendResponseHeaders(code, 0);
      r.getResponseBody().write(response.getBytes());
    } catch (IOException e) {
      System.err.println("Unable to send response.");
      e.printStackTrace();
    } finally {
      r.close();
    }
  }
}
