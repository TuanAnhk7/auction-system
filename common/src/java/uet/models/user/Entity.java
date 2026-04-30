package src.java.uet.models.user;

import java.io.Serializable;
import java.util.Date;

public abstract class Entity implements Serializable {
    private String id;
    private Date createTime;
    private Date modifiedTime;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getModifiedTime() { return modifiedTime; }
    public void setModifiedTime(Date modifiedTime) { this.modifiedTime = modifiedTime; }
}