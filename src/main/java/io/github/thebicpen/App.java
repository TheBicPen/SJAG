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
      Map<String, String> services = new HashMap<>();
      for (String arg : args) {
         String[] item = arg.split(",");
          if (item.length == 2) {
            services.put(item[0], item[1]);
            System.out.printf("Mapping %s -> %s\n", item[0], item[1]);
          }
      }
      server.createContext("/", new Gateway(services));
    }
    else {
      server.createContext("/", new Gateway());
    }
    server.start();
    System.out.printf("API Gateway server started on port %d...\n", PORT);
  }
}
