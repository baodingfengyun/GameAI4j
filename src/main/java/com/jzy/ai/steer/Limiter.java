package com.jzy.ai.steer;

/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


/**
 * A {@code Limiter} provides the maximum magnitudes of speed and acceleration for both linear and angular components.
 *
 * @author davebaol
 */
public interface Limiter {

    /**
     * Returns the threshold below which the linear speed can be considered zero. It must be a small positive value near to zero.
     * Usually it is used to avoid updating the orientation when the velocity vector has a negligible length.
     */
    public float getZeroLinearSpeedThreshold();

    /**
     * Sets the threshold below which the linear speed can be considered zero. It must be a small positive value near to zero.
     * Usually it is used to avoid updating the orientation when the velocity vector has a negligible length.
     */
    public void setZeroLinearSpeedThreshold(float value);

    /**
     * Returns the maximum linear speed.
     */
    public float getMaxLinearSpeed();

    /**
     * Sets the maximum linear speed.
     */
    public void setMaxLinearSpeed(float maxLinearSpeed);

    /**
     * Returns the maximum linear acceleration.
     */
    public float getMaxLinearAcceleration();

    /**
     * Sets the maximum linear acceleration.
     */
    public void setMaxLinearAcceleration(float maxLinearAcceleration);

    /**
     * Returns the maximum angular speed.
     */
    public float getMaxAngularSpeed();

    /**
     * Sets the maximum angular speed.
     */
    public void setMaxAngularSpeed(float maxAngularSpeed);

    /**
     * Returns the maximum angular acceleration.
     */
    public float getMaxAngularAcceleration();

    /**
     * Sets the maximum angular acceleration.
     */
    public void setMaxAngularAcceleration(float maxAngularAcceleration);
}

