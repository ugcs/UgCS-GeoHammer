package com.github.thecoldwine.sigrun.common.ext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugcs.gprvisualizer.app.parcers.GeoCoordinates;
import com.ugcs.gprvisualizer.app.parcers.GeoData;
import com.ugcs.gprvisualizer.app.parcers.csv.CSVParsersFactory;
import com.ugcs.gprvisualizer.app.parcers.csv.CsvParser;
import com.ugcs.gprvisualizer.app.yaml.FileTemplates;

public class CsvFile extends SgyFile {

	private static final Logger log = LoggerFactory.getLogger(CsvFile.class.getName());

    private Map<File, List<GeoData>> geoData = new HashMap<>();

	private CsvParser parser;

    private FileTemplates fileTemplates;

	public CsvFile(FileTemplates fileTemplates) {
		this.fileTemplates = fileTemplates;
	}

    private CsvFile(CsvFile file) {
        this(file.fileTemplates);
        this.setFile(file.getFile());
        this.setParser(file.getParser());
    }

    @Override
    public void open(File csvFile) throws Exception {

        String csvFileAbsolutePath = csvFile.getAbsolutePath();
        var fileTemplate = fileTemplates.findTemplate(fileTemplates.getTemplates(), csvFileAbsolutePath);

        if (fileTemplate == null) {
            throw new RuntimeException("Can`t find template for file " + csvFile.getName());
        }

        log.debug("template: {}", fileTemplate.getName());

        parser = new CSVParsersFactory().createCSVParser(fileTemplate);

        List<GeoCoordinates> coordinates = parser.parse(csvFileAbsolutePath);

        if (getFile() == null) {
            setFile(csvFile);
        }

        for (GeoCoordinates coord : coordinates) {
            // Added points if lat and lon are not 0 and point is close to the first point
            if (coord.getLatitude().intValue() != 0 && coord.getLongitude().intValue() != 0 
                    && (getGeoData().isEmpty()  
                        || (Math.abs(coord.getLatitude().intValue() 
                            - getGeoData().get(0).getLatitude().intValue()) <= 1  
                        && Math.abs(coord.getLongitude().intValue() 
                            - getGeoData().get(0).getLongitude().intValue()) <= 1))) {
                getTraces().add(new Trace(this, null, null, new float[] {},
                new LatLon(coord.getLatitude(), coord.getLongitude())));
                if (coord instanceof GeoData) {
                    getGeoData().add((GeoData) coord);
                }
            }            
        }
    }

    @Override
    public void save(File file) throws Exception {
			Path inputFile = getFile().toPath();
        	Path tempFile = file.toPath();

        	try (BufferedReader reader = Files.newBufferedReader(inputFile);
            	BufferedWriter writer = Files.newBufferedWriter(tempFile)) {

                String skippedLines = parser.getSkippedLines();
                
                // check if "Next WP" exists and is it the last column
                String nextWPColumnName = "Next WP";
                boolean isNextWPLast = false;
                log.debug("Source file skippedLines: {}", skippedLines);
                if (skippedLines.contains(nextWPColumnName)) {
                    isNextWPLast = skippedLines.endsWith(nextWPColumnName + System.lineSeparator());
                } else {
                    // add "Next WP" to the end of the header if not exists
                    skippedLines = skippedLines.replaceAll(System.lineSeparator() + "$", "," + nextWPColumnName + System.lineSeparator());
                    isNextWPLast = true;
                }

				writer.write(skippedLines);

            	String line;
            	int lineNumber = 0;

				Map<Integer, GeoData> geoDataMap = getGeoData().stream().collect(Collectors.toMap(GeoData::getLineNumber, gd -> gd));

                String valueTemplate = isNextWPLast ? ",%s" : ",%s,";

            	while ((line = reader.readLine()) != null) {
                	lineNumber++;
                	if (geoDataMap.keySet().contains(lineNumber)) {
						GeoData gd = geoDataMap.get(lineNumber);
						var lineSensor = gd.getLine();
						
                        if(line.indexOf(String.format(valueTemplate, lineSensor.originalData())) != -1) {
						    line = line.replaceFirst(String.format(valueTemplate, lineSensor.originalData()) + (isNextWPLast ? "$" : ""), String.format(valueTemplate, lineSensor.data()));
                        } else {
                            line = line + String.format(valueTemplate, lineSensor.data());
                        }

                    	writer.write(line);
                    	writer.newLine();
                	}
            	}

        	} catch (IOException e) {
            	e.printStackTrace();
        	}
    }

    public List<GeoData> getGeoData() {
		return geoData.computeIfAbsent(getFile(), k -> new ArrayList<>());
    }

    private void setParser(CsvParser parser) {
		this.parser = parser;
    }

	public CsvParser getParser() {
		return parser;
	}

    @Override
    public CsvFile copy() {
        return new CsvFile(this);
    }

    @Override
    public void saveAux(File file) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveAux'");
    }

    @Override
    public double getSamplesToCmGrn() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSamplesToCmGrn'");
    }

    @Override
    public double getSamplesToCmAir() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSamplesToCmAir'");
    }

    @Override
    public SgyFile copyHeader() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copyHeader'");
    }

    @Override
    public int getSampleInterval() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSampleInterval'");
    }

}
