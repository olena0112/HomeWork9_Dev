import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> respMap = new LinkedHashMap<>();
        HttpSession session = req.getSession(true);

        String timezone = req.getParameter("timezone");
        if (timezone != null) {
            // Збереження значення часового поясу в Cookie
            Cookie timezoneCookie = new Cookie("timezone", timezone);
            timezoneCookie.setMaxAge(30 * 24 * 60 * 60); // Налаштуйте бажаний термін життя Cookie (30 днів у цьому випадку)
            resp.addCookie(timezoneCookie);

            // Збереження значення часового поясу в Cookie "lastTimezone"
            Cookie lastTimezoneCookie = new Cookie("lastTimezone", timezone);
            lastTimezoneCookie.setMaxAge(30 * 24 * 60 * 60);
            resp.addCookie(lastTimezoneCookie);
        } else {
            // Отримання значення часового поясу з Cookie
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("timezone".equals(cookie.getName())) {
                        timezone = cookie.getValue();
                    }
                }
            }
        }

        String lastTimezone = null;
        // Отримання значення "Last timezone" з Cookie
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("lastTimezone".equals(cookie.getName())) {
                    lastTimezone = cookie.getValue();
                }
            }
        }

        String currentTime = "";
        try {
            ZoneId zoneId = ZoneId.of(timezone != null ? timezone : "UTC");
            ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'XXX");
            currentTime = formatter.format(zonedDateTime);
        } catch (Exception e) {
            respMap.put("error", e);
        }

        respMap.put("timeZone", currentTime);
        respMap.put("lastTimezone", lastTimezone); // Додаємо значення "Last timezone" у відповідь

        Context simpleContext = new Context(
                req.getLocale(),
                respMap
        );

        engine.process("time_template", simpleContext, resp.getWriter());
    }
}
