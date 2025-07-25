package com.benliebkemann;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class ChapterCheckList extends JPanel {

	private List<Chapter> chapters;
	private long startLoading;
	JPanel spacer;

	public ChapterCheckList() {
		super(new GridBagLayout());
		chapters = new ArrayList<Chapter>();
		spacer = new JPanel();
		add(spacer);
		startLoading = System.currentTimeMillis();
	}

	public void selectAll() {
		for (Component c : getComponents()) {
			if (c.getClass().equals(JCheckBox.class)) {
				((JCheckBox) c).setSelected(true);
			}
		}
		chapters.forEach(chapter -> chapter.setShouldUse(true));
		validate();
	}

	public void deselectAll() {
		for (Component c : getComponents()) {
			if (c.getClass().equals(JCheckBox.class)) {
				((JCheckBox) c).setSelected(false);
			}
		}
		chapters.forEach(chapter -> chapter.setShouldUse(false));
		validate();
	}

	public List<Chapter> getChapters() {
		return this.chapters;
	}

	public void addChapter(Chapter chapter) {
		chapters.add(chapter);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = chapters.size();
		c.fill = GridBagConstraints.NONE;
		c.weighty = 0;
		c.weightx = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		JCheckBox checkBox = new JCheckBox(chapter.getName());
		checkBox.setSelected(chapter.getShouldUse());
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chapter.setShouldUse(checkBox.isSelected());
			}
		});
		add(checkBox, c);

		c.weighty = 1.0;
		c.gridy += 1;
		c.fill = GridBagConstraints.BOTH;
		((GridBagLayout) getLayout()).setConstraints(spacer, c);

		validate();
		getParent().validate();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (getComponents().length > 1)
			return;

		for (int i = 1; i < 10; i++) {
			g.setColor(new Color(0, 0, 0, 255 / 10 * i));
			double angle = (System.currentTimeMillis() % 1000) * 1f / 500 * Math.PI;
			g.fillOval((int) (Math.cos(angle + Math.PI * 2 / 10 * i) * 30) - 5 + getWidth() / 2,
					(int) (Math.sin(angle + Math.PI * 2 / 10 * i) * 30) - 5 + getHeight() / 2, 10,
					10);
		}

		if (System.currentTimeMillis() > startLoading + 10000) {
			g.setColor(Color.gray);
			String outMessage = "This is taking a while.";
			String outMessage2 = "Delay could be due to antivirus software checking over the TTS script, please be patient.";
			g.drawString(outMessage, getWidth() / 2 - g.getFontMetrics().stringWidth(outMessage) / 2,
					getHeight() / 2 + 50 + 10);
			g.drawString(outMessage2, getWidth() / 2 - g.getFontMetrics().stringWidth(outMessage2) / 2,
					getHeight() / 2 + 70 + 10);
		}

		repaint();

	}

}
