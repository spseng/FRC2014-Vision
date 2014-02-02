package imgTest;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.mortennobel.imagescaling.ResampleOp;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class JFrameApp extends JFrame {
	private final static String TITLE = "Team 1512 Vision App";
	private final static int WIDTH = 1080;
	private final static int HEIGHT = 762;
	private final static int IMG_WIDTH = 640;
	private final static int IMG_HEIGHT = IMG_WIDTH * 9 / 16;
	
	private static String SETTINGS_FILE = System.getProperty("user.home") + System.getProperty("file.separator") + "1512VisionAppSettings.txt";
	private static String SAVED_IMAGES_DIR = System.getProperty("user.home") + System.getProperty("file.separator") + "Pictures";
	
	private JPanel contentPane;
	private JTextField threshold;
	private JRadioButton rdbtnR, rdbtnG, rdbtnB;
	
	private BufferedImage originalImg;
	private IplImage processedImg;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrameApp frame = new JFrameApp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public JFrameApp() {
		setTitle(TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel imageLabel = new JLabel("");
		imageLabel.setBounds(5, 5, 640, 360);
		imageLabel.setMaximumSize(new Dimension(640, 360));
		imageLabel.setMinimumSize(new Dimension(640, 360));
		imageLabel.setPreferredSize(new Dimension(640, 360));
		contentPane.add(imageLabel);
		
		Button loadImage = new Button("Load Image");
		loadImage.setBounds(651, 342, 97, 23);
		loadImage.addActionListener(new LoadImageListener(imageLabel));
		contentPane.add(loadImage);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(754, 12, 2, 719);
		contentPane.add(separator);
		
		JLabel lblSettings = new JLabel("Settings");
		lblSettings.setBounds(887, 12, 70, 15);
		contentPane.add(lblSettings);
		
		Button loadSettings = new Button("Refresh");
		loadSettings.addActionListener(new LoadYAMLListener()); 
		loadSettings.setBounds(857, 392, 100, 23);
		contentPane.add(loadSettings);
		
		Button saveSettings = new Button("Save Settings");
		saveSettings.addActionListener(new DumpYAMLListener());
		saveSettings.setBounds(960, 392, 106, 23);
		contentPane.add(saveSettings);
		
		JLabel lblTolerance = new JLabel("Threshold (0-255):");
		lblTolerance.setBounds(774, 39, 134, 15);
		contentPane.add(lblTolerance);
		
		Button saveImage = new Button("Save Image");
		saveImage.addActionListener(new SaveImageListener());
		saveImage.setBounds(651, 704, 97, 23);
		contentPane.add(saveImage);
		
		JLabel processedLabel = new JLabel("");
		processedLabel.setBounds(5, 371, 640, 360);
		contentPane.add(processedLabel);
		
		Button process = new Button("Process");
		process.addActionListener(new ImageProcessListener(processedLabel));
		process.setBounds(651, 371, 96, 23);
		contentPane.add(process);
		
		threshold = new JTextField();
		threshold.setBounds(913, 39, 114, 19);
		contentPane.add(threshold);
		threshold.setColumns(10);
		
		rdbtnR = new JRadioButton("r");
		rdbtnR.setBounds(784, 66, 44, 23);
		contentPane.add(rdbtnR);
		
		rdbtnG = new JRadioButton("g");
		rdbtnG.setBounds(783, 93, 44, 23);
		contentPane.add(rdbtnG);
		
		rdbtnB = new JRadioButton("b");
		rdbtnB.setBounds(784, 120, 149, 23);
		contentPane.add(rdbtnB);
		
		//group radio buttons
		ButtonGroup grp = new ButtonGroup();
		grp.add(rdbtnR);
		grp.add(rdbtnG);
		grp.add(rdbtnB);
	}
	
	class LoadImageListener implements ActionListener {
		JLabel label;
		public LoadImageListener(JLabel label) {
			this.label = label;
		}
		
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
		    FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "Images", "jpg", "gif", "png", "tiff", "bmp");
		    chooser.setFileFilter(filter);
		    int returnVal = chooser.showOpenDialog(label);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	try {
			    	originalImg = ImageIO.read(chooser.getSelectedFile());
			    	//resize img if necessary
			    	if (originalImg.getWidth() > IMG_WIDTH || originalImg.getHeight() > IMG_HEIGHT) {
			    		//debug
//			    		System.out.println("W: " + originalImg.getWidth() + " H: " + originalImg.getHeight());
			    		double scale = Math.min(IMG_WIDTH/(double)originalImg.getWidth(), IMG_HEIGHT/(double)originalImg.getHeight());
			    		ResampleOp rsop = new ResampleOp((int)(originalImg.getWidth()*scale), (int)(originalImg.getHeight()*scale));
			    		originalImg = rsop.filter(originalImg, null);	
			    		//debug
//	                    System.out.println("W: " + originalImg.getWidth() + " H: " + originalImg.getHeight());
			    	}
			    	label.setIcon(new ImageIcon(originalImg));
		    	} catch (IOException ex) {
			    	JOptionPane.showMessageDialog(contentPane, "Unable to load image. Try telling your computer you enjoy its company and try again.");
			    }
		    }
		}
	}
	
	class SaveImageListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				if (processedImg == null) {
					JOptionPane.showMessageDialog(contentPane, "Please load and process and image before attempting to save");
					return;
				}
				//setup default output file (with default name)
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
				Date date = new Date();
				String timestamp = df.format(date);
				File out = new File(SAVED_IMAGES_DIR + System.getProperty("file.separator") + "1512VisionApp_" + timestamp);
				//save dialog
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("*.jpg", "jpg");
				chooser.setFileFilter(filter);
				chooser.setSelectedFile(out);
				int retVal = chooser.showSaveDialog(contentPane);
				if (retVal == JFileChooser.APPROVE_OPTION) {
					out = chooser.getSelectedFile();
				}
				out = new File(out.getAbsolutePath() + ".jpg");
				//debug
