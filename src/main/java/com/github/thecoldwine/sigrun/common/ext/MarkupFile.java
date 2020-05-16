package com.github.thecoldwine.sigrun.common.ext;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxRect;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.app.auxcontrol.Hyperbola;

public class MarkupFile {

	
	public void load(SgyFile sgyFile) throws Exception {
		
		File file = sgyFile.getFile();
		File mkupfile = getMarkupFileBySgy(file);
		if(!mkupfile.exists()) {
			System.out.println(" not exists " + mkupfile.getAbsolutePath());
			return;
		}
		
		JSONObject jsonObject;
		JSONParser jsonParser = new JSONParser();
		FileReader reader = new FileReader(mkupfile);
		try {	    
			jsonObject = (JSONObject)jsonParser.parse(reader);
		}finally {
			reader.close();
		}
		
		JSONArray objects = (JSONArray)jsonObject.get("markup");
		for(Object t : objects) {
			JSONObject ob = (JSONObject)t;
			
			String clazz = (String) ob.get("clazz");
			BaseObject obj = null;
			if(clazz.equals(Hyperbola.class.getSimpleName())) {
				
				obj = new Hyperbola(ob, sgyFile.getOffset());
				
			}else if(clazz.equals(AuxRect.class.getSimpleName())) {
				
				obj = new AuxRect(ob, sgyFile.getOffset());
				
			}else if(clazz.equals(FoundPlace.class.getSimpleName())){
				
				//save in sgy file
				//obj = FoundPlace.loadFromJson(ob, sgyFile); 
			}
			
			if(obj != null) {
				sgyFile.getAuxElements().add(obj);
			}
			
		}		
	}

	private File getMarkupFileBySgy(File file) {
		String mrkupName = StringUtils.replaceIgnoreCase(file.getAbsolutePath(), ".sgy", ".mrkup");		
		File mkupfile = new File(mrkupName);
		return mkupfile;
	}
	
	public void save(SgyFile sgyFile, File nfile) {
		
		
		
		 JSONObject sampleObject = new JSONObject();
		 sampleObject.put("filename", nfile.getAbsolutePath());
		 
		 JSONArray objects = new JSONArray();
		 
		 for(BaseObject bo : sgyFile.getAuxElements()) {
			 
			 JSONObject boj = new JSONObject();

			 //save if object allows saving
			 if(bo.saveTo(boj)) {
				 boj.put("clazz", bo.getClass().getSimpleName());
				 objects.add(boj);
			 }
		 }
		 
		if(objects.isEmpty()) {
			deleteMarkup(nfile);
			
			return;
		}
		 

		 sampleObject.put("markup", objects);
		 

		 try {
			 File jsonFile = getMarkupFileBySgy(nfile);
			 
			 FileWriter file = new FileWriter(jsonFile);
			 
			 Gson gson = new GsonBuilder().setPrettyPrinting().create(); //
			 JsonParser jp = new JsonParser();
			 JsonElement je = jp.parse(sampleObject.toJSONString());
			 String prettyJsonString = gson.toJson(je);
			 
			 //ObjectMapper mapper = new ObjectMapper();
		       //file.write(sampleObject.toJSONString());
			 file.write(prettyJsonString);
			 
		        file.flush();
		        file.close();			 
		        
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
	}

	public void deleteMarkup(File nfile) {
		//try to delete
		File mrkpFile = getMarkupFileBySgy(nfile);
		if(mrkpFile.exists()) {
			try {
				mrkpFile.delete();
			}catch(Exception e) {
				e.printStackTrace();					
			}
		}
	}
	
	
}
