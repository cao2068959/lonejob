# 1. 简介

`lonejob` 是一个简单的`分布式任务`框架, 同时他也是一个`spring-boot-starter`,
所以只能运行在`spring-boot` 的环境下

### 1.1 能做什么？
市面上有很多优秀的`分布式任务`框架， 比如`elastic-job`, `xxl-job` 等， 但是我的需求因为太简单了，不需要复杂的
均衡策略，也不需要花里胡哨的监控以及动态配置定时任务， 我仅仅想当项目启动的时候去启动一个`定时器`，这个`定时器`将只会在一个实例中启动，当启动
`定时器`的实例挂了，别的实例会继续启动一个`定时器`来顶替原来实例的位置

同时也提供了只执行一次的功能，也就是集群中只会有一个实例在启动的时候执行一个业务方法`A()`，当执行了`A()` 的实例挂了之后，将会有另外一个实例去执行一次方法`A()`


# 2. 快速开始

### 2.1 环境构建
  - JDK 11
  - Spring boot 2


下载本仓库代码后执行

```shell script
  mvn clean install
```

项目中引用

```xml
    <dependency>
        <groupId>com.chy</groupId>
        <artifactId>lonejob-spring-starter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

### 2.2 配置项

```properties
#zk的地址，集群下用逗号分隔
lonejob.zk.address="127.0.0.1:2181"
#挂载到zk时候的namespace
lonejob.zk.namespace="chy"
#单位毫秒默认 5000
lonejob.zk.sessionTimeout=5000
#单位毫秒默认 5000
lonejob.zk.connectionTimeout=5000
#和zk断开连接后重连的时间间隔
#单位毫秒默认 1000
lonejob.zk.retryInterval=1000

# master节点掉线后，延迟多久才去强占节点，可能网络波动，或者zk挂了之后马上重启，这个参数能保证重连后，master保持不变
# 默认值 5000 单位 ms
lonejob.conf.waitMasterReconnect=5000

```

### 2.3 使用方式

```java
@LoneComponent
public class MyJob {

    @Autowired
    MyService service;

    @LoneJob
    public void test2() {
        service.yyy();
    }

    @LoneJob(cron = "0 0 0 * * ?")
    public void test1() {
        service.xxx();
    }

}

```

- @LoneComponent
    - 注解 `@LoneComponent` 上面继承了注解 `@Component` 所以他有`@Component` 的一切能力，被标注的类也将进入`ioc`容器

- @LoneJob
    - 用来标注要执行的job方法，如果没有传入`cron`参数的，将只会执行一次
    - `cron` 的规则和 `@Scheduled` 中的一致

 