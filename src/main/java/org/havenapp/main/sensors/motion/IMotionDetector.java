/*
 * Copyright (c) 2013-2015 Marco Ziccardi, Luca Bonato
 * Licensed under the MIT license.
 */


package org.havenapp.main.sensors.motion;

import java.util.List;

public interface IMotionDetector {
	
	/**
	 * Detects differences between old and new image
	 * and return pixel indexes that differ more than 
	 * a specified threshold
     */
    List<Integer> detectMotion(int[] oldImage, int[] newImage, int width, int height);

	/**
	 * Sets the sensitivity
     */
    void setThreshold(int thresh);
}
