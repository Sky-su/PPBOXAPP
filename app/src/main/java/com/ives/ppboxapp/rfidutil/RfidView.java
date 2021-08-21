package com.ives.ppboxapp.rfidutil;

import androidx.annotation.Nullable;

import java.util.Objects;

public class RfidView {

    private String name;
    private int number;

    public RfidView(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, number);
    }

    @Override
    public boolean equals(@Nullable Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RfidView baseBean = (RfidView) o;
        return number == baseBean.number &&
                name.equals(baseBean.name);
    }

    @Override
    public String toString() {
        return "RfidView{" +
                "name='" + name + '\'' +
                ", number=" + number +
                '}';
    }
}
