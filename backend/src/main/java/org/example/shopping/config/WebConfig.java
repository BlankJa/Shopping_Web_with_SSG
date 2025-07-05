package org.example.startup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${web.static.cache-period:3600}")
    private int staticCachePeriod;
    
    @Value("${web.html.cache-period:0}")
    private int htmlCachePeriod;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(staticCachePeriod)
                .resourceChain(false); // 禁用资源链处理
        
        // 配置images目录下的图片文件访问
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(staticCachePeriod)
                .resourceChain(false);
        
        // 配置HTML文件访问
        registry.addResourceHandler("/*.html")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(htmlCachePeriod)
                .resourceChain(false);
        
        // 配置根路径访问默认页面
        registry.addResourceHandler("/")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(htmlCachePeriod);
    }
}