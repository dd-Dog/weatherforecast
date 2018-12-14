package com.flyscale.weatherforecast.bean;

/**
 * Created by bian on 2018/12/14.
 */

public class WeatherInfos {
    /**
     * {"weatherinfo":{"city":"太仓","cityid":"101190408","temp1":"19℃","temp2":"25℃",
     * "weather":"大雨","img1":"n9.gif","img2":"d9.gif","ptime":"18:00"}}
     */
    public class WeatherInfo {
        public String city;
        public String cityid;
        public String temp1;
        public String temp2;
        public String weather;
        public String ptime;
        public String img1;
        public String img2;

        @Override
        public String toString() {
            return "WeatherInfo{" +
                    "city='" + city + '\'' +
                    ", cityid='" + cityid + '\'' +
                    ", temp1='" + temp1 + '\'' +
                    ", temp2='" + temp2 + '\'' +
                    ", weather='" + weather + '\'' +
                    ", ptime='" + ptime + '\'' +
                    ", img1='" + img1 + '\'' +
                    ", img2='" + img2 + '\'' +
                    '}';
        }
    }

    public WeatherInfo weatherinfo;

    @Override
    public String toString() {
        return "WeatherInfos{" +
                "weatherinfo=" + weatherinfo +
                '}';
    }
}
