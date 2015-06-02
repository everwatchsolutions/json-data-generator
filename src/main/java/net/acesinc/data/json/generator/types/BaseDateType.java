/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author andrewserff
 */
public abstract class BaseDateType extends TypeHandler {

    private Date min;
    private Date max;
//    public static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");
    public static final ThreadLocal<DateFormat> INPUT_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");
        }
    };

    public BaseDateType() {
    }

    @Override
    public void setLaunchArguments(String[] launchArguments) {
        super.setLaunchArguments(launchArguments);
        try {
            if (launchArguments.length == 0) {
                min = INPUT_DATE_FORMAT.get().parse("1970/01/01T00:00:00");
                max = new Date();
            } else if (launchArguments.length == 1) {
                //min only
                try {
                    min = INPUT_DATE_FORMAT.get().parse(stripQuotes(launchArguments[0]));
                } catch (ParseException pe) {
                    long timeOffset = NowBaseType.getTimeOffset(stripQuotes(launchArguments[0]));
                    min = new Date(new Date().getTime() + timeOffset);
                }
                max = new Date();
            } else if (launchArguments.length == 2) {
                try {
                    min = INPUT_DATE_FORMAT.get().parse(stripQuotes(launchArguments[0]));
                } catch (ParseException pe) {
                    long timeOffset = NowBaseType.getTimeOffset(stripQuotes(launchArguments[0]));
                    min = new Date(new Date().getTime() + timeOffset);
                }

                try {
                    max = INPUT_DATE_FORMAT.get().parse(stripQuotes(launchArguments[1]));
                } catch (ParseException pe) {
                    long timeOffset = NowBaseType.getTimeOffset(stripQuotes(launchArguments[1]));
                    max = new Date(new Date().getTime() + timeOffset);
                }
            }
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Provided date is invalid. Please use the format [ yyyy/MM/dd ]", ex);
        }

        if (!min.before(max) && !min.equals(max)) {
            throw new IllegalArgumentException("Min Date must be before Max Date");
        }
    }

    public Date getRandomDate() {
        GregorianCalendar gc = new GregorianCalendar();
        GregorianCalendar minCal = new GregorianCalendar();
        minCal.setTime(min);
        GregorianCalendar maxCal = new GregorianCalendar();
        maxCal.setTime(max);

        int year = getRand().nextInt(minCal.get(GregorianCalendar.YEAR), maxCal.get(GregorianCalendar.YEAR));
        gc.set(GregorianCalendar.YEAR, year);

        int month = -1;
        if (minCal.get(GregorianCalendar.YEAR) == maxCal.get(GregorianCalendar.YEAR)) {
            month = getRand().nextInt(minCal.get(GregorianCalendar.MONTH), maxCal.get(GregorianCalendar.MONTH));
        } else if (year == minCal.get(GregorianCalendar.YEAR)) {
            month = getRand().nextInt(minCal.get(GregorianCalendar.MONTH), gc.getActualMaximum(GregorianCalendar.MONTH));
        } else if (year == maxCal.get(GregorianCalendar.YEAR)) {
            month = getRand().nextInt(gc.getActualMinimum(GregorianCalendar.MONTH), maxCal.get(GregorianCalendar.MONTH));
        } else {
            month = getRand().nextInt(gc.getActualMinimum(GregorianCalendar.MONTH), gc.getActualMaximum(GregorianCalendar.MONTH));
        }
        gc.set(GregorianCalendar.MONTH, month);

        int day = -1;
        if (minCal.get(GregorianCalendar.YEAR) == maxCal.get(GregorianCalendar.YEAR) && minCal.get(GregorianCalendar.MONTH) == maxCal.get(GregorianCalendar.MONTH)) {
            day = getRand().nextInt(minCal.get(GregorianCalendar.DAY_OF_MONTH), maxCal.get(GregorianCalendar.DAY_OF_MONTH));
        } else if (year == minCal.get(GregorianCalendar.YEAR) && month == minCal.get(GregorianCalendar.MONTH)) {
            day = getRand().nextInt(minCal.get(GregorianCalendar.DAY_OF_MONTH), gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
        } else if (year == maxCal.get(GregorianCalendar.YEAR) && month == maxCal.get(GregorianCalendar.MONTH)) {
            day = getRand().nextInt(gc.getActualMinimum(GregorianCalendar.DAY_OF_MONTH), maxCal.get(GregorianCalendar.DAY_OF_MONTH));
        } else {
            day = getRand().nextInt(1, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
        }
        gc.set(GregorianCalendar.DAY_OF_MONTH, day);

        //generate a random time too
        int minHour = gc.getActualMinimum(GregorianCalendar.HOUR_OF_DAY);
        int minMin = gc.getActualMinimum(GregorianCalendar.MINUTE);
        int minSec = gc.getActualMinimum(GregorianCalendar.SECOND);
        int maxHour = gc.getActualMaximum(GregorianCalendar.HOUR_OF_DAY);
        int maxMin = gc.getActualMaximum(GregorianCalendar.MINUTE);
        int maxSec = gc.getActualMaximum(GregorianCalendar.SECOND);

        if (minCal.get(GregorianCalendar.YEAR) == gc.get(GregorianCalendar.YEAR) && minCal.get(GregorianCalendar.MONTH) == gc.get(GregorianCalendar.MONTH) && minCal.get(GregorianCalendar.DAY_OF_MONTH) == gc.get(GregorianCalendar.DAY_OF_MONTH)) {
            //same day as min.  Must be after the min hour, min, sec
            minHour = minCal.get(GregorianCalendar.HOUR_OF_DAY);
        }
        if (maxCal.get(GregorianCalendar.YEAR) == gc.get(GregorianCalendar.YEAR) && maxCal.get(GregorianCalendar.MONTH) == gc.get(GregorianCalendar.MONTH) && maxCal.get(GregorianCalendar.DAY_OF_MONTH) == gc.get(GregorianCalendar.DAY_OF_MONTH)) {
            //same day as max. Must be before max hour, min, sec
            maxHour = maxCal.get(GregorianCalendar.HOUR_OF_DAY);
        }
//        else {
//            //different day than either min or max. Time doesn't matter. 
//        }

        int hour = getRand().nextInt(minHour, maxHour);
        gc.set(GregorianCalendar.HOUR_OF_DAY, hour);
        if (minHour == maxHour) {
            minMin = minCal.get(GregorianCalendar.MINUTE);
            maxMin = maxCal.get(GregorianCalendar.MINUTE);
        } else if (hour == minHour) {
            minMin = minCal.get(GregorianCalendar.MINUTE);
        } else if (hour == maxHour) {
            maxMin = maxCal.get(GregorianCalendar.MINUTE);
        }
        
        int minute = getRand().nextInt(minMin, maxMin);
        gc.set(GregorianCalendar.MINUTE, minute);
        
        if (minHour == maxHour && minMin == maxMin) {
            minSec = minCal.get(GregorianCalendar.SECOND);
            maxSec = maxCal.get(GregorianCalendar.SECOND);
        } else if (hour == minHour && minute == minMin) {
            minSec = minCal.get(GregorianCalendar.SECOND);
        } else if (hour == maxHour && minute == maxMin) {
            maxSec = maxCal.get(GregorianCalendar.SECOND);
        }
        int sec = getRand().nextInt(minSec, maxSec);
        gc.set(GregorianCalendar.SECOND, sec);

        //clear MS because we don't care about that much precision
        gc.set(GregorianCalendar.MILLISECOND, 0);

        return gc.getTime();
    }
}
