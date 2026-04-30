package src.java.uet;

import java.util.Date;
import java.util.UUID;

public abstract class Entity {
    protected String id;
    protected Date createTime;
    protected Date modifiedTime;

    public Entity() {

        this.id = UUID.randomUUID().toString();
        this.createTime = new Date();
        this.modifiedTime = new Date();
    }

    public String getId() { return id; }

    public Date getCreateTime() { return createTime; }

    public void updateModifiedTime() {
        this.modifiedTime = new Date();
    }
}
