package com.ugcs.gprvisualizer.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CmdTest {

	private static final String RUN_CMD = "java -cp \"d:\\georadarData\\1\" CmdTestProducer f";

	public static void main(String[] argv) {
		try {
			System.out.println(System.getenv().get("SGYPROC"));
			String line;
			
			Process p = new ProcessBuilder(RUN_CMD).start();
			BufferedReader input = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
		System.out.println("finish");
	}

}
