package me.sajith.dep8.tasks.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import me.sajith.dep8.tasks.security.SecurityContextHolder;
import me.sajith.dep8.tasks.util.HttpResponseErrorMsg;
import me.sajith.dep8.tasks.util.HttpServlet2;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MultipartConfig(location = "/tmp",maxFileSize = 10 * 1024 * 1024)
@WebServlet(name = "DispatcherServlet", value = "/v1/users/*")
public class DispatcherServlet extends HttpServlet2 {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
            getServletContext().getNamedDispatcher("UserServlet").forward(req, resp);
        } else {
            String pattern = "/([A-Fa-f0-9\\-]{36})/?.*";
            Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
            if (matcher.find()) {
                String userId = matcher.group(1);
                if (!userId.equals(SecurityContextHolder.getPrincipal().getId())) {
                    resp.setContentType("application/json");
                    resp.setStatus(403);
                    Jsonb jsonb = JsonbBuilder.create();
                    jsonb.toJson(new HttpResponseErrorMsg(new Date().getTime(), 403, null,
                            "Permission denied", req.getRequestURI()), resp.getWriter());
                    return;
                }
            }

            if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/?")) {
                getServletContext().getNamedDispatcher("UserServlet").forward(req, resp);
            } else if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/lists(/\\d+)?/?")) {
                getServletContext().getNamedDispatcher("TaskListServlet").forward(req, resp);
            } else if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/lists/\\d+/tasks(/\\d+)?/?")) {
                getServletContext().getNamedDispatcher("TaskServlet").forward(req, resp);
            } else {
                resp.setContentType("application/json");
                resp.setStatus(404);
                Jsonb jsonb = JsonbBuilder.create();
                jsonb.toJson(new HttpResponseErrorMsg(new Date().getTime(), 404, null,
                        "Invalid location", req.getRequestURI()), resp.getWriter());
            }
        }

        }
    }