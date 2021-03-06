package com.flyscale.weatherforecast.bean;

import java.util.ArrayList;

/**
 * Created by MrBian on 2017/11/23.
 */

@Deprecated
public class WeatherToken {
    public WeatherInfos data;


    public class WeatherInfos {
        public String city;
        public ArrayList<Forecast> forecast;

        public class Forecast {
            public String date;
            public String fengli;
            public String fengxiang;
            public String high;
            public String low;
            public String type;

            @Override
            public String toString() {
                return "Forecast{" +
                        "date='" + date + '\'' +
                        ", fengli='" + fengli + '\'' +
                        ", fengxiang='" + fengxiang + '\'' +
                        ", high='" + high + '\'' +
                        ", low='" + low + '\'' +
                        ", type='" + type + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "WeatherInfos{" +
                    "city='" + city + '\'' +
                    ", forecast=" + forecast +
                    '}';
        }
    }

    public WeatherInfos getData() {
        return data;
    }

    public void setData(WeatherInfos data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "WeatherToken{" +
                "data=" + data +
                '}';
    }
}
