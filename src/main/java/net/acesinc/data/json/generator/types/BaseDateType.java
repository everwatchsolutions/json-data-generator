/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

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
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    public BaseDateType(String... args) throws ParseException {
        super();
        if (args.length == 0) {
            min = sdf.parse("1970/01/01");
            max = new Date();
        } else if (args.length == 1) {
            //min only
            min = sdf.parse(args[0]);
            max = new Date();
        } else if (args.length == 2) {
            min = sdf.parse(args[0]);
            max = sdf.parse(args[1]);
        }
        
        if (!min.before(max)) {
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
        
        return gc.getTime();
    }
}
