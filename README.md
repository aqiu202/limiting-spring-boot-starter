# limiting-spring-boot-starter
### 描述
对接口的限流功能的封装，主要应用场景： 

- 单位时间内防止重复提交

- 对某个接口进行并发数的限制

- 对某个接口进行单位时间内请求数量的限制（令牌桶算法）

### 引用
maven坐标

```
<dependency>
    <groupId>com.redsoft.starters.own</groupId>
    <artifactId>limiting-spring-boot-starter</artifactId>
</dependency>
```

### 用法
##### 开启
```
@SpringBootApplication
@EnableLimiting(
    mode = CacheMode.caffeine, //配置防重复提交的缓存使用caffeine（还可使用guava和redis）
    timeout = 3) //配置防重复提交的接口几秒内不能重复访问
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```
##### 使用
配置防重复提交的缓存key的生成规则(默认使用sessionID标识)
```
@Configuration
public class KenGeneratorConfig {
    @Bean(name = "keyGeneratorWithSession")
    public KeyGenerator keyGeneratorWithSession() {
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
}
```
接口试例：
```
@RestController
public class UploadController {
    
    //防重复提交
    @RepeatLimiting(generatorName = "keyGeneratorWithSession")
    @PostMapping("submit")
    public JsonResult<Void> testRepeat() {
        return JsonResult.ok();
    }

    //基于线程数控制并发量，只允许5个线程同时访问
    @ThreadLimiting(threads = 5)
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