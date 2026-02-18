package ru.hawk.wmadmin.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер-прокладка для поддержки SPA-роутинга на стороне фронтенда (React).
 *
 * @author olshansky
 * @since 18.02.2026
 */
@Controller
public class SpaForwardController {

    /**
     * Выполняет forward на {@code /index.html} для SPA-маршрутов.
     *
     * <p>Этот метод нужен для случаев прямого открытия страниц или обновления браузера на маршрутах
     * фронтенда (например, {@code /logs}, {@code /mappings}). В результате возвращается контент
     * {@code index.html}, а дальнейшую навигацию обрабатывает React.</p>
     *
     * @return строка представления для server-side forward на {@code /index.html}
     */
    @RequestMapping(value = {"/logs", "/mappings"})
    public String forward() {
        return "forward:/index.html";
    }
}
