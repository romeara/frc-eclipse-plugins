/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.AnalogChannel; //This is used for the arm potentiometer
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Jaguar; //If you use Victors, import them instead
import edu.wpi.first.wpilibj.Timer; //Make sure to use this version, not the java.util version of timer.

/**
 * This program initializes a PID Controller for the arm and drives
 * to the MIDDLE position using the potentiometer.  
 * PID works by changing the output of the motors by PID constants,
 * based on how far away you are from the target.
 * PID values need to be tuned for your robot.
 * 
 * @author: Fredric Silberberg
 */
public class ArmDemo extends SimpleRobot {

    //Elevator Motor
    private final SpeedController elevMotor = new Jaguar(6);
    
    //Potentiometer on the robot elevator
    private final AnalogChannel elevPot = new AnalogChannel(4);
    
    //Proportional, Integral, and Dervative constants
    //These values will need to be tuned for your robot
    private final double Kp = 0.05;
    private final double Ki = 0.0;
    private final double Kd = 0.0;
    
    //This should be fully initialized in the constructor
    private final PIDController elevPID;
    
    //Values for the potentiometer 
    //Make sure you use the values you get from elevPot.getAverageValue,
    //not the voltages.
    public static final double BOTTOM = 912;
    public static final double TOP = 10;
    public static final double MIDDLE = 452;
    
    //Constructor
    public ArmDemo() {
        //Initializes the elevator PID Controller
        elevPID = new PIDController(Kp, Ki, Kd, elevPot, elevMotor);
        
        //Enables elevPID
        elevPID.enable();
        
        //Sets the input range for the PID controller to be between the 
        //top and the bottom.
        //Ensure that it is in low, then high order,
        //or it will throw a BoundaryException.
        elevPID.setInputRange(TOP, BOTTOM);
    }
    
    /**
     * This function is called once each time the robot enters operator control.
     * Teleop Commands go in operatorControl
     */
    public void operatorControl() {
        //Drive claw to the middle position
        elevPID.setSetpoint(MIDDLE);
         
        //Delay so that the elevator can finish driving
        //before operatorControl exits
        Timer.delay(15);
    }
}
