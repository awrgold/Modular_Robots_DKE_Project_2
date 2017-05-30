package com.bobjob.engine.util.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.bobjob.engine.util.AcceptedFiles;
import com.bobjob.engine.util.ImagePreviewPanel;
import com.bobjob.engine.util.OSUtil;
import com.bobjob.engine.util.SystemColorChooserPanel;


public class SwingHandle implements SwingInstanceHandle{
	public static boolean debug;
	public Thread finalAction;
	boolean mac;
	SwingLauncher sl;
	JFrame frame;
	JFileChooser chooser;
	AcceptedFiles lastFilter;
	boolean lock;
	boolean onTop = true;
	public SwingHandle() {
		frame = new JFrame();
		frame.setVisible(false);
		
		chooser = new JFileChooser(".");
		ImagePreviewPanel preview = new ImagePreviewPanel();
		chooser.setAccessory(preview);
		chooser.addPropertyChangeListener(preview);

		Dimension d = chooser.getPreferredSize();
		d.width *= 2;
		d.height *= 1.5;
		chooser.setPreferredSize(d);

		mac = true;
	}
	

	public SwingHandle(boolean lockThread) throws Exception {
		lock = lockThread;
		int os = OSUtil.getOSType();
		
		if (os == OSUtil.MAC_OSX) {
			//System.out.println("MAC DETECTED");
			mac = true;
			sl = new SwingLauncher();
			sl.init(getClass().getCanonicalName());
			//System.out.println("MAC LOADED");
		} else {
			frame = new JFrame();
			frame.setAlwaysOnTop(true);
			frame.setVisible(false);
			chooser = new JFileChooser(".");
			
			ImagePreviewPanel preview = new ImagePreviewPanel();
			chooser.setAccessory(preview);
			chooser.addPropertyChangeListener(preview);
		}
	}

	public Object recievedFromSwing(String input) {
		if (input.contains(":")) {
			String swing_type = input.substring(0, input.indexOf(":")).toLowerCase();
			String swing_output = input.substring(input.indexOf(":")+1, input.length());
			switch (swing_type) {
			
			case "input_dialog":
				return swing_output;
				
			case "color_chooser":
				return swing_output;
				
			case "message_dialog":
				return null;
				
			case "file_chooser_open":
				return Integer.parseInt(swing_output);
				
			case "file_chooser_save":
				return Integer.parseInt(swing_output);
				
			case "file_chooser_single_file":
				return new File(swing_output.trim());
				
			case "file_chooser_files":
				StringTokenizer st = new StringTokenizer(swing_output, "\n");
				File files[] = new File[Integer.parseInt(st.nextToken())];
				for (int i = 0; i < files.length; i++) {
					files[i] = new File(st.nextToken().trim());
				}
				return files;

			}
		}
		
		return null;
	}

