package me.sajith.dep8.tasks.api;

import me.sajith.dep8.tasks.util.HttpServlet2;
import me.sajith.dep8.tasks.util.ResponseStatusException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "TestServlet", value = "/test")
public class TestServlet extends HttpServlet2 {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ResponseStatusException(400, "Something goes bad");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}