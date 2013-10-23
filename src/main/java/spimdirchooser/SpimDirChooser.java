package spimdirchooser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

public class SpimDirChooser extends JDialog {

	private JFileChooser chooser;
	private JCheckBox checkBox;
	private JFormattedTextField fieldT1, fieldT2;
	ArrayList<JComponent> editList;

	private String selectedDir;
	private int timeT1, timeT2;
	private int returnValue;
	public static final int SELECT_CANCEL = 0;
	public static final int SELECT_OPEN_SIMPLE = 1;
	public static final int SELECT_OPEN_INTERVAL = 2;

	public SpimDirChooser(String title) {
		initUI(title,null);
	}

	public SpimDirChooser(String title, String defaultDir) {
		initUI(title,defaultDir);
	}

	private void initUI(String title, String defaultDir) {
		
		if (defaultDir != null)
			chooser = new JFileChooser(defaultDir);
		else
			chooser = new JFileChooser();

		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.add(chooser, BorderLayout.CENTER);
		chooser.setApproveButtonText("Select");

		chooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
					approve();
				}
				if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
					cancel();
				}
			}
		});

		checkBox = new JCheckBox("import time interval:");

		checkBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				for (JComponent comp : editList)
					comp.setEnabled(checkBox.isSelected());

			}
		});

		NumberFormat format = NumberFormat.getInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(0);
		formatter.setMaximum(Integer.MAX_VALUE);
		formatter.setCommitsOnValidEdit(true);

		fieldT1 = new JFormattedTextField(formatter);
		fieldT1.setValue(1);
		fieldT1.setColumns(5);
		fieldT2 = new JFormattedTextField(formatter);
		fieldT2.setValue(10);
		fieldT2.setColumns(5);

		JLabel labelOffset = new JLabel("t1");
		JLabel labelCount = new JLabel("t2");

		editList = new ArrayList<JComponent>();

		editList.add(labelOffset);
		editList.add(fieldT1);
		editList.add(labelCount);
		editList.add(fieldT2);

		JPanel ctrlPanel = new JPanel(new FlowLayout());

		ctrlPanel.add(checkBox);

		for (JComponent comp : editList) {
			ctrlPanel.add(comp);
			comp.setEnabled(false);
		}

		this.add(ctrlPanel, BorderLayout.SOUTH);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.pack();
	}

	public int showRun() {
		setModal(true);
		setVisible(true);

		return getValue();
	}

	private int cancel() {
		selectedDir = null;
		timeT1 = -1;
		timeT2 = -1;
		setValue(SpimDirChooser.SELECT_CANCEL);
		dispose();

		return JFileChooser.CANCEL_OPTION;
	}

	private int approve() {
		File file = chooser.getSelectedFile();

		selectedDir = file.getAbsolutePath();
		timeT1 = ((Integer) fieldT1.getValue()).intValue();
		timeT2 = ((Integer) fieldT2.getValue()).intValue();

		if (checkBox.isSelected())
			setValue(SpimDirChooser.SELECT_OPEN_INTERVAL);
		else
			setValue(SpimDirChooser.SELECT_OPEN_SIMPLE);

		dispose();
		return JFileChooser.APPROVE_OPTION;
	}

	public String getSelectedDir() {
		return selectedDir;
	}

	public int getT1() {
		return timeT1;
	}

	public int getT2() {
		return timeT2;
	}

	public int getValue() {
		return returnValue;
	}

	public void setValue(int returnValue) {
		this.returnValue = returnValue;
	}

}
