package com.ywtong.springboothtml.config;

import com.ywtong.springboothtml.interceptor.JwtInterceptor;
import com.ywtong.springboothtml.interceptor.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private RoleInterceptor roleInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/images/**")
                .addResourceLocations("file:" + uploadPath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注意:暂时注释掉JWT拦截器,保持Session机制兼容
        // 如果要启用JWT,取消下面的注释,并注释掉RoleInterceptor


        // JWT拦截器 - 验证Token并提取用户信息
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/send-code",
                        "/api/user/reset-password",
                        "/api/product/search",
                        "/api/product/{id}"
                )
                .order(1);  // 优先级1,最先执行


        // 角色权限拦截器 - 基于角色的权限控制
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/send-code",
                        "/api/user/reset-password",
                        "/api/product/search",
                        "/api/product/{id}"
                )
                .order(2);  // 优先级2,在JWT拦截器之后执行

    }
}

