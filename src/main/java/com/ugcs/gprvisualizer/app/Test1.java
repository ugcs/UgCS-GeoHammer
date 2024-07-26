package com.ugcs.gprvisualizer.app;

import org.springframework.context.annotation.Bean;

import java.util.regex.Pattern;

public class Test1 {

    @Bean(name = {"name", "name2", "name3"})
    public static void main(String[] args) {
        System.out.println("ффф".replaceAll("(?U)ффф\\b", "fff"));
        System.out.println("фффfff".replaceAll("ффф\\b{0}", "fff"));
        System.out.println("ффф".replaceAll("ффф", "fff"));

        Pattern pattern = Pattern.compile("^\s*Counter,MFAM Fiducial", Pattern.MULTILINE | Pattern.DOTALL);
        System.out.println(pattern.matcher("    Counter,MFAM Fiducial,Date,Time,Latitude,Longitude,Mag, MagValid,CompassX, CompassY, CompassZ,GyroscopeX, GyroscopeY, GyroscopeZ,AccelerometerX, AccelerometerY, AccelerometerZ,ImuTemperature,Track,LocationSource,Hdop,FixQuality, SatellitesUsed, Altitude,HeightOverEllipsoid,SpeedOverGround,MagneticVariation,VariationDirection,ModeIndicator,GgaSentence,RmcSentence,EventCode,EventInfo,EventDataLength,EventData" + System.lineSeparator() + "1,1,2023/09/21,9:05:54.000,56.86325367,24.11186550,49741.02090,1,,,,,,,-0.00629,-0.07227,1.01245,40.684,0.0,G,0.490,1,12,8.20,23.10,0.003,0.000, ").find());

    }
}
