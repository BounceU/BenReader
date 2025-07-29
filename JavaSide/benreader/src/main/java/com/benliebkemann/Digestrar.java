package com.benliebkemann;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

// MVC ARCHITECTURE, THIS IS THE CONTROLLER

public class Digestrar {

	public static String PROGRAM_LOCATION = Main.filePrefix + "/tts" + File.separator + "tts_epub"
			+ (System.getProperty("os.name").startsWith("Windows") ? ".exe" : "");// "D:\\Programming\\VSCode
	// Python\\dist\\tts_epub.exe";

	private Process currentProcess;
	private PrintWriter processWriter;
	private OutputReaderWorker outputReaderWorker;
	private Controller controller;

	private ChapterCheckList chapterList;

	private boolean resuming;

	private JTextArea outArea;

	private BookModel book;

	public Digestrar(JTextArea textArea, ChapterCheckList checkList, BookModel book, Controller controller) {
		outArea = textArea;
		this.chapterList = checkList;
		this.book = book;
		this.resuming = false;
		this.controller = controller;
	}

	public Digestrar(JTextArea textArea, BookModel book, Controller controller) {
		outArea = textArea;
		this.book = book;
		this.resuming = true;
		this.controller = controller;
	}

	public BookModel getBook() {
		return book;
	}

	public void removeChaptersAndGo(List<Chapter> chapters) {
		removeChapters(chapters);
		go();
	}

	public void resume() {
		for (int removeChapter : book.getRemoveChapters()) {
			print("r " + removeChapter);
		}
		print("s " + book.getCurrentChapter());
		go();
	}

	private void removeChapters(List<Chapter> chapters) {
		int numChapters = 0;
		for (Chapter chapter : chapters) {
			if (!chapter.getShouldUse()) {
				print("r " + chapter.getNumber());
				book.getRemoveChapters().add(chapter.getNumber());
			} else {
				numChapters += 1;
			}
		}
		book.setNumChapters(numChapters);
	}

	private void go() {
		book.setProgressString("Starting up TTS");
		book.setGenerating(true);
		print("a " + book.getAudioCompression());
		print("g");
	}

	public void runTest() {
		startApp();
	}

