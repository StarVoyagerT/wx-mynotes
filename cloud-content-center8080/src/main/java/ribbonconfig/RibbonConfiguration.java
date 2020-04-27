package ribbonconfig;

import com.mynotes.contentcenter.config.NacosSameClusterWeightedRule;
import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 乔童
 * @Description: ribbon配置类
 * @Date: 2020/04/26 21:37
 * @Version: 1.0
 */
@Configuration
public class RibbonConfiguration {
    @Bean
    public IRule ribbonRule()
    {
        return new NacosSameClusterWeightedRule();
    }
}
