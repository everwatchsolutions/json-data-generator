/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.data.json.generator.log;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrewserff
 */
public class FileLogger implements EventLogger {

    private static final Logger log = LogManager.getLogger(FileLogger.class);
    public static final String OUTPUT_DIRECTORY_PROP_NAME = "output.directory";
    public static final String FILE_PREFIX_PROP_NAME = "file.prefix";
    public static final String FILE_EXTENSION_PROP_NAME = "file.extension";
    public static final String NUM_OF_LINES = "file.lines";


    private File outputDirectory;
    private String filePrefix;
    private String fileExtension;
    private int numOfLines = 1;
    private int counterOfLines = 1;
    File f;

    public FileLogger(Map<String, Object> props) throws IOException {
        String outputDir = (String) props.get(OUTPUT_DIRECTORY_PROP_NAME);
        outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdir()) {
                if (!outputDirectory.mkdirs()) {
                    throw new IOException("Output directory does not exist and we are unable to create it");
                }
            }
        }
        filePrefix = (String) props.get(FILE_PREFIX_PROP_NAME);
        fileExtension = (String) props.get(FILE_EXTENSION_PROP_NAME);
        numOfLines = Integer.valueOf((String) props.get(NUM_OF_LINES));
        f = File.createTempFile(filePrefix, fileExtension, outputDirectory);
        log.info("Running with: Number of lines: "+numOfLines);

    }

    @Override
    public void logEvent(String event, Map<String, Object> producerConfig) {
        logEvent(event);
    }
    
    private void logEvent(String event) {
        try {
            if (counterOfLines > numOfLines){
                f = File.createTempFile(filePrefix, fileExtension, outputDirectory);
                counterOfLines=1;
            }else{
                FileUtils.writeStringToFile(f, event+System.lineSeparator(), "UTF-8", true);
                counterOfLines++;
            }
        } catch (IOException ioe) {
            log.error("Unable to create temp file");
        }

    }

    @Override
    public void shutdown() {
        //we don't need to shut anything down
    }

}
