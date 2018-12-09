import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class FarhanFaruqui {
    private static JFrame frame = new JFrame("Image Editor GUI");
    private static Dimension monitor = Toolkit.getDefaultToolkit().getScreenSize();
    private static JPanel mainPanel = new JPanel(new BorderLayout());
    private static JMenuBar menuBar = new JMenuBar();
    private static JMenu options = new JMenu("Options");
    private static JMenu file = new JMenu("File");
    private static String imageFile = "";
    private static ImageIcon image = new ImageIcon(imageFile);
    private static JLabel imageLabel = new JLabel(image);
    private static BufferedImage mainImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    private static int[][] rgbArray = new int[0][0], originalArray = new int[0][0];
    private static int sliderDialogueValue = 1;
    private static double percent = 100;
    private static String newImageWidthString = "500";
    private static String newImageHeightString = "500";
    private static Color colour = Color.WHITE;
    private static Color themeColour = new Color(39, 39, 39);
    private static JScrollPane scrollPane = new JScrollPane();
    private static ArrayList<BufferedImage> buffList = new ArrayList<BufferedImage>();
    private static int undoCount = 1;

    public static void main(String[] args) {
        //sets Windows look and feel for all JComponents
        try {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        Dimension monitor = Toolkit.getDefaultToolkit().getScreenSize();
        mainPanel.setPreferredSize(new Dimension((int) monitor.getWidth(), (int) monitor.getHeight() - 90));

        menuBar.add(file);
        menuBar.add(options);
        mainPanel.setBackground(themeColour);

        //initialize all jmenuitems by adding to corresponding Jmenu, seeting text and shortcut keys
        menuItemInitialization(new JMenuItem("Open"), 79, 2, "File");
        menuItemInitialization(new JMenuItem("Save As"), 83, 2, "File");
        menuItemInitialization(new JMenuItem("New Image..."), 78, 2, "File");
        file.addSeparator(); //adds line to seperate JMenuItems
        menuItemInitialization(new JMenuItem("Exit"), 115, 8, "File");
        menuItemInitialization(new JMenuItem("Restore to Original"), 82, 2, "Options");
        options.addSeparator(); //adds line to seperate JMenuItems
        menuItemInitialization(new JMenuItem("Undo"), 90, 2, "Options");
        menuItemInitialization(new JMenuItem("Rotate..."), 84, 2, "Options");
        menuItemInitialization(new JMenuItem("Horizontal Flip"), 72, 2, "Options");
        menuItemInitialization(new JMenuItem("Vertical Flip"), 86, 2, "Options");
        menuItemInitialization(new JMenuItem("Gray Scale"), 71, 2, "Options");
        menuItemInitialization(new JMenuItem("Sepia Tone"), 80, 2, "Options");
        menuItemInitialization(new JMenuItem("Invert Colour"), 73, 2, "Options");
        menuItemInitialization(new JMenuItem("Gaussian Blur"), 85, 2, "Options");
        menuItemInitialization(new JMenuItem("Bulge Effect"), 66, 2, "Options");

        imageLabel.setVisible(false); //sets blank image to invisible

        frame.setIconImage(new ImageIcon("icon.jpg").getImage()); //icon for program
        frame.setLocation(-10, 0);
        frame.setState(JFrame.MAXIMIZED_BOTH);
        frame.setJMenuBar(menuBar);
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
    }

    public static void menuItemInitialization(JMenuItem menuItem, int keyStroke, int modifier, String menuType) {
        JMenuItem menu = menuItem;
        if (menuType.equals("Options")) {
            options.add(menu);
            menuItem.addActionListener(new Options()); //adding options class to perform corresponding effects
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyStroke, modifier)); //setting shortcut based of keystroke constants
        } else {
            file.add(menu);
            menuItem.addActionListener(new FileMenu());
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyStroke, modifier));
        }
    }

    public static void original(BufferedImage source) {
        originalArray = new int[source.getWidth()][source.getHeight()]; //sets array size to image dimensions
        for (int i = 0; i < source.getWidth(); i++) {
            for (int j = 0; j < source.getHeight(); j++) {
                originalArray[i][j] = source.getRGB(i, j); //storing RGB values of picture into array for restore function
            }
        }
    }

    public static void pixelArray(BufferedImage source) {
        rgbArray = new int[source.getWidth()][source.getHeight()]; //sets array size to image dimensions
        BufferedImage temp = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB); //temporary buffered image to add to buffered image list
        for (int i = 0; i < source.getWidth(); i++) {
            for (int j = 0; j < source.getHeight(); j++) {
                rgbArray[i][j] = source.getRGB(i, j); //storing RGB values of picture into array for effects and RGB manipulation
                temp.setRGB(i, j, new Color(rgbArray[i][j]).getRGB());
            }
        }

        buffList.add(temp); //adds temporary picture to list for undo command
    }

    public static boolean isImageLoaded() {
        boolean visibility = (imageLabel.isVisible() == true) ? true : false; //checks if image is visible making sure the user does not attempt to perform effects without an image loaded
        return visibility;
    }

    public static void displayImage() {
        mainPanel.remove(scrollPane);

        int width = 0;
        int height = 0;

        // converting scaled image dimensions based off slider value
        if (percent <= 1) {
            width = (int) (mainImage.getWidth() / (100));
            height = (int) (mainImage.getHeight() / (100));
        } else {
            width = (int) (mainImage.getWidth() / (100 / percent));
            height = (int) (mainImage.getHeight() / (100 / percent));
        }

        //displaying image below
        image = new ImageIcon(mainImage.getScaledInstance(width, height, Image.SCALE_DEFAULT));
        imageLabel = new JLabel(image);
        imageLabel.setBounds((int) ((monitor.getWidth() - image.getIconWidth()) / 2), (int) ((monitor.getHeight() - image.getIconHeight()) / 2 - 45), image.getIconWidth(), image.getIconHeight());

        scrollPane = new JScrollPane(imageLabel);
        scrollPane.getViewport().setBackground(themeColour);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.setState(JFrame.MAXIMIZED_BOTH);
        frame.setContentPane(mainPanel);
        frame.pack();
    }

    public static void viewerSlider() {
        JSlider viewSlider = new JSlider(0, 500, 100);
        JPanel sliderPanel = new JPanel();
        JMenu zoomPercent = new JMenu();

        sliderPanel.setLayout(new BorderLayout());

        zoomPercent.setEnabled(false);
        zoomPercent.setForeground(Color.BLACK);

        viewSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                percent = source.getValue();
                zoomPercent.setText("Zoom: " + String.valueOf((int) percent) + "%"); //showing zoom value in menubar 
                menuBar.add(zoomPercent);
                mainPanel.remove(scrollPane);
                displayImage(); //displaying image everytime the viewslider is changed
            }
        });

        viewSlider.setOpaque(false);
        viewSlider.setValue(100);
        viewSlider.setMinorTickSpacing(25);
        viewSlider.setMajorTickSpacing(50);
        viewSlider.setPaintTicks(true);
        viewSlider.setPaintLabels(true);
        viewSlider.setForeground(Color.WHITE);
        viewSlider.setBounds((int) (monitor.getWidth() - 205), (int) (monitor.getHeight() - 140), 200, 50);
        sliderPanel.add(viewSlider, BorderLayout.CENTER);

        mainPanel.setBackground(themeColour); //sets panel colour to given theme colour
        mainPanel.add(viewSlider, BorderLayout.SOUTH); //adds slider to the bottom

        frame.setState(JFrame.MAXIMIZED_BOTH);
        frame.setContentPane(mainPanel);
        frame.pack();
    }

    public static JLabel whiteLabel(String number) {
        //creates a white text label based of number parameter, used for sliderDialogue method
        JLabel label = new JLabel(number);
        label.setForeground(Color.WHITE);
        return label;
    }

    public static void sliderDialogue(String type, int min, int max, int initial, String title, int spacing, double divisor) {
        JFrame radiusFrame = new JFrame(title);
        JPanel sliderDialoguePanel = new JPanel();
        JSlider slider = new JSlider(1, 20, initial);
        JButton button = new JButton("Set");
        Hashtable<Integer, JLabel> label = new Hashtable(); //used for displaying slider labels

        sliderDialoguePanel.setLayout(null);
        sliderDialoguePanel.setPreferredSize(new Dimension(275, 175));
        sliderDialoguePanel.setBackground(themeColour);

        sliderDialogueValue = initial;

        slider.setValue(initial);
        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setBounds(38, 0, 200, 90);
        slider.setBackground(themeColour);
        slider.setForeground(Color.WHITE);
        slider.setMajorTickSpacing(spacing);
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setFocusable(false);

        //based of effect type labels are painted using math 
        for (double i = min; i < max + 1; i++) {
            if (type.equals("Gaussian") || type.equals("Rotate")) {
                label.put(new Integer((int) i), whiteLabel(String.valueOf((int) (i / divisor))));
            } else {
                label.put(new Integer((int) i), whiteLabel(String.valueOf(i / divisor)));
            }

        }
        slider.setLabelTable(label);

        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                sliderDialogueValue = source.getValue();
            }
        });

        button.setBorder(null);
        button.setBounds(100, 100, 75, 50);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Set")) {
                    radiusFrame.dispose();

                    if (type.equals("Gaussian")) {
                        double sigma = Math.sqrt(sliderDialogueValue);
                        double[][] gaussianWeights = new double[sliderDialogueValue][sliderDialogueValue]; //weight size is equal to given radius
                        double sum = 0;

                        //creates weight matrix by using gaussian formula, sums all individual weights
                        for (int k = 0; k < gaussianWeights.length; k++) {
                            for (int l = 0; l < gaussianWeights.length; l++) {
                                gaussianWeights[k][l] = (1 / (2 * Math.PI * Math.pow(sigma, 2)) * Math.exp(-(Math.pow(k - sliderDialogueValue / 2, 2) + Math.pow(l - sliderDialogueValue / 2, 2)) / (2 * Math.pow(sigma, 2))));
                                sum += gaussianWeights[k][l];
                            }
                        }

                        //divides entire weight matrix by total sum
                        for (int k = 0; k < gaussianWeights.length; k++) {
                            for (int l = 0; l < gaussianWeights.length; l++) {
                                gaussianWeights[k][l] /= sum;
                            }
                        }

                        for (int i = 0; i < mainImage.getWidth() - sliderDialogueValue; i++) {
                            for (int j = 0; j < mainImage.getHeight() - sliderDialogueValue; j++) {
                                double redSum = 0;
                                double greenSum = 0;
                                double blueSum = 0;

                                for (int k = 0; k < gaussianWeights.length; k++) {
                                    for (int l = 0; l < gaussianWeights.length; l++) {
                                        //blurs middle pixel by applying weight matrix to surrounding pixels in the image, summing each RGB channel individually
                                        if (sliderDialogueValue == 1) {
                                            redSum += (new Color(rgbArray[i][j]).getRed()) * gaussianWeights[k][l];
                                            greenSum += (new Color(rgbArray[i][j]).getGreen()) * gaussianWeights[k][l];
                                            blueSum += (new Color(rgbArray[i][j]).getBlue()) * gaussianWeights[k][l];
                                        } else {
                                            redSum += (new Color(rgbArray[i + sliderDialogueValue / 2 + k - 1][j + sliderDialogueValue / 2 + l - 1]).getRed()) * gaussianWeights[k][l];
                                            greenSum += (new Color(rgbArray[i + sliderDialogueValue / 2 + k - 1][j + sliderDialogueValue / 2 + l - 1]).getGreen()) * gaussianWeights[k][l];
                                            blueSum += (new Color(rgbArray[i + sliderDialogueValue / 2 + k - 1][j + sliderDialogueValue / 2 + l - 1]).getBlue()) * gaussianWeights[k][l];
                                        }
                                    }
                                }
                                mainImage.setRGB(i, j, new Color(((int) redSum), ((int) greenSum), ((int) blueSum)).getRGB());
                            }
                        }
                    } else if (type.equals("Bulge")) {
                        for (int i = 0; i < mainImage.getWidth(); i++) {
                            for (int j = 0; j < mainImage.getHeight(); j++) {
                                double k = (double) (sliderDialogueValue) / 10; //since slider labels cannot have decimals, slider value must be divided by 10
                                double originalRadius = Math.sqrt(Math.pow(i - mainImage.getWidth() / 2, 2) + Math.pow(j - mainImage.getHeight() / 2, 2));
                                double a = Math.atan2(j - mainImage.getHeight() / 2, i - mainImage.getWidth() / 2);
                                double m = 0;

                                //experimentally found m values by performing bulge on multiple images
                                if (k == 2) {
                                    m = 0.988;
                                } else if (k == 1.9) {
                                    m = 0.57;
                                } else if (k == 1.8) {
                                    m = 0.33;
                                } else if (k == 1.7) {
                                    m = 0.19;
                                } else if (k == 1.6) {
                                    m = 0.11;
                                } else if (k == 1.5) {
                                    m = 0.0635974032683;
                                } else if (k == 1.4) {
                                    m = 0.0369;
                                } else if (k == 1.3) {
                                    m = 0.021334038005457;
                                } else if (k == 1.2) {
                                    m = 0.01233444925794806750031921;
                                } else if (k == 1.1) {
                                    m = 0.0071312631231827903436042721577;
                                }

                                double radiusPrime = (mainImage.getWidth() >= mainImage.getHeight()) ? (Math.pow(originalRadius, k) / ((mainImage.getHeight() / 2) * m)) : (Math.pow(originalRadius, k) / ((mainImage.getWidth() / 2) * m));
                                int x = (int) (radiusPrime * Math.cos(a) + mainImage.getWidth() / 2);
                                int y = (int) (radiusPrime * Math.sin(a) + mainImage.getHeight() / 2);

                                if (x > 0 && y > 0 && x < mainImage.getWidth() && y < mainImage.getHeight()) {
                                    mainImage.setRGB(i, j, rgbArray[x][y]);
                                } else {
                                    mainImage.setRGB(i, j, Color.BLACK.getRGB());
                                }
                            }
                        }

                    } else if (type.equals("Rotate")) {
                        //resizes buffered image by flipping width and height based off slider value
                        if (sliderDialogueValue == 1) {
                            mainImage = new BufferedImage(rgbArray[0].length, rgbArray.length, BufferedImage.TYPE_INT_RGB);
                        } else if (sliderDialogueValue == 2) {
                            mainImage = new BufferedImage(rgbArray.length, rgbArray[0].length, BufferedImage.TYPE_INT_RGB);
                        } else if (sliderDialogueValue == 3) {
                            mainImage = new BufferedImage(rgbArray[0].length, rgbArray.length, BufferedImage.TYPE_INT_RGB);
                        }
                        for (int i = 0; i < mainImage.getWidth(); i++) {
                            for (int j = 0; j < mainImage.getHeight(); j++) {
                                if (sliderDialogueValue == 1) {
                                    mainImage.setRGB(i, j, rgbArray[j][mainImage.getWidth() - 1 - i]);
                                } else if (sliderDialogueValue == 2) {
                                    mainImage.setRGB(i, j, rgbArray[mainImage.getWidth() - 1 - i][mainImage.getHeight() - 1 - j]);
                                } else if (sliderDialogueValue == 3) {
                                    mainImage.setRGB(i, j, rgbArray[mainImage.getHeight() - 1 - j][i]);
                                }
                            }
                        }
                    }
                    displayImage();
                }
            }
        });

        sliderDialoguePanel.add(slider);
        sliderDialoguePanel.add(button);
        radiusFrame.setResizable(false);
        radiusFrame.setContentPane(sliderDialoguePanel);
        radiusFrame.setVisible(true);
        radiusFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        radiusFrame.pack();
    }

    public static void sliderInitialiser(JSlider slider, JLabel label, int sliderY, int labelY) {
        //initializes image dimension sliders for new image option
        slider.setBounds(75, sliderY, 450, 50);
        slider.setMajorTickSpacing(100);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(themeColour);
        slider.setForeground(Color.WHITE);
        slider.setFocusable(false);
        label.setForeground(Color.WHITE);
        label.setBounds(75, labelY, 100, 100);
    }

    public static void saveMethod() {
        if (imageLabel.isVisible() == true) {
            JFileChooser saveFile = new JFileChooser();
            String extension = imageFile.substring(imageFile.length() - 4, imageFile.length());
            File directory = new File(System.getProperty("user.dir")); //directory set to wherever program is saved
            saveFile.setCurrentDirectory(directory);
            saveFile.setDialogTitle("Save As");

            saveFile.setSelectedFile(new File(imageFile.substring(0, imageFile.length() - 4))); //sets suggested name for saved file based on image opened

            if (saveFile.showSaveDialog(new JMenuItem("Save As")) == JFileChooser.APPROVE_OPTION) {
                File file = new File(saveFile.getSelectedFile() + extension);

                if (file.exists()) {
                    //warning user about over writing files
                    int overwriteWarning = JOptionPane.showConfirmDialog(null, file.getName() + " already exists. Do you want to replace it?", "Confirm Save As", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (overwriteWarning == 0) {
                        try {
                            ImageIO.write(mainImage, extension.replace(".", "").toUpperCase(), file); //writes image to location chosen by user
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        saveMethod(); //if user does not want to over write image the method is run again
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "You do not have an image loaded!", "Image not loaded", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static class FileMenu implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case "Open":
                    JFileChooser selectFile = new JFileChooser();
                    File directory = new File(System.getProperty("user.dir")); //directory set to wherever program is saved
                    selectFile.setCurrentDirectory(directory);
                    selectFile.setDialogTitle("Open");

                    //creates file filter with multiple common image extensions
                    selectFile.addChoosableFileFilter(new FileFilter() {

                        public String getDescription() {
                            return "All Pictures (*.jpg;*.jpeg*.png)";
                        }

                        public boolean accept(File f) {
                            if (f.getName().endsWith("jpg") || f.getName().endsWith("jpeg") || f.getName().endsWith("png") || f.isDirectory()) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });

                    selectFile.setAcceptAllFileFilterUsed(true);

                    if (selectFile.showOpenDialog(new JMenuItem("Open")) == JFileChooser.APPROVE_OPTION) {
                        try {
                            mainImage = ImageIO.read(selectFile.getSelectedFile());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        frame.setTitle("Image Editor GUI | " + selectFile.getSelectedFile().getName()); //adds image name to frame title

                        imageFile = (selectFile.getSelectedFile()).toString(); //string image path to string
                        mainPanel.removeAll(); //removing image off panel
                        percent = 100; //reseeting view to 100% zoom
                        displayImage();
                        viewerSlider();
                        original(mainImage); //storing original RGB values of image
                        pixelArray(mainImage); // storing image RGB values for manipulation
                    }
                    break;
                case "Save As":
                    saveMethod();
                    break;
                case "Exit":
                    frame.dispose(); //closes frame 
                    System.exit(0); //stops program
                    break;
                case "New Image...":
                    JFrame newImageFrame = new JFrame("New Image");
                    JPanel newImagePanel = new JPanel();
                    JSlider newImageWidthSlider = new JSlider(0, 1000, 500);
                    JSlider newImageHeightSlider = new JSlider(0, 1000, 500);
                    JLabel newImageWidthLabel = new JLabel("Width: 500");
                    JLabel newImageHeightLabel = new JLabel("Height: 500");
                    JLabel colourLabelText = new JLabel("Background Colour: ");
                    JButton create = new JButton("Create Image");
                    JTabbedPane tabbedPane = new JTabbedPane();
                    BufferedImage colourPreview = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
                    ImageIcon colourImageIcon = new ImageIcon(colourPreview);
                    JLabel colourLabel = new JLabel(colourImageIcon);
                    JPanel colourPanel = new JPanel();
                    JColorChooser chooser = new JColorChooser();

                    newImagePanel.setPreferredSize(new Dimension(600, 350));
                    newImagePanel.setLayout(null);
                    newImagePanel.setBackground(themeColour);

                    sliderInitialiser(newImageWidthSlider, newImageWidthLabel, 0, 100);
                    newImageWidthSlider.addChangeListener(new ChangeListener() {

                        public void stateChanged(ChangeEvent e) {
                            newImagePanel.remove(newImageWidthLabel);
                            JSlider source = (JSlider) e.getSource();
                            newImageWidthString = String.valueOf(source.getValue()); //changing label text when slider is moved
                            newImageWidthLabel.setText("Width: " + newImageWidthString);
                            newImagePanel.add(newImageWidthLabel);
                            newImageFrame.setContentPane(tabbedPane);
                        }
                    });

                    sliderInitialiser(newImageHeightSlider, newImageHeightLabel, 75, 125);
                    newImageHeightSlider.addChangeListener(new ChangeListener() {

                        public void stateChanged(ChangeEvent e) {
                            newImagePanel.remove(newImageHeightLabel);
                            JSlider source = (JSlider) e.getSource();
                            newImageHeightString = String.valueOf(source.getValue()); //changing label text when slider is moved
                            newImageHeightLabel.setText("Height: " + newImageHeightString);
                            newImagePanel.add(newImageHeightLabel);
                            newImageFrame.setContentPane(tabbedPane);
                        }
                    });

                    colourLabelText.setBounds(75, 175, 100, 100);
                    colourLabelText.setForeground(Color.WHITE);
                    newImagePanel.add(colourLabelText);

                    colourLabel.setBounds(180, 200, 50, 50);

                    //creating colour preview
                    for (int i = 0; i < colourPreview.getWidth(); i++) {
                        for (int j = 0; j < colourPreview.getHeight(); j++) {
                            colourPreview.setRGB(i, j, Color.WHITE.getRGB());
                        }
                    }

                    newImagePanel.add(colourLabel);

                    create.setBounds(250, 275, 100, 50);
                    newImagePanel.add(create);
                    create.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            newImageFrame.dispose(); //quits newImageFrame
                            mainImage = new BufferedImage(Integer.parseInt(newImageWidthString), Integer.parseInt(newImageHeightString), BufferedImage.TYPE_INT_RGB); //creates buffered image based off user give dimensions

                            for (int i = 0; i < mainImage.getWidth(); i++) {
                                for (int j = 0; j < mainImage.getHeight(); j++) {
                                    mainImage.setRGB(i, j, colour.getRGB()); //sets image RGB based on user chosen colour
                                }
                            }

                            mainPanel.removeAll();
                            viewerSlider();
                            original(mainImage);
                            displayImage();
                        }
                    });

                    colourPanel.add(chooser);
                    colourPanel.setBackground(themeColour);
                    chooser.setBackground(Color.WHITE);
                    chooser.getSelectionModel().addChangeListener(new ChangeListener() {

                        public void stateChanged(ChangeEvent e) {
                            colour = chooser.getColor();
                            for (int i = 0; i < colourPreview.getWidth(); i++) {
                                for (int j = 0; j < colourPreview.getHeight(); j++) {
                                    colourPreview.setRGB(i, j, colour.getRGB()); //changes colour preview when colour picker state changed
                                }
                            }
                        }
                    });

                    newImagePanel.add(newImageWidthSlider);
                    newImagePanel.add(newImageHeightSlider);
                    newImagePanel.add(newImageWidthLabel);
                    newImagePanel.add(newImageHeightLabel);

                    tabbedPane.addTab("Set Size", newImagePanel);
                    tabbedPane.addTab("Set Background Colour", chooser);

                    newImageFrame.setResizable(false);
                    newImageFrame.setContentPane(tabbedPane);
                    newImageFrame.setVisible(true);
                    newImageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    newImageFrame.pack();
                    break;
            }
        }
    }

    public static class Options implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            pixelArray(mainImage);
            if (isImageLoaded() == false) {
                //if image is not loaded user is notified
                JOptionPane.showMessageDialog(null, "You do not have an image loaded!", "Image not loaded", JOptionPane.WARNING_MESSAGE);
            } else {
                for (int i = 0; i < mainImage.getWidth(); i++) {
                    for (int j = 0; j < mainImage.getHeight(); j++) {
                        switch (e.getActionCommand()) {
                            case "Restore to Original":
                                if (imageLabel.isVisible() == true) {
                                    if (mainImage.getWidth() != originalArray.length) {
                                        //changes image dimensions if image has been rotated
                                        mainImage = new BufferedImage(originalArray.length, originalArray[0].length, BufferedImage.TYPE_INT_RGB);
                                    }
                                    mainImage.setRGB(i, j, new Color(originalArray[i][j]).getRGB()); //restores image to original RGB values
                                }
                                break;

                            case "Undo":
                                if (i == 0 && j == 0) {
                                    undoCount--; //undocount subtracted everytime undo command is run
                                }

                                if (undoCount > 0 && mainImage.getWidth() != buffList.get(undoCount).getWidth()) {
                                    //changes image dimensions if image has been rotated
                                    mainImage = new BufferedImage(originalArray.length, originalArray[0].length, BufferedImage.TYPE_INT_RGB);
                                }

                                if (undoCount <= 0 && i == 0 && j == 0) {
                                    //if user has reached maximum undo or in other words returned to original image, the user is notified they can no longer undo
                                    undoCount = 0;
                                    JOptionPane.showMessageDialog(null, "Undo limit reached!", "Undo Limit", JOptionPane.WARNING_MESSAGE);
                                } else if (undoCount > 0) {
                                    //image is set to previously stored image based off undocount 
                                    mainImage.setRGB(i, j, buffList.get(undoCount).getRGB(i, j));
                                }
                                break;

                            case "Rotate...":
                                if (i == 0 && j == 0) {
                                    //opens slider dialogue window based off given parameters
                                    sliderDialogue("Rotate", 1, 3, 2, "Rotate...", 1, Math.pow(90, -1));
                                }
                                break;

                            case "Horizontal Flip":
                                //horizontally flips image by taking setting the leftmost pixels to the rightmost pixels
                                mainImage.setRGB(i, j, rgbArray[mainImage.getWidth() - 1 - i][j]);
                                break;

                            case "Vertical Flip":
                                //vertically flips the image by setting the bottom pixels to the topmost pixels
                                mainImage.setRGB(i, j, rgbArray[i][mainImage.getHeight() - 1 - j]);
                                break;

                            case "Gray Scale":
                                //grayscales image by multiplying RGB values by pre given constants
                                mainImage.setRGB(i, j,
                                        new Color((int) ((new Color(rgbArray[i][j]).getRed()) * 0.3 + (new Color(rgbArray[i][j]).getGreen()) * 0.59 + (new Color(rgbArray[i][j]).getBlue()) * 0.11),
                                                (int) ((new Color(rgbArray[i][j]).getRed()) * 0.3 + (new Color(rgbArray[i][j]).getGreen()) * 0.59 + (new Color(rgbArray[i][j]).getBlue()) * 0.11),
                                                (int) ((new Color(rgbArray[i][j]).getRed()) * 0.3 + (new Color(rgbArray[i][j]).getGreen()) * 0.59 + (new Color(rgbArray[i][j]).getBlue()) * 0.11)).getRGB());
                                break;

                            case "Sepia Tone":
                                ////performs sepia tone by multiplying RGB values by pre given constants and if the value exceed 255 the value is set to 255
                                mainImage.setRGB(i, j,
                                        new Color(Math.min((int) (0.393 * new Color(rgbArray[i][j]).getRed() + 0.769 * (new Color(rgbArray[i][j]).getGreen()) + 0.189 * new Color(rgbArray[i][j]).getBlue()), 255),
                                                Math.min((int) (0.349 * new Color(rgbArray[i][j]).getRed() + 0.686 * (new Color(rgbArray[i][j]).getGreen()) + 0.168 * new Color(rgbArray[i][j]).getBlue()), 255),
                                                Math.min((int) (0.272 * new Color(rgbArray[i][j]).getRed() + 0.534 * (new Color(rgbArray[i][j]).getGreen()) + 0.131 * new Color(rgbArray[i][j]).getBlue()), 255)).getRGB());
                                break;

                            case "Invert Colour":
                                //inverts image by subtracting RGB values from 255
                                mainImage.setRGB(i, j, new Color(255 - new Color(rgbArray[i][j]).getRed(), 255 - (new Color(rgbArray[i][j]).getGreen()), 255 - new Color(rgbArray[i][j]).getBlue()).getRGB());
                                break;

                            case "Gaussian Blur":
                                if (i == 0 && j == 0) {
                                    //opens slider dialogue window based off given parameters
                                    sliderDialogue("Gaussian", 1, 5, 3, "Set Gaussian Blur sliderDialogueValue", 1, 1);
                                }
                                break;

                            case "Bulge Effect":
                                if (i == 0 && j == 0) {
                                    //opens slider dialogue window based off given parameters
                                    sliderDialogue("Bulge", 11, 20, 15, "Set Bulge Factor", 1, 10);
                                }
                                break;
                        }
                    }
                }
                if (!e.getActionCommand().equals("Undo")) {
                    undoCount++; //if effect is not undo, undocount is incremented
                }
                displayImage(); //after an effect is completed the image is displayed usig this method
            }
        }
    }
}