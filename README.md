# Smart Vertx

该框架底层为以为事件驱动的Vert.x框架，通过运行不同的Verticle实现高性能HTTP、TCP、GRPC等不同协议的访问，
为提升编码效率框架集成了spring boot核心用法

同一个容器内不同的Verticle可直接通过 EventBus 互相访问，

跨容器（服务）的访问可通过集群管理器 连接EventBus 互相访问

框架提供了2类集群管理器：Hazelcast、Ignite，根据部署环境可选择组播（调试）/TCP（k8s部署自动检测）
两种集群节点发现方式

其中通过HTTP协议实现的WEB框架，性能是传统spring MVC的4倍以上，压测数据通过Jmeter给出

框架提供了组件微服务化能力，具体提供了服务发现、熔断器、限流、ConfigMap统一配置管理
、服务间调用（EventBus方式/接口声明@WebClient注解方式）、核心metrics指标收集（支持Prometheus采集）
、opentracing等核心能力。

后续继续完善相关用例example

## HTTP使用方法

添加依赖:

```
<dependency>
	<groupId>com.smart.vertx</groupId>
        <artifactId>smart-vertx-web</artifactId>
	<version>1.2.0</version>
</dependency>
```
## TCP使用方法

添加依赖:

```
<dependency>
	<groupId>com.smart.vertx</groupId>
        <artifactId>smart-vertx-tcp</artifactId>
	<version>1.2.0</version>
</dependency>
```
## GRPC使用方法

添加依赖:

```
<dependency>
	<groupId>com.smart.vertx</groupId>
        <artifactId>smart-vertx-grpc</artifactId>
	<version>1.2.0</version>
</dependency>
```
```java
/**
 *启动入口
 */
@EnableVertxCluster//默认为stand-alone模式，此注解开启集群模式，集群管理器为JVM内置
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```
配置
```yaml
server:
  port: 8080

vertx:
  # 事件循环处理线程数
  nio-thread-count: 1
  # worker线程池大小
  worker-thread-count: 10
  # HTTP Server Verticle部署数量
  verticle-count: 6
  # 允许NIO线程处理单个请求的最长时间, 毫秒
  max-event-loop-execute-time: 2000
  service-name: application-test2
  cluster-type: hazelcast
  opentracing:
    host: 192.168.52.79
    port: 6831
  sign:
    client-key: 234234235
    client-secret: test
  auth:
    enable: false
  namespace: smart
  metrics: true

```
2022-6-8 新增GUICE容器，集成vertx框架
## 使用方法

添加依赖:

```
<dependency>
	<groupId>com.smart.vertx</groupId>
    <artifactId>smart-vertx-guice</artifactId>
	<version>1.2.0</version>
</dependency>
```
```java
/**
 *启动入口
 */
public class Main {
    public static void main(String[] args) {
        GuiceContext.run(VerticleTypeEnum.cluster);
    }
}
```
配置
```yaml
# 配置文件
vertx:
  port: 8088
  # 事件循环处理线程数
  nio-thread-count: 6
  # worker线程池大小
  worker-thread-count: 10
  # HTTP Server Verticle部署数量
  verticle-count: 1

  # 允许NIO线程处理单个请求的最长时间, 毫秒
  max-event-loop-execute-time: 2000
  service-name: application-test2
  package-name: com.smart.test

```
controller demo

