package ru.hawk.wmadmin.wiremock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import wiremock.Run;
import wiremock.com.github.jknack.handlebars.internal.lang3.StringUtils;

/**
 * Запускатор WireMock Standalone в фоне внутри приложения.
 *
 * <p>Компонент активируется при {@code wiremock.embedded.enabled=true} и стартует автоматически
 * на ранней фазе (см. {@link #getPhase()}), чтобы UI/Backend могли сразу ходить в WireMock Admin API.</p>
 * <p>
 * Запускается через {@link Run#main(String[])}, поэтому живёт как отдельный фоновой поток.
 *
 * @author olshansky
 * @since 18.02.2026
 */
@Component
@EnableConfigurationProperties(WireMockStandaloneLifecycle.Props.class)
@ConditionalOnProperty(prefix = "wiremock.embedded", name = "enabled", havingValue = "true")
public class WireMockStandaloneLifecycle implements SmartLifecycle {

    private static final String DIR_MAPPINGS = "mappings";
    private static final String DIR_FILES = "__files";

    private final Props props;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Thread thread;

    /**
     * Создаёт обёртку для запуска WireMock Standalone.
     *
     * @param props свойства запуска embedded WireMock (порт, rootDir, verbose и т.д.)
     */
    public WireMockStandaloneLifecycle(Props props) {
        this.props = props;
    }

    /**
     * Запускает WireMock Standalone в отдельном daemon-потоке.
     *
     * <p>Метод идемпотентен: повторный вызов при уже запущенном состоянии ничего не делает.</p>
     *
     * <p>Перед запуском:
     * <ul>
     *   <li>валидирует настройки (порт, rootDir);</li>
     *   <li>создаёт структуру директорий {@code rootDir/mappings} и {@code rootDir/__files};</li>
     *   <li>собирает аргументы командной строки для standalone-режима.</li>
     * </ul>
     * </p>
     */
    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            validate(props);

            Path root = Path.of(props.options.get("root-dir")).toAbsolutePath().normalize();
            ensureWireMockDirs(root);

            List<String> args = buildArgs(props);

            Thread t = new Thread(() -> runWireMock(args), "wiremock-standalone");
            t.setDaemon(true);
            thread = t;
            t.start();
        } catch (RuntimeException e) {
            running.set(false);
            throw e;
        }
    }

    /**
     * Пытается остановить WireMock Standalone.
     */
    @Override
    public void stop() {
        running.set(false);

        Thread t = thread;
        if (t != null) {
            try {
                t.interrupt();
                t.join(TimeUnit.SECONDS.toMillis(2));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Возвращает текущее состояние компонента.
     *
     * @return {@code true}, если компонент в состоянии running, иначе {@code false}
     */
    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Указывает Spring, что компонент должен стартовать автоматически.
     *
     * @return {@code true}
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * Возвращает фазу старта/остановки для ordering lifecycle-компонентов.
     *
     * @return фаза старта
     */
    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

    /**
     * Останавливает компонент и уведомляет Spring через callback.
     *
     * @param callback runnable, который должен быть вызван после завершения остановки
     */
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    /**
     * Запускает WireMock standalone entrypoint и логирует падения.
     *
     * @param args аргументы командной строки для WireMock standalone
     */
    private void runWireMock(List<String> args) {
        try {
            Run.main(args.toArray(new String[0]));
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            running.set(false);
        }
    }

    /**
     * Собирает список аргументов командной строки для запуска WireMock standalone.
     *
     * @param props свойства запуска
     * @return список аргументов, готовый для {@link Run#main(String[])}
     */
    private static List<String> buildArgs(Props props) {
        List<String> args = new ArrayList<>();
        if (props.options() != null) {
            props.options().forEach((k, v) -> {
                if (v == null) {
                    return;
                }
                args.add("--" + k + "=" + v);
            });
        }
        return args;
    }


    /**
     * Создаёт необходимые директории WireMock: rootDir, mappings и __files.
     *
     * @param rootDir корневая директория WireMock (абсолютная/нормализованная)
     * @throws IllegalStateException если директории создать не удалось
     */
    private static void ensureWireMockDirs(Path rootDir) {
        try {
            Files.createDirectories(rootDir);
            Files.createDirectories(rootDir.resolve(DIR_MAPPINGS));
            Files.createDirectories(rootDir.resolve(DIR_FILES));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create WireMock root-dir structure: " + rootDir, e);
        }
    }

    /**
     * Проверяет корректность настроек запуска embedded WireMock.
     *
     * @param props свойства запуска
     * @throws IllegalArgumentException если настройки некорректны
     */
    private static void validate(Props props) {
        if (StringUtils.isBlank(props.options.get("root-dir"))) {
            throw new IllegalArgumentException("wiremock.embedded.root-dir must not be blank");
        }
        if (props.options.get("port") == null
                || Integer.parseInt(props.options.get("port")) < 1
                || Integer.parseInt(props.options.get("port")) > 65535) {
            throw new IllegalArgumentException("wiremock.embedded.port must be in range 1..65535, got: " + props.options.get("port"));
        }
    }

    /**
     * Набор свойств для embedded запуска WireMock Standalone.
     */
    @ConfigurationProperties(prefix = "wiremock.embedded")
    public record Props(boolean enabled, Map<String, String> options) { }
}
