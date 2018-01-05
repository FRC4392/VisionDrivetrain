package org.usfirst.frc.team4932.Subsystems;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.TalonSRX;
import org.usfirst.frc.team4932.Constants;

/**
 * Created by kzakfeld on 1/4/18.
 */
public class Drivetrain {
    private static Drivetrain m_instance = null;

    private final CANTalon leftMaster, leftSlave, rightMaster, rightSlave;

    public static Drivetrain getInstance(){
        if (m_instance == null){
            m_instance = new Drivetrain();
        }
        return m_instance;
    }

    private Drivetrain(){
        leftMaster = new CANTalon(Constants.kDriveLeftTalonMasterID);
        leftSlave = new CANTalon(Constants.kDriveLeftTalonSlaveID);
        rightMaster = new CANTalon(Constants.kDriveRightMasterID);
        rightSlave = new CANTalon(Constants.kDriveRightSlaveID);

        leftMaster.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        leftMaster.reverseOutput(false);
        leftMaster.reverseSensor(false);

        leftSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
        leftSlave.set(Constants.kDriveLeftTalonMasterID);
        leftSlave.reverseOutput(false);

        rightMaster.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        rightMaster.reverseOutput(true);
        rightMaster.reverseSensor(true);

        rightSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
        rightSlave.set(Constants.kDriveRightMasterID);
        rightSlave.reverseOutput(false);

    }

    public void setLeftRight(double right, double left){
        rightMaster.set(-right);
        leftMaster.set(left);
    }

}
