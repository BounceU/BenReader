package com.benliebkemann;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ListCellRenderer;

import nl.siegmann.epublib.domain.Author;

public class BookListRenderer extends JPanel implements ListCellRenderer<BookModel> {

	public static final int LINE_HEIGHT = 80;

	private BookModel book;
	private ImageIcon image;

	private JLabel imageLabel = new JLabel();
	private JLabel titleLabel = new JLabel();
	private JLabel authorLabel = new JLabel();
	private JProgressBar progressBar;

	public BookListRenderer() {
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		setImage(new ImageIcon(Main.filePrefix + "/icons" + File.separator + "default_cover.png"));

		// Book Cover
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(10, 5, 10, 5);
		c.gridwidth = 1;
		c.weightx = 0;
		add(imageLabel, c);

		// Book title
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(10, 5, 10, 5);
		c.weightx = 0.3;
		add(titleLabel, c);

		// Book author
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(10, 5, 10, 5);
		c.weightx = 0.3;
		authorLabel.setForeground(Color.gray);
		add(authorLabel, c);

		// Information
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new Insets(10, 5, 10, 5);
		c.weightx = 1;
		progressBar = new JProgressBar(0, 100);
		add(progressBar, c);

		setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends BookModel> list, BookModel value, int index,
			boolean isSelected, boolean cellHasFocus) {

		setBook(value);

		try {
			setImage(new ImageIcon(book.getBook().getCoverImage().getData()));
		} catch (IOException e) {
			System.err.println("Couldn't load book cover for list: " + e.getMessage());
			e.printStackTrace();
		}

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		return this;
	}

	public void setBook(BookModel newBook) {
		this.book = newBook;

		String titleString = this.book.toString();

		int desiredLength = 50;
		if (titleString.length() > desiredLength) {
			titleString = titleString.substring(0, desiredLength - 3) + "...";
		}
		String newString = String.format("%-" + desiredLength + "s", titleString);
		titleLabel.setText("" + newString + "");
		titleLabel.setFont(new Font("Courier New", Font.BOLD, getFont().getSize()));

		desiredLength = 30;
		String allAuthors = "";
		for (Author author : this.book.getBook().getMetadata().getAuthors()) {
			allAuthors = allAuthors + author.getFirstname() + " " + author.getLastname() + ", ";
		}
		allAuthors = allAuthors.substring(0, allAuthors.length() - 2);
		if (allAuthors.length() > desiredLength) {
			allAuthors = allAuthors.substring(0, desiredLength - 3) + "...";
		}
		allAuthors = String.format("%-" + desiredLength + "s", allAuthors);
		authorLabel.setText(allAuthors);
		authorLabel.setFont(new Font("Courier New", Font.BOLD, getFont().getSize()));
		progressBar.setMaximum(this.book.getNumChapters());
		progressBar.setValue(this.book.getCurrentChapter() >= 0 ? this.book.getCurrentChapter() : 0);

	}

	public BookModel getBook() {
		return book;
	}

	public void setImage(ImageIcon newImage) {
		Image iconImage = newImage.getImage();
		int desiredHeight = LINE_HEIGHT - 20;

		newImage.setImage(iconImage.getScaledInstance(
				(int) (iconImage.getWidth(null) * 1f * desiredHeight / iconImage.getHeight(null)), desiredHeight,
				Image.SCALE_SMOOTH));
		this.image = newImage;
		imageLabel.setIcon(this.image);
	}

	public ImageIcon getImage() {
		return this.image;
	}

}
