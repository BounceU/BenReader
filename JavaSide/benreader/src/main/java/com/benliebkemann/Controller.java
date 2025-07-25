package com.benliebkemann;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Controller implements ActionListener, WindowListener {

	private Main main;

	private List<Digestrar> digestrars;
	private List<BookModel> queue;

	public Controller(Main main) {
		this.main = main;
		digestrars = new ArrayList<Digestrar>();
	}

	public void generateAudio() {
		BookModel toGenerate = main.getSelectedBook();
		if (toGenerate == null || toGenerate.getBook() == null) {
			showError("Couldn't generate book, book is null.");
			return;
		}

		for (Digestrar d : digestrars) {
			if (d.getBook() == null) {
				continue;
			}
			if (d.getBook().equals(toGenerate) || toGenerate.getGenerating()) {
				showError("Already generating");
				return;
			}
		}

		boolean newGeneration = true;
		if (toGenerate.getCurrentChapter() >= 0 && toGenerate.getCurrentChapter() < toGenerate.getNumChapters() - 1) {
			Object[] options = { "Resume", "Generate New", "Cancel" };
			int choice = JOptionPane.showOptionDialog(main, "Previous run detected, do you want to resume?", "Resume?",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (choice == JOptionPane.YES_OPTION) {
				newGeneration = false;
			} else if (choice == JOptionPane.NO_OPTION) {
				newGeneration = true;
			} else if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
				return;
			}
		}

		if (newGeneration) {
			SelectionFrame selectionFrame = new SelectionFrame("Select Chapters", this, toGenerate);
			Digestrar d = new Digestrar(selectionFrame.getLogArea(), selectionFrame.getCheckList(),
					selectionFrame.getBook(), this);
			digestrars.add(d);
			selectionFrame.start(d);
		} else {
			// Debug log frame
			JFrame debugFrame = new JFrame("Log");
			JTextArea logArea = new JTextArea();
			JScrollPane debugScrollPane = new JScrollPane(logArea);
			debugFrame.add(debugScrollPane);
			debugFrame.pack();
			debugFrame.setSize(600, 500);
			debugFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// debugFrame.setVisible(true);
			Digestrar d = new Digestrar(logArea, toGenerate, this);
			digestrars.add(d);
			d.runTest();

			try {
				Thread.sleep(50);
				Thread.sleep(4950);
			} catch (InterruptedException ie) {

			}

			d.resume();
		}

	}

	public void destroyDigestrars() {
		for (Digestrar d : digestrars) {
			d.pauseApp();
		}
	}

	public void showSettings() {
		SettingsWindow settingsWindow = new SettingsWindow();
		settingsWindow.run();
	}

	public void completeGenerating(SelectionFrame selectionFrame) {
		ChapterCheckList checkList = selectionFrame.getCheckList();
		List<Chapter> chapters = checkList.getChapters();
		Digestrar d = selectionFrame.getDigestrar();
		d.setAudioCompression(selectionFrame.useM4a.isSelected() ? "m4a" : "mp3");
		selectionFrame.dispose();
		cleanBook(d.getBook());
		d.removeChaptersAndGo(chapters);
	}

	public void addBook() {

		FileDialog fileDialog = new FileDialog(main, "Open epub", FileDialog.LOAD);
		fileDialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".epub"));
		fileDialog.setVisible(true);

		String directory = fileDialog.getDirectory();
		String fileName = fileDialog.getFile();

		if (directory != null && fileName != null && fileName.endsWith(".epub")) {
			System.out.println(directory + "\n" + fileName);
			main.addBook(new File(directory + File.separator + fileName));
		} else {
			System.out.println("Couldn't add file");
		}

		main.revalidate();
		main.repaint();
	}

	public void removeBook() {
		main.removeSelectedBook();
		main.revalidate();
		main.repaint();
	}

	public void pauseSelected() {
		Iterator<Digestrar> iterator = digestrars.iterator();
		while (iterator.hasNext()) {
			Digestrar d = iterator.next();
			if (d.getBook().equals(main.getSelectedBook())) {
				d.pauseApp();
				iterator.remove();
			}
		}
	}

	public void cleanBook(BookModel book) {
		book.clean(true);
		if (book != null && book.getBook() != null) {
			deleteDirectory(book.getWorkingDirectory());
			book.getWorkingDirectory().mkdirs();
			if (Main.SETTINGS.getClearOutput()) {
				deleteDirectory(book.getOutputDirectory());
				book.getOutputDirectory().mkdirs();
			}
		}
	}

	public void cleanSelected() {
		BookModel book = main.getSelectedBook();
		cleanBook(book);
	}

	public static void deleteDirectory(File directoryToDelete) {
		try {
			Files.walkFileTree(Paths.get(directoryToDelete.getAbsolutePath()), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc != null) {
						throw exc;
					}
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException ioe) {
			System.err.println("Error deleting directory: " + ioe.getMessage());
			showError("Error deleting directory: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	}

	public static void showError(String message) {
		JOptionPane.showMessageDialog(null,
				message,
				"Error",
				JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		try {
			// Selection frame so terminate
			((SelectionFrame) e.getWindow()).terminate();
		} catch (ClassCastException cce) {
			((JFrame) e.getComponent()).dispose();
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "Cancel":
				((SelectionFrame) (((JButton) e.getSource()).getTopLevelAncestor())).terminate();
				break;
			case "Continue":
				completeGenerating((SelectionFrame) (((JButton) e.getSource()).getTopLevelAncestor()));
				break;
			case "Select All":
				((SelectionFrame) (((JButton) e.getSource()).getTopLevelAncestor())).selectAll();
				break;
			case "Deselect All":
				((SelectionFrame) (((JButton) e.getSource()).getTopLevelAncestor())).deselectAll();
				break;
			default:
				break;
		}
	}

	private class SelectionFrame extends JFrame {

		private JFrame debugFrame;
		private JTextArea logArea;
		private Digestrar d;
		private ChapterCheckList checkList;
		private JCheckBox useM4a;

		private BookModel bookToGenerate;

		public BookModel getBook() {
			return bookToGenerate;
		}

		public void selectAll() {
			checkList.selectAll();
		}

		public void deselectAll() {
			checkList.deselectAll();
		}

		public void terminate() {
			d.stopApp();
			bookToGenerate.setProgressString("Cancelled");
			digestrars.remove(d);
			debugFrame.dispose();
			dispose();
		}

		public Digestrar getDigestrar() {
			return d;
		}

		public ChapterCheckList getCheckList() {
			return checkList;
		}

		public JTextArea getLogArea() {
			return logArea;
		}

		public SelectionFrame(String title, Controller controller, BookModel bookToGenerate) {
			super(title);

			this.bookToGenerate = bookToGenerate;

			// Debug log frame
			debugFrame = new JFrame("Log");
			logArea = new JTextArea();
			JScrollPane debugScrollPane = new JScrollPane(logArea);
			debugFrame.add(debugScrollPane);
			debugFrame.pack();
			debugFrame.addWindowListener(controller);

			// Selection Frame
			addWindowListener(controller);
			getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			JLabel label = new JLabel("Select Chapters To Include");
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
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 1.0;
			c.weighty = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.PAGE_START;
			add(spacerTop1, c);

			JButton selectAll = new JButton("Select All");
			selectAll.addActionListener(controller);
			c.gridy = 0;
			c.gridx = 2;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0;
			c.weighty = 0;
			c.insets = new Insets(10, 10, 10, 10);
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.SOUTHEAST;
			add(selectAll, c);

			JButton deselectAll = new JButton("Deselect All");
			deselectAll.addActionListener(controller);
			c.gridy = 0;
			c.gridx = 3;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0;
			c.weighty = 0;
			c.insets = new Insets(10, 10, 10, 20);
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.SOUTHEAST;
			add(deselectAll, c);

			checkList = new ChapterCheckList();
			JScrollPane scrollPane = new JScrollPane(checkList);
			c.gridy = 1;
			c.gridx = 0;
			c.gridwidth = 4;
			c.gridheight = 1;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(10, 10, 10, 10);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			add(scrollPane, c);

			useM4a = new JCheckBox("Use high quality audio");
			useM4a.setToolTipText(
					"If selected, uses lossless compression (ALAC) for audio. Otherwise it uses mp3 compression which has a significantly reduced filesize, but can lead to slight timing deviations.");
			useM4a.setSelected(Main.SETTINGS.getUseM4a());
			c.gridy = 3;
			c.gridx = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 1.0;
			c.weighty = 0;
			c.insets = new Insets(10, 10, 10, 10);
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.SOUTHEAST;
			add(useM4a, c);

			JPanel spacer = new JPanel();
			c.gridy = 3;
			c.gridx = 1;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 1.0;
			c.weighty = 0;
			c.insets = new Insets(10, 10, 10, 10);
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.SOUTHEAST;
			add(spacer, c);

			JButton stop = new JButton("Cancel");
			stop.addActionListener(controller);
			c.gridy = 3;
			c.gridx = 2;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0;
			c.weighty = 0;
			c.insets = new Insets(10, 10, 10, 10);
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.SOUTHEAST;
			add(stop, c);

			JButton go = new JButton("Continue");
			go.addActionListener(controller);
			c.gridy = 3;
			c.gridx = 3;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0;
			c.weighty = 0;
			c.insets = new Insets(10, 10, 10, 20);
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.SOUTHEAST;
			add(go, c);
			pack();
		}

		public void start(Digestrar d) {
			this.d = d;
			// debugFrame.setVisible(true);
			debugFrame.setSize(500, 500);
			debugFrame.setLocationRelativeTo(main);
			debugFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			setSize(800, 500);
			setMinimumSize(new Dimension(382, 284));
			setLocationRelativeTo(main);
			setVisible(true);
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			d.runTest();
		}

	}

	public void nextInQueue() {

	}

	public void registrarDone(Digestrar digestrar) {
		Iterator<Digestrar> iterator = digestrars.iterator();
		while (iterator.hasNext()) {
			Digestrar d = iterator.next();
			if (d.getBook().equals(main.getSelectedBook())) {
				d.getBook().setProgressString("Done.");
				d.getBook().clean(false);
				iterator.remove();
			}
		}

	}

}
