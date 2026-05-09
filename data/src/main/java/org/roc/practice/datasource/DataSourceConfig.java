package org.roc.practice.datasource;

import org.springframework.context.annotation.Configuration;

/**
 * 多数据源配置。
 *
 * <p>引入 {@code dynamic-datasource-spring-boot3-starter} 后，在 application.yml 中声明数据源即可，
 * 无需在此注册额外 Bean。本类作为扩展点保留，可按需添加连接池监控、健康检查等配置。
 *
 * <h3>application.yml 配置示例</h3>
 * <pre>
 * spring:
 *   datasource:
 *     dynamic:
 *       strict: true          # 数据源名称不匹配时抛异常，禁止静默回落到主库
 *       primary: master       # 默认数据源
 *       datasource:
 *         master:
 *           url: jdbc:mysql://127.0.0.1:3306/db?rewriteBatchedStatements=true
 *           username: root
 *           password: xxx
 *           driver-class-name: com.mysql.cj.jdbc.Driver
 *         slave1:             # 名称与 DsConstants.SLAVE_1 保持一致
 *           url: jdbc:mysql://127.0.0.1:3307/db?rewriteBatchedStatements=true
 *           username: root
 *           password: xxx
 *           driver-class-name: com.mysql.cj.jdbc.Driver
 * </pre>
 *
 * <h3>@DS 注解使用规则</h3>
 * <ul>
 *   <li>写操作 —— 不加 {@code @DS}，走默认 master</li>
 *   <li>非强一致性读 —— {@code @DS(DsConstants.SLAVE_1)}</li>
 *   <li>强一致性读（写后立即读，如支付后查余额）—— {@code @DS(DsConstants.MASTER)}</li>
 * </ul>
 *
 * <h3>禁止事项</h3>
 * <ul>
 *   <li>同类内部方法调用 {@code @DS} 不生效（Spring AOP 代理限制，与 {@code @Transactional} 同理）</li>
 *   <li>{@code @Transactional} 方法内禁止跨数据源（Seata 接入前数据源切换不在事务内，数据不一致且不报错）</li>
 *   <li>禁止开启 {@code MasterSlaveAutoRoutingPlugin}（事务内 select 会错误路由到从库）</li>
 *   <li>{@code @Async} 方法或线程池任务里 {@code @DS} 不生效（ThreadLocal 不传递，静默回落到默认数据源）</li>
 * </ul>
 */
@Configuration
public class DataSourceConfig {
    // 扩展点：如需注册连接池监控 Servlet（Druid）、自定义健康检查等，在此添加 @Bean
}
