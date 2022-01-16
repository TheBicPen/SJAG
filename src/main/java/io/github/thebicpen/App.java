package io.github.thebicpen;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class App {
  static int IN_PORT = 8000;
  static int OUT_PORT = 8000;

  public static void main(String[] args) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", IN_PORT), 0);
    Map<String, Endpoint> services = new HashMap<>();
    if (args.length >= 2) {
      try {
        IN_PORT = Integer.parseInt(args[0]);
        OUT_PORT = Integer.parseInt(args[1]);
      } catch (NumberFormatException e) {
        System.err.printf("Unable to parse port numbers: %d, %d\n", args[0], args[1]);
        System.exit(1);
      }
      for (String arg : Arrays.copyOfRange(args, 2, args.length)) {
        String[] item = arg.split(",");
        if (item.length >= 2) {
          try {
            int port = item.length == 2 ? OUT_PORT : Integer.parseInt(item[2]);
            services.put(item[0], new Endpoint(port, item[1]));
            System.out.printf("Mapping %s -> %s (port %d)\n", item[0], item[1], port);
          } catch (NumberFormatException e) {
            System.err.println("Unable to parse port number: " + item[2]);
            System.exit(1);
          }
        }
      }
    }
    server.createContext("/", new Gateway(services, OUT_PORT));
    server.start();
    System.out.printf("Routing requests to port %d\n", OUT_PORT);
    System.out.printf("API Gateway listening on port %d...\n", IN_PORT);
  }
}
