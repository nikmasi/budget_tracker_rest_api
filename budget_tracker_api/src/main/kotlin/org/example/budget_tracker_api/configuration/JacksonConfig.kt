package org.example.budget_tracker_api.configuration

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.cfg.CoercionAction
import tools.jackson.databind.cfg.CoercionInputShape
import tools.jackson.databind.type.LogicalType

@Configuration
class JacksonConfig {

    @Bean
    fun jacksonCustomizer() = JsonMapperBuilderCustomizer { builder ->
        builder.withCoercionConfig(LogicalType.Textual) {
            it.setCoercion(
                CoercionInputShape.Integer,
                CoercionAction.Fail
            )
            it.setCoercion(
                CoercionInputShape.Float,
                CoercionAction.Fail
            )
            it.setCoercion(
                CoercionInputShape.Boolean,
                CoercionAction.Fail
            )
        }
    }
}