package com.bobjob.engine.util;


import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;

public class SystemColorChooserPanel extends AbstractColorChooserPanel implements ItemListener {
	private static int NOT_FOUND = -1;

	JComboBox comboBox;
	String labels[] = { "BLACK", "BLUE", "CYAN", "WHITE", "GREEN", "<CUSTOM>" };
	Color colors[] = { Color.BLACK, Color.BLUE, Color.CYAN, Color.WHITE, Color.GREEN, null };

	private void setColor(Color newColor) {
		int position = findColorPosition(newColor);
		comboBox.setSelectedIndex(position);
	}

	private int findColorLabel(Object label) {
		String stringLabel = label.toString();
		int position = NOT_FOUND;
		for (int i = 0, n = labels.length; i < n; i++) {
			if (stringLabel.equals(labels[i])) {
				position = i;
				break;
			}
		}
		return position;
	}

	private int findColorPosition(Color color) {
		int position = colors.length - 1;

		int colorRGB = color.getRGB();
		for (int i = 0, n = colors.length; i < n; i++) {
			if ((colors[i] != null) && (colorRGB == colors[i].getRGB())) {
				position = i;
				break;
			}
		}
		return position;
	}

	public void itemStateChanged(ItemEvent itemEvent) {
		int state = itemEvent.getStateChange();
		if (state == ItemEvent.SELECTED) {
			int position = findColorLabel(itemEvent.getItem());

			if ((position != NOT_FOUND) && (position != labels.length - 1)) {
				ColorSelectionModel selectionModel = getColorSelectionModel();
				selectionModel.setSelectedColor(colors[position]);
			}
		}
	}

	public String getDisplayName() {
		return "SystemColor";
	}

	public Icon getSmallDisplayIcon() {
		return new ImageIcon("yourFile.gif");
	}

	public Icon getLargeDisplayIcon() {
		return new ImageIcon("yourFile.gif");
	}

	protected void buildChooser() {
		comboBox = new JComboBox(labels);
		comboBox.addItemListener(this);
		add(comboBox);
	}

	public Color color;
	public void updateChooser() {
		color = getColorFromModel();
		setColor(color);
	}

	public static void main(String[] a) {
		System.out.println(selectColor(Color.WHITE));
	}
	public static String selectColor(Color defaultColor) {
		JColorChooser colorChooser = new JColorChooser();
		colorChooser.addChooserPanel(new SystemColorChooserPanel());

		JDialog d = JColorChooser.createDialog(null, "", true, colorChooser, null, null);
		colorChooser.setColor(defaultColor);
		
		d.setVisible(true);
		Color c = colorChooser.getColor();
		d.dispose();
		String color = c.getRed() + " " + c.getGreen() + " " + c.getBlue();
		return color;
	}
}
