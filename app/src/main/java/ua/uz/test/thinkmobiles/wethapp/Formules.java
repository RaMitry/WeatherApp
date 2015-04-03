package ua.uz.test.thinkmobiles.wethapp;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Formules {

    public static double convertToCelsius(double temp)
    {
        double temp_celsius = temp - 273.15;
        Double truncatedDouble=new BigDecimal(temp_celsius ).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        return truncatedDouble;
    }


    public static String getTime(long timestamp)
    {
        long dv = timestamp*1000;
        Date df = new java.util.Date(dv);
        String time = new SimpleDateFormat("hh:mm a").format(df);
        return time;
    }
}
