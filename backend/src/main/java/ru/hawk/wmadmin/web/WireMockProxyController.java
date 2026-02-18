package ru.hawk.wmadmin.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * REST-контроллер-прокси к WireMock Admin API.
 *
 * <p>Контроллер предоставляет backend-endpoints для фронтенда (под {@code /api/...}) и проксирует
 * запросы в WireMock Admin API (под {@code /__admin/...}) через настроенный {@link WebClient}.</p>
 *
 * <p>Ключевой момент: ответы возвращаются как {@code byte[]}, а не {@link String}. Это позволяет
 * корректно проксировать как JSON, так и бинарные ответы (PNG/JPEG/PDF и т.д.), не повреждая содержимое.</p>
 *
 * @author olshansky
 * @since 18.02.2026
 */
@RestController
@RequestMapping("/api")
public class WireMockProxyController {

    private static final String WM_ADMIN_REQUESTS = "/__admin/requests";
    private static final String WM_ADMIN_REQUESTS_FIND = "/__admin/requests/find";
    private static final String WM_ADMIN_MAPPINGS = "/__admin/mappings";
    private static final String WM_ADMIN_MAPPINGS_ID = "/__admin/mappings/{id}";
    private static final String WM_ADMIN_MAPPINGS_SAVE = "/__admin/mappings/save";

    private final WebClient wm;

    /**
     * Создаёт контроллер.
     *
     * @param wiremockWebClient {@link WebClient}, настроенный на базовый URL WireMock
     */
    public WireMockProxyController(final WebClient wiremockWebClient) {
        this.wm = wiremockWebClient;
    }

    /**
     * Возвращает журнал запросов WireMock (requests journal).
     *
     * <p>Проксирует {@code GET /__admin/requests}.</p>
     *
     * @return {@link Mono} с {@link ResponseEntity}, содержащим ответ WireMock
     */
    @GetMapping("/requests")
    public Mono<ResponseEntity<byte[]>> getRequests() {
        return proxy(HttpMethod.GET, WM_ADMIN_REQUESTS);
    }

    /**
     * Ищет записи в журнале запросов WireMock по фильтру.
     *
     * <p>Проксирует {@code POST /__admin/requests/find}.</p>
     *
     * @param bodyJson JSON-тело запроса WireMock (criteria для поиска)
     * @return {@link Mono} с {@link ResponseEntity}, содержащим ответ WireMock
     */
    @PostMapping("/requests/find")
    public Mono<ResponseEntity<byte[]>> findRequests(@RequestBody final String bodyJson) {
        return proxy(HttpMethod.POST, WM_ADMIN_REQUESTS_FIND, bodyJson);
    }

    /**
     * Очищает журнал запросов WireMock.
     *
     * <p>Проксирует {@code DELETE /__admin/requests}.</p>
     *
     * @return {@link Mono} с {@link ResponseEntity}, содержащим ответ WireMock
     */
    @DeleteMapping("/requests")
    public Mono<ResponseEntity<byte[]>> resetRequests() {
        return proxy(HttpMethod.DELETE, WM_ADMIN_REQUESTS);
    }

    /**
     * Возвращает список маппингов WireMock.
     *
     * <p>Проксирует {@code GET /__admin/mappings}.</p>
     *
     * @return {@link Mono} с {@link ResponseEntity}, содержащим ответ WireMock
     */
    @GetMapping("/mappings")
    public Mono<ResponseEntity<byte[]>> getMappings() {
        return proxy(HttpMethod.GET, WM_ADMIN_MAPPINGS);
    }

    /**
     * Создаёт новый mapping в WireMock.
     *
     * <p>Проксирует {@code POST /__admin/mappings}.</p>
     *
     * @param mappingJson JSON mapping, совместимый с WireMock Admin API
     * @return {@link Mono} с {@link ResponseEntity}, содержащим ответ WireMock
     */
    @PostMapping("/mappings")
    public Mono<ResponseEntity<byte[]>> createMapping(@RequestBody final String mappingJson) {
        return proxy(HttpMethod.POST, WM_ADMIN_MAPPINGS, mappingJson);
    }

