package org.mort11.subsystems;

import static org.mort11.util.Constants.DrivetrainMotors.BACK_LEFT_MODULE_DRIVE_MOTOR;
import static org.mort11.util.Constants.DrivetrainMotors.BACK_LEFT_MODULE_STEER_ENCODER;
import static org.mort11.util.Constants.DrivetrainMotors.BACK_LEFT_MODULE_STEER_MOTOR;
import static org.mort11.util.Constants.DrivetrainMotors.BACK_LEFT_MODULE_STEER_OFFSET;
import static org.mort11.util.Constants.DrivetrainMotors.BACK_RIGHT_MODULE_DRIVE_MOTOR;
import static org.mort11.util.Constants.DrivetrainMotors.BACK_RIGHT_MODULE_STEER_ENCODER;
import static org.mort11.util.Constants.DrivetrainMotors.BACK_RIGHT_MODULE_STEER_MOTOR;
import static org.mort11.util.Constants.DrivetrainMotors.BACK_RIGHT_MODULE_STEER_OFFSET;
import static org.mort11.util.Constants.DrivetrainMotors.FRONT_LEFT_MODULE_DRIVE_MOTOR;
import static org.mort11.util.Constants.DrivetrainMotors.FRONT_LEFT_MODULE_STEER_ENCODER;
import static org.mort11.util.Constants.DrivetrainMotors.FRONT_LEFT_MODULE_STEER_MOTOR;
import static org.mort11.util.Constants.DrivetrainMotors.FRONT_LEFT_MODULE_STEER_OFFSET;
import static org.mort11.util.Constants.DrivetrainMotors.FRONT_RIGHT_MODULE_DRIVE_MOTOR;
import static org.mort11.util.Constants.DrivetrainMotors.FRONT_RIGHT_MODULE_STEER_ENCODER;
import static org.mort11.util.Constants.DrivetrainMotors.FRONT_RIGHT_MODULE_STEER_MOTOR;
import static org.mort11.util.Constants.DrivetrainMotors.FRONT_RIGHT_MODULE_STEER_OFFSET;
import static org.mort11.util.Constants.DrivetrainSpecs.DRIVETRAIN_TRACKWIDTH_METERS;
import static org.mort11.util.Constants.DrivetrainSpecs.DRIVETRAIN_WHEELBASE_METERS;
import static org.mort11.util.Constants.DrivetrainSpecs.MAX_VELOCITY_METERS_PER_SECOND;
import static org.mort11.util.Constants.DrivetrainSpecs.MAX_VOLTAGE;

