package com.benliebkemann;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ButtonPanel extends JPanel {

	JButton addBook;
	JButton removeBook;
	JButton generateAudio;
	JButton pauseGeneration;
	JButton settings;
	JButton clean;
	Controller controller;

	// CONTROLLER - ADD AND REMOVE BOOKS

	public ButtonPanel(Controller controller) {
		super(new FlowLayout(FlowLayout.LEFT));

		this.controller = controller;

		addBook = new JButton("Add Book", resizeIcon(new ImageIcon("icons/add_book.png")));
		addBook.setVerticalTextPosition(SwingConstants.BOTTOM);
		addBook.setHorizontalTextPosition(SwingConstants.CENTER);
		addBook.setMargin(new Insets(5, 5, 5, 5));
		addBook.setPreferredSize(new Dimension(100, 100));
		addBook.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.addBook();
			}
		});
		add(addBook, BorderLayout.LINE_START);

		removeBook = new JButton("Remove Book", resizeIcon(new ImageIcon("icons/remove_book.png")));
		removeBook.setVerticalTextPosition(SwingConstants.BOTTOM);
		removeBook.setHorizontalTextPosition(SwingConstants.CENTER);
		removeBook.setMargin(new Insets(5, 5, 5, 5));
		removeBook.setPreferredSize(new Dimension(100, 100));
		removeBook.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.removeBook();
			}
		});
		add(removeBook, BorderLayout.LINE_START);

		generateAudio = new JButton("Run TTS", resizeIcon(new ImageIcon("icons/generate_audio.png")));
		generateAudio.setVerticalTextPosition(SwingConstants.BOTTOM);
		generateAudio.setHorizontalTextPosition(SwingConstants.CENTER);
		generateAudio.setMargin(new Insets(5, 5, 5, 5));
		generateAudio.setPreferredSize(new Dimension(100, 100));
		generateAudio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.generateAudio();
			}
		});
		add(generateAudio, BorderLayout.LINE_START);

		generateAudio = new JButton("Pause TTS", resizeIcon(new ImageIcon("icons/pause_generation.png")));
		generateAudio.setVerticalTextPosition(SwingConstants.BOTTOM);
		generateAudio.setHorizontalTextPosition(SwingConstants.CENTER);
		generateAudio.setMargin(new Insets(5, 5, 5, 5));
		generateAudio.setPreferredSize(new Dimension(100, 100));
		generateAudio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.pauseSelected();
			}
		});
		add(generateAudio, BorderLayout.LINE_START);

		clean = new JButton("Clean", resizeIcon(new ImageIcon("icons/clean.png")));
		clean.setVerticalTextPosition(SwingConstants.BOTTOM);
		clean.setHorizontalTextPosition(SwingConstants.CENTER);
		clean.setMargin(new Insets(5, 5, 5, 5));
		clean.setPreferredSize(new Dimension(100, 100));
		clean.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.cleanSelected();
			}
		});
		add(clean, BorderLayout.LINE_START);

		settings = new JButton("Settings", resizeIcon(new ImageIcon("icons/settings.png")));
		settings.setVerticalTextPosition(SwingConstants.BOTTOM);
		settings.setHorizontalTextPosition(SwingConstants.CENTER);
		settings.setMargin(new Insets(5, 5, 5, 5));
		settings.setPreferredSize(new Dimension(100, 100));
		settings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.showSettings();
			}
		});
		add(settings, BorderLayout.LINE_START);

	}

	private static ImageIcon resizeIcon(ImageIcon in) {
		Image iconImage = in.getImage();
		int desiredHeight = 60;

		in.setImage(iconImage.getScaledInstance(
				(int) (iconImage.getWidth(null) * 1f * desiredHeight / iconImage.getHeight(null)), desiredHeight,
				Image.SCALE_SMOOTH));
		return in;
	}

}
