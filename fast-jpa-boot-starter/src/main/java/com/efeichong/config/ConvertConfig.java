package com.efeichong.config;

import com.efeichong.jpa.Convert;
import com.efeichong.jpa.GenericConverterImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * @author lxk
 * @date 2020/9/22
 * @description 添加转换器，将hibernate返回的Map类型转换为 {@link Convert}
 */
@Configuration
public class ConvertConfig {

    @Bean
    public DefaultConversionService defaultConversionService() {
        DefaultConversionService defaultConversionService = (DefaultConversionService) DefaultConversionService.getSharedInstance();
        defaultConversionService.addConverter(new GenericConverterImpl());
        return defaultConversionService;
    }
}
