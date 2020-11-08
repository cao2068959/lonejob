package com.chy.lonejob.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("lonejob.conf")
@Configuration
@Data
public class LoneJobProperties {

    private Integer waitMasterReconnect = 8000;


}
