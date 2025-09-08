package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Task;
import hibernate.User;
import hibernate.TaskStatus;
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
import org.hibernate.criterion.Order;
import com.google.gson.GsonBuilder;

@WebServlet(name = "TaskController", urlPatterns = {"/api/tasks/*"})
public class TaskController extends HttpServlet {

    private Gson gson = new GsonBuilder()
    .setDateFormat("yyyy-MM-dd HH:mm:ss")
    .create();

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
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo != null && pathInfo.startsWith("/user/")) {
                // Get tasks by user ID: /api/tasks/user/{userId}
                String userIdStr = pathInfo.substring(6); // Remove "/user/"
                int userId = Integer.parseInt(userIdStr);
                handleGetTasksByUser(userId, response, out);
            } else if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // Get task by ID: /api/tasks/{taskId}
                String taskIdStr = pathInfo.substring(1); // Remove leading "/"
                int taskId = Integer.parseInt(taskIdStr);
                handleGetTaskById(taskId, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(createErrorResponse("Invalid endpoint")));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(createErrorResponse("Invalid ID format")));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(createErrorResponse("Internal server error")));
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        setCorsHeaders(response);
        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        
        try {
            handleCreateTask(request, response, out);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(createErrorResponse("Internal server error")));
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        setCorsHeaders(response);
        response.setContentType("application/json");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                String taskIdStr = pathInfo.substring(1);
                int taskId = Integer.parseInt(taskIdStr);
                handleUpdateTask(taskId, request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(createErrorResponse("Invalid task ID")));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(createErrorResponse("Invalid task ID format")));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(createErrorResponse("Internal server error")));
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        setCorsHeaders(response);
        response.setContentType("application/json");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                String taskIdStr = pathInfo.substring(1);
                int taskId = Integer.parseInt(taskIdStr);
                handleDeleteTask(taskId, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(createErrorResponse("Invalid task ID")));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(createErrorResponse("Invalid task ID format")));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(createErrorResponse("Internal server error")));
        } finally {
            out.flush();
        }
    }

    private void handleGetTasksByUser(int userId, HttpServletResponse response, PrintWriter out) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            // Use Criteria API to get tasks by user
            Criteria criteria = session.createCriteria(Task.class);
            criteria.createAlias("user_id", "user");
            criteria.add(Restrictions.eq("user.id", userId));
            criteria.addOrder(Order.desc("created_at"));
            
            List<Task> tasks = criteria.list();
            out.print(gson.toJson(createSuccessResponse("Tasks retrieved successfully", tasks)));
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(createErrorResponse("Failed to retrieve tasks")));
        } finally {
            session.close();
        }
    }

    private void handleGetTaskById(int taskId, HttpServletResponse response, PrintWriter out) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Task task = (Task) session.get(Task.class, taskId);
            
            if (task != null) {
                out.print(gson.toJson(createSuccessResponse("Task retrieved successfully", task)));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(createErrorResponse("Task not found")));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(createErrorResponse("Failed to retrieve task")));
        } finally {
            session.close();
        }
    }

    private void handleCreateTask(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
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
            String title = requestJson.get("title").getAsString();
            String description = requestJson.get("description").getAsString();
            int taskStatusId = requestJson.get("task_status_id").getAsInt();
            int userId = requestJson.get("user_id").getAsInt();

            session = HibernateUtil.getSessionFactory().openSession();
            
            // Get user and task status using session.get()
            User user = (User) session.get(User.class, userId);
            TaskStatus taskStatus = (TaskStatus) session.get(TaskStatus.class, taskStatusId);
            
            if (user == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(createErrorResponse("User not found")));
                return;
            }
            
            if (taskStatus == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(createErrorResponse("Task status not found")));
                return;
            }

            // Create new task
            Task newTask = new Task();
            newTask.setTitle(title);
            newTask.setDescription(description);
            newTask.setUser_id(user);
            newTask.setTask_status_id(taskStatus);
            newTask.setCreated_at(new Date());
            newTask.setUpdated_at(new Date());

            transaction = session.beginTransaction();
            session.save(newTask);
            transaction.commit();

            out.print(gson.toJson(createSuccessResponse("Task created successfully", newTask)));
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(createErrorResponse("Failed to create task")));
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void handleUpdateTask(int taskId, HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
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
            String title = requestJson.get("title").getAsString();
            String description = requestJson.get("description").getAsString();
            int taskStatusId = requestJson.get("task_status_id").getAsInt();

            session = HibernateUtil.getSessionFactory().openSession();
            
            // Get existing task
            Task existingTask = (Task) session.get(Task.class, taskId);
            
            if (existingTask == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(createErrorResponse("Task not found")));
                return;
            }
            
            // Get task status
            TaskStatus taskStatus = (TaskStatus) session.get(TaskStatus.class, taskStatusId);
            
            if (taskStatus == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(createErrorResponse("Task status not found")));
                return;
            }

            // Update task
            existingTask.setTitle(title);
            existingTask.setDescription(description);
            existingTask.setTask_status_id(taskStatus);
            existingTask.setUpdated_at(new Date());

            transaction = session.beginTransaction();
            session.update(existingTask);
            transaction.commit();

            out.print(gson.toJson(createSuccessResponse("Task updated successfully", existingTask)));
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(createErrorResponse("Failed to update task")));
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void handleDeleteTask(int taskId, HttpServletResponse response, PrintWriter out) {
        Transaction transaction = null;
        Session session = null;
        
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            Task task = (Task) session.get(Task.class, taskId);
            
            if (task == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(createErrorResponse("Task not found")));
                return;
            }

            transaction = session.beginTransaction();
            session.delete(task);
            transaction.commit();

            out.print(gson.toJson(createSuccessResponse("Task deleted successfully", null)));
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(createErrorResponse("Failed to delete task")));
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