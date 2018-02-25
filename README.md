# Serverless function runtime for Java

[![Build Status](https://travis-ci.org/michaelmerg/serverless-function-runtime-java.svg?branch=master)](https://travis-ci.org/michaelmerg/serverless-function-runtime-java)

Java 8 template for [OpenFaas](https://github.com/openfaas/faas)

## Quick Start

Pull template from GitHub:

```
$ faas template pull https://github.com/michaelmerg/serverless-function-runtime-java
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