//				System.out.println("Saving file " + out.getAbsolutePath());
				ImageIO.write(processedImg.getBufferedImage(), "jpg", out);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(contentPane, "Unable to save image. Try telling your computer you enjoy its company and try again.");
			}
		}
	}
	
	class ImageProcessListener implements ActionListener {
		JLabel label;
		public ImageProcessListener(JLabel l) {
			this.label = l;
		}
		
		public void actionPerformed(ActionEvent e) {
			//ACTUAL VISION PROCESSING IS HERE
			if (originalImg == null) {
				JOptionPane.showMessageDialog(contentPane, "Please load an image to process");
				return;
			}
			IplImage original = IplImage.createFrom(originalImg);
			int width = original.width();
			int height = original.height();
			//we can only threshold single-channel images
			IplImage r = IplImage.create(width, height, IPL_DEPTH_8U, 1);
			IplImage g = IplImage.create(width, height, IPL_DEPTH_8U, 1);
			IplImage b = IplImage.create(width, height, IPL_DEPTH_8U, 1);
			cvSplit(original, b, g, r, null);
			if (rdbtnR.isSelected() && !rdbtnG.isSelected() && !rdbtnB.isSelected()) {
				cvThreshold(r, r, Integer.parseInt(threshold.getText()), 255, 0);
				processedImg = r.clone();
			} else if (!rdbtnR.isSelected() && rdbtnG.isSelected() && !rdbtnB.isSelected()) {
				cvThreshold(g, g, Integer.parseInt(threshold.getText()), 255, 0);
				processedImg = g.clone();
			} else if (!rdbtnR.isSelected() && !rdbtnG.isSelected() && rdbtnB.isSelected()) {
				cvThreshold(b, b, Integer.parseInt(threshold.getText()), 255, 0);
				processedImg = b.clone();
			} else System.out.println("There's a problem with the radio buttons...");
			
			label.setIcon(new ImageIcon(processedImg.getBufferedImage()));
		}
	}
	
	class LoadYAMLListener implements ActionListener {
		private void findYAML(String msg) {
			File settingsFile = new File(SETTINGS_FILE);
			if (!settingsFile.exists()) {
				int optionRetVal = JOptionPane.showConfirmDialog(contentPane, msg, "Oops", JOptionPane.OK_CANCEL_OPTION);
				if (optionRetVal == JOptionPane.OK_OPTION) {
					JFileChooser chooser = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Open vision settings file", "yaml", "txt");
					chooser.setFileFilter(filter);
					int chooserRetVal = chooser.showOpenDialog(contentPane);
					if (chooserRetVal == JFileChooser.APPROVE_OPTION) {
						SETTINGS_FILE = chooser.getSelectedFile().getAbsolutePath();
					} else JOptionPane.showMessageDialog(contentPane,
							"The settings file cannot be found. A new settings file will be created the next time settings are saved");
				} else JOptionPane.showMessageDialog(contentPane,
						"The settings file cannot be found. A new settings file will be created the next time settings are saved.");
			}
		}
		
		public void actionPerformed(ActionEvent e) {
			try {		
				HashMap<String, Object> settings = (HashMap<String, Object>)(new Yaml().load(new FileInputStream(SETTINGS_FILE)));
				rdbtnR.setSelected((Boolean)settings.get("radioR"));
				rdbtnG.setSelected((Boolean)settings.get("radioG"));
				rdbtnB.setSelected((Boolean)settings.get("radioB"));
				threshold.setText(settings.get("threshold").toString());
			} catch (FileNotFoundException e1) {
				findYAML("The settings file cannot be found. Please select a settings file.");
			} catch (YAMLException e2) {
				findYAML("Bad YAML file! Please choose a new one.");
			}
		}
	}
	
	class DumpYAMLListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				File settingsFile = new File(SETTINGS_FILE);
				//debug
//				System.out.println("Creating file in " + SETTINGS_FILE);
				//does nothing if the file already exists
				settingsFile.createNewFile();
				PrintWriter pw = new PrintWriter(SETTINGS_FILE);
                HashMap<String, Object> settings = new HashMap<String, Object>();
                settings.put("radioR", rdbtnR.isSelected());
			    settings.put("radioB", rdbtnG.isSelected());
			    settings.put("radioG", rdbtnB.isSelected());
			    settings.put("threshold", Integer.parseInt(threshold.getText()));
			    new Yaml().dump(settings, pw);
			} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(contentPane, "Settings file not found!");
//				e1.printStackTrace();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(contentPane, "Unable to create new file!");
//				e1.printStackTrace();
			}
		}
	}
}
