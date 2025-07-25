package com.benliebkemann;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SettingsWindow extends JFrame implements ActionListener {

	private JTextArea voice;
	private JCheckBox cleanOutput;
	private JCheckBox useM4a;

	public SettingsWindow() {
		super("Settings");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.getContentPane().setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		JLabel label = new JLabel("Settings");
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;
		c.insets = new Insets(10, 10, 10, 10);
		add(label, c);

		JPanel spacerTop1 = new JPanel();
		c.gridy = 0;
		c.gridx = 1;
		c.gridwidth = 5;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;
		add(spacerTop1, c);

		SettingsPanel settingsPanel = new SettingsPanel();
		JScrollPane scrollPane = new JScrollPane(settingsPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 6;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(10, 10, 10, 10);
		add(scrollPane, c);

		JPanel spacer = new JPanel();
		c.gridy = 3;
		c.gridx = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.SOUTHEAST;
		add(spacer, c);

		JButton ok = new JButton("OK");
		ok.addActionListener(this);
		c.gridy = 3;
		c.gridx = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHEAST;
		add(ok, c);

		JButton apply = new JButton("Apply");
		apply.addActionListener(this);
		c.gridy = 3;
		c.gridx = 4;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHEAST;
		add(apply, c);

		JButton close = new JButton("Cancel");
		close.addActionListener(this);
		c.gridy = 3;
		c.gridx = 5;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(10, 10, 10, 20);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHEAST;
		add(close, c);

		pack();

	}

	public void run() {
		setSize(800, 600);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void apply() {
		Main.SETTINGS.setVoice(voice.getText());
		Main.SETTINGS.setClearOutput(cleanOutput.isSelected());
		Main.SETTINGS.setUseM4a(useM4a.isSelected());
		Main.SETTINGS.updateSettings();
	}

	public void close() {
		dispose();
	}

	public class SettingsPanel extends JPanel implements ComponentListener {
		public SettingsPanel() {
			super(new GridBagLayout());
			addComponentListener(this);
			GridBagConstraints c = new GridBagConstraints();
			JLabel voiceLabel = new JLabel("Voice: ");
			c.gridy = 0;
			c.gridx = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(10, 10, 10, 10);
			add(voiceLabel, c);

			voice = new JTextArea(Main.SETTINGS.getVoice());
			voice.setLineWrap(true);
			c.gridy = 0;
			c.gridx = 1;
			c.gridwidth = 3;
			c.gridheight = 1;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(10, 10, 10, 10);
			add(voice, c);

			cleanOutput = new JCheckBox("Clear Output Folder on Clean");
			cleanOutput.setSelected(Main.SETTINGS.getClearOutput());
			c.gridy = 1;
			c.gridx = 1;
			c.gridwidth = 4;
			c.gridheight = 1;
			c.weightx = 0;
			c.weighty = 0;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(10, 10, 10, 10);
			add(cleanOutput, c);

			useM4a = new JCheckBox("Default to using m4a (higher quality, larger files)");
			useM4a.setToolTipText(
					"Using m4a leads to better audio quality. Not using m4a switches the audio compression to mp3. mp3 has significantly reduced filesize but may lead to slight timing inaccuracies");
			useM4a.setSelected(Main.SETTINGS.getUseM4a());
			c.gridy = 2;
			c.gridx = 1;
			c.gridwidth = 4;
			c.gridheight = 1;
			c.weightx = 0;
			c.weighty = 0;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(10, 10, 10, 10);
			add(useM4a, c);

		}

		@Override
		public void componentResized(ComponentEvent e) {
			voice.validate();
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentShown(ComponentEvent e) {
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "OK":
				apply();
				close();
				break;
			case "Cancel":
				close();
				break;
			case "Apply":
				apply();
				break;
		}
	}

}
