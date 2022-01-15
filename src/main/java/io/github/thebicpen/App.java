package io.github.thebicpen;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class App {
  static int PORT = 8000;

  public static void main(String[] args) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
    if (args.length > 0) {
      Map<String, Endpoint> services = new HashMap<>();
      for (String arg : args) {
        String[] item = arg.split(",");
        if (item.length == 2) {
          services.put(item[0], new Endpoint(PORT, item[1]));
          System.out.printf("Mapping %s -> %s (port %i)\n", item[0], item[1], PORT);
        } else if (item.length == 3) {
          try {
            int port = Integer.parseInt(item[2]);
            services.put(item[0], new Endpoint(port, item[1]));
            System.out.printf("Mapping %s -> %s (port %i)\n", item[0], item[1], port);
          } catch (NumberFormatException e) {
            System.err.println("Unable to parse port number: " + item[2]);
          }
        }
      }
      server.createContext("/", new Gateway(services, PORT));
    } else {
      server.createContext("/", new Gateway(PORT));
    }
    server.start();
    System.out.printf("API Gateway server started on port %d...\n", PORT);
  }
}
