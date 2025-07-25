package com.benliebkemann;

import javax.swing.JEditorPane;
import javax.swing.JFrame;

public class PreviewFrame extends JFrame {
	private BookModel bookModel;
	private JEditorPane editorPane;

	public PreviewFrame(BookModel bookModel) {
		this.bookModel = bookModel;
	}

}
