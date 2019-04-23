package com.ugcs.gprvisualizer.app;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainConsole {

	private static final Logger logger = LoggerFactory.getLogger(MainConsole.class.getName());
	
    public static void main(String[] args) {

    	File folder = new File("c:/work/georadar/GPR_data");
        //File folder = new File("c:/work/georadar/GPR_data/Greenland");
        //File folder = new File("c:/work/georadar/GPR_data/Frozen lake");
        


       	//SgyLoader work = new SgyLoader(true);

    	//CoordinateManager data = work.processFolder(folder, null);
    	
    	//data.filter();
		
    	//data.calcLocalCoordinates();
		
    	
    	
    	//data.imgset();    	
    	
        	
        	

        System.out.println("finish");
        System.exit(0);
    }
	
	
}
