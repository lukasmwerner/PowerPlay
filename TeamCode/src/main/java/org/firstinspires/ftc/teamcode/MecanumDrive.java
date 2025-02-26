package org.firstinspires.ftc.teamcode;

/* 2021-2022 FTC Robotics Freight-Frenzy
 * (c) 2021-2022 La Salle Robotics
 * Developed for the Freight Frenzy competition
 * Written By Lukas Werner ('22)
 */

import android.hardware.Sensor;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorREV2mDistance;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class MecanumDrive {

    // drive motors have 384.5 points per rotation at the output shaft according to Gobuilda

    public double speed = 1;
    private  double oldSpeed = 1;

    private static final double FRICTION_COEF = 1.75;

    static final double TICKS_PER_INCH = 40.88721;

    final double ticksPerDegree = 718 / 90;

    //front left
    private double flP = 0;

    //back left
    private double blP = 0;

    //front right
    private double frP = 0;

    //back right
    private double brP = 0;

    public final DcMotor leftFront;
    public final DcMotor rightFront;
    public final DcMotor leftBack;
    public final DcMotor rightBack;

    private ElapsedTime runtime;


    public MecanumDrive(HardwareMap hardwareMap, ElapsedTime runtime) {
        this.leftFront = hardwareMap.get(DcMotor.class, "fL");
        this.rightFront = hardwareMap.get(DcMotor.class, "fR");
        this.leftBack = hardwareMap.get(DcMotor.class, "bL");
        this.rightBack = hardwareMap.get(DcMotor.class, "bR");

        this.leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        this.leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        this.rightFront.setDirection(DcMotorSimple.Direction.FORWARD);
        this.rightBack.setDirection(DcMotorSimple.Direction.FORWARD);

        this.leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        this.leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        this.runtime = runtime;

    }

    public double magnitude(double x, double y) {
        return -Math.hypot(x, y);
    }

    public double angle(double x, double y) {
        return Math.atan2(y, x);
    }

    public void sleep(double sleepTime) {
        double time = runtime.time();
        double initTime = time;

        while (time <= initTime + sleepTime) {
            time = runtime.time();
        }
    }
    public MecanumDrive calculateDirectionsRobotCentric(double x, double y, double turn) {

        double phi = (angle(x,y));

        this.blP =
                magnitude(x,y)
                        * Math.sin( phi + (Math.PI / 4))
                        + turn; // flP
        this.flP =
                magnitude(x,y)
                        * Math.sin(phi - (Math.PI / 4))
                        + turn; // blP

        this.brP =
                magnitude(x,y)
                        * Math.sin(phi + (Math.PI / 4))
                        - turn; // frP
        this.frP =
                magnitude(x,y)
                        * Math.sin(phi - (Math.PI / 4))
                        - turn; // brP

        return this;
    }
    public MecanumDrive calculateDirectionsFieldCentric(double x, double y, double turn, double heading) {

        double phi = (angle(x,y)-heading);

        this.blP =
                magnitude(x,y)
                        * Math.sin( phi + (Math.PI / 4))
                        + turn; // flP
        this.flP =
                magnitude(x,y)
                        * Math.sin(phi - (Math.PI / 4))
                        + turn; // blP

        this.brP =
                magnitude(x,y)
                        * Math.sin(phi + (Math.PI / 4))
                        - turn; // frP
        this.frP =
                magnitude(x,y)
                        * Math.sin(phi - (Math.PI / 4))
                        - turn; // brP

        return this;
    }

    public MecanumDrive calculateDirections(double x, double y, double turn) {
        calculateDirectionsRobotCentric(x,y,turn);
        return this;
    }

    public MecanumDrive off() {
        flP = 0;
        blP = 0;
        frP = 0;
        brP = 0;
        leftFront.setPower(0);
        rightFront.setPower(0);
        leftBack.setPower(0);
        rightBack.setPower(0);
        return this;
    }

    public MecanumDrive applyPower() {
        leftFront.setPower(flP * speed);
        rightFront.setPower(frP * speed);
        leftBack.setPower(blP * speed);
        rightBack.setPower(brP * speed);
        return this;
    }

    public MecanumDrive goFor(double seconds) {
        applyPower();
        sleep(seconds);
        off();
        return this;
    }

    public MecanumDrive goDist(double runningDistance) {



        // clip the powers to -1 and 1
        int flD = 1; if (flP < 0) {flD=-1;}
        int frD = 1; if (frP < 0) {frD=-1;}
        int blD = 1; if (blP < 0) {blD=-1;}
        int brD = 1; if (brP < 0) {brD=-1;}


        this.runToPosition((int) (flD * runningDistance * TICKS_PER_INCH),
                (int) (frD * runningDistance * TICKS_PER_INCH),
                (int) (blD * runningDistance * TICKS_PER_INCH),
                (int) (brD * runningDistance * TICKS_PER_INCH));

        return this;
    }

    public MecanumDrive interruptableGoDist(double runningDistance, Rev2mDistanceSensor sensor) {



        // clip the powers to -1 and 1
        int flD = 1; if (flP < 0) {flD=-1;}
        int frD = 1; if (frP < 0) {frD=-1;}
        int blD = 1; if (blP < 0) {blD=-1;}
        int brD = 1; if (brP < 0) {brD=-1;}


        this.interruptableGoTarget((int) (flD * runningDistance * TICKS_PER_INCH),
                (int) (frD * runningDistance * TICKS_PER_INCH),
                (int) (blD * runningDistance * TICKS_PER_INCH),
                (int) (brD * runningDistance * TICKS_PER_INCH),
                sensor);

        return this;
    }

    public MecanumDrive interruptableGoTarget(int LF, int RF, int LB, int RB, Rev2mDistanceSensor sensor) {

        this.leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        this.leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        double p = .1;
        this.leftFront.setPower(p);
        this.rightFront.setPower(p);
        this.leftBack.setPower(p);
        this.rightBack.setPower(p);

        this.leftFront.setTargetPosition(LF);
        this.rightFront.setTargetPosition(RF);
        this.leftBack.setTargetPosition(LB);
        this.rightBack.setTargetPosition(RB);

        this.leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        this.rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        this.rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        this.leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);


        //while (leftFront.isBusy() || rightFront.isBusy() || leftBack.isBusy() || rightBack.isBusy()) {}
        while (true) {
            if (sensor.getDistance(DistanceUnit.CM) < 10) {

                int lb = leftBack.getCurrentPosition();
                int rb = rightBack.getCurrentPosition();
                int lf = leftFront.getCurrentPosition();
                int rf = rightFront.getCurrentPosition();



                leftBack.setTargetPosition(lb);
                rightBack.setTargetPosition(rb);
                leftFront.setTargetPosition(lf);
                rightFront.setTargetPosition(rf);

                while (true) {
                    if (Math.abs(leftFront.getTargetPosition()  - leftFront.getCurrentPosition()) < 10)  { break; }
                    if (Math.abs(rightFront.getTargetPosition() - rightFront.getCurrentPosition()) < 10) { break; }
                    if (Math.abs(leftBack.getTargetPosition()   - leftBack.getCurrentPosition()) < 10)   { break; }
                    if (Math.abs(rightBack.getTargetPosition()  - rightBack.getCurrentPosition()) < 10)  { break; }
                }
                break;
            }
            if (Math.abs(leftFront.getTargetPosition()  - leftFront.getCurrentPosition()) < 10)  { break; }
            if (Math.abs(rightFront.getTargetPosition() - rightFront.getCurrentPosition()) < 10) { break; }
            if (Math.abs(leftBack.getTargetPosition()   - leftBack.getCurrentPosition()) < 10)   { break; }
            if (Math.abs(rightBack.getTargetPosition()  - rightBack.getCurrentPosition()) < 10)  { break; }
        }

        this.leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        this.leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        this.rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        this.leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        this.rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);


        this.off();
        return this;
    }

    // support the old API style
    public MecanumDrive runFor(double seconds) {return goFor(seconds);}
    public MecanumDrive runDist(double d) {return goDist(d);}

    public MecanumDrive forward() {
        calculateDirections(0, -1, 0);
        applyPower();
        return this;
    }

    public MecanumDrive backward() {
        calculateDirections(0, 1, 0);
        applyPower();
        return this;
    }

    public MecanumDrive left() {
        calculateDirections(-1, 0, 0);
        applyPower();
        return this;
    }

    public MecanumDrive right() {
        calculateDirections(1, 0, 0);
        applyPower();
        return this;
    }

    public MecanumDrive backwardsLeft() {
        calculateDirections(-1, -1, 0);
        applyPower();
        return this;
    }

    public MecanumDrive backwardsRight() {
        calculateDirections(1, -1, 0);
        applyPower();
        return this;
    }

    public MecanumDrive forwardsLeft() {
        calculateDirections(-1, 1, 0);
        applyPower();
        return this;
    }

    public MecanumDrive forwardsRight() {
        calculateDirections(1, 1, 0);
        applyPower();
        return this;
    }

    public MecanumDrive rotateLeft() {
        calculateDirections(0, 0, -1);
        applyPower();
        return this;
    }

    public MecanumDrive rotateRight() {
        calculateDirections(0, 0, 1);
        applyPower();
        return this;
    }


    public MecanumDrive startSlowMode(double speed) {
        this.oldSpeed = this.speed;
        this.speed = speed;
        return this;
    }

    public MecanumDrive endSlowMode() {
        this.speed = this.oldSpeed;
        return this;
    }

    public void rotateRightEncoder(int degree) {
        this.runToPosition((int) (degree * ticksPerDegree), -(int) (degree * ticksPerDegree), (int) (degree * ticksPerDegree), -(int) (degree * ticksPerDegree));
    }

    public void rotateLeftEncoder(int degree) {
        this.runToPosition(-(int) (degree * ticksPerDegree), (int) (degree * ticksPerDegree), -(int) (degree * ticksPerDegree), (int) (degree * ticksPerDegree));
    }

    public MecanumDrive runToPosition(int LF, int RF, int LB, int RB) {
        this.leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        double p = .25;
        this.leftFront.setPower(p);
        this.rightFront.setPower(p);
        this.leftBack.setPower(p);
        this.rightBack.setPower(p);

        this.leftFront.setTargetPosition(LF);
        this.rightFront.setTargetPosition(RF);
        this.leftBack.setTargetPosition(LB);
        this.rightBack.setTargetPosition(RB);

        this.leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        this.rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        this.rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        this.leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        //while (leftFront.isBusy() || rightFront.isBusy() || leftBack.isBusy() || rightBack.isBusy()) {}
        while (true) {
            if (Math.abs(leftFront.getTargetPosition()-leftFront.getCurrentPosition()) < 10) {break;}
            if (Math.abs(rightFront.getTargetPosition()-rightFront.getCurrentPosition()) < 10) {break;}
            if (Math.abs(leftBack.getTargetPosition()-leftBack.getCurrentPosition()) < 10) {break;}
            if (Math.abs(rightBack.getTargetPosition()-rightBack.getCurrentPosition()) < 10) {break;}
        }

        this.leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        this.off();
        return this;
    }
}