```java

@Slf4j
@RestController("/test")
public class TestController {
    @Autowired
    TestService testService;
    @Autowired
    EventBus eventBus;

    @PostConstruct
    public void init() {
        //send(单向),request(双向),publish(广播)
        log.info("consumer start");
        eventBus.consumer("Event.smart.bus", handler -> {
            log.info("收到服务端消息：{}", handler.body());
            ProtoCommonMsg protoCommonMsg = new ProtoCommonMsg();
            for (int i = 0; i < 10; i++) {
                protoCommonMsg.put("name_" + i, "sssssssssssssssssssssssss");
            }
            DeliveryOptions options = new DeliveryOptions().setCodecName("protoMessage");
            handler.reply(protoCommonMsg, options);
            log.info("consumer ok");
        });
    }

    @RequestMapping(value = "/handler/:tenantId", method = RequestMethod.GET, blocked = false)
    public Single<Map<String, Object>> handle(RoutingContext route) throws InterruptedException {
        log.info("invoke TestController, path: {}", route.request().path());
        TimeUnit.SECONDS.sleep(2);
        return testService.test(route.pathParam("tenantId"));
    }

    @RequestMapping(value = "/handler1", method = RequestMethod.GET, blocked = false)
    public Single<Message<Object>> handle1(RoutingContext route) {
        ProtoCommonMsg commonMsg = new ProtoCommonMsg();
        for (int i = 0; i < 10; i++) {
            commonMsg.put("name_" + i, "wan_ke_receive");
        }
        DeliveryOptions options = new DeliveryOptions().setCodecName("protoMessage");
        return eventBus.request("Event.smart.bus", commonMsg, options);
    }

    @RequestMapping(value = "/handler2/:tenantId", method = RequestMethod.POST, blocked = false)
    public String handle2(RoutingContext route, @RequestBody TestCommand command, @PathVariable("tenantId") String tenantId) {
        log.info("invoke TestController, command: {}", command);
        return tenantId.concat("handler2");
    }

    @RequestMapping(value = "/handler3/:tenantId", method = RequestMethod.GET, blocked = false)
    public Completable handle3(RoutingContext route) {
        log.info("invoke TestController, path: {}", route.request().path());
        return testService.test(route.pathParam("tenantId"));
    }
}
```
新增集群模式，eventBus支撑
```java
//集群模式开启方式
@EnableVertxCluster
//eventBus开启方式
@Autowired
EventBus eventBus;
```

新增kafka支撑
```java
//集群模式开启方式
@EnableKafka
```

新增redis支撑
```java
//集群模式开启方式
@EnableRedis
```

新增mongodb支撑
```java
//集群模式开启方式
@EnableMongo
```


新增webClient服务发现

```java
/*声明*/
 @WebClient(path = "/application-test2/v1/test", value = "application-test2")
public interface DiscoveryService {
    @RequestMapping(method = RequestMethod.POST, value = "/handler2/333")
    Single<ResultEntity<?>> test(@RequestBody TestCommand command);
}
/*调用*/
@Resource
DiscoveryService discoveryService;
```
新增eventBus服务发现
1.新增 client server 共用SDK
```java

/*SomeDatabaseService.java*/
@ProxyGen
@VertxGen
@EventBus(address = "Event.smart.bus", timeOut = 400, serviceName = "application-test1")
public interface SomeDatabaseService {
    // A couple of factory methods to create an instance and a proxy
    int NO_NAME_ERROR = 2;
    int BAD_NAME_ERROR = 3;

    // Actual service operations here...

    // The service methods
    Future<JsonObject> process(JsonObject document);

}

/*package-info.java*/
@ModuleGen(name = "services", groupPackage = "com.smart.eventbus",useFutures = true)
package com.smart.eventbus;

import io.vertx.codegen.annotations.ModuleGen;
```
2.server服务暴露
```java
@Service
public class SomeDatabaseServiceImpl extends EventBusAbstract implements SomeDatabaseService {

    @PostConstruct
    public void create() {
        super.bind(SomeDatabaseService.class, this);
    }

    @Override
    public Future<JsonObject> process(JsonObject document) {
        System.out.println("Processing...");
        int i=1/0;
        JsonObject result = document.copy();
        if (!document.containsKey("name")) {
            return Future.failedFuture(new ServiceException(NO_NAME_ERROR, "No name in the document"));
        } else if (document.getString("name").isEmpty() || document.getString("name").equalsIgnoreCase("bad")) {
            return Future.failedFuture(new ServiceException(BAD_NAME_ERROR, "Bad name in the document: " + document.getString("name"), new JsonObject().put("name", document.getString("name"))));
        } else {
            result.put("approved", true);
            return Future.succeededFuture(result);
        }
    }
}
```
3.client服务调用
```java
@Slf4j
@RestController("/test")
public class TestController extends EventBusClient<SomeDatabaseService> {

    @RequestMapping(value = "/handler4/:tenantId", method = RequestMethod.GET, blocked = false)
    public Future<JsonObject> handle4(RoutingContext route) {
        return super.getProxy().map(service -> {
            JsonObject document = new JsonObject().put("name1", "vertx");
            System.out.println("get eventbus record success,{}" + service);
            return service.process(document);
        });
    }
}
```

新增GRPC支撑
```java
@EnableGrpc
```
新增TCP支撑
```java
@EnableTcp
```