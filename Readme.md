# JQuic
![logo](logo.png) <br>
Quic in Java using [lsquic](https://github.com/litespeedtech/lsquic).
* Http Servers
* Http Clients
* Proxies
* Http Proxies

# TL;DR

## What JQuic is
A Tool that allows to quickly create both, http agnostic and generic quic clients, servers and proxies using Java or JSON (http proxies only) for linux.

## What JQuic is not
Optimized for speed: This tool is not intended to be used for http servers in production environments but rather for proxies / servers in a test environment. Thus, whilst the underlying library (`lsquic`) is fast, this implementation is not. <br>
Cross Platform: Although JQuic is mostly developed in Java and `lsquic` supports multiple platforms, cross platform support was a non goal, so JQuic only works on linux as of now (although multi-platform support might be added in the future. Feel free to open Pull requests ;D)

## Installation
* requires Java 14

```bash
git clone https://github.com/LorenzHetterich/JQuic.git
cd JQuic
./gradlew release
cd run
./jquic tests
```

## Usage as Java Library
After running `./gradlew release`, you can find a stand-alone jar file in `run/jquic.jar` that can be used as Java library. You will also find a source jar in `run/jquic-sources.jar`

## Java configuration
Allows running your Java code without worrying about classpath etc.

```bash
./jquic -t java -f <path to java file>
```
e.g. 

```bash
./jquic -t java -f test/cli/java/LogPlaintext.java
```
* File contains one Java class with same name as file
* File will be compiled and added to the classpath (you have full access to all Java classes)
* static method `main(String[])` will be called if found

### Example files
* `test/cli/java/HttpServer.java`: Http server with some example endpoints
* `test/cli/java/LogPlaintext.java`: Http Proxy that logs all requests and responses to `log.txt`

## Http Proxy (Json configuration)
Allows running a http proxy configurable using `JSON` (more information in section `Json http configuration`)

```bash
./jquic -t json_http -f <json configuration> -c <certificate file> -k <key file> -sp <port>
```
e.g. 

```bash
./jquic -t json_http -f ../test/cli/json_http/modify_requests.json -c ../testcert/cert.crt -k ../testcert/key.key -sp 4001
```

### Example files
* `test/cli/json_http/modify_requests.json`: modifies some requests
* `test/cli/json_http/modify_responses.json`: modifies some responses
* `test/cli/json_http/replace_nothing.json`: does not modify anything

## Http Client
This is not yet implemented into the command line (as the priority is on the proxy). <br>
Stay tuned!


# The long description
JQuic mainly aims to provide a MITM Quic proxy that can modify content at will. <br>
The Proxy can be controlled by command-line, or be used as a Java library. <br>
The library provides a high level Proxy implementation using callbacks or annotations for modifying requests. <br>
In addition to this, the library also provides low-level access to the `lsquic` library. <br>
To redirect traffic, iptables and a special user (`proxy_user`) are used.

## Gradle Tasks
To make JQuic more portable and easier to work with, we decided to use gradle and provide some tasks for the most important operations. <br>
All our custom tasks belong to the group `application` namely:
* `buildLibrary`: compiles native user code (found in native). The user code is currently used to speed up reading and writing from / to quic streams.
* `release`: compiles the jquic binary, compiles native user code, builds the jar file and source jar file and puts everything into the `run` folder. Note that using the proxy only works if your working directory is the `run` folder (we are currently thinking about how to fix that). The jar file can be used as a normal java library (Linux x86-64 only).
* `gradleRun`: build native usercode and runs java main class `GradleMain` (`jquic.main`). Note that this is a special main class used only when running from gradle. This class may be used for some quick testing (no pun intended).
* `runProxy`: runs a terminal as the user proxy_user. This user forwards all UDP traffic to port 443 to 127.0.0.1:4000. Note that this only works if you run `IPTables/create_user.sh` before, to create and set up the proxy_user. Using this gradle task is not recommended, to run a terminal as proxy_user, you can also run the `release` task and then do `./jquic terminal` (in the `run` directory).
* `runTests`: compiles the jquic binary, compiles native user code, builds the jar file and runs some tests. Tests can also be executed by the jquic using `./jquic tests` (in the `run` directory)
* `manPage`: builds the manpage (`run/manpage`) from markdown (`src/main/java/manpage.md`)

## Why lsquic?
We choose lsquic as QUIC library, as it supports both, client and server code, is up to date since we started implementing the project with the latest drafts shortly after release and comes with integrated HTTP support. This allows easy handling of HTTP headers, which doesn't need to be done manually. <br>
With this feature set, `lsquic` is unrivaled amongst quic libraries (as far as we know). <br>
Some large scale projects and applications use other specific libraries. `curl` for instance uses `quiche`, which support IETF draft 29 (whilst `lsquic` currently supports all versions up to IETF draft 34, and makes it easy to switch version, which makes it perfect for a test environment). <br>
As `lsquic` is written in C, but we wanted to provide an easy to modify high level Java implementation for clients, servers and proxies, we needed to implement bindings first. <br>
However, before we go into more details about bindings and the Java code, let's checkout what JQuic can do.

## Commandline interface
Different types of configurations are supported. To select the configuration type, `-t <type>` is used. <br>
In addition the following arguments can be provided: <br>
* `-l <level>` (optional, default INFO): log level for JQuic loggers.
* `-L <level>` (optinal, default NONE): log level for lsquic library.
Additional arguments depend on the type of configuration used. <br>
Supported types include:

### Java configuration (`java`)
This configuration type takes as additional argument an input Java file (`-f <file>`). <br>
The file must contain a single Java class with the same name as the file (excluding '.java'). <br>
The Java file will be compiled with the library in the classpath and the resulting class will be loaded into the classpath. <br>
If this succeeds, methods matching the signature `main(String[])` will be called with the command-line arguments as argument. <br>
An Example proxy to log plain requests can be found in `test/cli/java/LogPlaintext.java`. <br>
To execute this example (from the `run` directory): <br>
`./jquic -t java -f ../test/cli/java/LogPlaintext.java`

### Json http configuration (`json_http`)
This configuration allows to define a http proxy using a `JSON` file. <br>
Arguments:
 * `-f <file>`: input file. More information below
 * `-c <file>`: certificate file
 * `-k <file>`: key file for certificate
 * `-v <version>` (optional, default h3-34): quic version to use
 * `-dp <port>` (optional, default 443): default port for proxy to server requests
 * `-sp <port>` (optional, default 4001): port of proxy server
 
It takes as additional argument an input Json file (`-f <file>`). <br>
The file must contain a single Json object with two arrays containing all rules:

```
{
 "requests": [...],
 "responses": [...]
}
```
The matching rule with the highest priority will be applied.

#### Json http request
A single request rule looks like this:

```
{
   "name": "example rule", # required
   "priority": 1,          # defaults to 0
   "path": "/example",     # defaults to ".*"
   "method": "GET",        # defaults to ".*"
   "content": null,        # defaults to null (no content required)
   "headers": {            # defaults to {}
     "test": ".*"       # header "test" must be present
     "Hello": "[0-9]+"  # header "Hello" must be present, value must match regex [0-9]+
   },
   "parameters": {},       # defaults to {}; works like headers
   "replace": [...]        # defaults to []; more information below
}
```

#### Json http response
A single response rule looks like this:

```
{
   "name": "example rule", # required
   "priority": 1,          # defaults to 0
   "status_code": "200",   # defaults to ".*"
   "content": null,        # defaults to null (no content required)
   "headers": {            # defaults to {}
     "test": ".*"       # header "test" must be present
     "Hello": "[0-9]+"  # header "Hello" must be present, value must match regex [0-9]+
   },
   "replace": [...]        # defaults to []; more information below
}
```

#### Json http replacements
A replacement has a type (`path`, `content`, `method`, `header`, `parameter` or `status_code`). The other fields depend on the type: <br>

##### Json http path, content, method and status_code replacement
```
{
   "type": "path",       # required; "path", "content", "method" or "status_code"
   "match": "test",      # regex the path must match for rule to be applied. Defaults to ".+"
   "replace": "blah"     # replacement. ${n} will be replaced by nth group of the regex match, ${header:test} by the value of the header test etc. defaults to "${0}"
}
```

##### Json http header and parameter replacement
Since headers and parameters have both a name and a value, the replacement rules have more possible fields:

```
{
   "type": "header",     # required; "header" or "parameter"
   "match_name": "test", # regex the name must match for rule to be applied. Defaults to ".+"
   "match_val": "blah",  # regex the value must match for rule to be applied. Defaults to ".+"
   "replace_name": "a",  # replacement for name. ${name:n} will be replaced by nth group of the regex match for name etc. defaults to ${name:0}
   "replace_val": "b"    # replacement for value. ${val:n} will be replaced by nth group of the regex match for value etc. defaults to ${val:0}
}
```

Note: if the regex for `match_name` (or `match_val`) match the input multiple times, it is not specified which matches group will be used for `${name:n}` (or `${val:n}` respectively), whilst replacing a different value.

#### Json http Examples
An example `json_http` proxy, that modifies no traffic can be found in `test/cli/json_http/replace_nothing.json`. <br>
It can be started listening on port 4001, redirecting to port 443 (whilst resolving hosts using sni) and using the certificate in the `testcert` folder (from the `run` directory) with the following command: <br>
`./jquic -t json_http -f ../test/cli/json_http/replace_nothing.json -c ../testcert/cert.crt -k ../testcert/key.key -sp 4001 -dp 443`

Another example which replaces some requests can be found in `test/cli/json_http/modify_requests.json`. 

## Using JQuic as Java Library
If you require more than just one Java file for your code, you can use JQuic like a normal Java library: <br>
run the `release` gradle task and you will find the library jar as well as a source jar in the `run` directory. <br>
Now just add the `jquic.jar` file as a library to your project (and if you want attach `jquic-sources.jar` as source-file). <br>
It is also planned to release JQuic on maven central at some point in the future!

### Callback based proxy development
To develop a callback based proxy, you can extend several different Java classes:

#### `QuicProxy` (`jquic.example.generic`)
This class can be used to develop generic quic proxies. <br>
It takes care of starting a client and a server engine (by default in non-http mode) and mirrors streams. (Every time a client opens a stream to the proxy, a stream from proxy to server will be opened and associated with it.) <br>
It will then try to read from each stream and call two methods when data is read: <br>
* `onData(ClientStream, ServerStream, byte[], int)`: Called whenever data is sent from client to proxy.
* `onData(ServerStream a, ClientStream b, byte[] data, int amount)`: Called whenever data is sent from server to proxy.
You can overwrite these methods to implement Proxy functionality. However, you need to make sure not to leak resources by keeping streams open if their "partner" was closed etc. Also, if you aggregate data, make sure it is fully sent before the stream is closed. The default implementation forwards everything that was read without any modifications.

#### `SimpleProxy` (`jquic.example.http`)
This class can be used to develop Http aware quic proxies. <br>
It takes care of starting a server engine (in http-mode) and provides a simple callback that will be invoked everytime a request is received. This callback then returns the `HttpMessage` to be sent to the client: <br>
* `getResponse(SimpleHttpMessage)`: Called whenever an `HttpMessage` is received from a client. Returns the response to be sent to the client
For a implementation example, you may look at `SimpleAnnotatedProxy` (`jquic.example.http.annotated`). This proxy starts a new client on each request, allows modifying requests (using a `RequestHandler`), forwards the request to the server (using the newly created client) and then allows modifying the response (using a `ResponseHandler`) before returning it.


### Annotation based proxy development
To develop http aware quic proxies, JQuic also provides a high level, annotation based implementation. <br>
It can be found in the package `jquic.example.http.annotated`. The `SimpleAnnotatedProxy` class can be used to develop http proxies. All you need to do is provide objects defining annotated methods to modify client to proxy requests before they are sent to the server as well as server to proxy requests before they are forwarded to the client. This can be done using the `RequestHandler` and `ResponseHandler`. <br>
As this is best explained with an example, you can find one in `AnnotatedHttpProxyTests` (`test`). (Modifying responses works just like modifying requests but instead of using the `Request` annotation, use the `Response` annotation)

### Annotation based http server development
Similar to Annotation based proxies, you can also build annotation based http servers. <br>
The base class you need for this is the `AnnotatedHttpServer` (`jquic.example.http.annotated`). It has a `RequestHandler` (`jquic.example.http.annotated`) which can be used to define methods that are called whenever a matching request is received. <br>
Using the `AnnotatedHttpServer` works similar to the `SimpleAnnotatedProxy`: Provide objects with methods annotated with `Request` to its `RequestHandler` to define the routes. <br>
An example can be found in `jquic.example.AnnotatedHttpServerExample`.

### High level bindings
JQuic provides high level bindings of the most important parts of the lsquic library:
* `QuicEngine` (`jquic.base.engine`) (corresponds to `lsquic_engine_t`): Can run in server or client mode (or both), however our implementation was only tested whilst running in one mode. A quic engine allows to create connections (client mode) or to accept connections from clients (server mode).
* `QuicConnection` (`jquic.base.connection`) (corresponds to `lsquic_conn_t`): Connections can be created by engines. They can be used to make streams (client only, server push is not really supported yet)
* `QuicStream` (`jquic.base.stream`) (corresponds to `lsquic_stream_t`): Streams allow to send and receive data. We have wrapped sending and receiving data into Java InputStreams and OutputStreams respectively. This allows using a lot of things working with streams (e.g. Scanner, BufferedReader, ...). Performance wasn't a major goal with this implementation so for performance critical tasks, you may want to check the low level bindings

### Low level bindings
These bindings allow access to many methods of the lsquic library. For convenience, JQuic also models some structs used in the library. All methods can be found in `LSQuic` (`lsquicbindings`). <br>
The structs can be found in the package `lsquicbindings.struct`. <br>
As BoringSsl is required for certificates etc. we also created bindings for its most needed functions. You can find them in `BoringSsl` (`boringsslbindings`). A helper class that makes working with BoringSsl easier can be found in `BoringSslHelper` (`boringsslbindings`). <br>
For both, `LSQuic` and `BoringSsl`, you can find an static instance in the `Constants` class in the same package.

## Curl support
As it's hard to persuade browsers to use `http3` yet, we decided work with `curl` instead (for now), as it's easier to configure. <br>
To setup curl with `http3` support yourself, you can follow the description [here](https://github.com/curl/curl/blob/master/docs/HTTP3.md). <br>
We tested curl with `quiche` (`quiche version` section in the document above). <br>
To use `curl` together with JQuic, make sure to set the quic version of the proxy to `h3-29`.

### Example 
We provide a small example server that can be used together with `curl`: <br>
server: `./jquic -t java -f ../test/curl/HttpServer.java` <br>
curl: `curl --http3 https://localhost:4000/` (feel free to make more fancy requests :D)


### Known issues
* When sending large responses to curl using the `lsquic` library, the sending will get stuck. This is caused by `lsquic` waiting for a `MAX_STREAM_DATA` frame but never receiving one. (Why exactly this is the case is currently unknown). This can also be reproduced using `lsquic`'s `http_server`, which means it is unlikely the issue is in our part of the implementation.

## Tested With
* `curl` (`quiche`): Works for small http messages, gets stuck for larger ones (see `Curl support` subsection `Known issues`)
* `lsquic http_server`: No issues detected so far (`test/lsquic/HttpClient.java`)
* `lsquic http_client`: No issues detected so far (`test/cli/java/HttpServer.java`)
* `lsquic echo_server`: No issues detected so far (`test/lsquic/EchoClient.java`)
* `lsquic echo_client`: No issues detected so far (`test/lsquic/EchoServer.java`)

## Libraries used:
* [lsquic](https://github.com/litespeedtech/lsquic) `2.29.5`: QUIC and HTTP/3 implementation in C
* [boringssl](https://boringssl.googlesource.com/boringssl) `a2278d4d2cabe73f6663e3299ea7808edfa306b9`: SSL library required by lsquic
* [JNA](https://github.com/java-native-access/jna) `5.6.0`: Java library for easy access to native libraries
* [gson](https://github.com/google/gson) `2.8.6`: JSON Java library
* [jcommander](https://jcommander.org) `1.81`: Java library for command-line parsing

## Contributing
We are happy for contributions of any kind :)

## Implementation progress

### Low level bindings 
- [X] Engine
- [X] Connection
- [X] Stream
- [X] HTTP mode

### High level bindings
- [X] Engine 
- [X] Connection
- [X] Stream 
- [X] HTTP mode

### Proxy
- [X] Quic Proxy
- [X] HTTP proxy
- [X] Callback based
- [X] Annotation based
- [X] Commandline Interface
- [ ] <s>Host file modification</s> (we use iptables with a dedicated user now)
- [X] iptables and user to redirect traffic
- [ ] configure iptables via some kind of cool interface (command-line etc.)

### Build
- [X] Gradle project
- [X] Java library

### Documentation
- [X] rogue outline
- [X] JavaDoc
- [X] Commandline interface
- [X] Low level bindings
- [X] High level bindings
- [X] Callback based Proxy
- [X] Annotation based Proxy
- [X] Examples
