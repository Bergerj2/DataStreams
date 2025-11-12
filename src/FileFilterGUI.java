import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileFilterGUI extends JFrame {

    private JTextArea originalTextArea;
    private JTextArea filteredTextArea;
    private JTextField searchTextField;
    private JLabel statusLabel;
    private JButton searchButton;
    private Path currentFilePath;
    private String fileContent;

    public FileFilterGUI() {
        super("Java Stream File Filter");
        setupGUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupGUI() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(10, 10));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JButton loadButton = new JButton("Load File");
        loadButton.addActionListener(e -> selectAndLoadFile());

        controlPanel.add(loadButton);
        controlPanel.add(new JLabel("Search String:"));

        searchTextField = new JTextField(20);
        controlPanel.add(searchTextField);

        searchButton = new JButton("Search File");
        searchButton.setEnabled(false);
        searchButton.addActionListener(e -> searchFile());
        controlPanel.add(searchButton);

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> System.exit(0));
        controlPanel.add(quitButton);

        contentPane.add(controlPanel, BorderLayout.NORTH);

        JPanel textPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        originalTextArea = new JTextArea();
        originalTextArea.setEditable(false);
        JScrollPane originalScrollPane = new JScrollPane(originalTextArea);
        originalScrollPane.setBorder(BorderFactory.createTitledBorder("Original File Content"));
        textPanel.add(originalScrollPane);

        filteredTextArea = new JTextArea();
        filteredTextArea.setEditable(false);
        JScrollPane filteredScrollPane = new JScrollPane(filteredTextArea);
        filteredScrollPane.setBorder(BorderFactory.createTitledBorder("Filtered Results"));
        textPanel.add(filteredScrollPane);

        contentPane.add(textPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Please load a text file.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        contentPane.add(statusLabel, BorderLayout.SOUTH);
    }

    private void selectAndLoadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Text File to Load");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            currentFilePath = selectedFile.toPath();

            try {
                fileContent = Files.readString(currentFilePath);
                originalTextArea.setText(fileContent);
                originalTextArea.setCaretPosition(0);

                filteredTextArea.setText("File loaded. Enter search string and click 'Search File'.");
                searchButton.setEnabled(true);
                statusLabel.setText("File loaded: " + currentFilePath.getFileName());

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(),
                        "File I/O Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error loading file.");
                searchButton.setEnabled(false);
                originalTextArea.setText("");
                filteredTextArea.setText("");
            }
        }
    }


    private void searchFile() {
        if (currentFilePath == null) {
            JOptionPane.showMessageDialog(this, "Please load a file first.",
                    "Missing File", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String searchString = searchTextField.getText().trim();
        if (searchString.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search string.",
                    "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String searchLower = searchString.toLowerCase();

        String filteredContent;

        try (Stream<String> lines = Files.lines(currentFilePath)) {

            filteredContent = lines
                    .filter(line -> line.toLowerCase().contains(searchLower))
                    .collect(Collectors.joining("\n"));


            if (filteredContent.isEmpty()) {
                filteredTextArea.setText(String.format("No lines found containing \"%s\".", searchString));
                statusLabel.setText("Search finished. 0 matches found.");
            } else {
                filteredTextArea.setText(filteredContent);
                filteredTextArea.setCaretPosition(0);

                long matchCount = filteredContent.chars().filter(ch -> ch == '\n').count() + 1;
                statusLabel.setText(String.format("Search finished. %d lines found containing \"%s\".",
                        matchCount, searchString));
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file during search: " + e.getMessage(),
                    "File I/O Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error during search.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileFilterGUI::new);
    }
}