package com.ugcs.gprvisualizer.app.kml;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.ugcs.gprvisualizer.app.auxcontrol.ConstPlace;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KmlReader {

    public static void main(String[] args) {

        new KmlReader().read(new File("c:\\work\\gpr-data\\2022-04-28 Cobra Plug-In\\DAT_0010.kml"), new Model());

    }

    public void read(File file, Model model) {

        Kml kml = Kml.unmarshal(file);

        Document doc = (Document) kml.getFeature();
        List<Feature> list = doc.getFeature();

        //Placemark> placemarks =
        list.stream()
            .flatMap(f -> {
                if (f instanceof Placemark) {
                    return Stream.of((((Placemark) f)));
                }
                if (f instanceof Folder) {
                    return (((Folder) f).getFeature()).stream()
                        .map(x -> (Placemark) x);
                }
                throw new RuntimeException("Uknown format of KML");

            })
        .forEach(f -> {

            Point pm = (Point) f.getGeometry();
            Coordinate c = pm.getCoordinates().get(0);

            LatLon ll = new LatLon(c.getLatitude(), c.getLongitude());


            ConstPlace cp = new ConstPlace(0, ll, null);
            model.getAuxElements().add(cp);

        });

        model.setKmlToFlagAvailable(true);
        //broadcast.notifyAll(new WhatChanged(Change.updateButtons));
    }

}
