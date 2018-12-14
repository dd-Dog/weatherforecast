package com.flyscale.weatherforecast.bean;

/**
 * Created by MrBian on 2017/11/24.
 */

public class Zone {
    public String name;
    public String code;

    public Zone(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Zone(String name) {

        this.name = name;
    }

    @Override
    public String toString() {
        return "Zone{" +
                "name='" + name + '\'' +
                '}';
    }
}
