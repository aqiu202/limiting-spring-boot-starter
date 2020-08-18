# limiting-spring-boot-starter
### 描述
对接口的限流功能的封装，主要应用场景： 

- 单位时间内防止重复提交

- 对某个接口进行并发数的限制

- 对某个接口进行单位时间内请求数量的限制（令牌桶算法）

【注】其功能实现都使用了缓存，其中缓存可选择redis、caffeine和guava， 
只有redis可以实现分布式，其他方式只能在单机上使用
### 引用
maven坐标（已经发布到maven中央仓库）

```xml
<dependency>
    <groupId>com.github.aqiu202</groupId>
    <artifactId>limiting-spring-boot-starter</artifactId>
    <version>0.0.2</version>
</dependency>
```

### 用法
##### 开启
```java
@SpringBootApplication
@EnableLimiting(
    cacheMode = CacheMode.caffeine, //配置防重复提交的缓存使用caffeine（还可使用guava和redis）
    timeout = 3,    //配置防重复提交的接口几秒内不能重复访问
    timeUnit = TimeUnit.SECONDS) //时间单位，默认秒
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```
##### 使用
1.key的生成规则
```java
@Configuration
public class KenGeneratorConfig {

    //配置通过token生成key的规则（防重复提交默认还会添加sessionID标识，详情查看SimpleKeyGenerator类）
    @Bean(name = "keyGeneratorWithToken")
    public KeyGenerator keyGeneratorWithToken() {
        return (request, target, method, params) -> {
            StringJoiner joiner = new StringJoiner(",");
            for (Object param : params) {
                joiner.add(param.getClass().getName());
            }
            String token = request.getHeader("token");
            return target.getClass().getName().concat(":")
                    .concat(method.getName()).concat(joiner.toString())
                    .concat(token);
        };
    }

    //配置通过方法生成key的规则（已内置名称为methodKeyGenerator的方法生成规则，自定义的配置会覆盖该规则）
    //并发数的限制和令牌桶算法的限流方式默认使用该规则生成key
    @Bean(name = KeyGenerator.DEFAULT_METHOD_KEY_GENERATOR)
    public KeyGenerator methodKeyGenerator() {
        return (request, target, method, params) -> {
           StringJoiner joiner = new StringJoiner(",");
           for (Object param : params) {
               joiner.add(param.getClass().getName());
           }
           return target.getClass().getName().concat(":").concat(method.getName()).concat(":")
                   .concat(joiner.toString());
        };
    }

}
```
2.还可以为每个接口单独设置key，可以使用SpEL表达式
接口试例：
```java
@RestController
public class UploadController {
    
    //防重复提交
    @PostMapping("submit")
    @RepeatLimiting(keyGenerator = "keyGeneratorWithToken")
    public JsonResult<Void> testRepeat() {
        return JsonResult.ok();
    }
    
    //防重复提交，单独使用SpEL设置key（已废弃spEl属性，处理时会自动判断是否是spEl表达式）
    @RepeatLimiting(key = "'test-repeat-' + #key")
    @PostMapping("submit")
    public JsonResult<Void> testRepeat(String key) {
        return JsonResult.ok();
    }

    //基于线程数控制并发量，只允许5个线程同时访问
    @ThreadLimiting(threads = 5, keyGenerator = KeyGenerator.DEFAULT_METHOD_KEY_GENERATOR)
    @GetMapping("thread")
    public JsonResult<Void> testThread() {
        return JsonResult.ok();
    }

    //基于令牌桶算法限制接口访问，每秒钟只允许两个用户访问
    @GetMapping("current")
    @CurrentLimiting(permits = 2)
    public JsonResult<Void> testCurrent() {
        return JsonResult.ok();
    }

}
```
3.进阶：spEl扩展，通过EvaluationFiller扩展自定义全局SpEl变量
```java
public class SpElConfiguration {
    @Bean
    public EvaluationFiller evaluationFiller() {
        //context spEl上下文，target当前类实例，method当前调用方法，parameters方法的参数
        return ((context, target, method, parameters) -> {
            context.setVariable("className", target.getClass().getName());
            //配置好参数以后可以在spEl中使用，例如：@RepeatLimiting(key="#className + '-key'")
        });
    }
}
```