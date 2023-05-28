package ru.seloid.pdf.merger.windows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.seloid.pdf.merger.constant.Direction;
import ru.seloid.pdf.merger.service.Merger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MainWindow {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainWindow.class);

    private static final String TITLE = "PdfMerger";
    private static final String AUTHOR_GITHUB_LINK = "https://github.com/AlexMem";
    private static final Dimension DEFAULT_SIZE = new Dimension(600, 400);
    private static final Font BUTTON_TEXT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private static final Font LIST_TEXT_FONT = BUTTON_TEXT_FONT;
    private static final Font TEXT_FIELD_FONT = LIST_TEXT_FONT;
    private static final Font LABEL_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 18);
    private static final Font STATUS_LABEL_FONT = BUTTON_TEXT_FONT;
    private static final FileNameExtensionFilter PDF_ONLY_FILTER = new FileNameExtensionFilter("PDF Only", "pdf");

    private static final Color SUCCESS_COLOR = new Color(34, 112, 18);
    private static final Color ERROR_COLOR = new Color(159, 30, 18);
    private static final Color HYPERLINK_COLOR = new Color(22, 96, 202);

    private MainWindow() {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle(TITLE);
        frame.setLocationRelativeTo(null);
        frame.setSize(DEFAULT_SIZE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());

        final JList<String> fileList = new JList<>();
        fileList.setFont(LIST_TEXT_FONT);
        final JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(createLabel("1. Add files and order them"), BorderLayout.CENTER);
        final JLabel authorHyperlink = createLabel("Author: @seloid ", new Font(LABEL_FONT.getName(), LABEL_FONT.getStyle(), 12));
        authorHyperlink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        authorHyperlink.setForeground(HYPERLINK_COLOR);
        authorHyperlink.addMouseListener(authorHyperlinkMouseAdapter());
        inputPanel.add(authorHyperlink, BorderLayout.EAST);

        final JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        final JPanel listButtonsPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        listButtonsPanel.add(createButton("add", addButtonListener(inputPanel, fileList)));
        listButtonsPanel.add(createButton("delete", deleteButtonListener(fileList)));
        listButtonsPanel.add(new JSeparator());
        listButtonsPanel.add(createButton("up", upButtonListener(fileList)));
        listButtonsPanel.add(createButton("down", downButtonListener(fileList)));

        final JPanel outputSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextField outputFileTextField = new JTextField(Merger.createResultFilePath(), 70);
        outputFileTextField.setFont(TEXT_FIELD_FONT);
        final JLabel outputLabel = createLabel("2. Select directory to store merged file");
        outputSelectPanel.add(outputFileTextField);
        outputSelectPanel.add(createButton("select", outputDirectorySelectButtonListener(inputPanel, outputFileTextField)));
        final JPanel outputPanel = new JPanel(new GridLayout(4, 1));
        outputPanel.add(outputLabel);
        outputPanel.add(outputSelectPanel);
        final JPanel startMergePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startMergePanel.add(createLabel("3. Merge files"));
        final JLabel statusLabel = new JLabel();
        statusLabel.setFont(STATUS_LABEL_FONT);
        startMergePanel.add(createButton("merge", mergeButtonListener(fileList, outputFileTextField, statusLabel)));
        outputPanel.add(startMergePanel);
        outputPanel.add(statusLabel);

        final JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(listPanel, BorderLayout.CENTER);
        mainPanel.add(listButtonsPanel, BorderLayout.EAST);
        mainPanel.add(outputPanel, BorderLayout.SOUTH);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public static void run() {
        new MainWindow();
    }

    private JLabel createLabel(final String text) {
        return createLabel(text, LABEL_FONT);
    }

    private JLabel createLabel(final String text, final Font font) {
        final JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }

    private JButton createButton(final String text, final ActionListener listener) {
        final JButton button = new JButton(text);
        button.addActionListener(listener);
        button.setFont(BUTTON_TEXT_FONT);
        button.setVisible(true);
        return button;
    }

    private JFileChooser createFileChooser(final FileFilter fileFilter, final int selectionMode) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(selectionMode);
        chooser.setMultiSelectionEnabled(selectionMode == JFileChooser.FILES_ONLY);
        if (fileFilter != null) {
            chooser.setFileFilter(fileFilter);
        }
        return chooser;
    }

    private MouseAdapter authorHyperlinkMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI(AUTHOR_GITHUB_LINK));
                } catch (Exception e) {
                    LOGGER.error("Error going to hyperlink", e);
                }
            }
        };
    }

    private ActionListener addButtonListener(final JPanel inputPanel, final JList<String> fileList) {
        return event -> {
            final JFileChooser fileChooser = createFileChooser(PDF_ONLY_FILTER, JFileChooser.FILES_ONLY);
            final int returnValue = fileChooser.showOpenDialog(inputPanel);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                final File[] selectedFiles = fileChooser.getSelectedFiles();
                LOGGER.debug("Selected {}", Arrays.toString(selectedFiles));
                final List<String> loadedFileNames = getAllData(fileList.getModel());
                fileList.setListData(new Vector<>(Stream.concat(loadedFileNames.stream(),
                                                                Arrays.stream(selectedFiles).map(File::getAbsolutePath))
                                                        .distinct()
                                                        .collect(Collectors.toList())));
            }
        };
    }

    private ActionListener deleteButtonListener(final JList<String> fileList) {
        return event -> {
            final int[] selectedIndices = fileList.getSelectedIndices();
            if (selectedIndices.length == 0) {
                return;
            }

            final Set<Integer> setOfIndices = Arrays.stream(selectedIndices).boxed().collect(Collectors.toSet());
            fileList.setListData(new Vector<>(getData(fileList.getModel(), i -> !setOfIndices.contains(i))));
        };
    }

    private ActionListener upButtonListener(final JList<String> fileList) {
        return event -> moveElements(fileList, Direction.UP);
    }

    private ActionListener downButtonListener(final JList<String> fileList) {
        return event -> moveElements(fileList, Direction.DOWN);
    }

    private ActionListener outputDirectorySelectButtonListener(final JPanel inputPanel, final JTextField outputFileTextField) {
        return event -> {
            final JFileChooser fileChooser = createFileChooser(null, JFileChooser.DIRECTORIES_ONLY);
            final int returnValue = fileChooser.showOpenDialog(inputPanel);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                final File selectedDirectory = fileChooser.getSelectedFile();
                LOGGER.debug(selectedDirectory.toString());
                outputFileTextField.setText(Merger.createResultFilePath(selectedDirectory.getAbsolutePath()));
            }
        };
    }

    private ActionListener mergeButtonListener(final JList<String> fileList, final JTextField outputFileTextField, final JLabel statusLabel) {
        return event -> {
            try {
                final List<String> filePaths = getAllData(fileList.getModel());
                if (filePaths.isEmpty()) {
                    throw new Exception("Empty file list");
                }
                final String outputFilePath = Merger.merge(filePaths, outputFileTextField.getText());
                statusLabel.setText("Success: result saved to " + outputFilePath);
                statusLabel.setForeground(SUCCESS_COLOR);
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setForeground(ERROR_COLOR);
            }
        };
    }

    private <T> List<T> getAllData(final ListModel<T> model) {
        return getData(model, i -> true);
    }

    private <T> List<T> getData(final ListModel<T> model, final IntPredicate indexFiltering) {
        return IntStream.range(0, model.getSize())
                        .filter(indexFiltering)
                        .mapToObj(model::getElementAt)
                        .collect(Collectors.toList());
    }

    private void moveElements(final JList<String> fileList, final Direction direction) {
        final int[] selectedIndices = fileList.getSelectedIndices();
        if (selectedIndices.length == 0) {
            return;
        }
        final List<String> data = getAllData(fileList.getModel());
        final int[] newSelectedIndexes = Arrays.stream(selectedIndices)
                                               .boxed()
                                               .sorted(direction == Direction.UP ? Comparator.comparing(Integer::intValue) : Comparator.comparing(Integer::intValue).reversed())
                                               .mapToInt(Integer::intValue)
                                               .map(index -> moveAtOne(data, index, direction))
                                               .distinct()
                                               .toArray();
        fileList.setListData(new Vector<>(data));
        fileList.setSelectedIndices(newSelectedIndexes);
    }

    private <T> int moveAtOne(final List<T> data, final int index, final Direction direction) {
        final int newIndex = direction == Direction.UP ? index - 1 : index + 1;
        if (newIndex < 0 || newIndex > data.size() - 1) {
            return index;
        }
        final T movable = data.remove(index);
        data.add(newIndex, movable);
        return newIndex;
    }
}