import com.kauailabs.navx.frc.AHRS;
import com.swervedrivespecialties.swervelib.Mk4iSwerveModuleHelper;
import com.swervedrivespecialties.swervelib.SwerveModule;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Drivetrain extends SubsystemBase {
    private final AHRS navX;

    public final SwerveDriveKinematics driveKinematics;
    private SwerveDriveOdometry driveOdometry;

    private final SwerveModule frontLeftModule;
    private final SwerveModule frontRightModule;
    private final SwerveModule backLeftModule;
    private final SwerveModule backRightModule;

    private ChassisSpeeds chassisSpeeds;

    private PIDController xController;
    private PIDController yController;
    private PIDController thetaController;

    private static Drivetrain drivetrain;

public Drivetrain() {
        navX = new AHRS(SerialPort.Port.kMXP);
        driveKinematics =
                new SwerveDriveKinematics(
                        // Front left
                        new Translation2d(
                                DRIVETRAIN_TRACKWIDTH_METERS / 2.0,
                                DRIVETRAIN_WHEELBASE_METERS / 2.0),
                        // Front right
                        new Translation2d(
                                DRIVETRAIN_TRACKWIDTH_METERS / 2.0,
                                -DRIVETRAIN_WHEELBASE_METERS / 2.0),
                        // Back left
                        new Translation2d(
                                -DRIVETRAIN_TRACKWIDTH_METERS / 2.0,
                                DRIVETRAIN_WHEELBASE_METERS / 2.0),
                        // Back right
                        new Translation2d(
                                -DRIVETRAIN_TRACKWIDTH_METERS / 2.0,
                                -DRIVETRAIN_WHEELBASE_METERS / 2.0));

        chassisSpeeds = new ChassisSpeeds(0.0, 0.0, 0.0);

        ShuffleboardTab tab = Shuffleboard.getTab("Drivetrain");

        frontLeftModule =
                Mk4iSwerveModuleHelper.createFalcon500(
                        tab.getLayout("Front Left Module", BuiltInLayouts.kList)
                                .withSize(2, 4)
                                .withPosition(0, 0),
                        Mk4iSwerveModuleHelper.GearRatio.L2,
                        FRONT_LEFT_MODULE_DRIVE_MOTOR,
                        FRONT_LEFT_MODULE_STEER_MOTOR,
                        FRONT_LEFT_MODULE_STEER_ENCODER,
                        FRONT_LEFT_MODULE_STEER_OFFSET);

        frontRightModule =
                Mk4iSwerveModuleHelper.createFalcon500(
                        tab.getLayout("Front Right Module", BuiltInLayouts.kList)
                                .withSize(2, 4)
                                .withPosition(2, 0),
                        Mk4iSwerveModuleHelper.GearRatio.L2,
                        FRONT_RIGHT_MODULE_DRIVE_MOTOR,
                        FRONT_RIGHT_MODULE_STEER_MOTOR,
                        FRONT_RIGHT_MODULE_STEER_ENCODER,
                        FRONT_RIGHT_MODULE_STEER_OFFSET);

        backLeftModule =
                Mk4iSwerveModuleHelper.createFalcon500(
                        tab.getLayout("Back Left Module", BuiltInLayouts.kList)
                                .withSize(2, 4)
                                .withPosition(4, 0),
                        Mk4iSwerveModuleHelper.GearRatio.L2,
                        BACK_LEFT_MODULE_DRIVE_MOTOR,
                        BACK_LEFT_MODULE_STEER_MOTOR,
                        BACK_LEFT_MODULE_STEER_ENCODER,
                        BACK_LEFT_MODULE_STEER_OFFSET);

        backRightModule =
                Mk4iSwerveModuleHelper.createFalcon500(
                        tab.getLayout("Back Right Module", BuiltInLayouts.kList)
                                .withSize(2, 4)
                                .withPosition(6, 0),
                        Mk4iSwerveModuleHelper.GearRatio.L2,
                        BACK_RIGHT_MODULE_DRIVE_MOTOR,
                        BACK_RIGHT_MODULE_STEER_MOTOR,
                        BACK_RIGHT_MODULE_STEER_ENCODER,
                        BACK_RIGHT_MODULE_STEER_OFFSET);
                                        
        xController = new PIDController(0.8, 0, 0);
        xController.setSetpoint(-3);
        xController.setTolerance(0.1);

        yController = new PIDController(.41, 0, 0);
        yController.setSetpoint(0);
        yController.setTolerance(0.1);
        
        thetaController = new PIDController(0.04, 0, 0);
        thetaController.setSetpoint(0);
        thetaController.setTolerance(0.5);
    }

    /** Sets the gyroscope angle to zero. */
    public void zeroGyroscope() {
        navX.zeroYaw();
    }

    public Rotation2d getGyroscopeRotation() {
        if (navX.isMagnetometerCalibrated()) {
            // We will only get valid fused headings if the magnetometer is calibrated
            return Rotation2d.fromDegrees(navX.getFusedHeading());
        }

        // We have to invert the angle of the NavX so that rotating the robot counter-clockwise
        // makes the angle increase.
        return Rotation2d.fromDegrees(360.0 - navX.getYaw());
    }

    public SwerveDriveKinematics getDriveKinematics() {
        return driveKinematics;
    }

    public Pose2d getPose() {
        return driveOdometry.getPoseMeters();
    }

    public void resetPose(Pose2d pose) {
        driveOdometry.resetPosition(getGyroscopeRotation(),                 new SwerveModulePosition[]{ frontLeftModule.getPosition(), frontRightModule.getPosition(), backLeftModule.getPosition(), backRightModule.getPosition() }
        , pose);
    }

    public void setModuleStates(SwerveModuleState[] states) {
            SwerveDriveKinematics.desaturateWheelSpeeds(states, MAX_VELOCITY_METERS_PER_SECOND);

            frontLeftModule.set(
                            states[0].speedMetersPerSecond / MAX_VELOCITY_METERS_PER_SECOND * MAX_VOLTAGE,
                            states[0].angle.getRadians());
            frontRightModule.set(
                            states[1].speedMetersPerSecond / MAX_VELOCITY_METERS_PER_SECOND * MAX_VOLTAGE,
                            states[1].angle.getRadians());
            backLeftModule.set(
                            states[2].speedMetersPerSecond / MAX_VELOCITY_METERS_PER_SECOND * MAX_VOLTAGE,
                            states[2].angle.getRadians());
            backRightModule.set(
                            states[3].speedMetersPerSecond / MAX_VELOCITY_METERS_PER_SECOND * MAX_VOLTAGE,
                            states[3].angle.getRadians());
    }

    public double getCompass() {
            return navX.getCompassHeading();
    }

    public void drive(ChassisSpeeds chassisSpeeds) {
        this.chassisSpeeds = chassisSpeeds;
    }
    
    public PIDController getXController() {
        return xController;
    }

    public PIDController getYController() {
        return yController;
    }
    
    public PIDController getThetaController() {
        return thetaController;
    }


    @Override
    public void periodic() {
        SwerveModuleState[] states = driveKinematics.toSwerveModuleStates(chassisSpeeds);
        setModuleStates(states);
        driveOdometry = new SwerveDriveOdometry(driveKinematics, getGyroscopeRotation(),
        new SwerveModulePosition[]{ frontLeftModule.getPosition(), frontRightModule.getPosition(), backLeftModule.getPosition(), backRightModule.getPosition() }
        );
    }

    public static Drivetrain getInstance() {
        if (drivetrain == null) {
            drivetrain = new Drivetrain();
        }
        return drivetrain;
    }
}
