/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.types;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class TypeHandlerFactory {

    private static final Logger log = LogManager.getLogger(TypeHandlerFactory.class);

    public static TypeHandler getTypeHandler(String name) throws IllegalArgumentException{
        if (name.contains("(")) {
            String typeName = name.substring(0, name.indexOf("("));
            String args = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
            String[] helperArgs = {};
            if (!args.isEmpty()) {
                helperArgs = args.split(",");
                helperArgs = stripQuotes(helperArgs);
            }
            log.debug("Helper Args: " + Arrays.toString(helperArgs));
            switch (typeName) {
                case FirstName.TYPE_NAME: {
                    return new FirstName();
                }
                case LastName.TYPE_NAME: {
                    return new LastName();
                }
                case UuidType.TYPE_NAME: {
                    return new UuidType();
                }
                case RandomType.TYPE_NAME: {
                    return new RandomType(helperArgs);
                }
                case BooleanType.TYPE_NAME: {
                    return new BooleanType();
                }
                case LongType.TYPE_NAME: {
                    return new LongType(helperArgs);
                }
                case IntegerType.TYPE_NAME: {
                    return new IntegerType(helperArgs);
                }
                case DoubleType.TYPE_NAME: {
                    return new DoubleType(helperArgs);
                }
                case AlphaType.TYPE_NAME: {
                    return new AlphaType(helperArgs);
                }
                case AlphaNumericType.TYPE_NAME: {
                    return new AlphaNumericType(helperArgs);
                }
                case DateType.TYPE_NAME: {
                    try {
                        return new DateType(helperArgs);
                    } catch (ParseException ex) {
                        log.warn("Bad date format");
                        throw new IllegalArgumentException("Unable to create date genertor due to an invalid date. Please use the correct format of yyyy/MM/dd");
                    }
                }
                case TimestampType.TYPE_NAME: {
                    try {
                        return new TimestampType(helperArgs);
                    } catch (ParseException ex) {
                        log.warn("Bad date format");
                        throw new IllegalArgumentException("Unable to create date genertor due to an invalid date. Please use the correct format of yyyy/MM/dd");
                    }
                }
                case NowType.TYPE_NAME: {
                    return new NowType();
                }
                case NowTimestampType.TYPE_NAME: {
                    return new NowTimestampType();
                }
                default: {
                    return null;
                }

            }
        } else {
            //not a type handler
            return null;
        }
    }

    public static String[] stripQuotes(String[] list) {
        List<String> newList = new ArrayList<>();
        for (String item : list) {
            newList.add(item.replaceAll("'", "").replaceAll("\"", "").trim());
        }
        return newList.toArray(new String[]{});
    }
}
