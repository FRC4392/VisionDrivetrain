package org.usfirst.frc.team4932.robot;

import org.usfirst.frc.team4932.vision.VisionServer;
import org.usfirst.frc.team4932.vision.VisionUpdateReceiver;
import org.usfirst.frc.team4932.vision.VisionUpdate;
import org.usfirst.frc.team4932.vision.TargetInfo;

import edu.wpi.first.wpilibj.IterativeRobot;

import java.util.List;

public class Robot extends IterativeRobot implements VisionUpdateReceiver {
	
	VisionServer visionServer = VisionServer.getInstance();

	@Override
	public void robotInit() {
		
	}

	@Override
	public void autonomousInit() {
	}

	@Override
	public void autonomousPeriodic() {
	}

	@Override
	public void teleopPeriodic() {
	}

	@Override
	public void testPeriodic() {
	}
	
	@Override
	public void gotUpdate(VisionUpdate update) {
		List<TargetInfo> infos = update.getTargets();
		
		for(TargetInfo target : infos) {
			System.out.println("Target Data Y: " + target.getY() + " Z: " + target.getZ());
		}
		
	}
}

