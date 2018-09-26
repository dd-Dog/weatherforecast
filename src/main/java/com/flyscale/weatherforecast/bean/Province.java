package com.flyscale.weatherforecast.bean;

import android.text.TextUtils;

/**
 * Created by MrBian on 2017/11/24.
 */

public class Province {
    public String name;
    public String sort;
    public String remark;
    public static final String  TYPE_AUTONOMOUS = "自治区";
    public static final String TYPE_MUNICIPALITIES = "直辖市";
    public static final String TYPE_PROVINCE = "省份";
    public static final String TYPE_SPECIAL = "特别行政区";
    private boolean pro;

    public Province(String name, String sort, String remark) {
        this.name = name;
        this.sort = sort;
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "Province{" +
                "name='" + name + '\'' +
                ", sort='" + sort + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }

    public Province(String name, String sort) {
        this.name = name;
        this.sort = sort;
    }

    public boolean isPro() {
        return TextUtils.equals(remark, TYPE_PROVINCE);
    }
}
