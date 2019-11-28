package com.ugcs.gprvisualizer.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CmdTest {

	public static void main(String argv[]) {
		try {
			
			System.out.println(System.getenv().get("SGYPROC"));
			
			String line;
			//Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tree.com /A");
			
			Process p = Runtime.getRuntime().exec("java -cp \"d:\\georadarData\\1\" CmdTestProducer f");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
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
