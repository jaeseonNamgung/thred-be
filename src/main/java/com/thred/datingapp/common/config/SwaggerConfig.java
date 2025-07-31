package com.thred.datingapp.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  private final String jwt = "JWT";
  @Bean
  public OpenAPI customOpenAPI() {
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
    Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
        .name(jwt)
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat(jwt)
    );
    return new OpenAPI()
        .components(components)
        .info(apiInfo())
        .addSecurityItem(securityRequirement);
  }

  private Info apiInfo() {
    return new Info()
        .title("ThRed API") // API의 제목
        .description("API documentation with JWT authentication") // API에 대한 설명
        .version("1.0.0"); // API의 버전
  }
}
