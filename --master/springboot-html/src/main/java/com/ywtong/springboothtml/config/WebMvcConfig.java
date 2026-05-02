package com.ywtong.springboothtml.config;

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
    private RoleInterceptor roleInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/images/**")
                .addResourceLocations("file:" + uploadPath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API接口
                .excludePathPatterns(
                        "/api/user/register",      // 排除注册接口
                        "/api/user/send-code",     // 排除发送验证码接口
                        "/api/user/reset-password", // 排除重置密码接口
                        "/api/product/search",     // 排除商品搜索(游客可访问)
                        "/api/product/{id}"        // 排除商品详情(游客可访问)
                );
    }
}
