package ru.hawk.wmadmin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spring-конфигурация клиента для взаимодействия с WireMock по HTTP.
 *
 * @author olshansky
 * @since 18.02.2026
 */
@Configuration
public class WireMockClientConfig {

    /**
     * Создаёт {@link WebClient} для запросов к WireMock.
     *
     * <p>Используется {@link ExchangeStrategies} для настройки кодеков и увеличения лимита
     * {@code maxInMemorySize} до 10 MiB. Это снижает вероятность ошибок вида
     * {@code DataBufferLimitException} при получении больших ответов от WireMock.
     *
     * @param baseUrl базовый URL WireMock, берётся из свойства {@code wiremock.base-url}
     *                (например {@code http://localhost:8080})
     * @return настроенный {@link WebClient} с заданным {@code baseUrl} и стратегиями обмена
     */
    @Bean
    public WebClient wiremockWebClient(@Value("${wiremock.base-url}") String baseUrl) {
        var strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .build();
    }
}
