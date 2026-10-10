package com.literaryemporium.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.literaryemporium.dao.UserDAO;
import com.literaryemporium.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/*"})
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        String pathInfo = request.getPathInfo();

        if ("/current".equals(pathInfo)) {
            HttpSession session = request.getSession(false);
            User user = (session != null) ? (User) session.getAttribute("user") : null;
            Map<String, Object> res = new HashMap<>();
            if (user != null) {
                res.put("status", "success");
                res.put("user", user);
            } else {
                res.put("status", "unauthenticated");
            }
            response.getWriter().write(gson.toJson(res));
            return;
        }

        if ("/logout".equals(pathInfo)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            Map<String, Object> res = new HashMap<>();
            res.put("status", "success");
            res.put("message", "Logged out successfully");
            response.getWriter().write(gson.toJson(res));
            return;
        }

        // Sellers list for Admin
        if ("/sellers".equals(pathInfo)) {
            response.getWriter().write(gson.toJson(userDAO.getUsersByRole("seller")));
            return;
        }

        // Buyers list for Admin
        if ("/buyers".equals(pathInfo)) {
            response.getWriter().write(gson.toJson(userDAO.getUsersByRole("buyer")));
            return;
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        String pathInfo = request.getPathInfo();

        JsonObject body = parseJsonBody(request);

        if ("/register".equals(pathInfo)) {
            handleRegister(body, request, response);
            return;
        }

        if ("/login".equals(pathInfo)) {
            handleLogin(body, request, response);
            return;
        }

        if ("/logout".equals(pathInfo)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            Map<String, Object> res = new HashMap<>();
            res.put("status", "success");
            res.put("message", "Logged out successfully");
            response.getWriter().write(gson.toJson(res));
            return;
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        String idParam = request.getParameter("id");
        Map<String, Object> res = new HashMap<>();
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                boolean deleted = userDAO.deleteUser(id);
                res.put("status", deleted ? "success" : "error");
                res.put("message", deleted ? "User deleted successfully" : "User not found");
            } catch (NumberFormatException e) {
                res.put("status", "error");
                res.put("message", "Invalid user ID");
            }
        } else {
            res.put("status", "error");
            res.put("message", "Missing user ID");
        }
        response.getWriter().write(gson.toJson(res));
    }

    private void handleRegister(JsonObject body, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> res = new HashMap<>();

        String name = getString(body, "name", request.getParameter("name"));
        String email = getString(body, "email", request.getParameter("email"));
        String password = getString(body, "password", request.getParameter("password"));
        String role = getString(body, "role", request.getParameter("role"));

        if (role == null || role.trim().isEmpty()) {
            role = "buyer";
        }

        if (name == null || email == null || password == null || email.trim().isEmpty() || password.length() < 6) {
            res.put("status", "error");
            res.put("message", "Invalid input or password too short (min 6 chars).");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(res));
            return;
        }

        if (userDAO.getByEmail(email) != null) {
            res.put("status", "error");
            res.put("message", "An account with this email already exists.");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write(gson.toJson(res));
            return;
        }

        User newUser = new User(name.trim(), email.trim().toLowerCase(), password, role.trim().toLowerCase());
        boolean success = userDAO.register(newUser);

        if (success) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", newUser);
            res.put("status", "success");
            res.put("message", "Registration successful.");
            res.put("user", newUser);
        } else {
            res.put("status", "error");
            res.put("message", "Registration failed due to a database error.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        response.getWriter().write(gson.toJson(res));
    }

    private void handleLogin(JsonObject body, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> res = new HashMap<>();

        String email = getString(body, "email", request.getParameter("email"));
        String password = getString(body, "password", request.getParameter("password"));

        if (email == null || password == null) {
            res.put("status", "error");
            res.put("message", "Email and password are required.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(res));
            return;
        }

        User user = userDAO.login(email.trim().toLowerCase(), password);

        if (user != null) {
            String requestedRole = getString(body, "role", request.getParameter("role"));
            if (requestedRole != null && !requestedRole.trim().isEmpty() && !requestedRole.equalsIgnoreCase(user.getRole())) {
                res.put("status", "error");
                res.put("message", "Access denied. This account has role '" + user.getRole() + "', not '" + requestedRole + "'.");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(gson.toJson(res));
                return;
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            res.put("status", "success");
            res.put("message", "Login successful.");
            res.put("user", user);
        } else {
            res.put("status", "error");
            res.put("message", "Invalid email or password.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        response.getWriter().write(gson.toJson(res));
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsAndJson(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCorsAndJson(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private JsonObject parseJsonBody(HttpServletRequest request) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            if (sb.length() > 0) {
                return JsonParser.parseString(sb.toString()).getAsJsonObject();
            }
        } catch (Exception ignored) {
        }
        return new JsonObject();
    }

    private String getString(JsonObject obj, String key, String fallback) {
        if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return fallback;
    }
}
