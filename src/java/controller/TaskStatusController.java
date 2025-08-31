package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.TaskStatus;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

@WebServlet(name = "TaskStatusController", urlPatterns = {"/api/taskstatus/*"})
public class TaskStatusController extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        setCorsHeaders(response);
        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        
        try {
            handleGetAllTaskStatuses(response, out);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(createErrorResponse("Internal server error")));
        } finally {
            out.flush();
        }
    }

    private void handleGetAllTaskStatuses(HttpServletResponse response, PrintWriter out) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            // Use Criteria API to get all task statuses
            Criteria criteria = session.createCriteria(TaskStatus.class);
            criteria.addOrder(Order.asc("id"));
            
            List<TaskStatus> taskStatuses = criteria.list();
            out.print(gson.toJson(createSuccessResponse("Task statuses retrieved successfully", taskStatuses)));
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(createErrorResponse("Failed to retrieve task statuses")));
        } finally {
            session.close();
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