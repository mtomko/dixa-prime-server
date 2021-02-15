# dixa-prime-service
A simple service and proxy that demonstrate the streaming features of gRPC. This project contains
five modules: `protobuf`, `server`, `client`, `proxy`, and `test`. It generates streams of prime 
numbers up to a provided maximum value.

## Components
### Protobuf
This contains only a single gRPC IDL file it generates code for use by downstream projects

### Server
A simple server implementation that uses the sieve of Eratosthenes. It utilizes `fs2` to generate
an "infinite" stream of primes. The server project provides a simple server app as well as library
code for embedding a server.

### Client
A simple client implementation plus a simple client app.

### Proxy
A proxy that uses the client and the same protocol as the server, but relies on an upstream sever
to generate primes. The proxy project contains both a proxy as library code as well as a simple
proxy app.

### Test
Integration tests for the client, server, and proxy are implemented here

## Running the code
You can use the `sbt-revolver` plugin to start the server and proxy:

```shell
sbt> reStart
```

then run the client:

```shell
sbt> client/run
```

This will print a number of primes to the console. You can shut down the server and proxy with:

```shell
sbt> reStop
```

