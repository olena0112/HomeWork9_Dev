package org.example;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix("templates/");
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
        log((String) session.getAttribute("calcTimeZone"));
        log((String) session.getAttribute("lastTimeZone"));

        String timezone = req.getParameter("timezone");
        if (timezone != null) {
            timezone = URLEncoder.encode(timezone, StandardCharsets.UTF_8);
        } else {
            timezone = "UTC";
        }

        String lastTimezone = null;
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if (cookie.getName().equals("lastReq")) {
                    lastTimezone = cookie.getValue();
                }
                log(cookie.getName() + "=" + cookie.getValue());
            }
        }
        respMap.put("lastTimeZone", lastTimezone);
        session.setAttribute("lastTimeZone", lastTimezone);
        log(timezone);
        resp.addCookie(new Cookie("lastReq", timezone));
        respMap.put("timeZone", DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()));
        try {
            resp.setContentType("text/html");
            respMap.put("calcTimeZone", DateTimeFormatter.ISO_DATE_TIME
                    .format(
                            LocalDateTime.now(ZoneId.of(timezone))
                    ));
            resp.addCookie(new Cookie("lastResp", (String) respMap.get("calcTimeZone")));
            session.setAttribute("calcTimeZone", (String) respMap.get("calcTimeZone"));
        } catch (Exception e) {
            respMap.put("error", e);
        }

        Context simpleContext = new Context(
                req.getLocale(),
                respMap
        );
        engine.process("time_template", simpleContext, resp.getWriter());
        resp.getWriter().close();
    }
}
