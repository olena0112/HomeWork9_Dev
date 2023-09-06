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
            // Зберегти передану таймзону в куках
            Cookie timezoneCookie = new Cookie("timezone", timezone);
            resp.addCookie(timezoneCookie);
        } else {
            // Отримати збережену таймзону з куків, якщо не передано нову
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("timezone".equals(cookie.getName())) {
                        timezone = cookie.getValue();
                        break;
                    }
                }
            }
        }

        String currentTime = "";

        try {
            ZoneId zoneId = ZoneId.of(timezone != null ? timezone : "UTC");
            ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
            currentTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'XXX").format(zonedDateTime);

        } catch (Exception e) {
            respMap.put("error", e);
        }

        respMap.put("timeZone", currentTime);

        Context simpleContext = new Context(
                req.getLocale(),
                respMap
        );

        engine.process("time_template", simpleContext, resp.getWriter());
    }
}
