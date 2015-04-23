/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);

    private static int currentPosition = 0;

    private static RandomDataGenerator rand = new RandomDataGenerator(); 
    
    public static Map<String, Object> readConfig(InputStream input) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(input, writer, "utf-8");
        String theString = writer.toString();

        Map<String, Object> props = processString(theString, currentPosition);

        return props;
    }

    public static Map<String, Object> processString(String s, int position) {
        Map<String, Object> props = new LinkedHashMap<>();
        List<Map<String, Object>> arrayBuilderList = new ArrayList<>();
        String curProp = null;
        StringBuilder sb = new StringBuilder();
        boolean objectStarted = false;
        boolean arrayStarted = false;
        boolean methodStarted = false;
        boolean singleQuoteStarted = false;
        boolean stringStarted = false;
        boolean propValueStarted = false;

        int slashCount = 0;
        int startCount = 0;
        boolean inComment = false;
        boolean inCommentBlock = false;

        while (currentPosition < s.length()) {
            char c = s.charAt(currentPosition);

            //First switch checks for comments
            switch (c) {
                case '/': {
                    if (!methodStarted && !inCommentBlock || (inCommentBlock && startCount == 2)) {
                        slashCount++;
                    }
                    if (methodStarted) {
                        sb.append(c);
                    }

                    if (slashCount == 2 && !inCommentBlock) {
                        inComment = true;
                    } else if (inCommentBlock && slashCount == 2) {
                        inCommentBlock = false;
                        slashCount = 0;
                    }
                    break;
                }
                case '*': {
                    startCount++;
                    if (slashCount == 1 && !inComment) {
                        inCommentBlock = true;
                    }
                    break;
                }
                case '\n': {
                    if (!inCommentBlock) {
                        slashCount = 0;
                    }
                    inComment = false;
                    break;
                }
            }

            if (!inComment && !inCommentBlock) {
                //If we aren't in a comment, then process the characters to see what's going on
                switch (c) {
                    case '/':
                    case '*':
                        //these were handled above
                        break;
                    case '{': {
                        //start object
                        //let's see if they put any specal instructions before this.
                        String specialFunc = null;
                        String[] specialFuncArgs = {};
                        if (sb.length() > 0) {
                            String name = sb.toString();
                            sb = new StringBuilder();
                            specialFunc = name.substring(0, name.indexOf("("));
                            String args = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
                            
                            if (!args.isEmpty()) {
                                specialFuncArgs = args.split(",");
                            }
                        }
                        objectStarted = true;
                        Map<String, Object> objectProps = processString(s, currentPosition++);
                        if (propValueStarted && !arrayStarted) {
                            props.put(curProp, objectProps);
                        } else if (arrayStarted) {
                            if (specialFunc != null) {
                                switch (specialFunc) {
                                    case "repeat": {
                                        int timesToRepeat = 1;
                                        if (specialFuncArgs.length == 1) {
                                            timesToRepeat = Integer.parseInt(specialFuncArgs[0]);
                                        } else {
                                            timesToRepeat = rand.nextInt(0, 10);
                                        }
                                        for (int i = 0; i < timesToRepeat; i++) {
                                            arrayBuilderList.add(objectProps);
                                        }
                                        break;
                                    }
                                }
                            } else {
                                arrayBuilderList.add(objectProps);
                            }
                        } else {
                            Map<String, Object> holder = new LinkedHashMap<>();
                            holder.put(null, objectProps);
                            return holder;
                        }
                        break;
                    }
                    case '}': {
                        //end object
                        objectStarted = false;
                        propValueStarted = false;
                        if (!arrayStarted) {
                            if (props.isEmpty() && sb.length() > 0) {
                                props.put(curProp, sb.toString());
                            }
                            return props;
                        }
                    }
                    case '[': {
                        //start array
                        //XXX What about arrays inside of arrays?!
                        arrayStarted = true;
                        arrayBuilderList = new ArrayList<>();
                        break;
                    }
                    case ']': {
                        //end array
                        arrayStarted = false;
                        propValueStarted = false;
                        props.put(curProp, arrayBuilderList);
                        break;
                    }
                    case '(': {
                        //start method params
                        if (!arrayStarted) {
                        methodStarted = true;
                        
                        }
                        sb.append(c);
                        break;
                    }
                    case ')': {
                        //end method params
                        sb.append(c);
                        if (!arrayStarted) {
                            methodStarted = false;
                            props.put(curProp, sb.toString());
                            sb = new StringBuilder();
                        }
                        
                        break;
                    }
                    case '"': {
                        //start/end quote
                        if (stringStarted && !methodStarted && !propValueStarted) {
                            //we are ending a property
                            curProp = sb.toString();
                            sb = new StringBuilder();
                        }
                        if (!methodStarted && !propValueStarted) {
                            stringStarted = !stringStarted;
                        } else {
                            sb.append(c);
                        }
                        break;
                    }
                    case '\'': {
                        //start/end quote
                        singleQuoteStarted = !singleQuoteStarted;
                        break;
                    }
                    case ':': {
                        //start of prop value
                        propValueStarted = true;
                        break;
                    }
                    case ',': {
                        //end of a line
                        if (!methodStarted && !arrayStarted) {
                            propValueStarted = false;
                            if (sb.length() > 0) {
                                props.put(curProp, sb.toString());
                                sb = new StringBuilder();
                            }
                        } else if (methodStarted) {
                            sb.append(c);
                        }

                        break;
                    }
                    case '\n': {
                        if (propValueStarted && !arrayStarted) {
                            if (sb.length() > 0) {
                                props.put(curProp, sb.toString());
                                sb = new StringBuilder();
                            }
                            propValueStarted = false;
                        }
                        break;
                    }
                    case ' ': {
                        //ignore space and return 
                        break;
                    }
                    default: {
                        //just a plain ol-character
                        sb.append(c);
                        break;
                    }

                }
            }
            currentPosition++;
        }
        return props;
    }
}
