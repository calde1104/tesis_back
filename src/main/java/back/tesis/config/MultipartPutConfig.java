package back.tesis.config;

import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultipartPutConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> multipartPutCustomizer() {
        return factory -> factory.addConnectorCustomizers(
                connector -> connector.setParseBodyMethods("POST,PUT"));
    }
}
