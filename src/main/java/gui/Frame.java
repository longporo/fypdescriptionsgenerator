package gui;

import common.exception.BusinessException;
import common.pojo.ProjectInfo;
import common.uitils.ExcelUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import service.PdfGenerator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * The Frame class<br>
 *
 * @author Zihao Long
 * @version 1.0, 2021年09月26日 03:28
 * @since excelToPdf 0.0.1
 */
public class Frame {

    /**
     * Define current system, the default is windows
     */
    private static boolean IS_WINDOWS = false;
    private static boolean IS_MAC_OS = false;

    /**
     * Define the PDF types
     */
    public static boolean IS_STUDENT_DESC_TYPE = false;
    public static boolean IS_INTERNAL_CHECK_TYPE = false;

    /**
     * The file path notation, the default is windows, if current system is MacOs, it will be '/'
     *
     */
    public static String FILE_PATH_NOTATION = null;

    /**
     * The pdf absolute file path
     */
    public static String PDF_FILE_PATH;

    public static Logger logger = Logger.getLogger(Frame.class);

    /**
     * GUI info
     */
    private JTextPane log_textarea;
    private JTextField excel_input;
    private JButton excel_btn;
    private JTextField pdf_input;
    private JButton pdf_btn;
    private JButton start_btn;
    private JPanel excel_div;
    private JPanel pdf_div;
    private JPanel start_div;
    private JPanel body;
    private JComboBox select;

    public static void main(String[] args) throws IOException {
        // init frame
        JFrame frame = new JFrame("Excel to PDF");
        frame.setSize(575, 400);
        frame.setContentPane(new Frame().body);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // window vertical center
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int screenWidth = screenSize.width / 2;
        int screenHeight = screenSize.height / 2;
        int height = frame.getHeight();
        int width = frame.getWidth();
        frame.setLocation(screenWidth - width / 2, screenHeight - height / 2);
    }

    public Frame() throws IOException {

        // log setting
        Logger root = Logger.getRootLogger();
        Appender appender = root.getAppender("WriterAppender");
        PipedReader reader = new PipedReader();
        Writer writer = new PipedWriter(reader);
        ((WriterAppender) appender).setWriter(writer);
        Thread t = new LogToGuiThread(reader);
        t.start();

        // excel file selecting button click event
        excel_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = showExcelFileDialog();
                if (file == null) {
                    return;
                }
                excel_input.setText(file.getAbsolutePath());
            }
        });

        // directory selecting button click event
        pdf_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = showDirChoosingDialog();
                if (file == null) {
                    return;
                }
                pdf_input.setText(file.getAbsolutePath());
            }
        });

        // 'start' button click event
        start_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String excelFilePath = excel_input.getText();
                String pdfDirPath = pdf_input.getText();
                String selectedStr = (String) select.getSelectedItem();

                if ("Select a PDF type".equalsIgnoreCase(selectedStr)) {
                    JOptionPane.showMessageDialog(null, "Please select a PDF type.", "Warning", 2);
                    return;
                }

                if (" Select a folder or excel file...".equalsIgnoreCase(excelFilePath)) {
                    JOptionPane.showMessageDialog(null, "Please select a folder or excel file.", "Warning", 2);
                    return;
                }
                if (" Select a folder to save pdf...".equalsIgnoreCase(pdfDirPath)) {
                    JOptionPane.showMessageDialog(null, "Please select a folder to save pdf.", "Warning", 2);
                    return;
                }
                int confirmResult = JOptionPane.showConfirmDialog(null, "Start to generate?", "Prompt", 0);
                if (confirmResult == 1) {
                    return;
                }

                // Generate
                generatePdf(excelFilePath, pdfDirPath, selectedStr);
            }
        });
    }

    /**
     * The generation method<br>
     *
     * @param [excelFilePath, pdfDirPath, selectedStr]
     * @return void
     * @author Zihao Long
     */
    private static void generatePdf(String excelFilePath, String pdfDirPath, String selectedStr) {
        try {
            // get system info
            if ( FILE_PATH_NOTATION == null) {
                String osName = System.getProperty("os.name");
                if (osName.startsWith("Windows")) {
                    IS_WINDOWS = true;
                    FILE_PATH_NOTATION = "\\";
                } else {
                    // MacOs, linux
                    IS_MAC_OS = true;
                    FILE_PATH_NOTATION = "/";
                }
            }

            // Reset type
            IS_STUDENT_DESC_TYPE = false;
            IS_INTERNAL_CHECK_TYPE = false;

            String pdfFileName = "CS_Projects";
            if ("Project Descriptions for Students".equalsIgnoreCase(selectedStr)) {
                IS_STUDENT_DESC_TYPE = true;
            } else if ("Project Descriptions for Internal Check".equalsIgnoreCase(selectedStr)) {
                IS_INTERNAL_CHECK_TYPE = true;
                pdfFileName = "Project_Descriptions_Computer_Science";
            }

            // gen pdf absolute path
            PDF_FILE_PATH = pdfDirPath + FILE_PATH_NOTATION + pdfFileName + ".pdf";

            logger.info("Reading excel file...");

            // Read data from excel
            List<ProjectInfo> dataList = ExcelUtils.importExcelForPdf(excelFilePath, 0, 1);


            logger.info("Parsing excel data successfully...");
            logger.info("Generating PDF file...");

            // Generate pdf
            PdfGenerator.genPdf(dataList);

            logger.info("SUCCESS!!!");

            // Ask if open file
            int confirmResult = JOptionPane.showConfirmDialog(null, "PDF has been generated! Open it?", "Prompt", 0);
            if (confirmResult == 1) {
                return;
            }
            openFile(PDF_FILE_PATH);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
//            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    /**
     * Open file by command line<br>
     *
     * @param []
     * @return void
     * @author Zihao Long
     */
    private static void openFile(String filePath) throws IOException {
        if (IS_WINDOWS) {
            Runtime.getRuntime().exec("explorer.exe /select, " + filePath);
            return;
        } else if (IS_MAC_OS) {
            Runtime.getRuntime().exec("open " + filePath);
            return;
        }
        throw new BusinessException("Unsupported system");
    }

    /**
     * Filter excel file<br>
     *
     * @param [file]
     * @return boolean
     * @author Zihao Long
     */
    public static boolean filterExcelFile(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith("csv")
                || fileName.endsWith("xls")
                || fileName.endsWith("xlsx")) {
            if (fileName.startsWith(".")) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * show excel file dialog<br>
     *
     * @param [type]
     * @return java.io.File
     * @author Zihao Long
     */
    public File showExcelFileDialog() {
        JFileChooser fileChooser = new JFileChooser();
        // set excel file filter
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                return filterExcelFile(file);
            }

            @Override
            public String getDescription() {
                return "folder or excel file (*.csv, *.xls, *.xlsx)";
            }
        });

        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.showDialog(new JLabel(), "select");
        File file = fileChooser.getSelectedFile();
        return file;
    }

    /**
     * show directory choosing dialog<br>
     *
     * @param [type
     * @return java.io.File
     * @author Zihao Long
     */
    public File showDirChoosingDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.showDialog(new JLabel(), "select");
        File file = fileChooser.getSelectedFile();
        return file;
    }

    /**
     * The thread class, log to GUI<br>
     *
     * @param 
     * @return 
     * @author Zihao Long
     */
    class LogToGuiThread extends Thread {

        PipedReader reader;

        public LogToGuiThread(PipedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            Scanner scanner = new Scanner(reader);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNext()) {
                sb.append(scanner.nextLine());
                sb.append("\r\n");
                log_textarea.setText(sb.toString());
            }
        }
    }
}