package com.flyscale.weatherforecast.bean;

/**
 * Created by MrBian on 2017/11/24.
 */

public class City {
    public String name;
    public String sort;

    public City(String name, String sort) {
        this.name = name;
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "Province{" +
                "name='" + name + '\'' +
                ", sort='" + sort + '\'' +
                '}';
    }
}
