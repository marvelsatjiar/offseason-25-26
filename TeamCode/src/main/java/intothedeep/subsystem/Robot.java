package intothedeep.subsystem;

import static intothedeep.subsystem.Common.mTelemetry;
import static intothedeep.subsystem.Common.robot;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import intothedeep.sensor.vision.LimelightEx;
import intothedeep.subsystem.enhancement.AutoWallPickUp;
import intothedeep.util.ActionScheduler;
import intothedeep.util.BulkReader;
import intothedeep.util.LoopUtil;
import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;

@Config
public final class Robot {
    public final Follower drivetrain;
    public final Extendo extendo;
    public final Intake intake;
    public final BulkReader bulkReader;
    public final Claw claw;
    public final Lift lift;
    public final Arm arm;
    public final ActionScheduler actionScheduler;
    public final Sweeper sweeper;
    public final Hang hang;
//    public final AutoAligner autoAligner;
    public final LimelightEx limelightEx;

    public enum State {
        NEUTRAL,
        EXTENDO_OUT,
        SETUP_SPECIMEN,
        TO_BE_TRANSFERRED,
        TRANSFERRED,
        SETUP_SCORE_BASKET,
        SETUP_HANG,
        DO_HANG,
        SCORED_BASKET,
        SETUP_DROP_SAMPLE,
        WALL_PICKUP
    }

    State currentState = State.NEUTRAL;

    /**
     * Constructor used in teleOp classes that makes the current pose2d, 0
     * @param hardwareMap A constant map that holds all the parts for config in code
     */
    public Robot(HardwareMap hardwareMap) {
        this(hardwareMap, false);
    }

    /**
     * Constructor for instantiating a new 'robot' class
     * @param hardwareMap: A constant map that holds all the parts for config in code
     */
    public Robot(HardwareMap hardwareMap, boolean isAuto) {
        Limelight3A limelight3A = hardwareMap.get(Limelight3A.class, "limelight");

        drivetrain = new Follower(hardwareMap, FConstants.class, LConstants.class);
        extendo = new Extendo(hardwareMap);
        bulkReader = new BulkReader(hardwareMap);
        intake = new Intake(hardwareMap);
        claw = new Claw(hardwareMap);
        lift = new Lift(hardwareMap);
        arm = new Arm(hardwareMap);
        sweeper = new Sweeper(hardwareMap);
        hang = new Hang(hardwareMap);
//        autoAligner = new AutoAligner(hardwareMap);
        limelightEx = new LimelightEx(limelight3A, hardwareMap);
//        autoWallPickUp = new AutoWallPickUp(limelightEx);
        actionScheduler = new ActionScheduler();

        limelight3A.stop();
        limelight3A.start();
    }

    // Reads all the necessary sensors (including battery volt.) in one bulk read
    public void readSensors() {
        bulkReader.bulkRead();
    }

    // Runs all the necessary mechanisms
    public void run() {

        actionScheduler.run();
        update();
    }

    public void update(){
        extendo.run(intake.getTargetV4BAngle().isV4BUnsafe());
        sweeper.run();
        intake.run(extendo.getTargetAngle());
        lift.run();
        claw.run();
        hang.run();
        arm.run();
    }

    // Prints data on the driver hub for debugging and other uses
    public void printTelemetry() {
        mTelemetry.addData("Robot State", robot.currentState.name());
        mTelemetry.addData("Loop time (hertz)", LoopUtil.getLoopTimeInHertz());

        mTelemetry.addData("current x", drivetrain.poseUpdater.getPose().getX());
        mTelemetry.addData("current y", drivetrain.poseUpdater.getPose().getY());
        mTelemetry.addData("current Heading", drivetrain.poseUpdater.getPose().getHeading());

        intake.printTelemetry();
        extendo.printTelemetry();
        lift.printTelemetry();

//        arm.printTelemetry();
//        intake.printTelemetry();
//        autoAligner.printTelemetry();
//        autoWallPickUp.printTelemetry();
        mTelemetry.update();
    }

    public State getCurrentState() {
        return currentState;
    }
    public void setCurrentState(State state) {
        this.currentState = state;
    }
}
