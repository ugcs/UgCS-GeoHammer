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

import com.ugcs.gprvisualizer.app.parcers.GeoCoordinates;
import com.ugcs.gprvisualizer.app.parcers.GeoData;
import com.ugcs.gprvisualizer.app.parcers.csv.CSVParsersFactory;
import com.ugcs.gprvisualizer.app.parcers.csv.CsvParser;
import com.ugcs.gprvisualizer.app.yaml.FileTemplates;

public class CsvFile extends SgyFile {

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

        System.out.println("template: " + fileTemplate.getName());
        
        parser = new CSVParsersFactory().createCSVParser(fileTemplate);

        //try {
            List<GeoCoordinates> coordinates = parser.parse(csvFileAbsolutePath);

            if (getFile() == null) {
                setFile(csvFile);
            }

            for (GeoCoordinates coord : coordinates) {
                getTraces().add(new Trace(this, null, null, new float[] {},
                        new LatLon(coord.getLatitude(), coord.getLongitude())));
                if (coord instanceof GeoData) {
                    getGeoData().add((GeoData) coord);
                }
            }

        //} catch (Exception e) {
            // TODO Auto-generated catch block
        //    e.printStackTrace();
        //}
    }

    @Override
    public void save(File file) throws Exception {
        		//if (isCsvFile()) {
			Path inputFile = getFile().toPath();
        	Path tempFile = file.toPath();

        	try (BufferedReader reader = Files.newBufferedReader(inputFile);
            	BufferedWriter writer = Files.newBufferedWriter(tempFile)) {

				CsvParser parser = getParser();	

				writer.write(parser.getSkippedLines());

            	String line;
            	int lineNumber = 0;

				Map<Integer, GeoData> geoDataMap = getGeoData().stream().collect(Collectors.toMap(gd -> gd.getLineNumber(), gd -> gd));

            	while ((line = reader.readLine()) != null) {
                	lineNumber++;
                	if (geoDataMap.keySet().contains(lineNumber)) {
						GeoData gd = geoDataMap.get(lineNumber);
						var lineSensor = gd.getLine();
						String template = ",%s,";
						line = line.replaceFirst(String.format(template, lineSensor.originalData()), String.format(template, lineSensor.data()));
                    	writer.write(line);
                    	writer.newLine();
                	}
            	}

        	} catch (IOException e) {
            	e.printStackTrace();
        	}
		//}
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
