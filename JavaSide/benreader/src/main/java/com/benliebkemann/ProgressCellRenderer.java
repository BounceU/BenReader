package com.benliebkemann;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressCellRenderer implements TableCellRenderer {

	JProgressBar bar = new JProgressBar(0, 1000);

	public ProgressCellRenderer() {
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		bar.setValue((int) ((float) value * 1000));
		return bar;
	}

}
