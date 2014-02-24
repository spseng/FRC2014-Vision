/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package imgTest;

import com.googlecode.javacv.cpp.opencv_core;
import edu.wpi.first.wpijavacv.*;
import java.util.ArrayList;

/**
 *
 * @author robots
 */
public class Processor {
    
    public ArrayList<WPIPoint> process(ColorImage img) {
        opencv_core.IplImage threshCopy = opencv_core.IplImage.createFrom(originalImg).clone();
            opencv_core.IplImage drawCopy = opencv_core.IplImage.createFrom(originalImg).clone();
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
            
                
            thresh.dispose();
            drawMe.dispose();
            threshMe.dispose();
            drawCopy.release();
    }
    
}
