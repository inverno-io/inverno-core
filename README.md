# <img src="src/img/winter.svg" style="width: 100%;"/>

The [Winter framework](https://www.winterframework.io) is an IoC and DI framework for the Java 9+ platform. It has the particularity of not using Java reflection for object instantiation and dependency injection, everything being verified and done statically during compilation.

This approach has many advantages over other IoC/DI solutions starting with the static checking of the bean dependency graph at compile time which guarantees that a program is correct and will run properly. Debugging is also made easier since you can actually access the source code where beans are instantiated and wired together. Finally, the startup time of a program is greatly reduced since everything is known ahead of time, such program can even be further optimized with ahead of time compilation solutions like [jaotc](https://docs.oracle.com/en/java/javase/13/docs/specs/man/jaotc.html) or [GraalVM](https://www.graalvm.org/)...

The framework has been designed to build highly modular applications using standard Java 9+ modules. A Winter module supports encapsulation, it only exposes the beans that need to be exposed and it clearly specifies the dependencies it requires to operate properly. This greatly improves program stability over time and simplifies the use of a module. Since a Winter module has a very small runtime footprint it can also be easily integrated in any application.

## Create a Winter module

The development of a Winter module is pretty easy using [Apache Maven](https://maven.apache.org/), you simply need to create a standard Java project that inherits from `io.winterframework:winter-parent` project and declare a dependency to `winter-core` artifact:

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
    
        ...
        <dependencies>
            ...
            <dependency>
                <groupId>io.winterframework</groupId>
                <artifactId>winter-core</artifactId>
            </dependency>
            ...
        </dependencies>
        ...
        
    </project>

Then define a Java module exporting `io.winterframework.example.hello` package, requiring `io.winterframework.core` and `io.winterframework.core.annotation` modules and annotated with `@Module`:

    // src/main/java/module-info.java
    @io.winterframework.core.annotation.Module
    module io.winterframework.example.hello {
        requires io.winterframework.core;
        requires io.winterframework.core.annotation;
        
        exports io.winterframework.example.hello;
    }

You can then create a `@Bean` annotated classes in the module and let the framework handles instantiation and dependency injection for you. You can for instance create a super fancy `HelloService`:

	// src/main/java/io/winterframework/example/hello/HelloService.java
    package io.winterframework.example.hello;
    
    import io.winterframework.core.annotation.Bean;
    
    @Bean
    public class HelloService {
    
        public HelloService() {}
    
        public void sayHello(String name) {
            System.out.println("Hello " + name + "!!!");
        }
    }

The module is built as follows:

    > mvn install

During the build, the Winter compiler processes generates a corresponding module class named after the module, `io.winterframework.example.hello.Hello` in our example. This class contains all the logic required to instantiate and wire your beans at runtime. 

This class can be used in a Java program to access and use the `HelloService`, this program can be in the same Java module or in any Java module which requires module `io.winterframework.example.hello`:

    // src/main/java/io/winterframework/example/hello/App.java
    package io.winterframework.example.hello;
    
    public class App {
        
        public static void main(String[] args) {
            Hello hello = Application.with(new Hello.Builder()).run();

            hello.helloService().sayHello(args[0]);
        }
    }

Now just rebuild the project and run your first Winter module:

    > mvn install
    
    > mvn exec:java -Dexec.mainClass=io.winterframework.example.hello.App -Dexec.arguments=John
    
    ...
    [INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ hello ---
    janv. 30, 2020 5:51:41 PM io.winterframework.core.v1.Module start
    INFO: Starting Module io.winterframework.example.hello...
    janv. 30, 2020 5:51:41 PM io.winterframework.core.v1.SingletonBean create
    INFO: Creating Singleton Bean io.winterframework.example.hello:helloService
    janv. 30, 2020 5:51:41 PM io.winterframework.core.v1.Module start
    INFO: Module io.winterframework.example.hello started in 18ms
    Hello John!!!
    janv. 30, 2020 5:51:41 PM io.winterframework.core.v1.Module stop
    INFO: Stopping Module io.winterframework.example.hello...
    janv. 30, 2020 5:51:41 PM io.winterframework.core.v1.SingletonBean destroy
    INFO: Destroying Singleton Bean io.winterframework.example.hello:helloService
    janv. 30, 2020 5:51:41 PM io.winterframework.core.v1.Module stop
    INFO: Module io.winterframework.example.hello stopped in 1ms
    ...

In this simple example, you created a Winter module which exposes one bean and use it to implement logic in an application. As you can imagine a real life application is far more complex than that, composed of many modules providing multiple beans in different ways and wired altogether. The Winter framework has been created to create such applications in a simple, elegant and efficient way, please consult the [complete documentation](doc/reference-guide.md) to get the full picture. 

## Building Winter framework

The Winter framework can be built using maven and Java 9+ with the following command:

    > mvn install

## License

The Winter Framework is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).