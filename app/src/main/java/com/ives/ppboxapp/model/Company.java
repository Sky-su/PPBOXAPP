package com.ives.ppboxapp.model;

public class Company {
    private int id;
    private String name;
    private int pid;
    private String editable;
    private String deleted;
    private String create_id;

    public Company() {
    }

    public Company(int id, String name, int pid) {
        this.id = id;
        this.name = name;
        this.pid = pid;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getEditable() {
        return editable;
    }

    public void setEditable(String editable) {
        this.editable = editable;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getCreate_id() {
        return create_id;
    }

    public void setCreate_id(String create_id) {
        this.create_id = create_id;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pid=" + pid +
                ", editable='" + editable + '\'' +
                ", deleted='" + deleted + '\'' +
                ", create_id='" + create_id + '\'' +
                '}';
    }
}
