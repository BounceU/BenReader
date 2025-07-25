package com.benliebkemann;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class CheckListRenderer extends JCheckBox
		implements ListCellRenderer<Chapter> {

	// public static final int LINE_HEIGHT = 80;

	public CheckListRenderer() {
		super();
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Chapter> list, Chapter value, int index,
			boolean isSelected, boolean cellHasFocus) {

		this.setText(value.getName());
		this.setSelected(value.getShouldUse());

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				value.setShouldUse(isSelected());
			}
		});

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		return this;

	}

}
