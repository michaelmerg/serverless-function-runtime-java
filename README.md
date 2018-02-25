# Serverless function runtime for Java

[![Build Status](https://travis-ci.org/michaelmerg/serverless-function-runtime-java.svg?branch=master)](https://travis-ci.org/michaelmerg/serverless-function-runtime-java)

Java 8 template for [OpenFaas](https://github.com/openfaas/faas)

## Quick Start

Pull template from GitHub:

```
$ faas-cli template pull https://github.com/michaelmerg/serverless-function-runtime-java
```

Create new function:

```
$ faas-cli new --lang java-gradle java-echo
```

Build function:

```
$ faas-cli build -f java-echo.yml
```

Deploy function:

```
$ faas-cli deploy -f java-echo.yml
```

Invoke function:

```
$ faas-cli invoke java-echo
```


## Overview

There are two different supported handler interfaces. The basic request handler supports simple Java types or a POJO types as input and output. With the streaming request handler you can handle the input and output as byte streams.

### Request handler

```java
public class Handler implements RequestHandler<String, String> {

  @Override
  public String handle(String input, Context context) {

    // Add function code here
    return input;
  }
}
```

### Streaming request handler

```Java
public class StreamingHandler implements StreamingRequestHandler {

  @Override
  public void handle(InputStream input, OutputStream output, Context context) {

    // read input stream
    // write output stream
  }
}
```
