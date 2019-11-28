package com.ugcs.gprvisualizer.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.draw.ToolProducer;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class PluginRunner implements ToolProducer {
	
	private static final String FOUND_ANOMALIES = "Found anomalies: ";

	private Button buttonRun = new Button("Find");
	
	private Model model;

	private ProgressTask saveTask = new ProgressTask() {
		@Override
		public void run(ProgressListener listener) {
			listener.progressMsg("searching start now");
			
			model.getFoundTrace().clear();
			for(SgyFile sgf : model.getFileManager().getFiles()) {
				processSgyFile(listener, sgf);
				
			}
			
		}

		private void processSgyFile(ProgressListener listener, SgyFile sgf) {
			try {
				
				String sgyprocPath = System.getenv().get("SGYPROC");
				System.out.println(sgyprocPath);
				
				String line;
				String filePath = sgf.getFile().getAbsolutePath();
				String cmd = "python \"" + sgyprocPath + "/main.py\" "
						+ "\""+ filePath +"\" "
						+ "--model \"" + sgyprocPath + "\\model.pb\" --no_progressbar";
				System.out.println(cmd);
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) {
					
					System.out.println(line);
					
					process(sgf, line);
					
					listener.progressMsg(line);
					
					
				}
				input.close();
			} catch (Exception err) {
				err.printStackTrace();
			}
		}

	};
	
	{
		buttonRun.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
				new TaskRunner(null, saveTask).start();
		    	
		    }
		});
	}
	
	public PluginRunner(Model model) {
		this.model = model;
	}

	@Override
	public List<Node> getToolNodes() {
		
		return Arrays.asList(buttonRun);
	}

	private void process(SgyFile file, String line) {//1680;2388;3284;3844
		if(line.startsWith(FOUND_ANOMALIES)) {
			String trnums = line.substring(FOUND_ANOMALIES.length());
			if(trnums.contains("no objects")) {
				return;
			}
			
			List<Integer> lst = new ArrayList<>();
			String[] nums = trnums.split(";");
			for(String n : nums) {
				int trindex = Integer.valueOf(n);
				
				lst.add(trindex);
				model.getFoundTrace().add( file.getTraces().get(trindex) );
			}
			
			model.getFoundIndexes().put(file, lst);
		}
		
	}

}
