package com.company.apimanager.app;

import org.springframework.stereotype.Component;

@Component
public class TestObj {
    private int userId;
    private int id;
    private String title;
    private Boolean completed;

    public TestObj()
    {
        this.userId = 10;
        this.id = 100;
        this.title = "123456";
        this.completed = true;
    }
    public TestObj(int userId, int id, String title, Boolean completed)
    {
        this.userId = userId;
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public String GetTitle() {
        return  title;
    }
}