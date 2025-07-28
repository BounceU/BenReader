package com.benliebkemann;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatIntelliJLaf;

public class Main extends JFrame implements WindowListener, ListSelectionListener {

    private Controller controller;

    public static Settings SETTINGS;

    // private JList<BookModel> bookList;
    // private SortedListModel listModel;
    private BookModel selectedBook;
    private List<BookModel> books;
    private ButtonPanel buttonPanel;
    private BookInformationView bookInformation;
    private BookTableModel tableModel;
    private JTable table;

    private DownloadServer downloadServer;

    public static void main(String[] args) {

        FlatIntelliJLaf.setup();
        new Main().run();
    }

    public DownloadServer getDownloadServer() {
        return downloadServer;
    }

    public BookModel getSelectedBook() {
        return selectedBook;
    }

    public Main() {
        super("BenReader");
        Image appIcon = new ImageIcon("icons/app_icon.png").getImage();
        final Taskbar taskbar = Taskbar.getTaskbar();

        try {
            taskbar.setIconImage(appIcon);
        } catch (final UnsupportedOperationException e) {
            System.out.println("The os does not support taskbar.setIconImage. " +
                    e.getMessage());
        } catch (final SecurityException e) {
            System.out.println("There was a security exception for taskbar.setIconImage. " + e.getMessage());
        }
        setIconImage(appIcon);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        this.downloadServer = new DownloadServer();
        Thread t = new Thread(this.downloadServer);
        t.start();
        books = new ArrayList<BookModel>();
        tableModel = new BookTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(80);
        table.setShowVerticalLines(false);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        bookInformation = new BookInformationView();
        controller = new Controller(this);
        buttonPanel = new ButtonPanel(controller);

    }

    public void addBook(File epub) {
        BookModel newBook = new BookModel(epub);
        if (newBook.getBook() == null) {
            return;
        }
        books.add(newBook);
        tableModel.addBook(newBook);
        tableModel.fireTableDataChanged();
        revalidate();
        validate();
        repaint();
    }

    public void removeSelectedBook() {
        BookModel book = selectedBook;
        if (selectedBook == null)
            return;
        bookInformation.clearInformation();
        books.remove(book);
        tableModel.removeBook(book);
        tableModel.fireTableDataChanged();
        File directory = book.getBookDirectory();
        // book = null;
        Controller.deleteDirectory(directory);

    }

    public void run() {

        List<BookModel> oldBooks = BookModel.getExisting();

        books.addAll(oldBooks);

        // Top Button View
        add(buttonPanel, BorderLayout.PAGE_START);

        // Center Scroll View
        tableModel = new BookTableModel();

        books.forEach(book -> tableModel.addBook(book));
        table.setModel(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.getColumnModel().getColumn(4).setCellRenderer(new ProgressCellRenderer());
        table.setAutoCreateRowSorter(true);
        // add(scrollPane, BorderLayout.CENTER);

        // Split pane
        JSplitPane splitPane = new ToggleSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0);
        splitPane.setOneTouchExpandable(true);
        splitPane.add(bookInformation);
        splitPane.add(scrollPane);
        add(splitPane, BorderLayout.CENTER);
        splitPane.setDividerLocation(300);
        // add(bookInformation, BorderLayout.LINE_START);

        setSize(1200, 768);

        setLocationRelativeTo(null);

        loadSettings();
        System.out.println(SETTINGS);

        setVisible(true);
    }

    public void loadSettings() {

        if (!(new File("config.json")).exists()) {
            setupSettings();
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper();
                SETTINGS = mapper.readValue(new File("config.json"), Settings.class);
            } catch (IOException ioe) {
                Controller.showError("Couldn't initialize settings: " + ioe.getMessage());
                setupSettings();
            }
        }
    }

    public void setupSettings() {
        String initialSettings = "{\"voice\":\"bm_george,bm_george,bm_george,bm_george,bm_george,bm_lewis,bm_lewis,bm_lewis\",\"clearOutput\":false,\"useM4a\":true}";
        try {
            FileWriter writer = new FileWriter("config.json");
            writer.write(initialSettings);
            writer.close();
            ObjectMapper mapper = new ObjectMapper();
            SETTINGS = mapper.readValue(initialSettings, Settings.class);
        } catch (IOException e) {
            Controller.showError("Couldn't initialize settings: " + e.getMessage());
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        controller.destroyDigestrars();
        for (BookModel book : books) {
            book.saveBook();
        }
        dispose();
        System.exit(0);
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
    public void valueChanged(ListSelectionEvent e) {

        ListSelectionModel lsm = (ListSelectionModel) e.getSource();

        if (!lsm.isSelectionEmpty()) {
            selectedBook = tableModel.getBook(table.getRowSorter().convertRowIndexToModel(table.getSelectedRow()));

            bookInformation.updateInformation(selectedBook);
        }

    }

}