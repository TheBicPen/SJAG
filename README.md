# SJAG
Simple Java Application Gateway


## Usage
Run `docker run sjag` to execute the container on its own.
Or, if this program is in a folder called `SJAG`, you can add it to a docker-compose.yml file like so: 
```
version: "3.8"
services:
  apigateway:
    container_name: apigateway
    build:
      context: ./SJAG
      dockerfile: Dockerfile
    ports:
      - 8000:8000
```


By default, SJAG routes requests to a service whose hostname is the first URL component of the request.
For example, a request to `http://localhost:8000/myservice/endpoint/1` gets routed to `http://myservice:8000/endpoint/1`.

### Arguments
SJAG accepts either no command line arguments, or 2 or more.
If 2 or more are given, the first will be interpreted as the port to listen on (see below).
The second will be interpreted as the default port to route requests to, but this can be overwritten by custom routes.

#### Custom routes
If 2 or more arguments are provided, the remaining arguments are treated as comma-separated mappings from URI component to hostname and port.
For example, running SJAG with the arguments `8000`, `8000`, `foo,bar,4321` will route `http://localhost:8000/foo/endpoint` to `http://bar:4321/endpoint`.

#### Port
The server listens on port 8000 by default. 
To listen on a different port, specify both the listen port and default routing port, 
like so: `docker run sjag:latest 9000 2000`. This will listen on port `9000` and route requests to port `2000` unless otherwise specified.
