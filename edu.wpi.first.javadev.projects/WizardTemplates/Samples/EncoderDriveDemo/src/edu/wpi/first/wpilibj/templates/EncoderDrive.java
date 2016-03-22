/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Jaguar; //If your team uses victors, import them instead
import edu.wpi.first.wpilibj.RobotDrive;

/**
 * This program sets up a basic robot, with two motors and encoders.
 * It then drives for five feet during teleop mode.
 * 
 * @author: Fredric Silberberg
 */
public class EncoderDrive extends SimpleRobot {
    
    //Initializes the motors
    private final SpeedController left = new Jaguar(2);
    private final SpeedController right = new Jaguar(1);
    
    //Initializes the encoders
    private final Encoder backLeft = new Encoder(1,2);
    private final Encoder backRight = new Encoder(4,3);
    
    //Initializes the drive
    private RobotDrive drive = new RobotDrive(left, right);
    
    public EncoderDrive(){
        //Starts the encoders
        backLeft.start();
        backRight.start();
        
        //Sets distance in inches
        //This was obtained by finding the circumfrance of the wheels,
        //finding the number of encoder pulses per rotation,
        //and diving the circumfrance by that number.
        backLeft.setDistancePerPulse(.000623);
        backRight.setDistancePerPulse(.000623);
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl() {
        //Drives until the average of the encoders is 60 inches, or five feet
        while((60-((backLeft.getDistance()+backRight.getDistance())/2)) > 0){
            drive.tankDrive(1, 1);
        }        
    }
}
