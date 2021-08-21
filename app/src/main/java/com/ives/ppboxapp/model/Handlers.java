package com.ives.ppboxapp.model;

public class Handlers {

    /**用户名**/
    private String account;
    /**密码**/
    private String password;
    /**真实名称**/
    private String name;
    /****/
    private int type;
    /****/
    private int id;
    /****/
    private int create_type;
    /****/
    private String create_id;

    public Handlers() {
    }

    public Handlers(String account, String password, String name, int type, int id, int create_type, String create_id) {
        this.account = account;
        this.password = password;
        this.name = name;
        this.type = type;
        this.id = id;
        this.create_type = create_type;
        this.create_id = create_id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreate_type() {
        return create_type;
    }

    public void setCreate_type(int create_type) {
        this.create_type = create_type;
    }

    public String getCreate_id() {
        return create_id;
    }

    public void setCreate_id(String create_id) {
        this.create_id = create_id;
    }
}
