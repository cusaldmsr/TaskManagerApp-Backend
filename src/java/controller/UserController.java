package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.User;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "UserController", urlPatterns = {"/api/user/*"})
public class UserController extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        setCorsHeaders(response);
        response.setContentType("application/json");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if ("/login".equals(pathInfo)) {
                handleLogin(request, response, out);
            } else if ("/register".equals(pathInfo)) {
                handleRegister(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(createErrorResponse("Endpoint not found")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(createErrorResponse("Internal server error")));
        } finally {
            out.flush();
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Session session = null;
        
        try {
            // Parse request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject requestJson = gson.fromJson(sb.toString(), JsonObject.class);
            String email = requestJson.get("email").getAsString();
            String password = requestJson.get("password").getAsString();

            session = HibernateUtil.getSessionFactory().openSession();
            
            // Use Criteria API to find user
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", email));
            criteria.add(Restrictions.eq("password", password));
            
            List<User> users = criteria.list();

            if (!users.isEmpty()) {
                User user = users.get(0);
                
                // Create response object
                JsonObject responseData = new JsonObject();
                JsonObject userJson = gson.toJsonTree(user).getAsJsonObject();
                userJson.remove("password"); // Don't send password back
                
                responseData.add("user", userJson);
                responseData.addProperty("token", "user_token_" + user.getId());
                
                out.print(gson.toJson(createSuccessResponse("Login successful", responseData)));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(gson.toJson(createErrorResponse("Invalid email or password")));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(createErrorResponse("Invalid request format")));
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Transaction transaction = null;
        Session session = null;
        
        try {
            // Parse request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject requestJson = gson.fromJson(sb.toString(), JsonObject.class);
            String username = requestJson.get("username").getAsString();
            String email = requestJson.get("email").getAsString();
            String password = requestJson.get("password").getAsString();

            session = HibernateUtil.getSessionFactory().openSession();
            
            // Check if email already exists using Criteria
            Criteria emailCriteria = session.createCriteria(User.class);
            emailCriteria.add(Restrictions.eq("email", email));
            List<User> existingUsers = emailCriteria.list();
            
            if (!existingUsers.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(gson.toJson(createErrorResponse("Email already registered")));
                return;
            }

            // Create new user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setCreated_at(new Date());

            transaction = session.beginTransaction();
            session.save(newUser);
            transaction.commit();

            // Remove password from response
            JsonObject userJson = gson.toJsonTree(newUser).getAsJsonObject();
            userJson.remove("password");
            
            out.print(gson.toJson(createSuccessResponse("Registration successful", userJson)));
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(createErrorResponse("Registration failed")));
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    private JsonObject createSuccessResponse(String message, Object data) {
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("message", message);
        if (data != null) {
            response.add("data", gson.toJsonTree(data));
        }
        return response;
    }

    private JsonObject createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("success", false);
        response.addProperty("message", message);
        return response;
    }
}