	private void startApp() {
		new Thread(() -> {
			try {
				String[] command;
				// command = new String[] { "python \"D:\\Programming\\VSCode
				// Python\\testing.py\"" };
				command = new String[] {
						// "python", "/Users/benliebkemann/Desktop/App Dev/Book AI/VSCode
						// Python/testing.py"
						PROGRAM_LOCATION, "-v", Main.SETTINGS.getVoice(), "-i", book.getBookFile().getAbsolutePath(),
						"-d",
						book.getWorkingDirectory().getAbsolutePath(), "-m"

				};
				ProcessBuilder processBuilder = new ProcessBuilder(command);

				processBuilder.redirectErrorStream(true);
				currentProcess = processBuilder.start();
				processWriter = new PrintWriter(new OutputStreamWriter(currentProcess.getOutputStream()));

				outputReaderWorker = new OutputReaderWorker(currentProcess.getInputStream());
				outputReaderWorker.execute();

				int exitCode = currentProcess.waitFor();
				if (exitCode != 0 && currentProcess != null) {
					book.setProgressString("Error. Check log.");
				} else if (exitCode == 0) {
					try {
						String lastPath = book.getWorkingDirectory().getPath() + File.separator
								+ book.getBookFile().getName().replace(".epub", "").replaceAll("[^a-zA-Z0-9]", "")
								+ ".zip";
						File created = new File(lastPath);
						String newPath = book.getOutputDirectory().getPath() + File.separator
								+ book.getBookFile().getName().replace(".epub", "").replaceAll("[^a-zA-Z0-9]", "")
								+ ".benr";
						if (created.exists()) {
							Files.move(Paths.get(lastPath), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
						}
					} catch (IOException ioe) {
						Controller.showError("Could not copy to output folder: " + ioe.getMessage());
					}
				}
				SwingUtilities.invokeLater(() -> {
					outArea.append("\n Application exited with code " + exitCode + "\n");
					stopApp();
				});
			} catch (IOException ioe) {
				outArea.append("Error starting application: " + ioe.getMessage() + "\n");
				ioe.printStackTrace();
				stopApp();
			} catch (InterruptedException ie) {
				outArea.append("Error starting application: " + ie.getMessage() + "\n");
				ie.printStackTrace();
				stopApp();
			}
		}).start();
	}

	public void stopApp() {
		if (currentProcess != null) {
			outArea.append("Stopping command-line application...\n");
			currentProcess.destroy();
			try {
				if (!currentProcess.waitFor(5, TimeUnit.SECONDS)) {
					currentProcess.destroyForcibly();
					outArea.append("Closed forcibly\n");
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				outArea.append("Interrupted while waiting to stop");
			}
		}

		if (outputReaderWorker != null) {
			outputReaderWorker.cancel(true);
			outputReaderWorker = null;
		}

		if (processWriter != null) {
			processWriter.close();
			processWriter = null;
		}

		currentProcess = null;
		outArea.append("Application ended.\n");

		try {
			FileWriter writer = new FileWriter(book.getBookDirectory().toPath() + File.separator + "log.txt");
			writer.write(outArea.getText());
			writer.close();
		} catch (IOException ioe) {
			Controller.showError("Couldn't write log: " + ioe.getMessage());
		}
		controller.registrarDone(this);
	}

	public void pauseApp() {
		if (currentProcess != null) {
			outArea.append("Stopping command-line application...\n");
			currentProcess.destroy();
			try {
				if (!currentProcess.waitFor(5, TimeUnit.SECONDS)) {
					currentProcess.destroyForcibly();
					outArea.append("Closed forcibly\n");
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				outArea.append("Interrupted while waiting to stop");
			}
		}

		if (outputReaderWorker != null) {
			outputReaderWorker.cancel(true);
			outputReaderWorker = null;
		}

		if (processWriter != null) {
			processWriter.close();
			processWriter = null;
		}

		currentProcess = null;
		outArea.append("Application ended.\n");

		try {
			FileWriter writer = new FileWriter(book.getBookDirectory().toPath() + File.separator + "log.txt");
			writer.write(outArea.getText());
			writer.close();
		} catch (IOException ioe) {
			Controller.showError("Couldn't write log: " + ioe.getMessage());
		}
		book.setProgressString("Stopped");
		book.setGenerating(false);
	}

	private void print(String out) {
		outArea.append("OUT: " + out + "\n");
		outArea.setCaretPosition(outArea.getDocument().getLength());
		processWriter.println(out);
		processWriter.flush();
	}

	private class OutputReaderWorker extends SwingWorker<Void, String> {
		private BufferedReader reader;
		private boolean inChapters;
		private boolean inProgress;
		private boolean zipping;

		public OutputReaderWorker(InputStream inputStream) {
			this.reader = new BufferedReader(new InputStreamReader(inputStream));
			inChapters = false;
			inProgress = false;
			zipping = false;
		}

		@Override
		protected Void doInBackground() throws Exception {
			String line;
			try {
				while (!isCancelled() && (line = reader.readLine()) != null) {
					publish(line);
				}

			} catch (IOException ioe) {
				if (!isCancelled()) {
					publish("Error reading output: " + ioe.getMessage());
				}
			} finally {
				try {
					reader.close();
				} catch (IOException ioe) {
					System.err.println("Error closing reader: " + ioe.getMessage());
				}
			}
			return null;
		}

		@Override
		protected void process(List<String> chunks) {

			// Debug Log
			for (String line : chunks) {
				outArea.append("IN: " + line + "\n");
			}
			outArea.setCaretPosition(outArea.getDocument().getLength());

			// Going through each
			for (int i = 0; i < chunks.size(); i++) {

				// Reset
				if (chunks.get(i).equals("done")) {
					inChapters = false;
					inProgress = false;
					if (zipping) {
						book.setProgressString("Done.");
					}
				}

				// Book Name
				if (chunks.get(i).equals("Book Name:")) {
					if (chunks.size() > i + 1) {
						book.setProgressString("Loaded book name: " + chunks.get(i + 1));
						print("");
					}
				}

				// Check Chapters
				if (inChapters) {
					String in = chunks.get(i);
					String[] sections = in.split(":");
					try {
						int chapterNum = Integer.parseInt(sections[0]);
						if (!resuming) {
							chapterList.addChapter(new Chapter(chapterNum,
									in.substring(sections[0].length() + 2, in.length())));
							book.setProgressString("Loaded chapter: "
									+ chapterList.getChapters().get(chapterList.getChapters().size() - 1));
						}
					} catch (NumberFormatException | ArrayIndexOutOfBoundsException nfe) {
						inChapters = false;
						// chapterList.setModel(checkBoxes);
					}
				}

				// Check progress
				if (inProgress) {
					String in = chunks.get(i);
					try {
						int chapterNum = Integer.parseInt(in);
						book.setCurrentChapter(chapterNum);
						book.setProgressString("Generated " + (chapterNum + 1) + "/" + book.getNumChapters());
					} catch (NumberFormatException | ArrayIndexOutOfBoundsException nfe) {
						inProgress = false;
						// chapterList.setModel(checkBoxes);
					}
				}

				if (chunks.get(i).equals("combining into " + book.getAudioCompression())) {
					book.setProgressString("Combining wav files into " + book.getAudioCompression());
				}
				if (chunks.get(i).equals("zipping")) {
					book.setProgressString("Creating final zip file");
					zipping = true;
				}

				// Check if we're listing chapters
				if (chunks.get(i).equals("Chapters:")) {
					inChapters = true;
					inProgress = false;
				}

				if (chunks.get(i).equals("Progress:")) {
					book.setProgressString("Generating audio files");
					inProgress = true;
					inChapters = false;
				}

			}
		}

		@Override
		protected void done() {
			try {
				get();
				// ((JFrame) outArea.getTopLevelAncestor()).dispose();
			} catch (InterruptedException ie) {
				outArea.append("Output reader worker error: " + ie.getMessage() + "\n");
				ie.printStackTrace();
			} catch (ExecutionException ee) {
				outArea.append("Output reader worker error: " + ee.getMessage() + "\n");
				ee.printStackTrace();
			} catch (CancellationException ce) {
				System.out.println("Finished task after being cancelled. " + ce.getMessage());
			}

			if (currentProcess != null && !currentProcess.isAlive()) {
				SwingUtilities.invokeLater(Digestrar.this::stopApp);
			}

			outputReaderWorker = null;
		}

	}

	public void setAudioCompression(String audioCompression) {
		book.setAudioCompression(audioCompression);
	}

}
