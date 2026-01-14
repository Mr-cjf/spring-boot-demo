package top.cjf_rb.mp.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.cjf_rb.core.context.UserContextHolder;
import top.cjf_rb.core.util.Nones;
import top.cjf_rb.mp.type.ExtendedSqlInjector;

import java.util.List;

/**
 mybatis-plus配置

 @author cjf */
@Configuration
@RequiredArgsConstructor
@EnableTransactionManagement
@Import({AppTenantProperties.class})
@MapperScan("top.cjf_rb.mp.**.mapper")
public class AppMybatisPlusConfig {

    /**
     自定义的多租户配置
     */
    private final AppTenantProperties tenantProperties;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        JacksonTypeHandler.setObjectMapper(objectMapper);
    }

    /**
     插件详细 : <a href="https://baomidou.com/pages/2976a3">插件主体</a>
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();

        // 多租户插件
        if (tenantProperties.isEnabled()) {
            mybatisPlusInterceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
                @Override
                public Expression getTenantId() {
                    Long userid = UserContextHolder.getPrincipal();
                    return new LongValue(userid);
                }

                @Override
                public String getTenantIdColumn() {
                    return tenantProperties.getTenantColumn();
                }

                // 这是 default 方法,默认返回 false 表示所有表都需要拼多租户条件
                @Override
                public boolean ignoreTable(String tableName) {
                    List<String> ignoreTables = tenantProperties.getIgnoreTables();
                    return !Nones.isEmpty(ignoreTables) && ignoreTables.contains(tableName);
                }
            }));

        }

        // 分页插件
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 针对 update 和 delete 语句 作用: 阻止恶意的全表更新删除
        mybatisPlusInterceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 乐观锁插件
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return mybatisPlusInterceptor;
    }

    /**
     公共字段自动填充插件
     */
    @Bean
    public MetaObjectHandler mybatisPlusAutofillHandler() {
        return new MybatisPlusAutofillHandler();
    }

    /**
     扩展BaseMapper方法
     */
    @Bean
    @Primary
    public ExtendedSqlInjector extendedSqlInjector() {
        return new ExtendedSqlInjector();
    }

}

