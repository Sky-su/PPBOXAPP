package com.ives.ppboxapp.model;

import java.util.List;

/**
 * 一级分类，相当于左侧菜单
 * Created by hanj on 14-9-25.
 */
public class FirstClassItem {
    private int id;
    private String name;
    private List<Company> secondList;

    public FirstClassItem(){

    }

    public FirstClassItem(int id, String name, List<Company> secondList) {
        this.id = id;
        this.name = name;
        this.secondList = secondList;
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

    public List<Company> getSecondList() {
        return secondList;
    }

    public void setSecondList(List<Company> secondList) {
        this.secondList = secondList;
    }

    @Override
    public String toString() {
        return "FirstClassItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", secondList=" + secondList +
                '}';
    }
}
