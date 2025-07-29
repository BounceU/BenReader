package com.benliebkemann;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class BookModel implements Comparable<BookModel>, PropertyChangeListener {

	public static final int DIRECTORY_NAME_LENGTH = 8; // Arbitrary
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@JsonIgnore
	private Book book;

	private int currentChapter; // (-1 = not started, -2 = finished generating)
	private File bookDirectory; // Folder that the book's files are stored in. The folder will be a random
	// alphanumeric string so I don't have to deal with naming conflicts
	private File bookFile;
	private File workingDirectory;
	private File outputDirectory;

	private long elapsedTime;
	private long startTime;
	private boolean generating;

	private List<Integer> removeChapters;

	private int numChapters;

	private String progressString;

	private String audioCompression;

	@JsonCreator
	public BookModel(
			@JsonProperty("currentChapter") int currentChapter,
			@JsonProperty("bookDirectory") File bookDirectory,
			@JsonProperty("bookFile") File bookFile,
			@JsonProperty("workingDirectory") File workingDirectory,
			@JsonProperty("outputDirectory") File outputDirectory,
			@JsonProperty("numChapters") int numChapters,
			@JsonProperty("progressString") String progressString,
			@JsonProperty("removeChapters") List<Integer> removeChapters,
			@JsonProperty("elapsedTime") long elapsedTime,
			@JsonProperty("startTime") long startTime,
			@JsonProperty("generating") boolean generating,
			@JsonProperty("audioCompression") String audioCompression) {
		this.currentChapter = currentChapter;
		this.bookDirectory = bookDirectory;
		this.bookFile = bookFile;
		this.workingDirectory = workingDirectory;
		this.outputDirectory = outputDirectory;
		this.numChapters = numChapters;
		this.progressString = progressString;
		this.removeChapters = removeChapters;
		this.elapsedTime = elapsedTime;
		this.startTime = startTime;
		this.generating = generating;
		this.audioCompression = audioCompression;
		EpubReader epubReader = new EpubReader();
		try {
			book = epubReader.readEpub(
					new FileInputStream(bookFile));
		} catch (IOException e) {
			System.err.println("Error loading file " + bookFile
					+ "\n" + e.getMessage());
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Error loading file " + bookFile
							+ "\n" + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	/**
	 * Creates book model from an epub file on disk.
	 * Creates a new folder in the "res" directory and copies the epub file there.
	 * The loaded "book" is the one copied into the folder
	 * A "working" folder is also created in the folder that will house our
	 * intermediate files from the python generator.
	 * 
	 * @param epubOnDisk
	 */
	public BookModel(File epubOnDisk) {
		numChapters = 0;
		File resDirectory = new File(Main.filePrefix + "/res" + File.separator);
		String[] directories = resDirectory.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		String randomAlphanumeric = generateRandomString(DIRECTORY_NAME_LENGTH);

		while (Arrays.asList(directories).contains(randomAlphanumeric)) {
			randomAlphanumeric = generateRandomString(DIRECTORY_NAME_LENGTH);
		}

		bookDirectory = new File(Main.filePrefix + "/res" + File.separator + randomAlphanumeric + File.separator);
		workingDirectory = new File(Main.filePrefix +
				"/res" + File.separator + randomAlphanumeric + File.separator + "working" + File.separator);
		outputDirectory = new File(Main.filePrefix +
				"/res" + File.separator + randomAlphanumeric + File.separator + "output" + File.separator);
		workingDirectory.mkdirs();
		outputDirectory.mkdirs();

		Path sourcePath = Paths.get(epubOnDisk.getAbsolutePath());
		Path destinationPath = Paths.get(bookDirectory.getAbsolutePath() + File.separator + epubOnDisk.getName());
		try {
			Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			bookFile = destinationPath.toFile();
		} catch (IOException e) {
			System.err.println("Error copying file: " + e.getMessage());
			e.printStackTrace();
		}

		EpubReader epubReader = new EpubReader();
		try {
			book = epubReader.readEpub(new ZipInputStream(new FileInputStream(bookFile)));

		} catch (IOException e) {
			System.err.println("Error loading epub: " + e.getMessage());
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Error loading file " + destinationPath.toString()
							+ "\n" + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			Controller.deleteDirectory(bookDirectory);
			return;
		}

		this.removeChapters = new ArrayList<Integer>();
		this.elapsedTime = 0l;
		this.startTime = 0l;
		this.generating = false;
		this.audioCompression = "m4a";
		currentChapter = -1;
		this.pcs.addPropertyChangeListener(this);
		saveBook();

	}

	public void startingTTS() {
		generating = true;
		startTime = System.currentTimeMillis();
	}

	/**
	 * Creates book model object from the name of a folder in our
	 * "res" directory. Used for reloading books on different runs
	 * 
	 * @param bookName Name of folder containing book
	 * 
	 */
	public BookModel(String folderName) {

		if (new File(Main.filePrefix + "/res" + File.separator + folderName + File.separator + "book_info.json")
				.exists()) {
			loadBook(new File(
					Main.filePrefix + "/res" + File.separator + folderName + File.separator + "book_info.json"));
			this.pcs.addPropertyChangeListener(this);
			EpubReader epubReader = new EpubReader();
			try {
				book = epubReader.readEpub(
						new FileInputStream(bookFile));
			} catch (IOException e) {
				System.err.println("Error loading file " + bookFile
						+ "\n" + e.getMessage());
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Error loading file " + bookFile
								+ "\n" + e.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {

			numChapters = 0;
			bookDirectory = new File(Main.filePrefix + "/res" + File.separator + folderName + File.separator);
			workingDirectory = new File(Main.filePrefix +
					"/res" + File.separator + folderName + File.separator + "working" + File.separator);
			outputDirectory = new File(Main.filePrefix +
					"/res" + File.separator + folderName + File.separator + "output" + File.separator);
			workingDirectory.mkdirs();
			outputDirectory.mkdirs();

			String[] files = bookDirectory.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).getAbsolutePath().endsWith(".epub");
				}
			});

			if (files.length != 1) {
				System.err.println("Error, unexpected number of epub files: " + files.length);
				return;
			}

			bookFile = new File(bookDirectory.getAbsolutePath() + File.separator + files[0]);
			EpubReader epubReader = new EpubReader();
			try {
				book = epubReader.readEpub(
						new FileInputStream(bookFile));
			} catch (IOException e) {
				System.err.println("Error loading file " + bookFile
						+ "\n" + e.getMessage());
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Error loading file " + bookFile
								+ "\n" + e.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			this.removeChapters = new ArrayList<Integer>();
			this.elapsedTime = 0l;
			this.startTime = 0l;
			this.generating = false;
			currentChapter = -1;
			this.pcs.addPropertyChangeListener(this);
			saveBook();
		}

	}

	public void saveBook() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(new File(bookDirectory + File.separator + "book_info.json"),
					this);
		} catch (IOException ioe) {
			Controller.showError("Couldn't save book: " + ioe.getMessage());
		}
	}

	public void loadBook(File file) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			BookModel loadBook = mapper.readValue(file, BookModel.class);
			this.setBook(loadBook.getBook());
			this.setCurrentChapter(loadBook.getCurrentChapter());
			this.setBookDirectory(loadBook.getBookDirectory());
			this.setBookFile(loadBook.getBookFile());
			this.setWorkingDirectory(loadBook.getWorkingDirectory());
			this.setOutputDirectory(loadBook.getOutputDirectory());
			this.setNumChapters(loadBook.getNumChapters());
			this.setProgressString(loadBook.getProgressString());
			this.setRemoveChapters(loadBook.getRemoveChapters());
			this.setGenerating(loadBook.getGenerating());
			this.setElapsedTime(loadBook.getElapsedTime());
			this.setStartTime(loadBook.getStartTime());
			this.setAudioCompression(loadBook.getAudioCompression());
		} catch (IOException ioe) {
			Controller.showError("Couldn't load book: " + ioe.getMessage());
		}
	}

	public static String generateRandomString(int length) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder sb = new StringBuilder();
		Random random = new Random();

		for (int i = 0; i < length; i++) {
			int index = random.nextInt(characters.length());
			sb.append(characters.charAt(index));
		}
		return sb.toString();
	}

	public static List<BookModel> getExisting() {
		List<BookModel> bookModels = new ArrayList<BookModel>();
		File resDirectory = new File(Main.filePrefix + "/res" + File.separator);
		String[] directories = resDirectory.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		for (String directory : directories) {
			BookModel temp = new BookModel(directory);
			if (temp.book != null)
				bookModels.add(temp);
		}

		return bookModels;

	}

	@Override
	public String toString() {
		return book.getTitle();
	}

	@Override
	public int compareTo(BookModel o) {
		return (o).toString().compareTo(this.toString());
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		Book oldBook = this.book;
		this.book = book;
		this.pcs.firePropertyChange("book", oldBook, book);
	}

	public int getCurrentChapter() {
		return currentChapter;
	}

	public void setCurrentChapter(int currentChapter) {
		int oldCurrentChapter = this.currentChapter;
		this.currentChapter = currentChapter;
		if (generating) {
			setElapsedTime(elapsedTime + System.currentTimeMillis() - startTime);
			setStartTime(System.currentTimeMillis());
		}
		this.pcs.firePropertyChange("currentChapter", oldCurrentChapter, currentChapter);
	}

	public File getBookDirectory() {
		return bookDirectory;
	}

	public void setBookDirectory(File bookDirectory) {
		File oldBookDirectory = this.bookDirectory;
		this.bookDirectory = bookDirectory;
		this.pcs.firePropertyChange("bookDirectory", oldBookDirectory, bookDirectory);
	}

	public File getBookFile() {
		return bookFile;
	}

	public void setBookFile(File bookFile) {
		File oldBookFile = this.bookFile;
		this.bookFile = bookFile;
		this.pcs.firePropertyChange("bookFile", oldBookFile, bookFile);
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(File workingDirectory) {
		File oldWorkingDirectory = this.workingDirectory;
		this.workingDirectory = workingDirectory;
		this.pcs.firePropertyChange("workingDirectory", oldWorkingDirectory, workingDirectory);
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		File oldOutputDirectory = this.outputDirectory;
		this.outputDirectory = outputDirectory;
		this.pcs.firePropertyChange("outputDirectory", oldOutputDirectory, outputDirectory);
	}

	public int getNumChapters() {
		return numChapters;
	}

	public void setNumChapters(int numChapters) {
		int oldNumChapters = this.numChapters;
		this.numChapters = numChapters;
		this.pcs.firePropertyChange("numChapters", oldNumChapters, numChapters);
	}

	public String getProgressString() {
		return progressString;
	}

	public void setProgressString(String progressString) {
		String oldProgressString = this.progressString;
		this.progressString = progressString;
		this.pcs.firePropertyChange("progressString", oldProgressString, progressString);
	}

	public List<Integer> getRemoveChapters() {
		return removeChapters;
	}

	public void setRemoveChapters(List<Integer> removeChapters) {
		List<Integer> oldRemoveChapters = this.removeChapters;
		this.removeChapters = removeChapters;
		this.pcs.firePropertyChange("removeChapters", oldRemoveChapters, removeChapters);
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		long oldElapsedTime = this.elapsedTime;
		this.elapsedTime = elapsedTime;
		this.pcs.firePropertyChange("elapsedTime", oldElapsedTime, elapsedTime);
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		long oldStartTime = this.startTime;
		this.startTime = startTime;
		this.pcs.firePropertyChange("startTime", oldStartTime, startTime);
	}

	public boolean getGenerating() {
		return generating;
	}

	public void setGenerating(boolean generating) {
		boolean oldGenerating = this.generating;
		this.generating = generating;
		this.pcs.firePropertyChange("generating", oldGenerating, generating);
		if (generating) {
			setStartTime(System.currentTimeMillis());
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public void setAudioCompression(String audioCompression) {
		String oldAudioCompression = this.audioCompression;
		this.audioCompression = audioCompression;
		this.pcs.firePropertyChange("audioCompression", oldAudioCompression, audioCompression);
	}

	public String getAudioCompression() {
		return audioCompression;
	}

	public void clean(boolean cleanText) {
		setGenerating(false);
		if (cleanText) {
			setProgressString("Cleaned.");
			setElapsedTime(0);
			setStartTime(0);
			setNumChapters(getBook().getSpine().size());
			setCurrentChapter(-1);
		}
		setRemoveChapters(new ArrayList<Integer>());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		saveBook();
	}
}
