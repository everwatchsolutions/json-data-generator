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

    private File outputDirectory;
    private String filePrefix;
    private String fileExtension;

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
    }

    @Override
    public void logEvent(String event, Map<String, Object> producerConfig) {
        logEvent(event);
    }
    
    private void logEvent(String event) {
        try {
            File f = File.createTempFile(filePrefix, fileExtension, outputDirectory);
            FileUtils.writeStringToFile(f, event, "UTF-8");
        } catch (IOException ioe) {
            log.error("Unable to create temp file");
        }

    }

    @Override
    public void shutdown() {
        //we don't need to shut anything down
    }

}
