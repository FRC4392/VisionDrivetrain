package org.usfirst.frc.team4932.robot;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team4932.Subsystems.Drivetrain;
import org.usfirst.frc.team4932.vision.VisionServer;
import org.usfirst.frc.team4932.vision.VisionUpdateReceiver;
import org.usfirst.frc.team4932.vision.VisionUpdate;
import org.usfirst.frc.team4932.vision.TargetInfo;

import edu.wpi.first.wpilibj.IterativeRobot;

import java.util.List;

public class Robot extends IterativeRobot implements VisionUpdateReceiver {
	
	VisionServer visionServer = VisionServer.getInstance();
	Drivetrain drive = Drivetrain.getInstance();

	@Override
	public void robotInit() {
		visionServer.addVisionUpdateReceiver(this);
		SmartDashboard.putNumber("VisionP", 2);
	}

	@Override
	public void autonomousInit() {
		drive.setLeftRight(-.1, -.1);
	}

	@Override
	public void autonomousPeriodic() {

	}

	@Override
	public void teleopPeriodic() {
		double speed = SmartDashboard.getNumber("Vision Position", 0);
		double kp = SmartDashboard.getNumber("VisionP", 1);
		speed *= kp;
	}

	@Override
	public void testPeriodic() {
	}
	
	@Override
	public void gotUpdate(VisionUpdate update) {
		List<TargetInfo> infos = update.getTargets();

		if (infos.size() == 1){
			TargetInfo pos = infos.get(0);
			double position = pos.getY();
			SmartDashboard.putNumber("Vision Position", position);
		}
		
	}
}