    /**
     * Обновляет существующий mapping по id.
     *
     * <p>Проксирует {@code PUT /__admin/mappings/{id}}.</p>
     *
     * @param id          идентификатор маппинга в WireMock
     * @param mappingJson JSON mapping (полное содержимое/новая версия)
     * @return {@link Mono} с {@link ResponseEntity}, содержащим ответ WireMock
     */
    @PutMapping("/mappings/{id}")
    public Mono<ResponseEntity<byte[]>> updateMapping(@PathVariable("id") final String id,
                                                      @RequestBody final String mappingJson) {
        return proxy(HttpMethod.PUT, WM_ADMIN_MAPPINGS_ID, mappingJson, id);
    }

    /**
     * Удаляет mapping по id.
     *
     * <p>Проксирует {@code DELETE /__admin/mappings/{id}}.</p>
     *
     * @param id идентификатор маппинга в WireMock
     * @return {@link Mono} с {@link ResponseEntity}, содержащим ответ WireMock (обычно пустое тело)
     */
    @DeleteMapping("/mappings/{id}")
    public Mono<ResponseEntity<byte[]>> deleteMapping(@PathVariable("id") final String id) {
        return proxy(HttpMethod.DELETE, WM_ADMIN_MAPPINGS_ID, null, id);
    }

    /**
     * Сохраняет текущие маппинги WireMock на диск.
     *
     * <p>Проксирует {@code POST /__admin/mappings/save}.
     * Это нужно, чтобы маппинги “переживали” рестарт WireMock (если он настроен на файловое хранение).</p>
     *
     * @return {@link Mono} с {@link ResponseEntity}, содержащим ответ WireMock
     */
    @PostMapping("/mappings/save")
    public Mono<ResponseEntity<byte[]>> saveMappingsToDisk() {
        return proxy(HttpMethod.POST, WM_ADMIN_MAPPINGS_SAVE);
    }

    /**
     * Универсальный прокси-вызов без тела запроса.
     *
     * <p>Используется для {@code GET}/{@code DELETE} и прочих запросов без body.</p>
     *
     * @param method HTTP-метод
     * @param uri    путь WireMock Admin API (например {@code /__admin/mappings})
     * @return {@link Mono} с {@link ResponseEntity<byte>}, содержащим статус/заголовки/тело WireMock
     */
    private Mono<ResponseEntity<byte[]>> proxy(final HttpMethod method, final String uri) {
        return proxy(method, uri, null);
    }

    /**
     * Универсальный прокси-вызов в WireMock Admin API с возможным JSON-телом.
     *
     * <p>Критично: ответ читается как {@code byte[]} и возвращается без преобразования в {@link String}.
     * Это предотвращает повреждение бинарных данных (PNG/JPEG/PDF и т.п.).</p>
     *
     * <p>Если {@code jsonBody != null}, то устанавливается {@code Content-Type: application/json}.</p>
     *
     * @param method  HTTP-метод
     * @param uri     путь WireMock Admin API (например {@code /__admin/requests/find})
     * @param jsonBody JSON-тело запроса или {@code null}, если тела нет
     * @param uriVariables переменные для {@link WebClient} (например id)
     * @return {@link Mono} с {@link ResponseEntity<byte>}, содержащим статус/заголовки/тело WireMock
     */
    private Mono<ResponseEntity<byte[]>> proxy(final HttpMethod method,
                                               final String uri,
                                               @Nullable final String jsonBody,
                                               final Object... uriVariables) {

        final WebClient.RequestHeadersSpec<?> req =
                (jsonBody == null)
                        ? wm.method(method).uri(uri, uriVariables)
                        : wm.method(method)
                        .uri(uri, uriVariables)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(jsonBody);

        return req.exchangeToMono(resp ->
                resp.bodyToMono(byte[].class)
                        .defaultIfEmpty(new byte[0])
                        .map(bytes -> {
                            final HttpHeaders headers = new HttpHeaders();
                            headers.putAll(resp.headers().asHttpHeaders());
                            return new ResponseEntity<>(bytes, headers, resp.statusCode());
                        })
        );
    }
}
