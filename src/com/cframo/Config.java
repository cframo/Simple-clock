package com.cframo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Config {

    boolean mode24;
    boolean date;
    boolean lang;
    boolean haveAlarm;
    String alarm="";

    public String getAMPM(){
        if (!mode24)
            return alarm.substring(6,8);
        return "";
    }

    public String getHour12(){
        if (!mode24)
            return alarm.substring(0,5);
        return "";
    }

    public void convert_time(boolean to24)
    {
        SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm");
        SimpleDateFormat hour12 = new SimpleDateFormat("KK:mm aa");
        Date date;

        try {
            if (to24){
                date = hour12.parse(alarm);
                alarm = hour24.format(date);
            }else{
                date = hour24.parse(alarm);
                alarm =  hour12.format(date);
            }
        }catch (Exception e){
            System.out.println("Error en la conversion de horas");
        }
    }
}
