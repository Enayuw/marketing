package com.br.marketing.api.config;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

public class SwaggerOptConfig implements OperationBuilderPlugin {
    @Override
    public void apply(OperationContext context) {

    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
