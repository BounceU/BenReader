package com.benliebkemann;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import nl.siegmann.epublib.domain.Author;

public class BookInformationView extends JPanel implements PropertyChangeListener {

	private BookModel book;

	private JLabel coverImage = new JLabel();
	private JLabel title = new JLabel();
	private JLabel author = new JLabel();
	private JLabel description = new JLabel();
	private JTextPane pathText = new JTextPane();
	private JTextPane numChaptersPane = new JTextPane();
	private JLabel timeText = new JLabel();

	public BookInformationView() {
		super(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		// Cover Image
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(20, 0, 20, 0);
		add(coverImage, c);

		// Title
		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 1;
		c.gridwidth = 2;
		add(title, c);

		// Author
		c.gridy = 2;
		c.gridwidth = 2;
		author.setForeground(Color.gray);
		add(author, c);

		// Description
		c.gridy = 3;
		c.gridwidth = 2;
		add(description, c);

		// Timing
		c.gridy = 4;
		c.gridwidth = 2;
		add(timeText, c);

		// Number of chapters
		c.gridy = 5;
		c.gridwidth = 2;
		numChaptersPane.setEditable(false);
		numChaptersPane.setBackground(getBackground());
		numChaptersPane.setForeground(getForeground());
		add(numChaptersPane, c);

		// Path
		c.gridy = 6;
		c.gridx = 0;
		c.gridwidth = 2;

		pathText.setEditable(false);
		pathText.setBackground(getBackground());
		pathText.setForeground(getForeground());
		pathText.setContentType("text/html");
		if (Desktop.isDesktopSupported()) {
			pathText.setForeground(Color.blue);
			pathText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			pathText.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						Desktop.getDesktop().open(new File(book.getBookDirectory().getAbsolutePath() + File.separator));
					} catch (IOException ioe) {
						ioe.printStackTrace();
						Controller.showError("Error opening path:\n" + ioe.getMessage());
					}
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					SimpleAttributeSet attributeSet = new SimpleAttributeSet();
					StyleConstants.setUnderline(attributeSet, true);
					pathText.getStyledDocument().setCharacterAttributes(0, pathText.getText().length(), attributeSet,
							false);

				}

				@Override
				public void mouseExited(MouseEvent e) {
					SimpleAttributeSet attributeSet = new SimpleAttributeSet();
					StyleConstants.setUnderline(attributeSet, false);
					pathText.getStyledDocument().setCharacterAttributes(0, pathText.getText().length(), attributeSet,
							false);
				}

			});
		}
		add(pathText, c);

	}

	public void clearInformation() {
		coverImage.setIcon(null);
		title.setText("");
		author.setText("");
		pathText.setText("");
		description.setText("");
		numChaptersPane.setText("");
		this.book = null;
	}

	public void updateInformation(BookModel inBook) {
		if (this.book != null) {
			this.book.removePropertyChangeListener(this);
		}
		this.book = inBook;
		if (this.book != null)
			this.book.addPropertyChangeListener(this);
		try {
			coverImage.setIcon(resizeIcon(new ImageIcon(book.getBook().getCoverImage().getData())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		title.setText(
				"<html><div style=\"width:200px;text-align:center;\">" + book.getBook().getTitle() + "</div></html>");
		String allAuthors = "";
		for (Author author : this.book.getBook().getMetadata().getAuthors()) {
			allAuthors = allAuthors + author.getFirstname() + " " + author.getLastname() + ", ";
		}
		allAuthors = allAuthors.substring(0, allAuthors.length() - 2);
		author.setText("<html><div style=\"width:200px;text-align:center;\">" + allAuthors + "</div></html>");
		description.setText(book.getProgressString());
		numChaptersPane.setText(
				(book.getBook().getSpine().getSpineReferences().size() + " Chapters in Book").replace("\n", ""));
		setTimeText(book.getElapsedTime());
		// pathText.setText(book.getBookDirectory().getAbsolutePath() + File.separator);
		String bookLocation = "<html><p style=\"width:200px;text-align:center;word-wrap:break-word;word-break:break-all;white-space:pre-wrap;white-space:normal;\">"
				+ book.getBookDirectory().getAbsolutePath() + File.separator + "</p></html>";
		pathText.setText(bookLocation);
	}

	private static ImageIcon resizeIcon(ImageIcon in) {
		Image iconImage = in.getImage();
		int desiredHeight = 200;

		in.setImage(iconImage.getScaledInstance(
				(int) (iconImage.getWidth(null) * 1f * desiredHeight / iconImage.getHeight(null)), desiredHeight,
				Image.SCALE_SMOOTH));
		return in;
	}

	private void setTimeText(long totalTime) {
		if (totalTime == 0) {
			timeText.setText("Not enough information to estimate time");
			return;
		}
		int divisor = book.getCurrentChapter() == -1 ? 1 : book.getCurrentChapter() + 1;
		long averageTime = totalTime / divisor;
		long guess = (book.getCurrentChapter() == -1) ? 0 : (book.getNumChapters() - divisor) * averageTime;
		long hoursElapsed = totalTime / 3600000;
		long minutesElapsed = (totalTime % 3600000) / 60000;
		long secondsElapsed = (totalTime % 60000) / 1000;
		long hoursLeft = guess / 3600000;
		long minutesLeft = (guess % 3600000) / 60000;
		long secondsLeft = (guess % 60000) / 1000;
		timeText.setText(String.format("%02d:%02d:%02d / %02d:%02d:%02d", hoursElapsed, minutesElapsed,
				secondsElapsed, hoursLeft, minutesLeft, secondsLeft));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
			case "elapsedTime":
				setTimeText((long) evt.getNewValue());
				break;
			case "currentChapter":
				description.setText("Progress: " + (((int) evt.getNewValue()) + 1) + "/" + book.getNumChapters());
				break;
			case "progressString":
				description.setText((String) evt.getNewValue());
				break;
			default:
				break;
		}
	}
}
