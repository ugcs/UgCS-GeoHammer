package com.github.thecoldwine.sigrun.common.ext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.gpr.Model;

public class MarkupFile {

	
	public void load(SgyFile sgyFile, Model model) throws Exception {
		File mkupfile = new File(sgyFile.getFile().getAbsolutePath().replaceAll(".sgy", ".mrkup"));
		if(!mkupfile.exists()) {
			System.out.println(" not exists " + mkupfile.getAbsolutePath());
			return;
		}
		
		FileReader reader = new FileReader(mkupfile);
	    JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject)jsonParser.parse(reader);
		
		JSONArray objects = (JSONArray)jsonObject.get("markup");
		for(Object t : objects) {
			JSONObject ob = (JSONObject)t;
						
			BaseObject obj = new AuxRect(ob, model.getVField());
			
			
			sgyFile.getAuxElements().add(obj);
			
		}		
	}
	
	public void save(SgyFile sgyFile, File nfile) {
		
		 JSONObject sampleObject = new JSONObject();
		 sampleObject.put("filename", nfile.getAbsolutePath());
		 
		 JSONArray objects = new JSONArray();
		 
		 for(BaseObject bo : sgyFile.getAuxElements()) {
			 
			 JSONObject boj = new JSONObject();
			 
			 bo.saveTo(boj);
			 
			 objects.add(boj);			 
		 }
		 

		 sampleObject.put("markup", objects);
		 

		 try {
			Files.write(Paths.get(nfile.getAbsolutePath().replaceAll(".sgy", ".mrkup")), sampleObject.toJSONString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
	}
	
	
}
