package com.ugcs.gprvisualizer.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.auxcontrol.AuxRect;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

public class PluginRunner implements ToolProducer {

	private static final String FOUND_ANOMALIES = "Found anomalies: ";

	private Button buttonRunNeuralNetwork = new Button("Find anomalies");

	private Model model;

	private void processSgyFile(ProgressListener listener, SgyFile sgf) {
		try {

			String sgyprocPath = System.getenv().get("SGYPROC");
			System.out.println(sgyprocPath);

			String line;
			String filePath = sgf.getFile().getAbsolutePath();
			String cmd = "python \"" + sgyprocPath + "/main.py\" " + "\"" + filePath + "\" " + "--model \""
					+ sgyprocPath + "\\model.pb\" --no_progressbar";
			System.out.println(cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {

				System.out.println(line);

				// processV2(sgf, line);

				listener.progressMsg(line);

			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
			
			
			showError("");			
			
		}
	}




	public void showError(String msg) {
		
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Neural network is not available now");
				alert.setContentText(msg);
				alert.showAndWait().ifPresent(rs -> {
				    if (rs == ButtonType.OK) {
				        //System.out.println("Pressed OK.");
				    }
				});
			}
		});
	}	
	
	private void processSgyFiles(ProgressListener listener, List<SgyFile> sgfl) {
		try {
			
			StringBuilder sb = new StringBuilder();
			for(SgyFile sf : sgfl) {
				if(sb.length() > 0) {
					sb.append(",");
				}
				sb.append(sf.getFile().getAbsolutePath());
			}
			
			String sgyprocPath = System.getenv().get("SGYPROC");
			if(StringUtils.isBlank(sgyprocPath)) {
				showError("System environment variable 'SGYPROC' absent");
				return;
			}
			
			System.out.println(sgyprocPath);
			
			String line;
			//String filePath = sgf.getFile().getAbsolutePath();
			String cmd = "python \"" + sgyprocPath + "/main.py\" "
					+ "\""+ sb.toString() +"\" "
					+ "--model \"" + sgyprocPath + "\\model.pb\" --no_progressbar";
			System.out.println(cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			
			
			//
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				
				if(!line.contains("Iterations")) {
					System.out.println(line);
					listener.progressMsg(line);
				}
				
				processV2(sgfl, line);
				
			}
			input.close();
			
			System.out.println("p.exitValue() " + p.exitValue());
			if(p.exitValue() != 0) {
				String text = IOUtils.toString(p.getErrorStream(), StandardCharsets.UTF_8.name());
				System.out.println(text);
				
				throw new RuntimeException("p.exitValue() != 0 " + p.exitValue());
			}
			
			listener.progressMsg("search finished");
			
		} catch (Exception err) {
			err.printStackTrace();
			
			showError("");
		}
		
		
	}
	
	private ProgressTask neuralNetworkTask = new ProgressTask() {
		@Override
		public void run(ProgressListener listener) {
			listener.progressMsg("searching start now");
			
			//model.getFoundTrace().clear();
			//for(SgyFile sgf : model.getFileManager().getFiles()) {
			//	processSgyFile(listener, sgf);
				
			//}
			processSgyFiles(listener, model.getFileManager().getFiles());
			
			AppContext.notifyAll(new WhatChanged(Change.justdraw));
			
		}
	};
		
		
	{
		buttonRunNeuralNetwork.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
				new TaskRunner(null, neuralNetworkTask).start();
		    	
		    }
		});
	}

	public PluginRunner(Model model) {
		this.model = model;
	}

	@Override
	public List<Node> getToolNodes() {
		
		return Arrays.asList(buttonRunNeuralNetwork);
	}

	// {"filename":
	// "D:\\georadarData\\poligonnewRadar\\processed_001\\DAT_0047_CH_1_001.sgy",
	// "file_number": 1, "anomalies": [[56, 167]]}
	private void processV2(List<SgyFile> sgyFileList, String line) throws Exception {
		if (line.startsWith("{")) {

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(line);
			int pos = (int) (long) (Long) jsonObject.get("file_number");

			SgyFile sgyFile = sgyFileList.get(pos - 1);
			//System.out.println(sgyFile.getFile().getName());
			
			loadAnomalies(sgyFile, jsonObject);
		}

	}

	private void loadAnomalies(SgyFile sgyFile, JSONObject jsonObject) {
		JSONArray objects = (JSONArray) jsonObject.get("anomalies");
		for(Object t : objects) {

			JSONObject json = (JSONObject) t;

			sgyFile.getAuxElements().add(
				new AuxRect(json, sgyFile.getOffset()));
			
//			int t1 = (int) (long) (Long) ar.get(0);
//			int t2 = (int) (long) (Long) ar.get(1);
//
//			sgyFile.getAuxElements()
//					.add(new FoundPlace(
//							sgyFile.getTraces().get(t1), 
//							sgyFile.getTraces().get(t2), 
//							sgyFile.getTraces().get((t1+t2)/2),
//							sgyFile.getOffset()));
		}

		model.updateAuxElements();
	}

	private void process(SgyFile sgyFile, String line) {// 1680;2388;3284;3844
		if (line.startsWith(FOUND_ANOMALIES)) {
			String trnums = line.substring(FOUND_ANOMALIES.length());
			if (trnums.contains("no objects")) {
				return;
			}

			// List<Integer> lst = new ArrayList<>();
			String[] nums = trnums.split(";");
			for (String n : nums) {
				// int trindex = Integer.valueOf(n);

				// sgyFile.getAuxElements().add(
				// new FoundPlace(sgyFile.getTraces().get(trindex), sgyFile.getOffset()));
			}

			model.updateAuxElements();
			// model.getFoundIndexes().put(file, lst);
		}

	}

}
