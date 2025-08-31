
package hibernate;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "task")
public class Task implements Serializable{
    
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user_id;
    
    @Column(name = "title", length = 15, nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;
    
    @ManyToOne
    @JoinColumn(name="task_status_id")
    private TaskStatus task_status_id;

    @Column(name = "created_at", nullable = false)
    private Date created_at;
    
    @Column(name = "updated_at", length = 45, nullable = false)
    private Date updated_at;

    public Task(){}
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser_id() {
        return user_id;
    }

    public void setUser_id(User user_id) {
        this.user_id = user_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getTask_status_id() {
        return task_status_id;
    }

    public void setTask_status_id(TaskStatus task_status_id) {
        this.task_status_id = task_status_id;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
}
