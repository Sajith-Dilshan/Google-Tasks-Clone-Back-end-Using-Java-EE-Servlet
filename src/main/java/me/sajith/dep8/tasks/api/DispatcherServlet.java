package me.sajith.dep8.tasks.api;

import me.sajith.dep8.tasks.util.HttpServlet2;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@MultipartConfig(location = "/tmp",maxFileSize = 10 * 1024 * 1024)
@WebServlet(name = "DispatcherServlet", value = "/v1/users/*")
public class DispatcherServlet extends HttpServlet2 {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getPathInfo() == null || req.getPathInfo().equals("/")){
            // User Servlet
            // /v1/users
            // /v1/users/
            getServletContext().getNamedDispatcher("UserServlet").forward(req, resp);
        }else{
            if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/?")){
//                /v1/users/{{user_uuid}
//                /v1/users/{{user_uuid}/
                getServletContext().getNamedDispatcher("UserServlet").forward(req, resp);
            }else if (req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/lists(/\\d+)?/?")){
//                /v1/users/{{user_uuid}}/lists
//                /v1/users/{{user_uuid}}/lists/
//                /v1/users/{{user_uuid}}/lists/{{tasklist_id}}
//                /v1/users/{{user_uuid}}/lists/{{tasklist_id}}/

                getServletContext().getNamedDispatcher("TaskListServlet").forward(req, resp);

            }else if(req.getPathInfo().matches("/[A-Fa-f0-9\\-]{36}/lists/\\d+/tasks(/\\d+)?/?")) {
            getServletContext().getNamedDispatcher("TaskServlet").forward(req, resp);
            }else{
            super.service(req,resp);
            }
        }

    }

}
