package imgTest;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.imageio.ImageIO;

import edu.wpi.first.wpijavacv.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacpp.Loader;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.mortennobel.imagescaling.ResampleOp;

import java.util.Arrays;

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
	private JTextArea textSettings;
	
	private HashMap<String, Object> visionParams;
	private BufferedImage originalImg;
	private IplImage processedImg;
	private CanvasFrame cf1, cf2;

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
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setLocationRelativeTo(null);
		addWindowListener(new CleanupListener());
		
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
		loadSettings.setBounds(857, 704, 100, 23);
		contentPane.add(loadSettings);
		
		Button saveSettings = new Button("Save Settings");
		saveSettings.addActionListener(new DumpYAMLListener());
		saveSettings.setBounds(960, 704, 106, 23);
		contentPane.add(saveSettings);
		
		Button saveImage = new Button("Save Image");
		saveImage.addActionListener(new SaveImageListener());
		saveImage.setBounds(651, 704, 97, 23);
		contentPane.add(saveImage);
		
		JLabel processedLabel = new JLabel("");
		processedLabel.setBounds(5, 371, 640, 360);
        processedLabel.setBorder(BorderFactory.createLineBorder(Color.red));
		contentPane.add(processedLabel);
                
        JLabel processedLabel2 = new JLabel("");
        processedLabel2.setBounds(5, 735, 640, 360);
        processedLabel2.setBorder(BorderFactory.createLineBorder(Color.red));
        contentPane.add(processedLabel2);
		
		Button process = new Button("Process");
		process.addActionListener(new ImageProcessListener(processedLabel, processedLabel2));
		process.setBounds(651, 371, 96, 23);
		contentPane.add(process);
		
		textSettings = new JTextArea();
		textSettings.setBounds(768, 39, 296, 659);
		contentPane.add(textSettings);
		
		cf1 = new CanvasFrame("window 1");
		cf2 = new CanvasFrame("window 2");
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
		JLabel label, label2;
		
		public ImageProcessListener(JLabel L1, JLabel L2) {
			this.label = L1;
            this.label2 = L2;
		}
		
		public void actionPerformed(ActionEvent e) {
			// parse vision parameters
			if (visionParams == null || visionParams.isEmpty()) {
				JOptionPane.showMessageDialog(contentPane, "Please save settings before processing");
				return;
			}
//			int threshold = Integer.parseInt((String)visionParams.get("Threshold"));
//			String thresholdChannel = visionParams.get("Channel").toString();
//			boolean drawContours = Boolean.parseBoolean((String)visionParams.get("DrawContours"));
			
			//debug
//			System.out.println(Integer.toString(threshold));
//			System.out.println(thresholdChannel);

			//ACTUAL VISION PROCESSING IS HERE
            int THRESHOLD = Integer.parseInt((String)visionParams.get("Threshold"));
            int MAX_CONTOURS = Integer.parseInt((String)visionParams.get("Max Contours"));
            int CONTOUR_MIN_WIDTH = Integer.parseInt((String)visionParams.get("Min Contour Width"));
            int CONTOUR_MAX_WIDTH = Integer.parseInt((String)visionParams.get("Max Contour Width"));
            int CONTOUR_MIN_HEIGHT = Integer.parseInt((String)visionParams.get("Min Contour Height"));
            int CONTOUR_MAX_HEIGHT = Integer.parseInt((String)visionParams.get("Max Contour Height"));
//          int EQUIV_RECT_SHORT_MIN = Integer.parseInt((String)visionParams.get("Threshold"));;
//          int EQUIV_RECT_SHORT_MAX = Integer.parseInt((String)visionParams.get("Threshold"));;
//          int EQUIV_RECT_LONG_MIN = Integer.parseInt((String)visionParams.get("Threshold"));;
//          int EQUIV_RECT_LONG_MAX = Integer.parseInt((String)visionParams.get("Threshold"));;
//          int MAX_PARTICLES = Integer.parseInt((String)visionParams.get("Threshold"));;
            
//            cf1.showImage(img.getBufferedImage(1.0));
            
            //go through much pain and suffering to explicitly create separate buffers for the 3 images 
            IplImage threshCopy = IplImage.createFrom(originalImg).clone();
            IplImage drawCopy = IplImage.createFrom(originalImg).clone();
            WPIColorImage threshMe = new WPIColorImage(threshCopy.getBufferedImage());
            WPIColorImage drawMe = new WPIColorImage(drawCopy.getBufferedImage());

            WPIBinaryImage thresh = threshMe.getGreenChannel().getThreshold(THRESHOLD);
            WPIContour[] contours = thresh.findContours();
            System.out.println(contours.length);
          
//          WPIColorImage contourImage = new WPIColorImage(original.getBufferedImage());
            ArrayList<WPIPoint> points = new ArrayList<WPIPoint>();
            for (int i=0; i<MAX_CONTOURS; i++) {
            	if (i >= contours.length) break;
            	WPIContour c = contours[i];
            	if (c.getWidth() > CONTOUR_MIN_WIDTH && c.getWidth() < CONTOUR_MAX_WIDTH &&
            			c.getHeight() > CONTOUR_MIN_HEIGHT && c.getHeight() < CONTOUR_MAX_HEIGHT) {
            		int center_x = c.getX() + (c.getWidth() / 2);
            		int center_y = c.getY() + (c.getHeight() / 2);
                    drawMe.drawContour(c, WPIColor.RED, 3);
                    drawMe.drawPoint(new WPIPoint(center_x, center_y), WPIColor.BLUE, 2);
                    points.add(new WPIPoint(center_x, center_y));
            	}
            }
            System.out.println("\t" + Integer.toString(points.size()) + "\n");
            if (!cf1.isShowing()) cf1.setVisible(true);
            if (!cf2.isShowing()) cf2.setVisible(true);
            cf1.showImage(thresh.getBufferedImage());
            cf2.showImage(drawMe.getBufferedImage());
                
            thresh.dispose();
            drawMe.dispose();
            threshMe.dispose();
            drawCopy.release();;
            threshCopy.release();
                       
			/*if (originalImg == null) {
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

			switch(thresholdChannel) {
			case "red":
				cvThreshold(r, r, threshold, 255, 0);
				processedImg = r.clone();
				break;
			case "green":
				cvThreshold(g, g, threshold, 255, 0);
				processedImg = g.clone();
				break;
			case "blue":
				cvThreshold(b, b, threshold, 255, 0);
				processedImg = b.clone();
				break;
			default:
				//do no thresholding
				processedImg = original;
				break;
			}
			
			if (drawContours) {
				//draw contours
				CvMemStorage storage = CvMemStorage.create();
				CvSeq contour = new CvSeq(null);
				cvFindContours(processedImg, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
				CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class),
                    storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.02, 0);
//				cvDrawContours(processedImg, points, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);
				CvBox2D rect = cvMinAreaRect2(points, storage);
                                float h = rect.size().height(), w = rect.size().width();
                                System.out.println(w + " " + h);
			}
		
			label.setIcon(new ImageIcon(processedImg.getBufferedImage()));*/
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
				visionParams = (HashMap<String, Object>)(new Yaml().load(new FileInputStream(SETTINGS_FILE)));
				//debug
				if (visionParams == null) {
					JOptionPane.showMessageDialog(contentPane, "The settings file is empty! New settings will be overwritten on next save.");
					return;
				}
//				else System.out.println("Good hashmap, printing keys...\n");
				String displayMe = "";
				for (String key: visionParams.keySet()) {
					//debug
//					System.out.println("A");
					String out = key + ": " + visionParams.get(key).toString();
//					System.out.println(out);
					displayMe = displayMe.concat(out + "\n");
				}
				textSettings.setText(displayMe);
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
				//check that we have settings to save before doing anything else
				if (textSettings.getText().equals("")) {
					JOptionPane.showMessageDialog(contentPane, "Settings pane is empty. Refresh settings or add new settings to overwrite.");
					return;
				}
				File settingsFile = new File(SETTINGS_FILE);
				//debug
//				System.out.println("Creating file in " + SETTINGS_FILE);
				//does nothing if the file already exists
				settingsFile.createNewFile();
				PrintWriter pw = new PrintWriter(SETTINGS_FILE);
				//overwrite the current settings every time (allows for deletion of settings)
				visionParams = new HashMap<String, Object>();
                for (String line : textSettings.getText().split("\n")) {
                	String[] kv = line.split(": ");
                	visionParams.put(kv[0], kv[1]);
                }
			    new Yaml().dump(visionParams, pw);
			} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(contentPane, "Settings file not found!");
//				e1.printStackTrace();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(contentPane, "Unable to create new file!");
//				e1.printStackTrace();
			}
		}
	}
	
	class CleanupListener implements WindowListener {

		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			if (cf1 != null) cf1.dispose();
			if (cf2 != null) cf2.dispose();
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
