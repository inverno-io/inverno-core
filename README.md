<img src="src/img/winter.svg" style="width: 100%;"/>

The [Winter framework](https://www.winterframework.io) is an Inversion of Control and Dependency Injection framework for the Java 9+ platform. It has the particularity of not using Java reflection for object instantiation and dependency injection, everything being verified and done statically during compilation.

This approach has many advantages over other IoC/DI solutions starting with the static checking of the bean dependency graph at compile time which guarantees that a program is correct and will run properly. Debugging is also made easier since you can actually access the source code where beans are instantiated and wired together. Finally, the startup time of a program is greatly reduced since everything is known in advance, such program can even be further optimized with ahead of time compilation solutions like [jaotc](https://docs.oracle.com/en/java/javase/13/docs/specs/man/jaotc.html) or [GraalVM](https://www.graalvm.org/)...

The framework has been designed to build highly modular applications using standard Java 9+ modules. A Winter module supports encapsulation, it only exposes the beans that need to be exposed and it clearly specifies the dependencies it requires to operate properly. This greatly improves program stability over time and simplifies the use of a module. Since a Winter module has a very small runtime footprint it can also be easily integrated in any application.

## Creating a Winter module

A **Winter Module** is a regular Java module, that requires `io.winterframework.core` and `io.winterframework.core.annotation` modules, and which is annotated with `@Module` annotation. The following `hello` module is a simple Winter module:

```java
@io.winterframework.core.annotation.Module
module io.winterframework.example.hello {
    requires io.winterframework.core;
    requires io.winterframework.core.annotation;
    
    exports io.winterframework.example.hello;
}
```

A **Winter Bean** can be a regular Java class annotated with `@Bean` annotation. A bean represents the basic building block of an application which is typically composed of multiple interconnected beans instances. For instance, the following `HelloService` bean can be used to create a basic application:

```java
package io.winterframework.example.hello;

import io.winterframework.core.annotation.Bean;

@Bean
public class HelloService {

    public HelloService() {}

    public void sayHello(String name) {
        System.out.println("Hello " + name + "!!!");
    }
}
```

At compile time, the Winter framework will generate a module class named after the module, `io.winterframework.example.hello.Hello` in our example. This class contains all the logic required to instantiate and wire the application beans at runtime. It can be used in a Java program to access and use the `HelloService`. This program can be in the same Java module or in any other Java module which requires module `io.winterframework.example.hello`:

```java
package io.winterframework.example.hello;

import io.winterframework.core.v1.Application;

public class App {
    
    public static void main(String[] args) {
        Hello hello = Application.with(new Hello.Builder()).run();

        hello.helloService().sayHello(args[0]);
    }
}
```

### Building and running with Maven

The development of a Winter module is pretty easy using [Apache Maven](https://maven.apache.org/), you simply need to create a standard Java project that inherits from `io.winterframework:winter-parent` project and declare a dependency to `winter-core` artifact:

```xml
<!-- pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>io.winterframework</groupId>
        <artifactId>winter-parent</artifactId>
        <version>1.0.0</version>
    </parent>
    <groupId>io.winterframework.example</groupId>
    <artifactId>hello</artifactId>

    <dependencies>
        <dependency>
            <groupId>io.winterframework</groupId>
            <artifactId>winter-core</artifactId>
        </dependency>
    </dependencies>
</project> 
```

Java source files for `io.winterframework.example.hello` module must be place in `src/main/java` directory, the module can then be built using Maven:

```shell
$ mvn install
```

You can also run the hello application:

```shell
$ mvn exec:java -Dexec.mainClass=io.winterframework.example.hello.App -Dexec.arguments=John

[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ hello ---
juin 08, 2020 11:45:05 PM io.winterframework.core.v1.Application run
INFOS: Winter is starting...
juin 08, 2020 11:45:05 PM io.winterframework.core.v1.Module start
INFOS: Starting Module io.winterframework.example.hello...
juin 08, 2020 11:45:05 PM io.winterframework.core.v1.SingletonBean create
INFOS: Creating Singleton Bean io.winterframework.example.hello:helloService
juin 08, 2020 11:45:05 PM io.winterframework.core.v1.Module start
INFOS: Module io.winterframework.example.hello started in 12ms
Hello John!!!

```

### Building and running with pure Java

You can also choose to build your Winter module using pure Java commands. Assuming Winter framework modules are located under `lib/` directory and Java source files for `io.winterframework.example.hello` module are placed in `src/io.winterframework.example.hello` directory, you can build the module with `javac` command:

```shell
$ javac --processor-module-path lib/ --module-path lib/ --module-source-path src/ -d jmods/ --module io.winterframework.example.hello 
```

The hello application can then be run as follows:

```shell
$ java --module-path lib/:jmods/ --module io.winterframework.example.hello/io.winterframework.example.hello.App John
juin 09, 2020 12:02:28 AM io.winterframework.core.v1.Application run
INFOS: Winter is starting...
juin 09, 2020 12:02:28 AM io.winterframework.core.v1.Module start
INFOS: Starting Module io.winterframework.example.hello...
juin 09, 2020 12:02:28 AM io.winterframework.core.v1.SingletonBean create
INFOS: Creating Singleton Bean io.winterframework.example.hello:helloService
juin 09, 2020 12:02:28 AM io.winterframework.core.v1.Module start
INFOS: Module io.winterframework.example.hello started in 8ms
Hello John!!!
```

### Summary

In this simple example, we created a Winter module which exposes one bean and use it to implement logic in an application. As you can imagine a real life application is far more complex than that, composed of many modules providing multiple beans in different ways and wired altogether. The Winter framework has been created to create such applications in a simple, elegant and efficient way, please consult the [complete documentation](doc/reference-guide.md) to get the full picture.

## Building Winter framework

The Winter framework can be built using maven and Java 9+ with the following command:

```shell
$ mvn install
```

## License

The Winter Framework is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).