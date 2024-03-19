package com.ugcs.gprvisualizer.app.parcers.csv;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.ugcs.gprvisualizer.app.parcers.GeoCoordinates;
import com.ugcs.gprvisualizer.app.yaml.Template;
import com.ugcs.gprvisualizer.app.yaml.data.DateTime;

public class MagDroneCsvParser extends CsvParser {

    private DecimalFormat format;

    public MagDroneCsvParser(Template template) {
        super(template);
    }

    @Override
    public List<GeoCoordinates> parse(String logPath) throws FileNotFoundException {
        
        if (template == null) {
            throw new NullPointerException("Template is not set");
        }

        File file = new File(logPath);

        if (!file.exists()) {
            throw new FileNotFoundException("File " + logPath + " does not exist");
        }

        if (template.getDataMapping().getDate().getSource() == com.ugcs.gprvisualizer.app.yaml.data.Date.Source.FileName) {
            throw new FileNotFoundException("File " + logPath + " does not exist");
        }

        List<GeoCoordinates> coordinates = new ArrayList<>();
        try (var reader = new BufferedReader(new FileReader(logPath))) {
            String line = skipLines(reader);

            if (template.getFileFormat().isHasHeader()) {
                if (line == null) {
                    line = reader.readLine();
                    findIndexesByHeaders(line);
                    skippedLines.append(line + "\n");
                } else
                    findIndexesByHeaders(line);
            }

            format = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.US));

            int traceCount = 0;
            LocalDateTime firstDateTime = null;
            int timestampOfTheFirstDatetime = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(getTemplate().getFileFormat().getCommentPrefix()))
                    continue;
                String[] data = line.split(getTemplate().getFileFormat().getSeparator());
                double lat = parseDouble(getTemplate().getDataMapping().getLatitude(), data[getTemplate().getDataMapping().getLatitude().getIndex()]);
                double lon = parseDouble(getTemplate().getDataMapping().getLongitude(), data[getTemplate().getDataMapping().getLongitude().getIndex()]);
                double alt = getTemplate().getDataMapping().getAltitude().getIndex() != null &&
                        getTemplate().getDataMapping().getAltitude().getIndex() != -1 ? parseDouble(getTemplate().getDataMapping().getAltitude(), data[getTemplate().getDataMapping().getAltitude().getIndex()]) : 0.00;
                int timestamp = parseInt(getTemplate().getDataMapping().getTimestamp(), data[getTemplate().getDataMapping().getTimestamp().getIndex()]);
                int traceNumber = getTemplate().getDataMapping().getTraceNumber() != null && getTemplate().getDataMapping().getTraceNumber().getIndex() != -1 ?
                        parseInt(getTemplate().getDataMapping().getTraceNumber(), data[getTemplate().getDataMapping().getTraceNumber().getIndex()]) : traceCount;
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                boolean isRowHasTime = false;
                
                try {
                    sdf.parse(data[getTemplate().getDataMapping().getTime().getIndex()]);
                    isRowHasTime = true;
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (!isRowHasTime) {
                    if (firstDateTime != null) {
                        LocalDateTime date = firstDateTime.plus(timestamp - timestampOfTheFirstDatetime, ChronoUnit.MILLIS);
                        coordinates.add(new GeoCoordinates(date, lat, lon, alt, traceNumber));
                    } else
                        continue;
                } else {
                    LocalDateTime date = parseDateTime(data);
                    if (firstDateTime == null) {
                        firstDateTime = date;
                        timestampOfTheFirstDatetime = timestamp;
                    }
                    coordinates.add(new GeoCoordinates(date, lat, lon, alt, traceNumber));
                }
                traceCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return coordinates;
    }

}    