/*
 * Copyright (c) 2015, FRC3161.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ca.team3161.lib.robot.pid;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.SpeedController;

/**
 * A SpeedController implementation which treats its input and output values as proportions of PID velocity targets,
 * using an Encoder to measure the rotational rate of the associated SpeedController (ex Talon, Victor, Jaguar).
 * Intended usage is to call {@link ca.team3161.lib.robot.pid.VelocityController#set(double)} whenever the target
 * value changes OR a PID loop iteration is desired. {@link ca.team3161.lib.robot.pid.VelocityController#pid(float)}
 * will only return the adjusted value for the next PID iteration; this value represents an actual motor output value,
 * but is not automatically applied to the backing SpeedController instance.
 */
public class VelocityController extends SimplePID implements SpeedController {

    protected final SpeedController speedController;
    protected float maxRotationalRate = 0;
    protected float target = 0;

    /**
     * Construct a new VelocityController instance.
     * @param speedController a backing SpeedController (ex physical Jaguar, Talon, Victor).
     * @param encoder an Encoder which measures the output of the associated physical SpeedController.
     * @param maxRotationalRate the maximum rotational rate as reported by the Encoder.
     * @param kP the Proportional PID constant.
     * @param kI the Integral PID constant.
     * @param kD the Derivative PID constant.
     */
    public VelocityController(final SpeedController speedController, final Encoder encoder, final float maxRotationalRate,
                              final float kP, final float kI, final float kD) {
        this(speedController, new EncoderRatePidSrc(encoder), maxRotationalRate, kP, kI, kD);
    }

    /**
     * Construct a new VelocityController instance.
     * @param speedController a backing SpeedController (ex physical Jaguar, Talon, Victor).
     * @param encoderPidSrc an EncoderPidSrc which measures the output of the associated physical SpeedController.
     * @param maxRotationalRate the maximum rotational rate as reported by the Encoder.
     * @param kP the Proportional PID constant.
     * @param kI the Integral PID constant.
     * @param kD the Derivative PID constant.
     */
    public VelocityController(final SpeedController speedController, final EncoderRatePidSrc encoderPidSrc, final float maxRotationalRate,
                              final float kP, final float kI, final float kD) {
        super(encoderPidSrc, -1, -1, null, kP, kI, kD);
        this.maxRotationalRate = maxRotationalRate;
        this.speedController = speedController;
    }

    /**
     * Get the target rotational rate proportion which this VelocityController is set to.
     * @return the proportional rotational rate target.
     */
    @Override
    public double get() {
        return target;
    }

    /**
     * Set the target rotational rate of this VelocityController.
     * This method should be called very frequently, as the PID loop only iterates when this method
     * or {@link ca.team3161.lib.robot.pid.VelocityController#set(double)} is called.
     * @param v target value.
     * @param b syncgroup.
     */
    @Override
    public void set(final double v, final byte b) {
        this.target = (float) v;
        speedController.set(pid(target * maxRotationalRate), b);
    }

    /**
     * Set the target rotational rate of this VelocityController.
     * This method should be called very frequently, as the PID loop only iterates when this method
     * or {@link ca.team3161.lib.robot.pid.VelocityController#set(double, byte)} is called.
     * @param v target value.
     */
    @Override
    public void set(final double v) {
        this.target = (float) v;
        speedController.set(pid(target * maxRotationalRate));
    }

    /**
     * Disable this VelocityController.
     */
    @Override
    public void disable() {
        speedController.disable();
    }

    /**
     * Used with WPILib PIDControllers.
     * @param v pid value.
     */
    @Override
    public void pidWrite(final double v) {
        speedController.pidWrite(v);
    }
}
