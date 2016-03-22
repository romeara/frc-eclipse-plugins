/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2011. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.KinectStick;
import edu.wpi.first.wpilibj.Timer;

/**
 *
 * @author koconnor
 * This code demonstrates the use of the KinectStick
 * class to drive your robot during the autonomous mode
 * of the match, making it a hybrid machine. The gestures
 * used to control the KinectStick class are described in the
 * "Getting Started with the Microsoft Kinect for FRC" document
 *
 */
public class KinectStickExample extends SimpleRobot {

    RobotDrive drivetrain;
    KinectStick leftArm;
    KinectStick rightArm;
    boolean exampleButton;

    public KinectStickExample() {
        drivetrain = new RobotDrive(1,2);
        leftArm = new KinectStick(1);
        rightArm = new KinectStick(2);
    }

    /**
     * This function is called once each time the robot enters autonomous mode.
     */
    public void autonomous() {
        while(isEnabled()){
            /**
             * KinectStick axis values are accessed identically to those of a joystick
             * In this example the axis values have been scaled by ~1/3 for safer
             * operation when learning to use the Kinect.
             */
            drivetrain.tankDrive(leftArm.getY()*.33, rightArm.getY()*.33);

            /* An alternative illustrating that the KinectStick can be used just like a Joystick */
            //drivetrain.tankDrive(leftArm, rightArm);

            /*Example illustrating that accessing buttons is identical to a Joystick */
            exampleButton = leftArm.getRawButton(1);

            Timer.delay(.01); /* Delay 10ms to reduce processing load */
        }
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl() {
       /**
        * supply your own teleop code here
        */
    }
}
