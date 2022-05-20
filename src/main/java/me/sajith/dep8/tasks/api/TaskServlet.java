package me.sajith.dep8.tasks.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import me.sajith.dep8.tasks.dto.TaskListDTO;
import me.sajith.dep8.tasks.util.HttpServlet2;
import me.sajith.dep8.tasks.util.ResponseStatusException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "TaskServlet")
public class TaskServlet extends HttpServlet2 {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Request came here");
    }

    private Logger logger = Logger.getLogger(TaskServlet.class.getName());
    private AtomicReference<DataSource> pool;

    public void init(){
        try{

            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/pool");
            pool =new AtomicReference<>(ds);


        } catch (NamingException e) {
            logger.log(Level.SEVERE,"Failed to locate the JNDI Pool",e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().startsWith("application/json")) {
            throw new ResponseStatusException(415, "Invalid content type or content type is empty");
        }

        String pattern = "/([A-Fa-f0-9\\-]{36})/lists/\\d+/tasks/?";
        if (!req.getPathInfo().matches(pattern)) {
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid end point for POST request");
        }
        Matcher matcher = Pattern.compile(pattern).matcher(req.getPathInfo());
        matcher.find();
        String userId = matcher.group(1);

        try (Connection connection = pool.get().getConnection()) {


            Jsonb jsonb = JsonbBuilder.create();
            TaskListDTO taskList = jsonb.fromJson(req.getReader(), TaskListDTO.class);

            if (taskList.getTitle().trim().isEmpty()) {
                throw new ResponseStatusException(400, "Invalid title or title is empty");
            }

            PreparedStatement stm = connection.prepareStatement("INSERT INTO task_list (name,user_id) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, taskList.getTitle());
            stm.setString(2, userId);
            if (stm.executeUpdate() != 1) {
                throw new SQLException("Failed to save the task list");
            }

            ResultSet rst = stm.getGeneratedKeys();
            rst.next();
            taskList.setId(rst.getInt(1));

            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            jsonb.toJson(taskList, resp.getWriter());
        } catch (JsonbException e) {
            throw new ResponseStatusException(400, "Invalid JSON", e);
        } catch (SQLException e) {
            throw new ResponseStatusException(500, e.getMessage(), e);
        }

    }


}
