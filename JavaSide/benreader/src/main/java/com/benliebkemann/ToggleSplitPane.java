package com.benliebkemann;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JSplitPane;

public class ToggleSplitPane extends JSplitPane {

	public ToggleSplitPane(int i) {
		super(i);
		addPropertyChangeListener("dividerLocation", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				int oldValue = (int) evt.getOldValue();
				int newValue = (int) evt.getNewValue();

				System.out.println("old: " + oldValue + ", new: " + newValue);
				if (oldValue == 0 && newValue > 1) {
					setDividerLocation(300);
				} else if (oldValue == 0) {
					setDividerLocation(0);
				} else if (oldValue > 0) {
					if (newValue < 300) {
						if (newValue != 0)
							setDividerLocation(0);
					} else if (newValue != 300) {
						setDividerLocation(300);
					}
				}
			}

		});

	}

}
