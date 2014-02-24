/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imgTest;

import java.awt.EventQueue;

/**
 *
 * @author robots
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            //are we in gui mode?
            if ("--gui".equals(args[0])) {
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
            } else {
                new CmdApp();
            }
        }
    }

}
