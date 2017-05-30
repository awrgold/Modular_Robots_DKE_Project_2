package com.bobjob.engine.util.swing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.bobjob.engine.util.JustOneLock;
import com.bobjob.engine.util.OSUtil;


public class SwingLauncher {
	
	BufferedWriter out;
	BufferedReader in;
	JustOneLock ua;
	public void init(String lsClassName) throws Exception {
		
		String id;
		do {
			id =// onTop + 
					":swing_engine:" + Math.random();
			ua = new JustOneLock(id);
		} while (ua.isAppActive());
		

		String commands[] = new String[] {
				System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
				"-cp",
				System.getProperty("java.class.path"),
				"-Dapple.awt.UIElement=true",
				SwingLauncher.class.getCanonicalName(),
				lsClassName,
				id};
		
		
		Runtime runtime = Runtime.getRuntime();
		final Process p = runtime.exec(commands);
		
		//ProcessKiller pk = new ProcessKiller(p);
		//runtime.addShutdownHook(pk);
	
		InputStream OsOut = p.getInputStream();
		InputStreamReader rs = new InputStreamReader(OsOut);
		in = new BufferedReader(rs);
		
		/*
		Thread tz = new Thread() {
			public void run() {
				try {
					String line;
					while ((line = in.readLine()) != null) {
						line = line.replaceAll("!n", "\n");
						line = line.replaceAll("!!", "!");
						lsHandle.recievedFromSwing(line);
					}
					in.close();
					System.out.println("finished Input");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		tz.setDaemon(true);
		tz.start(); */
		
		InputStream lsOut = p.getErrorStream();
		
		InputStreamReader r = new InputStreamReader(lsOut);
		final BufferedReader ine = new BufferedReader(r);

		Thread t = new Thread() {
			public void run() {
				try {
					String line;
					while ((line = ine.readLine()) != null) {
						System.err.println(line);
					}
					ine.close();
					//System.out.println("finished Error Input");
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				p.destroy();
			}
			
		};
		t.setDaemon(true);
		t.start();
		
		OutputStream lsIn = p.getOutputStream();
		OutputStreamWriter or = new OutputStreamWriter(lsIn);
		out = new BufferedWriter(or);
	}
	
	public Object updateFromSwing(SwingInstanceHandle lsHandle) {
		try {
			String line;
			if((line = in.readLine()) != null) {
				line = line.replaceAll("!n", "\n");
				line = line.replaceAll("!v", "!");
				return lsHandle.recievedFromSwing(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Object updateSwingAndWait(SwingInstanceHandle lsHandle, String type) {
		Object o = null;
		try {
			String line = "";
			while (!line.startsWith(type)) {

				
				Thread.sleep(10);
				if((line = in.readLine()) != null) {
					line = line.replaceAll("!n", "\n");
					line = line.replaceAll("!v", "!");
					o = lsHandle.recievedFromSwing(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}
	public void closeSwingStream() {
		try {
			ua.closeLock();
			ua.deleteFile();
			//in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void sendToSwing(String output) {
		try {
			output = output.replaceAll("!", "!v");
			output = output.replaceAll("\n", "!n");
			out.write(output);
			out.write("\n");
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		final String id = args[1];
		System.out.println("LAUNCHED: " + id);
		boolean alwaysOnTop = true;
		//try {
		//	alwaysOnTop = Boolean.parseBoolean(id.substring(0, id.indexOf(":")) );
		//} catch (Exception e) {
		//	System.err.println(e.getMessage());
		//}
	

		
		
		Thread run = new Thread() {
			public void run() {
				BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
				
				try {
					Class c = Class.forName(args[0]);
					SwingInstanceHandle lsHandle = (SwingInstanceHandle)c.newInstance();
					lsHandle.setOnTop(alwaysOnTop);
					String line;
					while ((line = scanner.readLine()) != null) {
						line = line.replaceAll("!n", "\n");
						line = line.replaceAll("!v", "!");
						lsHandle.recievedFromLWJGL(line);
					}
					
					scanner.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		};
		run.setDaemon(true);
		

		try {
			JustOneLock ua = new JustOneLock(id);
			if (ua.isAppActive()) {
				SwingUtilities.invokeLater(run);
				//run.start();
				while (ua.isAppActive()) {
					Thread.sleep(1000);
				}
			}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
	}
	

}