	@Override
	public Object recievedFromLWJGL(String input) {
		if (debug) System.err.println(input);
		String swing_type = input.substring(0, input.indexOf(":")).toLowerCase();
		String swing_input = input.substring(input.indexOf(":")+1, input.length());
		String output = "";
		
		if (frame != null) {
			frame.setAlwaysOnTop(onTop);
			//frame.validate();
		}
		switch (swing_type) {
		case "to_front":
			break;
		case "textarea_dialog":
			String user_input = showTextAreaDialog(swing_input);
			if (user_input != null) {
				output = "textarea_dialog:" + user_input;
			} else {
				output = "textarea_dialog";
			}
			break;
			
		case "color_chooser":
			StringTokenizer st = new StringTokenizer(swing_input, " ");
			Color c = new Color(
					Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()));
			output = "color_chooser:" + SystemColorChooserPanel.selectColor(c);
			break;
			
		case "input_dialog":
			
			st = new StringTokenizer(swing_input, "\n");
			String defaultInput = swing_input.substring(0, swing_input.indexOf("\n"));
			String message = swing_input.substring(swing_input.indexOf("\n")+1, swing_input.length());

			user_input = JOptionPane.showInputDialog(frame, message, defaultInput);
			
			if (user_input != null) {
				output = "input_dialog:" + user_input;
			} else {
				output = "input_dialog";
			}
			break;
			
		case "message_dialog":
			JOptionPane.showMessageDialog(frame, swing_input);
			output = "message_dialog:close";
			break;
		case "file_chooser_open":
			output = "file_chooser_open:" + chooser.showOpenDialog(frame);
			break;
		case "file_chooser_save":
			output = "file_chooser_save:" + chooser.showSaveDialog(frame);
			break;
		case "file_chooser_multiple":
			chooser.setMultiSelectionEnabled(Boolean.parseBoolean(swing_input));
			break;
		case "file_chooser_files":
			File files[];
			if (chooser.isMultiSelectionEnabled()) {
				files = chooser.getSelectedFiles();
			} else {
				File f = chooser.getSelectedFile();
				if (f != null) {
					files = new File[] {f};
				} else {
					files = null;
				}
			}
			
		
			output = "file_chooser_files:" + files.length + "\n";
			for (int i = 0; i < files.length; i++) {
				output += files[i] + "\n";
			}
			break;
			
		case "file_chooser_single_file":
			File file = chooser.getSelectedFile();
			output = "file_chooser_single_file:" + file + "\n";
			break;
			
		case "file_chooser_filter":
			st = new StringTokenizer(swing_input, "\n");
			String description = st.nextToken().trim();
			String extentions[] = new String[Integer.parseInt(st.nextToken())];
			for (int i = 0; i < extentions.length; i++) {
				extentions[i] = st.nextToken().trim();
			}
			chooser.setAcceptAllFileFilterUsed(false);
			if (lastFilter != null) chooser.removeChoosableFileFilter(lastFilter);
			AcceptedFiles af = new AcceptedFiles(extentions, description);
			lastFilter = af;
			chooser.addChoosableFileFilter(af);
			break;
			
		case "file_chooser_no_filter":
			chooser.setAcceptAllFileFilterUsed(true);
			if (lastFilter != null) chooser.removeChoosableFileFilter(lastFilter);
			break;
		case "set_current_directory":
			try {
				String dir = swing_input.trim();
				File f = new File(dir);
				chooser.setCurrentDirectory(f);
			} catch (Exception e) {
				System.out.println(swing_input);
				e.printStackTrace();
			}
			break;
		case "exit":
			frame.dispose();
			break;
		
		default: {
			System.err.println("unknown swing command: " + swing_type);
			System.exit(0);
		}
				
		}
			
		
		if (frame != null) {
			frame.setAlwaysOnTop(false);
		}
		
		
		if (mac) {
			
			output = output.replaceAll("!", "!v");
			output = output.replaceAll("\n", "!n");
			System.out.println(output);
			System.out.flush();
		} else {
			return recievedFromSwing(output);
		}

		
		return null;
	} 

	
	public void dispose() {
		if (sl == null) {
			chooser.setEnabled(false);
			frame.dispose();
		} else {
			sl.sendToSwing("exit:");
			sl.updateFromSwing(this);
			sl.closeSwingStream();
		}
		
	}
	
	private String showTextAreaDialog(String swingInput) {
		
		String values[] = swingInput.split(":n");
		
		JPanel panel = new JPanel();
		JTextArea m1 = new JTextArea(values[1].replaceAll(":v",  ":"));
		JTextArea m2 = new JTextArea(values[2].replaceAll(":v",  ":"));
		m1.setOpaque(false);
		m1.setEditable(false);
		m2.setOpaque(false);
		m2.setEditable(false);
		panel.setLayout(new BorderLayout());
		JTextArea textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setAutoscrolls(true);
		JScrollPane scroll = new JScrollPane (textArea);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    scroll.setPreferredSize(new Dimension (640, 200));
	    panel.add(m1, BorderLayout.NORTH);
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(m2, BorderLayout.SOUTH);
		
	
		textArea.setText(values[3].replaceAll(":v",  ":"));
		int option = JOptionPane.showOptionDialog(frame, panel, values[0].replaceAll(":v",  ":"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		if (option == JOptionPane.OK_OPTION) {
			return textArea.getText();
		}
		return null;
	}
	
	public String showTextDialog(String title, String message1, String message2, String defaultText) {
		StringBuffer op = new StringBuffer("textarea_dialog:");
		op.append(title.replaceAll(":", ":v"));
		op.append(":n");
		op.append(message1.replaceAll(":", ":v"));
		op.append(":n");
		op.append(message2.replaceAll(":", ":v"));
		op.append(":n");
		op.append(defaultText.replaceAll(":", ":v"));
		String output = op.toString();
		if (sl == null) {
			return (String)recievedFromLWJGL(output);
		} else {
			sl.sendToSwing(output);
			if (lock) return (String)sl.updateSwingAndWait(this, "textarea_dialog");
		}
		
		return null;
	}
	
	
	public String showInputDialog(String message) {
		return showInputDialog(message, "");
	}
	public String showInputDialog(String message, Object defaultInput) {
		String startInput = defaultInput.toString().replaceAll("\n", "");
		String output = "input_dialog:" + startInput + "\n" + message;
		if (sl == null) {
			return (String)recievedFromLWJGL(output);
		} else {
			sl.sendToSwing(output);
			if (lock) return (String)sl.updateSwingAndWait(this, "input_dialog");
		}

		return null;
	}
	public String showColorChooser() {
		return showColorChooser("255 255 255");
	}
	public String showColorChooser(String defaultColor) {
		String output = "color_chooser:" + defaultColor;
		if (sl == null) {
			return (String)recievedFromLWJGL(output);
		} else {
			sl.sendToSwing(output);
			if (lock) return (String)sl.updateSwingAndWait(this, "color_chooser");
		}
		return null;
	}
	

	
	public void showMessageDialog(String message) {
		String output = "message_dialog:" + message;
		if (sl == null) {
			recievedFromLWJGL(output);
		} else {
			sl.sendToSwing(output);
			if (lock) sl.updateSwingAndWait(this, "message_dialog:");
		}
		
		return;
	}

	public void setMultiSelectionEnabled(boolean enable) {
		String output = "file_chooser_multiple:" + enable;
		if (sl == null) {
			recievedFromLWJGL(output);
		} else {
			sl.sendToSwing(output);
		}
		
	}
	
	public int showSaveDialog() {
		String output = "file_chooser_save:";
		if (sl == null) {
			return (int)recievedFromLWJGL(output);
		} else {
			sl.sendToSwing(output);
			if (lock) return (int)sl.updateSwingAndWait(this, "file_chooser_save:");
		}
		
		return JOptionPane.UNDEFINED_CONDITION;
	}
	public int showOpenDialog() {
		String output = "file_chooser_open:";
		if (sl == null) {
			return (int)recievedFromLWJGL(output);
		} else {
			sl.sendToSwing(output);
			if (lock) return (int)sl.updateSwingAndWait(this, "file_chooser_open:");
		}
		
		return JOptionPane.UNDEFINED_CONDITION;
	}
	
	public File[] getSelectedFiles() {
		String output = "file_chooser_files:";
		if (sl == null) {
			return (File[])recievedFromLWJGL(output);
		} else {
			sl.sendToSwing(output);
			if (lock) return (File[])sl.updateSwingAndWait(this, "file_chooser_files:");
		}
		return null;
	}
	public File getSelectedFile() {
		String output = "file_chooser_single_file:";
		if (sl == null) {
			Object o = recievedFromLWJGL(output);
			if (o != null)  return (File)o;
			return null;
		} else {
			sl.sendToSwing(output);
		}
		
		if (lock) return (File)sl.updateSwingAndWait(this, "file_chooser_single_file:");
		return null;
	}
	public void setCurrentDirectory(File dir) {
		String output = "set_current_directory:" + dir.getAbsolutePath();
		if (sl == null) {
			this.recievedFromLWJGL(output);
		} else {
			sl.sendToSwing(output);
		}
		
	}

	
	public void setChoosableFileFilter(String[] extentions, String description) {
		String output;
		if (extentions == null) output = "file_chooser_no_filter:";
		else {
			output = "file_chooser_filter:" + description + "\n" + extentions.length;
			for (int i = 0; i < extentions.length; i++) {
				output +=  "\n" + extentions[i];
			}
		}
		if (sl == null) {
			recievedFromLWJGL(output);
		} else{
			sl.sendToSwing(output);	
		}
		
	}

	@Override
	public void setOnTop(boolean onTop) {
		this.onTop = onTop;
		
	}
}
