/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package imgTest;

import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpijavacv.WPIPoint;
import java.util.ArrayList;
/**
 *
 * @author robots
 */
public class CmdApp {
    AxisCamera camera;
    Processor proc;
    
    //has Ctrl-C been pressed
    boolean interrupt;
    
    public CmdApp() {
        print("================ Team 1512 Vision Tracker 2014 ================");
        print("initializing...");
        camera = AxisCamera.getInstance("10.15.12.11");
        proc = new Processor();
        print("done.\n");
    }
    
    private void run() {
        while(!interrupt) {
            ArrayList<WPIPoint> results = proc.process(camera.getImage());
            //write to networktable
        }
        cleanup();
    }
    
    private void cleanup() {
        print("Goodbye!");
    }
    
    private void print(String... args) {
        for (String s: args) {
            System.out.print(args + " ");
        }
        System.out.print("\n");
    }
    
}
