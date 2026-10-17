package com.news.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Displays the contact page. Form submission is handled via Ajax to ApiServlet.
 */
@WebServlet("/contact")
public class ContactServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", "Contact Us - TimoNews");
        req.getRequestDispatcher("/pages/contact.html").forward(req, resp);
    }
}
