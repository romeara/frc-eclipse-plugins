#include "ExamplePIDSubsystem.h"
#include "../Robotmap.h"
#include "SmartDashboard/SmartDashboard.h"

ExamplePIDSubsystem::ExamplePIDSubsystem() : PIDSubsystem("ExamplePIDSubsystem", Kp, Ki, Kd) {
	// Use these to get going:
	// SetSetpoint() -  Sets where the PID controller should move the system
	//                  to
	// Enable() - Enables the PID controller.
}

double ExamplePIDSubsystem::ReturnPIDInput() {
	// Return your input value for the PID loop
	// e.g. a sensor, like a potentiometer:
	// yourPot->SetAverageVoltage() / kYourMaxVoltage;
	return 0.0;
}

void ExamplePIDSubsystem::UsePIDOutput(double output) {
	// Use output to drive your system, like a motor
	// e.g. yourMotor->Set(output);
}

void ExamplePIDSubsystem::InitDefaultCommand() {
	// Set the default command for a subsystem here.
	//setDefaultCommand(new MySpecialCommand());
}
