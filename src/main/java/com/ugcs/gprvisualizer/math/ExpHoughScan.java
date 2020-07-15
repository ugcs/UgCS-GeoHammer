package com.ugcs.gprvisualizer.math;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.github.thecoldwine.sigrun.common.ext.Trace;
import com.ugcs.gprvisualizer.app.AppContext;
import com.ugcs.gprvisualizer.app.ProgressListener;
import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.app.commands.AsinqCommand;
import com.ugcs.gprvisualizer.app.commands.Command;
import com.ugcs.gprvisualizer.app.commands.EdgeFinder;
import com.ugcs.gprvisualizer.app.commands.EdgeSubtractGround;
import com.ugcs.gprvisualizer.app.commands.LevelScanner;
import com.ugcs.gprvisualizer.app.commands.ProgressCommand;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.gpr.Model;

@Component
@Scope(value = "prototype")
public class ExpHoughScan  implements AsinqCommand {

	@Autowired
	private Model model;
	
	@Override
	public void execute(SgyFile file, ProgressListener listener) {
		Sout.p("start");
		
		if (file.groundProfile == null) {
			new LevelScanner().execute(file, listener);
		}
		new EdgeFinder().execute(file, listener);
		new EdgeSubtractGround().execute(file, listener);

		//
		int maxSmp = Math.min(AppContext.model.getSettings().layer 
				+ AppContext.model.getSettings().hpage,
				file.getMaxSamples() - 2);

		double threshold = model.getSettings().hyperSensitivity.doubleValue();

		for (int pinTr = 0; pinTr < file.size(); pinTr++) {
			//log progress
			if (pinTr % 100 == 0) {
				listener.progressSubMsg(" -  traces processed: " + pinTr + "/" + file.size());
			}
			
			Trace tr = file.getTraces().get(pinTr);
			tr.good = new byte[file.getMaxSamples()];

			for (int pinSmp = AppContext.model.getSettings().layer; 
					pinSmp < maxSmp; pinSmp++) {
				
				boolean isGood = scan(file, pinTr, pinSmp, threshold);

				if (isGood) {
					tr.good[pinSmp] = 3;
				}
			}
		}
		
		new ScanGood().execute(file, listener);	
		
		Sout.p("finish");
	}

	private boolean scan(SgyFile file, int pinTr, int pinSmp, double threshold) {

		HoughExperimentsAnalizer hea = new HoughExperimentsAnalizer(file);
		
		return hea.analize(pinTr, pinSmp);
	}

	@Override
	public String getButtonText() {
		
		return "exp hough scan";
	}

	@Override
	public Change getChange() {
		return Change.justdraw;
	}

}
