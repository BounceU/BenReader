package com.benliebkemann;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import nl.siegmann.epublib.domain.Author;

public class BookTableModel extends AbstractTableModel implements PropertyChangeListener {

	private List<BookModel> books;
	public static final int LINE_HEIGHT = 80;

	public BookTableModel() {
		books = new ArrayList<BookModel>();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int row = books.indexOf((BookModel) evt.getSource());
		switch (evt.getPropertyName()) {
			case "currentChapter":
				fireTableRowsUpdated(row, row);
				break;
			case "progressString":
				fireTableRowsUpdated(row, row);
				break;
			default:
				break;
		}

	}

	public boolean addBook(BookModel book) {
		book.addPropertyChangeListener(this);
		return books.add(book);
	}

	public boolean removeBook(BookModel book) {
		book.removePropertyChangeListener(this);
		return books.remove(book);
	}

	public BookModel getBook(int index) {
		if (index < books.size()) {
			return books.get(index);
		} else {
			return books.get(0);
		}
	}

	@Override
	public int getRowCount() {
		return books.size();

	}

	@Override
	public int getColumnCount() {
		// Cover, title, author, debug, progress bar
		return 5;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case 0:
				return "Cover";
			case 1:
				return "Title";

			case 2:
				return "Author";
			case 3:
				return "Information";
			case 4:
				return "Progress";
			default:
				System.out.println("Tried to get unknown column name");
				return "Unknown";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
				return ImageIcon.class;
			case 1:
				return String.class;
			case 2:
				return String.class;
			case 3:
				return String.class;
			case 4:
				return Float.class;
			default:
				return String.class;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= books.size())
			return null;
		switch (columnIndex) {
			case 0:
				try {
					return bookImage(books.get(rowIndex));
				} catch (IOException e) {
					e.printStackTrace();
				}
			case 1:
				return books.get(rowIndex).toString();
			case 2:
				List<Author> authors = books.get(rowIndex).getBook().getMetadata().getAuthors();
				if (authors.size() <= 0) {
					return "";
				} else {
					return authors.get(0).getFirstname() + " " + authors.get(0).getLastname();
				}
			case 3:
				return books.get(rowIndex).getProgressString();
			case 4:
				return (books.get(rowIndex).getCurrentChapter() + 1) * 1f / books.get(rowIndex).getNumChapters();
			default:
				return null;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (rowIndex >= books.size())
			return;
		switch (columnIndex) {
			case 0:
				// Can't set cover
				break;
			case 1:
				// Can't set title
				break;
			case 2:
				// Can't set author
				break;
			case 3:
				try {
					books.get(rowIndex).setProgressString((String) aValue);
				} catch (ClassCastException cce) {
					System.out.println("Unable to set value of progress string " + cce.getMessage());
				}
				break;
			case 4:
				try {
					books.get(rowIndex).setCurrentChapter((Integer) aValue);
				} catch (ClassCastException cce) {
					System.out.println("Unable to set value of progress bar " + cce.getMessage());
				}
				break;
			default:
				return;
		}
	}

	public ImageIcon bookImage(BookModel book) throws IOException {
		ImageIcon newImage = new ImageIcon(book.getBook().getCoverImage().getData());
		Image iconImage = newImage.getImage();
		int desiredHeight = LINE_HEIGHT - 20;

		newImage.setImage(iconImage.getScaledInstance(
				(int) (iconImage.getWidth(null) * 1f * desiredHeight / iconImage.getHeight(null)), desiredHeight,
				Image.SCALE_SMOOTH));
		return newImage;
	}

}
