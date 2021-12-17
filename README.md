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


By default, SJAG routes requests to a service whose hostname is the first URL component of the request/
For example, a request to `http://localhost:8000/myservice/endpoint/1` gets routed to `http://myservice:8000/endpoint/1`.

#### Arguments
The only command line arguments SJAG accepts are comma-separated mappings from URI component to hostname.
For example, running SJAG with the argument `foo,bar` will route `http://localhost:8000/foo/1` to `http://bar/1`.

#### Port
Currently, it always routes requests to the port `8000`. This can easily be changed in the code.
