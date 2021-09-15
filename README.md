[inverno-io]: https://www.inverno.io

[inverno-core-root-doc]: https://github.com/inverno-io/inverno-core/tree/master/doc/reference-guide.md

[graal-vm]: https://www.graalvm.org/
[maven]: https://maven.apache.org/
[apache-license]: https://www.apache.org/licenses/LICENSE-2.0

# Inverno Core

[![CI/CD](https://github.com/inverno-io/inverno-core/actions/workflows/maven.yml/badge.svg)](https://github.com/inverno-io/inverno-core/actions/workflows/maven.yml)

The [Inverno core framework][inverno-io] project provides an Inversion of Control and Dependency Injection framework for the Java™ platform. It has the particularity of not using reflection for object instantiation and dependency injection, everything being verified and done statically during compilation.

This approach has many advantages over other IoC/DI solutions starting with the static checking of the bean dependency graph at compile time which guarantees that a program is correct and will run properly. Debugging is also made easier since you can actually access the source code where beans are instantiated and wired together. Finally, the startup time of a program is greatly reduced since everything is known in advance, such program can even be further optimized with ahead of time compilation solutions like [GraalVM][graal-vm]...

The framework has been designed to build highly modular applications using standard Java modules. An Inverno module supports encapsulation, it only exposes the beans that need to be exposed and it clearly specifies the dependencies it requires to operate properly. This greatly improves program stability over time and simplifies the use of a module. Since an Inverno module has a very small runtime footprint it can also be easily integrated in any application.

## Creating an Inverno module

An **Inverno  module** is a regular Java module, that requires `io.inverno.core` modules, and which is annotated with `@Module` annotation. The following *hello* module is a simple Inverno module:

```java
@io.inverno.core.annotation.Module
module io.inverno.example.hello {
    requires io.inverno.core;
}
```

An **Inverno bean** can be a regular Java class annotated with `@Bean` annotation. A bean represents the basic building block of an application which is typically composed of multiple interconnected beans instances. The following `HelloService` bean can be used to create a basic application:

```java
package io.inverno.example.hello;

import io.inverno.core.annotation.Bean;

@Bean
public class HelloService {

    public HelloService() {}

    public void sayHello(String name) {
        System.out.println("Hello " + name + "!!!");
    }
}
```

At compile time, the Inverno framework will generate a module class named after the module, `io.inverno.example.hello.Hello` in our example. This class contains all the logic required to instantiate and wire the application beans at runtime. It can be used in a Java program to access and use the `HelloService`. This program can be in the same Java module or in any other Java module which requires module `io.inverno.example.hello`:

```java
package io.inverno.example.hello;

import io.inverno.core.v1.Application;

public class Main {
    
    public static void main(String[] args) {
        Hello hello = Application.with(new Hello.Builder()).run();

        hello.helloService().sayHello(args[0]);
    }
}
```

### Building and running with Maven

The development of an Inverno module is pretty easy using [Apache Maven][maven], you simply need to create a standard Java project that inherits from `io.inverno.dist:inverno-parent` project and declare a dependency to `io.inverno:inverno-core`:

```xml
<!-- pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>io.inverno.dist</groupId>
        <artifactId>inverno-parent</artifactId>
        <version>${VERSION_INVERNO_DIST}</version>
    </parent>
    <groupId>io.inverno.example</groupId>
    <artifactId>hello</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>io.inverno</groupId>
            <artifactId>inverno-core</artifactId>
        </dependency>
    </dependencies>
</project>
```

Java source files for `io.inverno.example.hello` module must be placed in `src/main/java` directory, the module can then be built using Maven:

```plaintext
$ mvn install
```

You can then run the application:

```plaintext
$ mvn inverno:run -Dinverno.run.arguments=John

[INFO] --- inverno-maven-plugin:${VERSION_INVERNO_TOOLS}:run (default-cli) @ app-hello ---
[INFO] Running project: io.inverno.example.hello@1.0.0-SNAPSHOT...
Hello John!!!

```

### Building and running with pure Java

You can also choose to build your Inverno module using pure Java commands. Assuming Inverno framework modules are located under `lib/` directory and Java source files for `io.inverno.example.hello` module are placed in `src/io.inverno.example.hello` directory, you can build the module with the `javac` command:

```plaintext
$ javac --processor-module-path lib/ --module-path lib/ --module-source-path src/ -d jmods/ --module io.inverno.example.hello 
```

The application can then be run as follows:

```plaintext
$ java --module-path lib/:jmods/ --module io.inverno.example.hello/io.inverno.example.hello.Main John
Hello John!!!
```

### Summary

In this simple example, we created an Inverno module which exposes one bean and use it to implement logic in an application. As you can imagine a real life application is far more complex than that, composed of many modules providing multiple beans in different ways and wired altogether. The Inverno framework has been designed to create such applications in a simple, elegant and efficient way, please consult the [reference documentation][inverno-core-root-doc] to get the full picture.

## Building Inverno core framework

The Inverno core framework can be built using Maven and a JDK 11+ with the following command:

```plaintext
$ mvn install
```

## License

The Inverno Framework is released under version 2.0 of the [Apache License][apache-license].

