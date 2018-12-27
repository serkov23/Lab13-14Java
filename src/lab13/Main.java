package lab13;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JFrame {
    private final static Pattern PATTERN = Pattern.compile("[\\s\n]*#([\\s\n]+\\d+[\\s\n]+\\w+([\\s\n]+\\d+([\\s\n]+\\w+[\\s\n]+\\d+)+)+)[\\s\n]*");
    private JList<StudentMarks> studList = new JList<>();
    private Vector<StudentMarks> v = new Vector<>();

    Main() {
        JTextField format = new JTextField();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setMinimumSize(new Dimension(700, 250));

        JMenuBar menu = new JMenuBar();
        JMenu mainMenu = new JMenu("File");
        menu.add(mainMenu);
        var open = new JMenuItem("Open");
        var openXML = new JMenuItem("Open XML");
        var saveXML = new JMenuItem("Save XML");
        var sort = new JMenuItem("Sort");
        sort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Scanner sc = new Scanner(format.getText());
                try {
                    int sem = sc.nextInt();
                    Vector<String> subjects = new Vector<>();
                    while (sc.hasNext())
                        subjects.add(sc.next());
                    v.sort(StudentMarks.getCmp(sem, subjects));
                    studList.setListData(v);
                } catch (NoSuchElementException e1) {
                    errorMessage("invalid semester number");
                }
            }
        });
        openXML.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("D:\\projects\\Lab13_Java");
                if (fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                        parser.parse(fileChooser.getSelectedFile(), new MyHandler());
                        studList.setListData(v);
                        showFile(fileChooser.getSelectedFile());
                    } catch (ParserConfigurationException | SAXException | IOException e1) {
                        errorMessage("problems with parser");
                    }
                }
            }
        });
        saveXML.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("D:\\projects\\Lab13_Java");
                if (fileChooser.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        FileWriter writer = new FileWriter(fileChooser.getSelectedFile());
                        saveVToXML(writer);
                        writer.close();
                    } catch (IOException e1) {
                        errorMessage("Invalid file");
                    }
                }
            }
        });

        var add = new JMenuItem("Add");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MyDialog();
            }
        });
        mainMenu.add(add);
        mainMenu.add(open);
        mainMenu.add(openXML);
        mainMenu.add(saveXML);
        mainMenu.add(sort);
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("D:\\projects\\Lab13_Java");
                if (fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        Scanner scanner = new Scanner(fileChooser.getSelectedFile());
                        parseStud(scanner);
                        showFile(fileChooser.getSelectedFile());
                    } catch (FileNotFoundException e1) {
                        errorMessage("no such file");
                    } catch (NoSuchElementException e1) {
                        errorMessage("invalid input");
                    }
                }
            }
        });

        setLayout(new BorderLayout());
        setJMenuBar(menu);
        JPanel panel1 = new JPanel(new GridLayout(2, 1));
        JButton find = new JButton("Find");
        JList<Student> list = new JList<>();
        find.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Scanner sc = new Scanner(format.getText());
                try {
                    int sem = sc.nextInt();
                    Vector<String> subjects = new Vector<>();
                    while (sc.hasNext())
                        subjects.add(sc.next());
                    Chooser choose = new Chooser(sem, subjects);
                    for (var i : v)
                        i.accept(choose);
                    list.setListData(choose.getRes());
                } catch (NoSuchElementException e1) {
                    errorMessage("invalid semester number");
                }
            }
        });
        panel1.add(format);
        panel1.add(find);
        add(panel1, BorderLayout.NORTH);

        JPanel panel2 = new JPanel(new BorderLayout()), panel3 = new JPanel(new GridLayout(2, 1)), panel4 = new JPanel(new GridLayout(2, 1));
        panel3.add(new JLabel("students:"));
        panel3.add(new JScrollPane(studList));
        panel4.add(new JLabel("result"));
        panel4.add(new JScrollPane(list));
        panel2.add(panel3, BorderLayout.WEST);
        panel2.add(panel4, BorderLayout.EAST);
        add(panel2, BorderLayout.CENTER);

    }

    private static void write(StudentMarks marks, FileWriter writer) throws IOException {
        writer.write(String.format("<bookNumber>%d</bookNumber>\n", marks.getStudent().getBookNumber()));
        writer.write("<name>");
        writer.write(marks.getStudent().getName());
        writer.write("</name>\n");
        for (Map.Entry<Integer, SemesterMarks> entry : marks.getMarks().entrySet()) {
            Integer sem = entry.getKey();
            SemesterMarks semesterMarks = entry.getValue();
            writer.write(String.format("<semester num=\"%d\">\n", sem));
            writeSemesterMarks(semesterMarks, writer);
            writer.write("</semester>\n");

        }

    }

    private static void writeSemesterMarks(SemesterMarks semesterMarks, FileWriter writer) throws IOException {
        for (Map.Entry<String, Integer> entry : semesterMarks.entrySet()) {
            String subject = entry.getKey();
            Integer mark = entry.getValue();
            writer.write("<exam>\n");
            writer.write("<subject>");
            writer.write(subject);
            writer.write("</subject>\n");
            writer.write(String.format("<mark>%d</mark>\n", mark));
            writer.write("</exam>\n");

        }
    }

    private static StudentMarks parse(Scanner sc) {
        Student stud = new Student(sc.nextInt(), sc.next());
        TreeMap<Integer, SemesterMarks> m = new TreeMap<>();
        while (sc.hasNext()) {
            parseMap(m, sc);
        }
        return new StudentMarks(stud, m);
    }

    private static void parseMap(TreeMap<Integer, SemesterMarks> m, Scanner sc) {
        int key = sc.nextInt();
        SemesterMarks marks = new SemesterMarks();
        while (sc.hasNext() && !sc.hasNextInt()) {
            marks.put(sc.next(), sc.nextInt());
        }
        m.put(key, marks);
    }

    public static void main(String[] args) {
        (new Main()).setVisible(true);
    }

    private void showFile(File selectedFile) throws FileNotFoundException {
        StringBuilder str = new StringBuilder();
        Scanner scanner = new Scanner(selectedFile);
        while (scanner.hasNextLine()) {
            str.append(scanner.nextLine()).append("\n");
        }
        JDialog dialog = new ShowDialog(str.toString());
        dialog.setVisible(true);
    }

    private void saveVToXML(FileWriter writer) throws IOException {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        writer.write("<Students>\n");
        for (var i : v) {
            writer.write("<Student>\n");
            write(i, writer);
            writer.write("</Student>\n");
        }
        writer.write("</Students>\n");
    }

    private void errorMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void parseStud(Scanner sc) {
        StringBuilder str = new StringBuilder();
        while (sc.hasNextLine())
            str.append(sc.nextLine()).append("\n");
        Matcher matcher = PATTERN.matcher(str.toString());
        while (matcher.find()) {
            Scanner sc1 = new Scanner(matcher.group(1));
            v.add(parse(sc1));
        }
        studList.setListData(v);
    }

    private class ShowDialog extends JDialog {
        public ShowDialog(String str) {
            super(Main.this);
            setSize(500, 500);
            JTextArea field = new JTextArea(str);
            field.setEditable(false);
            add(new JScrollPane(field));
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }
    }

    class MyHandler extends DefaultHandler {
        Student student;
        TreeMap<Integer, SemesterMarks> marks;
        SemesterMarks semMarks;
        int semesterNumber;
        String curentTag;
        String key;
        int val;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            try {
                switch (qName) {
                    case ("Student"):
                        student = new Student();
                        marks = new TreeMap<>();
                        break;
                    case ("semester"):
                        semesterNumber = Integer.parseInt(attributes.getValue("num"));
                        semMarks = new SemesterMarks();
                        break;

                }
                curentTag = qName;
            } catch (Exception e) {
                throw new SAXException();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            try {
                switch (qName) {
                    case ("Student"):
                        v.add(new StudentMarks(student, marks));
                        break;
                    case ("semester"):
                        marks.put(semesterNumber, semMarks);
                        break;
                    case "exam":
                        semMarks.put(key, val);
                        break;
                }
            } catch (Exception e) {
                throw new SAXException();
            }
            curentTag = "";
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String str = new String(ch, start, length);
            try {
                switch (curentTag) {
                    case "bookNumber":
                        student.setBookNumber(Integer.parseInt(str));
                        break;
                    case "name":
                        student.setName(str);
                        break;
                    case "subject":
                        key = str;
                        break;
                    case "mark":
                        val = Integer.parseInt(str);
                }
            } catch (Exception e) {
                throw new SAXException();
            }
        }
    }

    class MyDialog extends JDialog {
        MyDialog() {
            super(Main.this);
            setSize(250, 250);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            JTextArea textArea = new JTextArea();
            setLayout(new BorderLayout());
            add(new JScrollPane(textArea));
            JButton button = new JButton("add");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        v.add(parse(new Scanner(textArea.getText())));
                        studList.setListData(v);
                    } catch (NoSuchElementException e1) {
                        errorMessage("input error");
                    }
                }
            });
            add(button, BorderLayout.SOUTH);
            setVisible(true);
        }

        void errorMessage(String message) {
            JOptionPane.showMessageDialog(this, message);
        }

    }
}
/*
123231 Sergey
1
GA 3
MA 10
2
MA 9
GA 9
Programming 10
*/