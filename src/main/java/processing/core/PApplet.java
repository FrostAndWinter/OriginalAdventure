/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012-15 The Processing Foundation
  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 2.1.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

package processing.core;

// used by link()

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

// used by loadImage() functions
// allows us to remove our own MediaTracker code
// used by selectInput(), selectOutput(), selectFolder()
// used to present the fullScreen() warning about Spaces on OS X
// used by desktopFile() method
// loadXML() error handling


/**
 * Base class for all sketches that use processing.core.
 * <p/>
 * The <A HREF="https://github.com/processing/processing/wiki/Window-Size-and-Full-Screen">
 * Window Size and Full Screen</A> page on the Wiki has useful information
 * about sizing, multiple displays, full screen, etc.
 * <p/>
 * Processing uses active mode rendering. All animation tasks happen on the
 * "Processing Animation Thread". The setup() and draw() methods are handled
 * by that thread, and events (like mouse movement and key presses, which are
 * fired by the event dispatch thread or EDT) are queued to be safely handled
 * at the end of draw().
 * <p/>
 * Starting with 3.0a6, blit operations are on the EDT, so as not to cause
 * GUI problems with Swing and AWT. In the case of the default renderer, the
 * sketch renders to an offscreen image, then the EDT is asked to bring that
 * image to the screen.
 * <p/>
 * For code that needs to run on the EDT, use EventQueue.invokeLater(). When
 * doing so, be careful to synchronize between that code and the Processing
 * animation thread. That is, you can't call Processing methods from the EDT
 * or at any random time from another thread. Use of a callback function or
 * the registerXxx() methods in PApplet can help ensure that your code doesn't
 * do something naughty.
 * <p/>
 * As of Processing 3.0, we have removed Applet as the base class for PApplet.
 * This means that we can remove lots of legacy code, however one downside is
 * that it's no longer possible (without extra code) to embed a PApplet into
 * another Java application.
 * <p/>
 * As of Processing 3.0, we have discontinued support for versions of Java
 * prior to 1.8. We don't have enough people to support it, and for a
 * project of our (tiny) size, we should be focusing on the future, rather
 * than working around legacy Java code.
 */
public class PApplet implements PConstants {
  /** Full name of the Java version (i.e. 1.5.0_11). */
  static public final String javaVersionName =
    System.getProperty("java.version");

//  /** Short name of Java version, i.e. 1.8. */
//  static public final String javaVersionShort =
//    //javaVersionName.substring(0, 3);
//    javaVersionName.substring(0, javaVersionName.indexOf(".", 2));
//    // can't use this one, it's 1.8.0 and breaks things
//    //javaVersionName.substring(0, javaVersionName.indexOf("_"));

  static public final int javaPlatform =
    PApplet.parseInt(PApplet.split(javaVersionName, '.')[1]);
//  static {
//    try {
//      javaPlatform = PApplet.split(javaVersionName, '.')[1];
//    } catch (Exception e) {
//      javaPlatform = "8";  // set a default in case
//    }
//  }

  /**
   * Version of Java that's in use, whether 1.1 or 1.3 or whatever,
   * stored as a float.
   * <p>
   * Note that because this is stored as a float, the values may not be
   * <EM>exactly</EM> 1.3 or 1.4. The PDE will make 1.8 or whatever into
   * a float automatically, so outside the PDE, make sure you're comparing
   * against 1.3f or 1.4f, which will have the same amount of error
   * (i.e. 1.40000001). This could just be a double, but since Processing
   * only uses floats, it's safer as a float because specifying a double
   * (with this narrow case especially) with the preprocessor is awkward.
   * <p>
   * @deprecated Java 10 is around the corner. Use javaPlatform when you need
   * a number for comparisons, i.e. "if (javaPlatform >= 7)".
   */
  @Deprecated
  public static final float javaVersion =
    new Float(javaVersionName.substring(0, 3));
//  public static final float javaVersion =
//    new Float(javaVersionName.substring(0, javaVersionName.indexOf(".", 2))).floatValue();
//  // Making this a String in 3.0, in anticipation of Java 10
//  public static final String javaVersion = "1." + javaPlatform;

  /**
   * Current platform in use, one of the
   * PConstants WINDOWS, MACOSX, MACOS9, LINUX or OTHER.
   */
  static public int platform;

  static {
    String osname = System.getProperty("os.name");

    if (osname.contains("Mac")) {
      platform = MACOSX;

    } else if (osname.contains("Windows")) {
      platform = WINDOWS;

    } else if (osname.equals("Linux")) {  // true for the ibm vm
      platform = LINUX;

    } else {
      platform = OTHER;
    }
  }

  //////////////////////////////////////////////////////////////

  // MATH

  // lots of convenience methods for math with floats.
  // doubles are overkill for processing applets, and casting
  // things all the time is annoying, thus the functions below.

/**
   * ( begin auto-generated from abs.xml )
   *
   * Calculates the absolute value (magnitude) of a number. The absolute
   * value of a number is always positive.
   *
   * ( end auto-generated )
   * @param n number to compute
   */
  static public float abs(float n) {
    return (n < 0) ? -n : n;
  }

  static public int abs(int n) {
    return (n < 0) ? -n : n;
  }

/**
   * ( begin auto-generated from sq.xml )
   *
   * Squares a number (multiplies a number by itself). The result is always a
   * positive number, as multiplying two negative numbers always yields a
   * positive result. For example, -1 * -1 = 1.
   *
   * ( end auto-generated )
   * @param n number to square
   * @see PApplet#sqrt(float)
   */
  static public float sq(float n) {
    return n*n;
  }

/**
   * ( begin auto-generated from sqrt.xml )
   *
   * Calculates the square root of a number. The square root of a number is
   * always positive, even though there may be a valid negative root. The
   * square root <b>s</b> of number <b>a</b> is such that <b>s*s = a</b>. It
   * is the opposite of squaring.
   *
   * ( end auto-generated )
   * @param n non-negative number
   * @see PApplet#pow(float, float)
   * @see PApplet#sq(float)
   */
  static public float sqrt(float n) {
    return (float)Math.sqrt(n);
  }

/**
   * ( begin auto-generated from log.xml )
   *
   * Calculates the natural logarithm (the base-<i>e</i> logarithm) of a
   * number. This function expects the values greater than 0.0.
   *
   * ( end auto-generated )
   * @param n number greater than 0.0
   */
  static public float log(float n) {
    return (float)Math.log(n);
  }

/**
   * ( begin auto-generated from exp.xml )
   *
   * Returns Euler's number <i>e</i> (2.71828...) raised to the power of the
   * <b>value</b> parameter.
   *
   * ( end auto-generated )
   * @param n exponent to raise
   */
  static public float exp(float n) {
    return (float)Math.exp(n);
  }

/**
   * ( begin auto-generated from pow.xml )
   *
   * Facilitates exponential expressions. The <b>pow()</b> function is an
   * efficient way of multiplying numbers by themselves (or their reciprocal)
   * in large quantities. For example, <b>pow(3, 5)</b> is equivalent to the
   * expression 3*3*3*3*3 and <b>pow(3, -5)</b> is equivalent to 1 / 3*3*3*3*3.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param n base of the exponential expression
   * @param e power by which to raise the base
   * @see PApplet#sqrt(float)
   */
  static public float pow(float n, float e) {
    return (float)Math.pow(n, e);
  }

/**
   * ( begin auto-generated from max.xml )
   *
   * Determines the largest value in a sequence of numbers.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param a first number to compare
   * @param b second number to compare
   * @see PApplet#min(float, float, float)
   */
  static public final int max(int a, int b) {
    return (a > b) ? a : b;
  }

  static public float max(float a, float b) {
    return (a > b) ? a : b;
  }

  /*
  static public final double max(double a, double b) {
    return (a > b) ? a : b;
  }
  */

/**
 * @param c third number to compare
 */
  static public final int max(int a, int b, int c) {
    return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
  }


  static public float max(float a, float b, float c) {
    return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
  }

  static final String ERROR_MIN_MAX =
          "Cannot use min() or max() on an empty array.";

  /**
   * @param list array of numbers to compare
   */
  static public final int max(int[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    int max = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] > max) max = list[i];
    }
    return max;
  }

  static public float max(float[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    float max = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] > max) max = list[i];
    }
    return max;
  }


  /**
   * Find the maximum value in an array.
   * Throws an ArrayIndexOutOfBoundsException if the array is length 0.
   * @return The maximum value
   */
  /*
  static public final double max(double[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    double max = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] > max) max = list[i];
    }
    return max;
  }
  */


  static public final int min(int a, int b) {
    return (a < b) ? a : b;
  }

  static public float min(float a, float b) {
    return (a < b) ? a : b;
  }

  /*
  static public final double min(double a, double b) {
    return (a < b) ? a : b;
  }
  */


  static public final int min(int a, int b, int c) {
    return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
  }

/**
   * ( begin auto-generated from min.xml )
   *
   * Determines the smallest value in a sequence of numbers.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param a first number
   * @param b second number
   * @param c third number
   * @see PApplet#max(float, float, float)
   */
  static public float min(float a, float b, float c) {
    return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
  }

  /*
  static public final double min(double a, double b, double c) {
    return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
  }
  */


  /**
   * @param list array of numbers to compare
   */
  static public final int min(int[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    int min = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] < min) min = list[i];
    }
    return min;
  }

  static public float min(float[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    float min = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] < min) min = list[i];
    }
    return min;
  }


  /*
   * Find the minimum value in an array.
   * Throws an ArrayIndexOutOfBoundsException if the array is length 0.
   * @param list the source array
   * @return The minimum value
   */
  /*
  static public final double min(double[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    double min = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] < min) min = list[i];
    }
    return min;
  }
  */


  static public final int constrain(int amt, int low, int high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }

/**
   * ( begin auto-generated from constrain.xml )
   *
   * Constrains a value to not exceed a maximum and minimum value.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param amt the value to constrain
   * @param low minimum limit
   * @param high maximum limit
   * @see PApplet#max(float, float, float)
   * @see PApplet#min(float, float, float)
   */

  static public float constrain(float amt, float low, float high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }

/**
   * ( begin auto-generated from sin.xml )
   *
   * Calculates the sine of an angle. This function expects the values of the
   * <b>angle</b> parameter to be provided in radians (values from 0 to
   * 6.28). Values are returned in the range -1 to 1.
   *
   * ( end auto-generated )
   * @ math:trigonometry
   * @param angle an angle in radians
   * @see PApplet#cos(float)
   * @see PApplet#tan(float)
   * @see PApplet#radians(float)
   */
  static public float sin(float angle) {
    return (float)Math.sin(angle);
  }

/**
   * ( begin auto-generated from cos.xml )
   *
   * Calculates the cosine of an angle. This function expects the values of
   * the <b>angle</b> parameter to be provided in radians (values from 0 to
   * PI*2). Values are returned in the range -1 to 1.
   *
   * ( end auto-generated )
   * @ math:trigonometry
   * @param angle an angle in radians
   * @see PApplet#sin(float)
   * @see PApplet#tan(float)
   * @see PApplet#radians(float)
   */
  static public float cos(float angle) {
    return (float)Math.cos(angle);
  }

/**
   * ( begin auto-generated from tan.xml )
   *
   * Calculates the ratio of the sine and cosine of an angle. This function
   * expects the values of the <b>angle</b> parameter to be provided in
   * radians (values from 0 to PI*2). Values are returned in the range
   * <b>infinity</b> to <b>-infinity</b>.
   *
   * ( end auto-generated )
   * @ math:trigonometry
   * @param angle an angle in radians
   * @see PApplet#cos(float)
   * @see PApplet#sin(float)
   * @see PApplet#radians(float)
   */
  static public float tan(float angle) {
    return (float)Math.tan(angle);
  }

/**
   * ( begin auto-generated from asin.xml )
   *
   * The inverse of <b>sin()</b>, returns the arc sine of a value. This
   * function expects the values in the range of -1 to 1 and values are
   * returned in the range <b>-PI/2</b> to <b>PI/2</b>.
   *
   * ( end auto-generated )
   * @ math:trigonometry
   * @param value the value whose arc sine is to be returned
   * @see PApplet#sin(float)
   * @see PApplet#acos(float)
   * @see PApplet#atan(float)
   */
  static public float asin(float value) {
    return (float)Math.asin(value);
  }

/**
   * ( begin auto-generated from acos.xml )
   *
   * The inverse of <b>cos()</b>, returns the arc cosine of a value. This
   * function expects the values in the range of -1 to 1 and values are
   * returned in the range <b>0</b> to <b>PI (3.1415927)</b>.
   *
   * ( end auto-generated )
   * @ math:trigonometry
   * @param value the value whose arc cosine is to be returned
   * @see PApplet#cos(float)
   * @see PApplet#asin(float)
   * @see PApplet#atan(float)
   */
  static public float acos(float value) {
    return (float)Math.acos(value);
  }

/**
   * ( begin auto-generated from atan.xml )
   *
   * The inverse of <b>tan()</b>, returns the arc tangent of a value. This
   * function expects the values in the range of -Infinity to Infinity
   * (exclusive) and values are returned in the range <b>-PI/2</b> to <b>PI/2 </b>.
   *
   * ( end auto-generated )
   * @ math:trigonometry
   * @param value -Infinity to Infinity (exclusive)
   * @see PApplet#tan(float)
   * @see PApplet#asin(float)
   * @see PApplet#acos(float)
   */
  static public float atan(float value) {
    return (float)Math.atan(value);
  }

/**
   * ( begin auto-generated from atan2.xml )
   *
   * Calculates the angle (in radians) from a specified point to the
   * coordinate origin as measured from the positive x-axis. Values are
   * returned as a <b>float</b> in the range from <b>PI</b> to <b>-PI</b>.
   * The <b>atan2()</b> function is most often used for orienting geometry to
   * the position of the cursor.  Note: The y-coordinate of the point is the
   * first parameter and the x-coordinate is the second due the the structure
   * of calculating the tangent.
   *
   * ( end auto-generated )
   * @ math:trigonometry
   * @param y y-coordinate of the point
   * @param x x-coordinate of the point
   * @see PApplet#tan(float)
   */
  static public float atan2(float y, float x) {
    return (float)Math.atan2(y, x);
  }

/**
   * ( begin auto-generated from degrees.xml )
   *
   * Converts a radian measurement to its corresponding value in degrees.
   * Radians and degrees are two ways of measuring the same thing. There are
   * 360 degrees in a circle and 2*PI radians in a circle. For example,
   * 90&deg; = PI/2 = 1.5707964. All trigonometric functions in Processing
   * require their parameters to be specified in radians.
   *
   * ( end auto-generated )
   * @ math:trigonometry
   * @param radians radian value to convert to degrees
   * @see PApplet#radians(float)
   */
  static public float degrees(float radians) {
    return radians * RAD_TO_DEG;
  }

/**
   * ( begin auto-generated from radians.xml )
   *
   * Converts a degree measurement to its corresponding value in radians.
   * Radians and degrees are two ways of measuring the same thing. There are
   * 360 degrees in a circle and 2*PI radians in a circle. For example,
   * 90&deg; = PI/2 = 1.5707964. All trigonometric functions in Processing
   * require their parameters to be specified in radians.
   *
   * ( end auto-generated )
   * @ math:trigonometry
   * @param degrees degree value to convert to radians
   * @see PApplet#degrees(float)
   */
  static public float radians(float degrees) {
    return degrees * DEG_TO_RAD;
  }

/**
   * ( begin auto-generated from ceil.xml )
   *
   * Calculates the closest int value that is greater than or equal to the
   * value of the parameter. For example, <b>ceil(9.03)</b> returns the value 10.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param n number to round up
   * @see PApplet#floor(float)
   * @see PApplet#round(float)
   */
  static public final int ceil(float n) {
    return (int) Math.ceil(n);
  }

/**
   * ( begin auto-generated from floor.xml )
   *
   * Calculates the closest int value that is less than or equal to the value
   * of the parameter.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param n number to round down
   * @see PApplet#ceil(float)
   * @see PApplet#round(float)
   */
  static public final int floor(float n) {
    return (int) Math.floor(n);
  }

/**
   * ( begin auto-generated from round.xml )
   *
   * Calculates the integer closest to the <b>value</b> parameter. For
   * example, <b>round(9.2)</b> returns the value 9.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param n number to round
   * @see PApplet#floor(float)
   * @see PApplet#ceil(float)
   */
  static public final int round(float n) {
    return Math.round(n);
  }


  static public float mag(float a, float b) {
    return (float)Math.sqrt(a*a + b*b);
  }

/**
   * ( begin auto-generated from mag.xml )
   *
   * Calculates the magnitude (or length) of a vector. A vector is a
   * direction in space commonly used in computer graphics and linear
   * algebra. Because it has no "start" position, the magnitude of a vector
   * can be thought of as the distance from coordinate (0,0) to its (x,y)
   * value. Therefore, mag() is a shortcut for writing "dist(0, 0, x, y)".
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param a first value
   * @param b second value
   * @param c third value
   * @see PApplet#dist(float, float, float, float)
   */
  static public float mag(float a, float b, float c) {
    return (float)Math.sqrt(a*a + b*b + c*c);
  }


  static public float dist(float x1, float y1, float x2, float y2) {
    return sqrt(sq(x2-x1) + sq(y2-y1));
  }

/**
   * ( begin auto-generated from dist.xml )
   *
   * Calculates the distance between two points.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param x1 x-coordinate of the first point
   * @param y1 y-coordinate of the first point
   * @param z1 z-coordinate of the first point
   * @param x2 x-coordinate of the second point
   * @param y2 y-coordinate of the second point
   * @param z2 z-coordinate of the second point
   */
  static public float dist(float x1, float y1, float z1,
                                 float x2, float y2, float z2) {
    return sqrt(sq(x2-x1) + sq(y2-y1) + sq(z2-z1));
  }

/**
   * ( begin auto-generated from lerp.xml )
   *
   * Calculates a number between two numbers at a specific increment. The
   * <b>amt</b> parameter is the amount to interpolate between the two values
   * where 0.0 equal to the first point, 0.1 is very near the first point,
   * 0.5 is half-way in between, etc. The lerp function is convenient for
   * creating motion along a straight path and for drawing dotted lines.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param start first value
   * @param stop second value
   * @param amt float between 0.0 and 1.0
   * @see PGraphics#curvePoint(float, float, float, float, float)
   * @see PGraphics#bezierPoint(float, float, float, float, float)
   * @see PVector#lerp(PVector, float)
   * @see PGraphics#lerpColor(int, int, float)
   */
  static public float lerp(float start, float stop, float amt) {
    return start + (stop-start) * amt;
  }

  /**
   * ( begin auto-generated from norm.xml )
   *
   * Normalizes a number from another range into a value between 0 and 1.
   * <br/> <br/>
   * Identical to map(value, low, high, 0, 1);
   * <br/> <br/>
   * Numbers outside the range are not clamped to 0 and 1, because
   * out-of-range values are often intentional and useful.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param value the incoming value to be converted
   * @param start lower bound of the value's current range
   * @param stop upper bound of the value's current range
   * @see PApplet#map(float, float, float, float, float)
   * @see PApplet#lerp(float, float, float)
   */
  static public float norm(float value, float start, float stop) {
    return (value - start) / (stop - start);
  }

  /**
   * ( begin auto-generated from map.xml )
   *
   * Re-maps a number from one range to another. In the example above,
   * the number '25' is converted from a value in the range 0..100 into
   * a value that ranges from the left edge (0) to the right edge (width)
   * of the screen.
   * <br/> <br/>
   * Numbers outside the range are not clamped to 0 and 1, because
   * out-of-range values are often intentional and useful.
   *
   * ( end auto-generated )
   * @ math:calculation
   * @param value the incoming value to be converted
   * @param start1 lower bound of the value's current range
   * @param stop1 upper bound of the value's current range
   * @param start2 lower bound of the value's target range
   * @param stop2 upper bound of the value's target range
   * @see PApplet#norm(float, float, float)
   * @see PApplet#lerp(float, float, float)
   */
  static public float map(float value,
                                float start1, float stop1,
                                float start2, float stop2) {
    float outgoing =
      start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    String badness = null;
    if (outgoing != outgoing) {
      badness = "NaN (not a number)";

    } else if (outgoing == Float.NEGATIVE_INFINITY ||
               outgoing == Float.POSITIVE_INFINITY) {
      badness = "infinity";
    }
    if (badness != null) {
      final String msg =
        String.format("map(%s, %s, %s, %s, %s) called, which returns %s",
                      nf(value), nf(start1), nf(stop1),
                      nf(start2), nf(stop2), badness);
      PGraphics.showWarning(msg);
    }
    return outgoing;
  }


  /*
  static public final double map(double value,
                                 double istart, double istop,
                                 double ostart, double ostop) {
    return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
  }
  */



  //////////////////////////////////////////////////////////////

  // RANDOM NUMBERS


  Random internalRandom;

  /**
   *
   */
  public final float random(float high) {
    // avoid an infinite loop when 0 or NaN are passed in
    if (high == 0 || high != high) {
      return 0;
    }

    if (internalRandom == null) {
      internalRandom = new Random();
    }

    // for some reason (rounding error?) Math.random() * 3
    // can sometimes return '3' (once in ~30 million tries)
    // so a check was added to avoid the inclusion of 'howbig'
    float value = 0;
    do {
      value = internalRandom.nextFloat() * high;
    } while (value == high);
    return value;
  }

  /**
   * ( begin auto-generated from randomGaussian.xml )
   *
   * Returns a float from a random series of numbers having a mean of 0
   * and standard deviation of 1. Each time the <b>randomGaussian()</b>
   * function is called, it returns a number fitting a Gaussian, or
   * normal, distribution. There is theoretically no minimum or maximum
   * value that <b>randomGaussian()</b> might return. Rather, there is
   * just a very low probability that values far from the mean will be
   * returned; and a higher probability that numbers near the mean will
   * be returned.
   *
   * ( end auto-generated )
   * @ math:random
   * @see PApplet#random(float,float)
   * @see PApplet#noise(float, float, float)
   */
  public final float randomGaussian() {
    if (internalRandom == null) {
      internalRandom = new Random();
    }
    return (float) internalRandom.nextGaussian();
  }


  /**
   * ( begin auto-generated from random.xml )
   *
   * Generates random numbers. Each time the <b>random()</b> function is
   * called, it returns an unexpected value within the specified range. If
   * one parameter is passed to the function it will return a <b>float</b>
   * between zero and the value of the <b>high</b> parameter. The function
   * call <b>random(5)</b> returns values between 0 and 5 (starting at zero,
   * up to but not including 5). If two parameters are passed, it will return
   * a <b>float</b> with a value between the the parameters. The function
   * call <b>random(-5, 10.2)</b> returns values starting at -5 up to (but
   * not including) 10.2. To convert a floating-point random number to an
   * integer, use the <b>int()</b> function.
   *
   * ( end auto-generated )
   * @ math:random
   * @param low lower limit
   * @param high upper limit
   * @see PApplet#randomSeed(long)
   * @see PApplet#noise(float, float, float)
   */
  public final float random(float low, float high) {
    if (low >= high) return low;
    float diff = high - low;
    return random(diff) + low;
  }


 /**
   * ( begin auto-generated from randomSeed.xml )
   *
   * Sets the seed value for <b>random()</b>. By default, <b>random()</b>
   * produces different results each time the program is run. Set the
   * <b>value</b> parameter to a constant to return the same pseudo-random
   * numbers each time the software is run.
   *
   * ( end auto-generated )
   * @ math:random
   * @param seed seed value
   * @see PApplet#random(float,float)
   * @see PApplet#noise(float, float, float)
   * @see PApplet#noiseSeed(long)
   */
  public final void randomSeed(long seed) {
    if (internalRandom == null) {
      internalRandom = new Random();
    }
    internalRandom.setSeed(seed);
  }



  //////////////////////////////////////////////////////////////

  // PERLIN NOISE

  // [toxi 040903]
  // octaves and amplitude amount per octave are now user controlled
  // via the noiseDetail() function.

  // [toxi 030902]
  // cleaned up code and now using bagel's cosine table to speed up

  // [toxi 030901]
  // implementation by the german demo group farbrausch
  // as used in their demo "art": http://www.farb-rausch.de/fr010src.zip

  static final int PERLIN_YWRAPB = 4;
  static final int PERLIN_YWRAP = 1<<PERLIN_YWRAPB;
  static final int PERLIN_ZWRAPB = 8;
  static final int PERLIN_ZWRAP = 1<<PERLIN_ZWRAPB;
  static final int PERLIN_SIZE = 4095;

  int perlin_octaves = 4; // default to medium smooth
  float perlin_amp_falloff = 0.5f; // 50% reduction/octave

  // [toxi 031112]
  // new vars needed due to recent change of cos table in PGraphics
  int perlin_TWOPI, perlin_PI;
  float[] perlin_cosTable;
  float[] perlin;

  Random perlinRandom;


  /**
   */
  public float noise(float x) {
    // is this legit? it's a dumb way to do it (but repair it later)
    return noise(x, 0f, 0f);
  }

  /**
   */
  public float noise(float x, float y) {
    return noise(x, y, 0f);
  }

  /**
   * ( begin auto-generated from noise.xml )
   *
   * Returns the Perlin noise value at specified coordinates. Perlin noise is
   * a random sequence generator producing a more natural ordered, harmonic
   * succession of numbers compared to the standard <b>random()</b> function.
   * It was invented by Ken Perlin in the 1980s and been used since in
   * graphical applications to produce procedural textures, natural motion,
   * shapes, terrains etc.<br /><br /> The main difference to the
   * <b>random()</b> function is that Perlin noise is defined in an infinite
   * n-dimensional space where each pair of coordinates corresponds to a
   * fixed semi-random value (fixed only for the lifespan of the program).
   * The resulting value will always be between 0.0 and 1.0. Processing can
   * compute 1D, 2D and 3D noise, depending on the number of coordinates
   * given. The noise value can be animated by moving through the noise space
   * as demonstrated in the example above. The 2nd and 3rd dimension can also
   * be interpreted as time.<br /><br />The actual noise is structured
   * similar to an audio signal, in respect to the function's use of
   * frequencies. Similar to the concept of harmonics in physics, perlin
   * noise is computed over several octaves which are added together for the
   * final result. <br /><br />Another way to adjust the character of the
   * resulting sequence is the scale of the input coordinates. As the
   * function works within an infinite space the value of the coordinates
   * doesn't matter as such, only the distance between successive coordinates
   * does (eg. when using <b>noise()</b> within a loop). As a general rule
   * the smaller the difference between coordinates, the smoother the
   * resulting noise sequence will be. Steps of 0.005-0.03 work best for most
   * applications, but this will differ depending on use.
   *
   * ( end auto-generated )
   *
   * @ math:random
   * @param x x-coordinate in noise space
   * @param y y-coordinate in noise space
   * @param z z-coordinate in noise space
   * @see PApplet#noiseSeed(long)
   * @see PApplet#noiseDetail(int, float)
   * @see PApplet#random(float,float)
   */
  public float noise(float x, float y, float z) {
    if (perlin == null) {
      if (perlinRandom == null) {
        perlinRandom = new Random();
      }
      perlin = new float[PERLIN_SIZE + 1];
      for (int i = 0; i < PERLIN_SIZE + 1; i++) {
        perlin[i] = perlinRandom.nextFloat(); //(float)Math.random();
      }
      // [toxi 031112]
      // noise broke due to recent change of cos table in PGraphics
      // this will take care of it
      perlin_cosTable = PGraphics.cosLUT;
      perlin_TWOPI = perlin_PI = PGraphics.SINCOS_LENGTH;
      perlin_PI >>= 1;
    }

    if (x<0) x=-x;
    if (y<0) y=-y;
    if (z<0) z=-z;

    int xi=(int)x, yi=(int)y, zi=(int)z;
    float xf = x - xi;
    float yf = y - yi;
    float zf = z - zi;
    float rxf, ryf;

    float r=0;
    float ampl=0.5f;

    float n1,n2,n3;

    for (int i=0; i<perlin_octaves; i++) {
      int of=xi+(yi<<PERLIN_YWRAPB)+(zi<<PERLIN_ZWRAPB);

      rxf=noise_fsc(xf);
      ryf=noise_fsc(yf);

      n1  = perlin[of&PERLIN_SIZE];
      n1 += rxf*(perlin[(of+1)&PERLIN_SIZE]-n1);
      n2  = perlin[(of+PERLIN_YWRAP)&PERLIN_SIZE];
      n2 += rxf*(perlin[(of+PERLIN_YWRAP+1)&PERLIN_SIZE]-n2);
      n1 += ryf*(n2-n1);

      of += PERLIN_ZWRAP;
      n2  = perlin[of&PERLIN_SIZE];
      n2 += rxf*(perlin[(of+1)&PERLIN_SIZE]-n2);
      n3  = perlin[(of+PERLIN_YWRAP)&PERLIN_SIZE];
      n3 += rxf*(perlin[(of+PERLIN_YWRAP+1)&PERLIN_SIZE]-n3);
      n2 += ryf*(n3-n2);

      n1 += noise_fsc(zf)*(n2-n1);

      r += n1*ampl;
      ampl *= perlin_amp_falloff;
      xi<<=1; xf*=2;
      yi<<=1; yf*=2;
      zi<<=1; zf*=2;

      if (xf>=1.0f) { xi++; xf--; }
      if (yf>=1.0f) { yi++; yf--; }
      if (zf>=1.0f) { zi++; zf--; }
    }
    return r;
  }

  // [toxi 031112]
  // now adjusts to the size of the cosLUT used via
  // the new variables, defined above
  private float noise_fsc(float i) {
    // using bagel's cosine table instead
    return 0.5f*(1.0f-perlin_cosTable[(int)(i*perlin_PI)%perlin_TWOPI]);
  }

  // [toxi 040903]
  // make perlin noise quality user controlled to allow
  // for different levels of detail. lower values will produce
  // smoother results as higher octaves are surpressed

  /**
   * ( begin auto-generated from noiseDetail.xml )
   *
   * Adjusts the character and level of detail produced by the Perlin noise
   * function. Similar to harmonics in physics, noise is computed over
   * several octaves. Lower octaves contribute more to the output signal and
   * as such define the overal intensity of the noise, whereas higher octaves
   * create finer grained details in the noise sequence. By default, noise is
   * computed over 4 octaves with each octave contributing exactly half than
   * its predecessor, starting at 50% strength for the 1st octave. This
   * falloff amount can be changed by adding an additional function
   * parameter. Eg. a falloff factor of 0.75 means each octave will now have
   * 75% impact (25% less) of the previous lower octave. Any value between
   * 0.0 and 1.0 is valid, however note that values greater than 0.5 might
   * result in greater than 1.0 values returned by <b>noise()</b>.<br /><br
   * />By changing these parameters, the signal created by the <b>noise()</b>
   * function can be adapted to fit very specific needs and characteristics.
   *
   * ( end auto-generated )
   * @ math:random
   * @param lod number of octaves to be used by the noise
   * @see PApplet#noise(float, float, float)
   */
  public void noiseDetail(int lod) {
    if (lod>0) perlin_octaves=lod;
  }

  /**
   * @param falloff falloff factor for each octave
   */
  public void noiseDetail(int lod, float falloff) {
    if (lod>0) perlin_octaves=lod;
    if (falloff>0) perlin_amp_falloff=falloff;
  }

  /**
   * ( begin auto-generated from noiseSeed.xml )
   *
   * Sets the seed value for <b>noise()</b>. By default, <b>noise()</b>
   * produces different results each time the program is run. Set the
   * <b>value</b> parameter to a constant to return the same pseudo-random
   * numbers each time the software is run.
   *
   * ( end auto-generated )
   * @ math:random
   * @param seed seed value
   * @see PApplet#noise(float, float, float)
   * @see PApplet#noiseDetail(int, float)
   * @see PApplet#random(float,float)
   * @see PApplet#randomSeed(long)
   */
  public void noiseSeed(long seed) {
    if (perlinRandom == null) perlinRandom = new Random();
    perlinRandom.setSeed(seed);
    // force table reset after changing the random number seed [0122]
    perlin = null;
  }



  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  protected String[] loadImageFormats;

  /**
   * ( begin auto-generated from loadImage.xml )
   *
   * Loads an image into a variable of type <b>PImage</b>. Four types of
   * images ( <b>.gif</b>, <b>.jpg</b>, <b>.tga</b>, <b>.png</b>) images may
   * be loaded. To load correctly, images must be located in the data
   * directory of the current sketch. In most cases, load all images in
   * <b>setup()</b> to preload them at the start of the program. Loading
   * images inside <b>draw()</b> will reduce the speed of a program.<br/>
   * <br/> <b>filename</b> parameter can also be a URL to a file found
   * online. For security reasons, a Processing sketch found online can only
   * download files from the same server from which it came. Getting around
   * this restriction requires a <a
   * href="http://wiki.processing.org/w/Sign_an_Applet">signed
   * graphics</a>.<br/>
   * <br/> <b>extension</b> parameter is used to determine the image type in
   * cases where the image filename does not end with a proper extension.
   * Specify the extension as the second parameter to <b>loadImage()</b>, as
   * shown in the third example on this page.<br/>
   * <br/> an image is not loaded successfully, the <b>null</b> value is
   * returned and an error message will be printed to the console. The error
   * message does not halt the program, however the null value may cause a
   * NullPointerException if your code does not check whether the value
   * returned from <b>loadImage()</b> is null.<br/>
   * <br/> on the type of error, a <b>PImage</b> object may still be
   * returned, but the width and height of the image will be set to -1. This
   * happens if bad image data is returned or cannot be decoded properly.
   * Sometimes this happens with image URLs that produce a 403 error or that
   * redirect to a password prompt, because <b>loadImage()</b> will attempt
   * to interpret the HTML as image data.
   *
   * ( end auto-generated )
   *
   * @ image:loading_displaying
   * @param filename name of file to load, can be .gif, .jpg, .tga, or a handful of other image types depending on your platform
   * @see PImage
   * @see PGraphics#image(PImage, float, float, float, float)
   * @see PGraphics#imageMode(int)
   * @see PGraphics#background(float, float, float, float)
   */
  public PImage loadImage(String filename) {
//    return loadImage(filename, null, null);
    return loadImage(filename, null);
  }

//  /**
//   * @param extension the type of image to load, for example "png", "gif", "jpg"
//   */
//  public PImage loadImage(String filename, String extension) {
//    return loadImage(filename, extension, null);
//  }

//  /**
//   * @no
//   */
//  public PImage loadImage(String filename, Object params) {
//    return loadImage(filename, null, params);
//  }

  /**
          * ( begin auto-generated from loadBytes.xml )
          *
          * Reads the contents of a file or url and places it in a byte array. If a
  * file is specified, it must be located in the sketch's "data"
          * directory/folder.<br />
          * <br />
          * The filename parameter can also be a URL to a file found online. For
  * security reasons, a Processing sketch found online can only download
  * files from the same server from which it came. Getting around this
          * restriction requires a <a
  * href="http://wiki.processing.org/w/Sign_an_Applet">signed graphics</a>.
          *
          * ( end auto-generated )
          * @ input:files
  * @param filename name of a file in the data folder or a URL.
  *
          */
  public byte[] loadBytes(String filename) {
    InputStream is = createInput(filename);
    if (is != null) {
      byte[] outgoing = loadBytes(is);
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();  // shouldn't happen
      }
      return outgoing;
    }

    System.err.println("The file \"" + filename + "\" " +
            "is missing or inaccessible, make sure " +
            "the URL is valid or that the file has been " +
            "added to your sketch and is readable.");
    return null;
  }

  /**
   * @no
   */
  static public byte[] loadBytes(InputStream input) {
    try {
      BufferedInputStream bis = new BufferedInputStream(input);
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      int c = bis.read();
      while (c != -1) {
        out.write(c);
        c = bis.read();
      }
      return out.toByteArray();

    } catch (IOException e) {
      e.printStackTrace();
      //throw new RuntimeException("Couldn't load bytes from stream");
    }
    return null;
  }


  /**
   * @param extension type of image to load, for example "png", "gif", "jpg"
   */
  public PImage loadImage(String filename, String extension) { //, Object params) {
    if (extension == null) {
      String lower = filename.toLowerCase();
      int dot = filename.lastIndexOf('.');
      if (dot == -1) {
        extension = "unknown";  // no extension found
      }
      extension = lower.substring(dot + 1);

      // check for, and strip any parameters on the url, i.e.
      // filename.jpg?blah=blah&something=that
      int question = extension.indexOf('?');
      if (question != -1) {
        extension = extension.substring(0, question);
      }
    }

    // just in case. them users will try anything!
    extension = extension.toLowerCase();

    if (extension.equals("tif") || extension.equals("tiff")) {
      byte bytes[] = loadBytes(filename);
      PImage image =  (bytes == null) ? null : PImage.loadTIFF(bytes);
//      if (params != null) {
//        image.setParams(g, params);
//      }
      return image;
    }

    // For jpeg, gif, and png, load them using createImage(),
    // because the javax.imageio code was found to be much slower.
    // http://dev.processing.org/bugs/show_bug.cgi?id=392
    try {
      if (extension.equals("jpg") || extension.equals("jpeg") ||
          extension.equals("gif") || extension.equals("png") ||
          extension.equals("unknown")) {
        byte bytes[] = loadBytes(filename);
        if (bytes == null) {
          return null;
        } else {
          //Image awtImage = Toolkit.getDefaultToolkit().createImage(bytes);
          Image awtImage = new ImageIcon(bytes).getImage();

          if (awtImage instanceof BufferedImage) {
            BufferedImage buffImage = (BufferedImage) awtImage;
            int space = buffImage.getColorModel().getColorSpace().getType();
            if (space == ColorSpace.TYPE_CMYK) {
              System.err.println(filename + " is a CMYK image, " +
                                 "only RGB images are supported.");
              return null;
              /*
              // wishful thinking, appears to not be supported
              // https://community.oracle.com/thread/1272045?start=0&tstart=0
              BufferedImage destImage =
                new BufferedImage(buffImage.getWidth(),
                                  buffImage.getHeight(),
                                  BufferedImage.TYPE_3BYTE_BGR);
              ColorConvertOp op = new ColorConvertOp(null);
              op.filter(buffImage, destImage);
              image = new PImage(destImage);
              */
            }
          }

          PImage image = new PImage(awtImage);
          if (image.width == -1) {
            System.err.println("The file " + filename +
                               " contains bad image data, or may not be an image.");
          }

          // if it's a .gif image, test to see if it has transparency
          if (extension.equals("gif") || extension.equals("png") ||
              extension.equals("unknown")) {
            image.checkAlpha();
          }

//          if (params != null) {
//            image.setParams(g, params);
//          }
          return image;
        }
      }
    } catch (Exception e) {
      // show error, but move on to the stuff below, see if it'll work
      e.printStackTrace();
    }

    if (loadImageFormats == null) {
      loadImageFormats = ImageIO.getReaderFormatNames();
    }
    if (loadImageFormats != null) {
      for (int i = 0; i < loadImageFormats.length; i++) {
        if (extension.equals(loadImageFormats[i])) {
          return loadImageIO(filename);
//          PImage image = loadImageIO(filename);
//          if (params != null) {
//            image.setParams(g, params);
//          }
//          return image;
        }
      }
    }

    // failed, could not load image after all those attempts
    System.err.println("Could not find a method to load " + filename);
    return null;
  }


//  /**
//   * @no
//   */
//  public PImage requestImage(String filename, String extension, Object params) {
//    PImage vessel = createImage(0, 0, ARGB, params);
//    AsyncImageLoader ail =
//      new AsyncImageLoader(filename, extension, vessel);
//    ail.start();
//    return vessel;
//  }


  /**
   * By trial and error, four image loading threads seem to work best when
   * loading images from online. This is consistent with the number of open
   * connections that web browsers will maintain. The variable is made public
   * (however no accessor has been added since it's esoteric) if you really
   * want to have control over the value used. For instance, when loading local
   * files, it might be better to only have a single thread (or two) loading
   * images so that you're disk isn't simply jumping around.
   */
  public int requestImageMax = 4;
  volatile int requestImageCount;

  class AsyncImageLoader extends Thread {
    String filename;
    String extension;
    PImage vessel;

    public AsyncImageLoader(String filename, String extension, PImage vessel) {
      this.filename = filename;
      this.extension = extension;
      this.vessel = vessel;
    }

    @Override
    public void run() {
      while (requestImageCount == requestImageMax) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) { }
      }
      requestImageCount++;

      PImage actual = loadImage(filename, extension);

      // An error message should have already printed
      if (actual == null) {
        vessel.width = -1;
        vessel.height = -1;

      } else {
        vessel.width = actual.width;
        vessel.height = actual.height;
        vessel.format = actual.format;
        vessel.pixels = actual.pixels;

        vessel.pixelWidth = actual.width;
        vessel.pixelHeight = actual.height;
        vessel.pixelDensity = 1;
      }
      requestImageCount--;
    }
  }


  // done internally by ImageIcon
//  /**
//   * Load an AWT image synchronously by setting up a MediaTracker for
//   * a single image, and blocking until it has loaded.
//   */
//  protected PImage loadImageMT(Image awtImage) {
//    MediaTracker tracker = new MediaTracker(this);
//    tracker.addImage(awtImage, 0);
//    try {
//      tracker.waitForAll();
//    } catch (InterruptedException e) {
//      //e.printStackTrace();  // non-fatal, right?
//    }
//
//    PImage image = new PImage(awtImage);
//    image.parent = this;
//    return image;
//  }

  /**
   * Call openStream() without automatic gzip decompression.
   */
  public static InputStream createInputRaw(String filename) {
    if (filename == null) return null;

    if (filename.length() == 0) {
      // an error will be called by the parent function
      //System.err.println("The filename passed to openStream() was empty.");
      return null;
    }

    // First check whether this looks like a URL. This will prevent online
    // access logs from being spammed with GET /sketchfolder/http://blahblah
    if (filename.contains(":")) {  // at least smells like URL
      try {
        URL url = new URL(filename);
        URLConnection conn = url.openConnection();
        if (conn instanceof HttpURLConnection) {
          HttpURLConnection httpConn = (HttpURLConnection) conn;
          // Will not handle a protocol change (see below)
          httpConn.setInstanceFollowRedirects(true);
          int response = httpConn.getResponseCode();
          // Normally will not follow HTTPS redirects from HTTP due to security concerns
          // http://stackoverflow.com/questions/1884230/java-doesnt-follow-redirect-in-urlconnection/1884427
          if (response >= 300 && response < 400) {
            String newLocation = httpConn.getHeaderField("Location");
            return createInputRaw(newLocation);
          }
          return conn.getInputStream();
        } else if (conn instanceof JarURLConnection) {
          return url.openStream();
        }
      } catch (MalformedURLException mfue) {
        // not a url, that's fine

      } catch (FileNotFoundException fnfe) {
        // Added in 0119 b/c Java 1.5 throws FNFE when URL not available.
        // http://dev.processing.org/bugs/show_bug.cgi?id=403

      } catch (IOException e) {
        // changed for 0117, shouldn't be throwing exception
        e.printStackTrace();
        //System.err.println("Error downloading from URL " + filename);
        return null;
        //throw new RuntimeException("Error downloading from URL " + filename);
      }
    }

    InputStream stream = null;

    // Moved this earlier than the getResourceAsStream() checks, because
    // calling getResourceAsStream() on a directory lists its contents.
    // http://dev.processing.org/bugs/show_bug.cgi?id=716
    try {
      // First see if it's in a data folder. This may fail by throwing
      // a SecurityException. If so, this whole block will be skipped.
      File file = new File(filename);

      if (file.isDirectory()) {
        return null;
      }
      if (file.exists()) {
        try {
          // handle case sensitivity check
          String filePath = file.getCanonicalPath();
          String filenameActual = new File(filePath).getName();
          // make sure there isn't a subfolder prepended to the name
          String filenameShort = new File(filename).getName();
          // if the actual filename is the same, but capitalized
          // differently, warn the user.
          //if (filenameActual.equalsIgnoreCase(filenameShort) &&
          //!filenameActual.equals(filenameShort)) {
          if (!filenameActual.equals(filenameShort)) {
            throw new RuntimeException("This file is named " +
                    filenameActual + " not " +
                    filename + ". Rename the file " +
                    "or change your code.");
          }
        } catch (IOException e) { }
      }

      // if this file is ok, may as well just load it
      stream = new FileInputStream(file);
      if (stream != null) return stream;

      // have to break these out because a general Exception might
      // catch the RuntimeException being thrown above
    } catch (IOException ioe) {
    } catch (SecurityException se) { }

    // Using getClassLoader() prevents java from converting dots
    // to slashes or requiring a slash at the beginning.
    // (a slash as a prefix means that it'll load from the root of
    // the jar, rather than trying to dig into the package location)
    ClassLoader cl = PApplet.class.getClassLoader();

    // by default, data files are exported to the root path of the jar.
    // (not the data folder) so check there first.
    stream = cl.getResourceAsStream("data/" + filename);
    if (stream != null) {
      String cn = stream.getClass().getName();
      // this is an irritation of sun's java plug-in, which will return
      // a non-null stream for an object that doesn't exist. like all good
      // things, this is probably introduced in java 1.5. awesome!
      // http://dev.processing.org/bugs/show_bug.cgi?id=359
      if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
        return stream;
      }
    }

    // When used with an online script, also need to check without the
    // data folder, in case it's not in a subfolder called 'data'.
    // http://dev.processing.org/bugs/show_bug.cgi?id=389
    stream = cl.getResourceAsStream(filename);
    if (stream != null) {
      String cn = stream.getClass().getName();
      if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
        return stream;
      }
    }

    try {
      // attempt to load from a local file, used when running as
      // an application, or as a signed graphics
      try {  // first try to catch any security exceptions

        try {
          stream = new FileInputStream(filename);
          if (stream != null) return stream;
        } catch (IOException e1) { }

      } catch (SecurityException se) { }  // online, whups

    } catch (Exception e) {
      //die(e.getMessage(), e);
      e.printStackTrace();
    }

    return null;
  }


  /**
   * ( begin auto-generated from createInput.xml )
   *
   * This is a function for advanced programmers to open a Java InputStream.
   * It's useful if you want to use the facilities provided by PApplet to
   * easily open files from the data folder or from a URL, but want an
   * InputStream object so that you can use other parts of Java to take more
   * control of how the stream is read.<br />
   * <br />
   * The filename passed in can be:<br />
   * - A URL, for instance <b>openStream("http://processing.org/")</b><br />
   * - A file in the sketch's <b>data</b> folder<br />
   * - The full path to a file to be opened locally (when running as an
   * application)<br />
   * <br />
   * If the requested item doesn't exist, null is returned. If not online,
   * this will also check to see if the user is asking for a file whose name
   * isn't properly capitalized. If capitalization is different, an error
   * will be printed to the console. This helps prevent issues that appear
   * when a sketch is exported to the web, where case sensitivity matters, as
   * opposed to running from inside the Processing Development Environment on
   * Windows or Mac OS, where case sensitivity is preserved but ignored.<br />
   * <br />
   * If the file ends with <b>.gz</b>, the stream will automatically be gzip
   * decompressed. If you don't want the automatic decompression, use the
   * related function <b>createInputRaw()</b>.
   * <br />
   * In earlier releases, this function was called <b>openStream()</b>.<br />
   * <br />
   *
   * ( end auto-generated )
   *
   * <h3>Advanced</h3>
   * Simplified method to open a Java InputStream.
   * <p>
   * This method is useful if you want to use the facilities provided
   * by PApplet to easily open things from the data folder or from a URL,
   * but want an InputStream object so that you can use other Java
   * methods to take more control of how the stream is read.
   * <p>
   * If the requested item doesn't exist, null is returned.
   * (Prior to 0096, die() would be called, killing the graphics)
   * <p>
   * For 0096+, the "data" folder is exported intact with subfolders,
   * and openStream() properly handles subdirectories from the data folder
   * <p>
   * If not online, this will also check to see if the user is asking
   * for a file whose name isn't properly capitalized. This helps prevent
   * issues when a sketch is exported to the web, where case sensitivity
   * matters, as opposed to Windows and the Mac OS default where
   * case sensitivity is preserved but ignored.
   * <p>
   * It is strongly recommended that libraries use this method to open
   * data files, so that the loading sequence is handled in the same way
   * as functions like loadBytes(), loadImage(), etc.
   * <p>
   * The filename passed in can be:
   * <UL>
   * <LI>A URL, for instance openStream("http://processing.org/");
   * <LI>A file in the sketch's data folder
   * <LI>Another file to be opened locally (when running as an application)
   * </UL>
   *
   * @ input:files
   * @param filename the name of the file to use as input
   *
   */
  public static InputStream createInput(String filename) {
    InputStream input = createInputRaw(filename);
    final String lower = filename.toLowerCase();
    if ((input != null) &&
            (lower.endsWith(".gz") || lower.endsWith(".svgz"))) {
      try {
        return new GZIPInputStream(input);
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }
    return input;
  }

  /**
   * Use Java 1.4 ImageIO methods to load an image.
   */
  protected PImage loadImageIO(String filename) {
    InputStream stream = createInput(filename);
    if (stream == null) {
      System.err.println("The image " + filename + " could not be found.");
      return null;
    }

    try {
      BufferedImage bi = ImageIO.read(stream);
      PImage outgoing = new PImage(bi.getWidth(), bi.getHeight());

      bi.getRGB(0, 0, outgoing.width, outgoing.height,
                outgoing.pixels, 0, outgoing.width);

      // check the alpha for this image
      // was gonna call getType() on the image to see if RGB or ARGB,
      // but it's not actually useful, since gif images will come through
      // as TYPE_BYTE_INDEXED, which means it'll still have to check for
      // the transparency. also, would have to iterate through all the other
      // types and guess whether alpha was in there, so.. just gonna stick
      // with the old method.
      outgoing.checkAlpha();

      stream.close();
      // return the image
      return outgoing;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }



  //////////////////////////////////////////////////////////////

  // ARRAY UTILITIES


  /**
   * ( begin auto-generated from arrayCopy.xml )
   *
   * Copies an array (or part of an array) to another array. The <b>src</b>
   * array is copied to the <b>dst</b> array, beginning at the position
   * specified by <b>srcPos</b> and into the position specified by
   * <b>dstPos</b>. The number of elements to copy is determined by
   * <b>length</b>. The simplified version with two arguments copies an
   * entire array to another of the same size. It is equivalent to
   * "arrayCopy(src, 0, dst, 0, src.length)". This function is far more
   * efficient for copying array data than iterating through a <b>for</b> and
   * copying each element.
   *
   * ( end auto-generated )
   * @ data:array_functions
   * @param src the source array
   * @param srcPosition starting position in the source array
   * @param dst the destination array of the same data type as the source array
   * @param dstPosition starting position in the destination array
   * @param length number of array elements to be copied
   * @see PApplet#concat(boolean[], boolean[])
   */
  static public void arrayCopy(Object src, int srcPosition,
                               Object dst, int dstPosition,
                               int length) {
    System.arraycopy(src, srcPosition, dst, dstPosition, length);
  }

  /**
   * Convenience method for arraycopy().
   * Identical to <CODE>arraycopy(src, 0, dst, 0, length);</CODE>
   */
  static public void arrayCopy(Object src, Object dst, int length) {
    System.arraycopy(src, 0, dst, 0, length);
  }

  /**
   * Shortcut to copy the entire contents of
   * the source into the destination array.
   * Identical to <CODE>arraycopy(src, 0, dst, 0, src.length);</CODE>
   */
  static public void arrayCopy(Object src, Object dst) {
    System.arraycopy(src, 0, dst, 0, Array.getLength(src));
  }

  //
  /**
   * @deprecated Use arrayCopy() instead.
   */
  static public void arraycopy(Object src, int srcPosition,
                               Object dst, int dstPosition,
                               int length) {
    System.arraycopy(src, srcPosition, dst, dstPosition, length);
  }

  /**
   * @deprecated Use arrayCopy() instead.
   */
  static public void arraycopy(Object src, Object dst, int length) {
    System.arraycopy(src, 0, dst, 0, length);
  }

  /**
   * @deprecated Use arrayCopy() instead.
   */
  static public void arraycopy(Object src, Object dst) {
    System.arraycopy(src, 0, dst, 0, Array.getLength(src));
  }

  /**
   * ( begin auto-generated from expand.xml )
   *
   * Increases the size of an array. By default, this function doubles the
   * size of the array, but the optional <b>newSize</b> parameter provides
   * precise control over the increase in size.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) expand(originalArray)</em>.
   *
   * ( end auto-generated )
   *
   * @ data:array_functions
   * @param list the array to expand
   * @see PApplet#shorten(boolean[])
   */
  static public boolean[] expand(boolean list[]) {
    return expand(list, list.length << 1);
  }

  /**
   * @param newSize new size for the array
   */
  static public boolean[] expand(boolean list[], int newSize) {
    boolean temp[] = new boolean[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public byte[] expand(byte list[]) {
    return expand(list, list.length << 1);
  }

  static public byte[] expand(byte list[], int newSize) {
    byte temp[] = new byte[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public char[] expand(char list[]) {
    return expand(list, list.length << 1);
  }

  static public char[] expand(char list[], int newSize) {
    char temp[] = new char[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public int[] expand(int list[]) {
    return expand(list, list.length << 1);
  }

  static public int[] expand(int list[], int newSize) {
    int temp[] = new int[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public long[] expand(long list[]) {
    return expand(list, list.length << 1);
  }

  static public long[] expand(long list[], int newSize) {
    long temp[] = new long[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public float[] expand(float list[]) {
    return expand(list, list.length << 1);
  }

  static public float[] expand(float list[], int newSize) {
    float temp[] = new float[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public double[] expand(double list[]) {
    return expand(list, list.length << 1);
  }

  static public double[] expand(double list[], int newSize) {
    double temp[] = new double[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public String[] expand(String list[]) {
    return expand(list, list.length << 1);
  }

  static public String[] expand(String list[], int newSize) {
    String temp[] = new String[newSize];
    // in case the new size is smaller than list.length
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

 /**
  * @no
  */
  static public Object expand(Object array) {
    return expand(array, Array.getLength(array) << 1);
  }

  static public Object expand(Object list, int newSize) {
    Class<?> type = list.getClass().getComponentType();
    Object temp = Array.newInstance(type, newSize);
    System.arraycopy(list, 0, temp, 0,
                     Math.min(Array.getLength(list), newSize));
    return temp;
  }

  // contract() has been removed in revision 0124, use subset() instead.
  // (expand() is also functionally equivalent)

  /**
   * ( begin auto-generated from append.xml )
   *
   * Expands an array by one element and adds data to the new position. The
   * datatype of the <b>element</b> parameter must be the same as the
   * datatype of the array.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) append(originalArray, element)</em>.
   *
   * ( end auto-generated )
   *
   * @ data:array_functions
   * @param array array to append
   * @param value new data for the array
   * @see PApplet#shorten(boolean[])
   * @see PApplet#expand(boolean[])
   */
  static public byte[] append(byte array[], byte value) {
    array = expand(array, array.length + 1);
    array[array.length-1] = value;
    return array;
  }

  static public char[] append(char array[], char value) {
    array = expand(array, array.length + 1);
    array[array.length-1] = value;
    return array;
  }

  static public int[] append(int array[], int value) {
    array = expand(array, array.length + 1);
    array[array.length-1] = value;
    return array;
  }

  static public float[] append(float array[], float value) {
    array = expand(array, array.length + 1);
    array[array.length-1] = value;
    return array;
  }

  static public String[] append(String array[], String value) {
    array = expand(array, array.length + 1);
    array[array.length-1] = value;
    return array;
  }

  static public Object append(Object array, Object value) {
    int length = Array.getLength(array);
    array = expand(array, length + 1);
    Array.set(array, length, value);
    return array;
  }


 /**
   * ( begin auto-generated from shorten.xml )
   *
   * Decreases an array by one element and returns the shortened array.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) shorten(originalArray)</em>.
   *
   * ( end auto-generated )
   *
   * @ data:array_functions
   * @param list array to shorten
   * @see PApplet#append(byte[], byte)
   * @see PApplet#expand(boolean[])
   */
  static public boolean[] shorten(boolean list[]) {
    return subset(list, 0, list.length-1);
  }

  static public byte[] shorten(byte list[]) {
    return subset(list, 0, list.length-1);
  }

  static public char[] shorten(char list[]) {
    return subset(list, 0, list.length-1);
  }

  static public int[] shorten(int list[]) {
    return subset(list, 0, list.length-1);
  }

  static public float[] shorten(float list[]) {
    return subset(list, 0, list.length-1);
  }

  static public String[] shorten(String list[]) {
    return subset(list, 0, list.length-1);
  }

  static public Object shorten(Object list) {
    int length = Array.getLength(list);
    return subset(list, 0, length - 1);
  }


  /**
   * ( begin auto-generated from splice.xml )
   *
   * Inserts a value or array of values into an existing array. The first two
   * parameters must be of the same datatype. The <b>array</b> parameter
   * defines the array which will be modified and the second parameter
   * defines the data which will be inserted.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) splice(array1, array2, index)</em>.
   *
   * ( end auto-generated )
   * @ data:array_functions
   * @param list array to splice into
   * @param value value to be spliced in
   * @param index position in the array from which to insert data
   * @see PApplet#concat(boolean[], boolean[])
   * @see PApplet#subset(boolean[], int, int)
   */
  static public boolean[] splice(boolean list[],
                                       boolean value, int index) {
    boolean outgoing[] = new boolean[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static public boolean[] splice(boolean list[],
                                       boolean value[], int index) {
    boolean outgoing[] = new boolean[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }

  static public byte[] splice(byte list[],
                                    byte value, int index) {
    byte outgoing[] = new byte[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static public byte[] splice(byte list[],
                                    byte value[], int index) {
    byte outgoing[] = new byte[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }


  static public char[] splice(char list[],
                                    char value, int index) {
    char outgoing[] = new char[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static public char[] splice(char list[],
                                    char value[], int index) {
    char outgoing[] = new char[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }

  static public int[] splice(int list[],
                                   int value, int index) {
    int outgoing[] = new int[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static public int[] splice(int list[],
                                   int value[], int index) {
    int outgoing[] = new int[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }

  static public float[] splice(float list[],
                                     float value, int index) {
    float outgoing[] = new float[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static public float[] splice(float list[],
                                     float value[], int index) {
    float outgoing[] = new float[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }

  static public String[] splice(String list[],
                                      String value, int index) {
    String outgoing[] = new String[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static public String[] splice(String list[],
                                      String value[], int index) {
    String outgoing[] = new String[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }

  static public Object splice(Object list, Object value, int index) {
    Class<?> type = list.getClass().getComponentType();
    Object outgoing = null;
    int length = Array.getLength(list);

    // check whether item being spliced in is an array
    if (value.getClass().getName().charAt(0) == '[') {
      int vlength = Array.getLength(value);
      outgoing = Array.newInstance(type, length + vlength);
      System.arraycopy(list, 0, outgoing, 0, index);
      System.arraycopy(value, 0, outgoing, index, vlength);
      System.arraycopy(list, index, outgoing, index + vlength, length - index);

    } else {
      outgoing = Array.newInstance(type, length + 1);
      System.arraycopy(list, 0, outgoing, 0, index);
      Array.set(outgoing, index, value);
      System.arraycopy(list, index, outgoing, index + 1, length - index);
    }
    return outgoing;
  }

  static public boolean[] subset(boolean list[], int start) {
    return subset(list, start, list.length - start);
  }

 /**
   * ( begin auto-generated from subset.xml )
   *
   * Extracts an array of elements from an existing array. The <b>array</b>
   * parameter defines the array from which the elements will be copied and
   * the <b>offset</b> and <b>length</b> parameters determine which elements
   * to extract. If no <b>length</b> is given, elements will be extracted
   * from the <b>offset</b> to the end of the array. When specifying the
   * <b>offset</b> remember the first array element is 0. This function does
   * not change the source array.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) subset(originalArray, 0, 4)</em>.
   *
   * ( end auto-generated )
  * @ data:array_functions
  * @param list array to extract from
  * @param start position to begin
  * @param count number of values to extract
  * @see PApplet#splice(boolean[], boolean, int)
  */
  static public boolean[] subset(boolean list[], int start, int count) {
    boolean output[] = new boolean[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }

  static public byte[] subset(byte list[], int start) {
    return subset(list, start, list.length - start);
  }

  static public byte[] subset(byte list[], int start, int count) {
    byte output[] = new byte[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public char[] subset(char list[], int start) {
    return subset(list, start, list.length - start);
  }

  static public char[] subset(char list[], int start, int count) {
    char output[] = new char[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }

  static public int[] subset(int list[], int start) {
    return subset(list, start, list.length - start);
  }

  static public int[] subset(int list[], int start, int count) {
    int output[] = new int[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }

  static public float[] subset(float list[], int start) {
    return subset(list, start, list.length - start);
  }

  static public float[] subset(float list[], int start, int count) {
    float output[] = new float[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public String[] subset(String list[], int start) {
    return subset(list, start, list.length - start);
  }

  static public String[] subset(String list[], int start, int count) {
    String output[] = new String[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public Object subset(Object list, int start) {
    int length = Array.getLength(list);
    return subset(list, start, length - start);
  }

  static public Object subset(Object list, int start, int count) {
    Class<?> type = list.getClass().getComponentType();
    Object outgoing = Array.newInstance(type, count);
    System.arraycopy(list, start, outgoing, 0, count);
    return outgoing;
  }


 /**
   * ( begin auto-generated from concat.xml )
   *
   * Concatenates two arrays. For example, concatenating the array { 1, 2, 3
   * } and the array { 4, 5, 6 } yields { 1, 2, 3, 4, 5, 6 }. Both parameters
   * must be arrays of the same datatype.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) concat(array1, array2)</em>.
   *
   * ( end auto-generated )
  * @ data:array_functions
  * @param a first array to concatenate
  * @param b second array to concatenate
  * @see PApplet#splice(boolean[], boolean, int)
  * @see PApplet#arrayCopy(Object, int, Object, int, int)
  */
  static public boolean[] concat(boolean a[], boolean b[]) {
    boolean c[] = new boolean[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public byte[] concat(byte a[], byte b[]) {
    byte c[] = new byte[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public char[] concat(char a[], char b[]) {
    char c[] = new char[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public int[] concat(int a[], int b[]) {
    int c[] = new int[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public float[] concat(float a[], float b[]) {
    float c[] = new float[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public String[] concat(String a[], String b[]) {
    String c[] = new String[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public Object concat(Object a, Object b) {
    Class<?> type = a.getClass().getComponentType();
    int alength = Array.getLength(a);
    int blength = Array.getLength(b);
    Object outgoing = Array.newInstance(type, alength + blength);
    System.arraycopy(a, 0, outgoing, 0, alength);
    System.arraycopy(b, 0, outgoing, alength, blength);
    return outgoing;
  }

  //


 /**
   * ( begin auto-generated from reverse.xml )
   *
   * Reverses the order of an array.
   *
   * ( end auto-generated )
  * @ data:array_functions
  * @param list booleans[], bytes[], chars[], ints[], floats[], or Strings[]
  */
  static public boolean[] reverse(boolean list[]) {
    boolean outgoing[] = new boolean[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public byte[] reverse(byte list[]) {
    byte outgoing[] = new byte[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public char[] reverse(char list[]) {
    char outgoing[] = new char[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public int[] reverse(int list[]) {
    int outgoing[] = new int[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public float[] reverse(float list[]) {
    float outgoing[] = new float[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public String[] reverse(String list[]) {
    String outgoing[] = new String[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public Object reverse(Object list) {
    Class<?> type = list.getClass().getComponentType();
    int length = Array.getLength(list);
    Object outgoing = Array.newInstance(type, length);
    for (int i = 0; i < length; i++) {
      Array.set(outgoing, i, Array.get(list, (length - 1) - i));
    }
    return outgoing;
  }



  //////////////////////////////////////////////////////////////

  // STRINGS


  /**
   * ( begin auto-generated from trim.xml )
   *
   * Removes whitespace characters from the beginning and end of a String. In
   * addition to standard whitespace characters such as space, carriage
   * return, and tab, this function also removes the Unicode "nbsp" character.
   *
   * ( end auto-generated )
   * @ data:string_functions
   * @param str any string
   * @see PApplet#split(String, String)
   * @see PApplet#join(String[], char)
   */
  static public String trim(String str) {
    return str.replace('\u00A0', ' ').trim();
  }


 /**
  * @param array a String array
  */
  static public String[] trim(String[] array) {
    String[] outgoing = new String[array.length];
    for (int i = 0; i < array.length; i++) {
      if (array[i] != null) {
        outgoing[i] = array[i].replace('\u00A0', ' ').trim();
      }
    }
    return outgoing;
  }


  /**
   * ( begin auto-generated from join.xml )
   *
   * Combines an array of Strings into one String, each separated by the
   * character(s) used for the <b>separator</b> parameter. To join arrays of
   * ints or floats, it's necessary to first convert them to strings using
   * <b>nf()</b> or <b>nfs()</b>.
   *
   * ( end auto-generated )
   * @ data:string_functions
   * @param list array of Strings
   * @param separator char or String to be placed between each item
   * @see PApplet#split(String, String)
   * @see PApplet#trim(String)
   * @see PApplet#nf(float, int, int)
   * @see PApplet#nfs(float, int, int)
   */
  static public String join(String[] list, char separator) {
    return join(list, String.valueOf(separator));
  }


  static public String join(String[] list, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.length; i++) {
      if (i != 0) sb.append(separator);
      sb.append(list[i]);
    }
    return sb.toString();
  }


  static public String[] splitTokens(String value) {
    return splitTokens(value, WHITESPACE);
  }


  /**
   * ( begin auto-generated from splitTokens.xml )
   *
   * The splitTokens() function splits a String at one or many character
   * "tokens." The <b>tokens</b> parameter specifies the character or
   * characters to be used as a boundary.
   * <br/> <br/>
   * If no <b>tokens</b> character is specified, any whitespace character is
   * used to split. Whitespace characters include tab (\\t), line feed (\\n),
   * carriage return (\\r), form feed (\\f), and space. To convert a String
   * to an array of integers or floats, use the datatype conversion functions
   * <b>int()</b> and <b>float()</b> to convert the array of Strings.
   *
   * ( end auto-generated )
   * @ data:string_functions
   * @param value the String to be split
   * @param delim list of individual characters that will be used as separators
   * @see PApplet#split(String, String)
   * @see PApplet#join(String[], String)
   * @see PApplet#trim(String)
   */
  static public String[] splitTokens(String value, String delim) {
    StringTokenizer toker = new StringTokenizer(value, delim);
    String pieces[] = new String[toker.countTokens()];

    int index = 0;
    while (toker.hasMoreTokens()) {
      pieces[index++] = toker.nextToken();
    }
    return pieces;
  }


  /**
   * ( begin auto-generated from split.xml )
   *
   * The split() function breaks a string into pieces using a character or
   * string as the divider. The <b>delim</b> parameter specifies the
   * character or characters that mark the boundaries between each piece. A
   * String[] array is returned that contains each of the pieces.
   * <br/> <br/>
   * If the result is a set of numbers, you can convert the String[] array to
   * to a float[] or int[] array using the datatype conversion functions
   * <b>int()</b> and <b>float()</b> (see example above).
   * <br/> <br/>
   * The <b>splitTokens()</b> function works in a similar fashion, except
   * that it splits using a range of characters instead of a specific
   * character or sequence.
   * <!-- /><br />
   * This function uses regular expressions to determine how the <b>delim</b>
   * parameter divides the <b>str</b> parameter. Therefore, if you use
   * characters such parentheses and brackets that are used with regular
   * expressions as a part of the <b>delim</b> parameter, you'll need to put
   * two blackslashes (\\\\) in front of the character (see example above).
   * You can read more about <a
   * href="http://en.wikipedia.org/wiki/Regular_expression">regular
   * expressions</a> and <a
   * href="http://en.wikipedia.org/wiki/Escape_character">escape
   * characters</a> on Wikipedia.
   * -->
   *
   * ( end auto-generated )
   * @ data:string_functions
   * @usage web_application
   * @param value the String to be split
   * @param delim the character or String used to separate the data
   */
  static public String[] split(String value, char delim) {
    // do this so that the exception occurs inside the user's
    // program, rather than appearing to be a bug inside split()
    if (value == null) return null;
    //return split(what, String.valueOf(delim));  // huh

    char chars[] = value.toCharArray();
    int splitCount = 0; //1;
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] == delim) splitCount++;
    }
    // make sure that there is something in the input string
    //if (chars.length > 0) {
      // if the last char is a delimeter, get rid of it..
      //if (chars[chars.length-1] == delim) splitCount--;
      // on second thought, i don't agree with this, will disable
    //}
    if (splitCount == 0) {
      String splits[] = new String[1];
      splits[0] = value;
      return splits;
    }
    //int pieceCount = splitCount + 1;
    String splits[] = new String[splitCount + 1];
    int splitIndex = 0;
    int startIndex = 0;
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] == delim) {
        splits[splitIndex++] =
          new String(chars, startIndex, i-startIndex);
        startIndex = i + 1;
      }
    }
    //if (startIndex != chars.length) {
      splits[splitIndex] =
        new String(chars, startIndex, chars.length-startIndex);
    //}
    return splits;
  }


  static public String[] split(String value, String delim) {
    ArrayList<String> items = new ArrayList<String>();
    int index;
    int offset = 0;
    while ((index = value.indexOf(delim, offset)) != -1) {
      items.add(value.substring(offset, index));
      offset = index + delim.length();
    }
    items.add(value.substring(offset));
    String[] outgoing = new String[items.size()];
    items.toArray(outgoing);
    return outgoing;
  }


  static protected LinkedHashMap<String, Pattern> matchPatterns;

  static Pattern matchPattern(String regexp) {
    Pattern p = null;
    if (matchPatterns == null) {
      matchPatterns = new LinkedHashMap<String, Pattern>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
          // Limit the number of match patterns at 10 most recently used
          return size() == 10;
        }
      };
    } else {
      p = matchPatterns.get(regexp);
    }
    if (p == null) {
      p = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL);
      matchPatterns.put(regexp, p);
    }
    return p;
  }


  /**
   * ( begin auto-generated from match.xml )
   *
   * The match() function is used to apply a regular expression to a piece of
   * text, and return matching groups (elements found inside parentheses) as
   * a String array. No match will return null. If no groups are specified in
   * the regexp, but the sequence matches, an array of length one (with the
   * matched text as the first element of the array) will be returned.<br />
   * <br />
   * To use the function, first check to see if the result is null. If the
   * result is null, then the sequence did not match. If the sequence did
   * match, an array is returned.
   * If there are groups (specified by sets of parentheses) in the regexp,
   * then the contents of each will be returned in the array.
   * Element [0] of a regexp match returns the entire matching string, and
   * the match groups start at element [1] (the first group is [1], the
   * second [2], and so on).<br />
   * <br />
   * The syntax can be found in the reference for Java's <a
   * href="http://download.oracle.com/javase/6/docs/api/">Pattern</a> class.
   * For regular expression syntax, read the <a
   * href="http://download.oracle.com/javase/tutorial/essential/regex/">Java
   * Tutorial</a> on the topic.
   *
   * ( end auto-generated )
   * @ data:string_functions
   * @param str the String to be searched
   * @param regexp the regexp to be used for matching
   * @see PApplet#matchAll(String, String)
   * @see PApplet#split(String, String)
   * @see PApplet#splitTokens(String, String)
   * @see PApplet#join(String[], String)
   * @see PApplet#trim(String)
   */
  static public String[] match(String str, String regexp) {
    Pattern p = matchPattern(regexp);
    Matcher m = p.matcher(str);
    if (m.find()) {
      int count = m.groupCount() + 1;
      String[] groups = new String[count];
      for (int i = 0; i < count; i++) {
        groups[i] = m.group(i);
      }
      return groups;
    }
    return null;
  }


  /**
   * ( begin auto-generated from matchAll.xml )
   *
   * This function is used to apply a regular expression to a piece of text,
   * and return a list of matching groups (elements found inside parentheses)
   * as a two-dimensional String array. No matches will return null. If no
   * groups are specified in the regexp, but the sequence matches, a two
   * dimensional array is still returned, but the second dimension is only of
   * length one.<br />
   * <br />
   * To use the function, first check to see if the result is null. If the
   * result is null, then the sequence did not match at all. If the sequence
   * did match, a 2D array is returned. If there are groups (specified by
   * sets of parentheses) in the regexp, then the contents of each will be
   * returned in the array.
   * Assuming, a loop with counter variable i, element [i][0] of a regexp
   * match returns the entire matching string, and the match groups start at
   * element [i][1] (the first group is [i][1], the second [i][2], and so
   * on).<br />
   * <br />
   * The syntax can be found in the reference for Java's <a
   * href="http://download.oracle.com/javase/6/docs/api/">Pattern</a> class.
   * For regular expression syntax, read the <a
   * href="http://download.oracle.com/javase/tutorial/essential/regex/">Java
   * Tutorial</a> on the topic.
   *
   * ( end auto-generated )
   * @ data:string_functions
   * @param str the String to be searched
   * @param regexp the regexp to be used for matching
   * @see PApplet#match(String, String)
   * @see PApplet#split(String, String)
   * @see PApplet#splitTokens(String, String)
   * @see PApplet#join(String[], String)
   * @see PApplet#trim(String)
   */
  static public String[][] matchAll(String str, String regexp) {
    Pattern p = matchPattern(regexp);
    Matcher m = p.matcher(str);
    ArrayList<String[]> results = new ArrayList<>();
    int count = m.groupCount() + 1;
    while (m.find()) {
      String[] groups = new String[count];
      for (int i = 0; i < count; i++) {
        groups[i] = m.group(i);
      }
      results.add(groups);
    }
    if (results.isEmpty()) {
      return null;
    }
    String[][] matches = new String[results.size()][count];
    for (int i = 0; i < matches.length; i++) {
      matches[i] = results.get(i);
    }
    return matches;
  }



  //////////////////////////////////////////////////////////////

  // CASTING FUNCTIONS, INSERTED BY PREPROC


  /**
   * Convert a char to a boolean. 'T', 't', and '1' will become the
   * boolean value true, while 'F', 'f', or '0' will become false.
   */
  /*
  static public boolean parseBoolean(char what) {
    return ((what == 't') || (what == 'T') || (what == '1'));
  }
  */

  /**
   * <p>Convert an integer to a boolean. Because of how Java handles upgrading
   * numbers, this will also cover byte and char (as they will upgrade to
   * an int without any sort of explicit cast).</p>
   * <p>The preprocessor will convert boolean(what) to parseBoolean(what).</p>
   * @return false if 0, true if any other number
   */
  static public boolean parseBoolean(int what) {
    return (what != 0);
  }

  /*
  // removed because this makes no useful sense
  static public boolean parseBoolean(float what) {
    return (what != 0);
  }
  */

  /**
   * Convert the string "true" or "false" to a boolean.
   * @return true if 'what' is "true" or "TRUE", false otherwise
   */
  static public boolean parseBoolean(String what) {
    return Boolean.parseBoolean(what);
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  /*
  // removed, no need to introduce strange syntax from other languages
  static public boolean[] parseBoolean(char what[]) {
    boolean outgoing[] = new boolean[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] =
        ((what[i] == 't') || (what[i] == 'T') || (what[i] == '1'));
    }
    return outgoing;
  }
  */

  /**
   * Convert a byte array to a boolean array. Each element will be
   * evaluated identical to the integer case, where a byte equal
   * to zero will return false, and any other value will return true.
   * @return array of boolean elements
   */
  /*
  static public boolean[] parseBoolean(byte what[]) {
    boolean outgoing[] = new boolean[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (what[i] != 0);
    }
    return outgoing;
  }
  */

  /**
   * Convert an int array to a boolean array. An int equal
   * to zero will return false, and any other value will return true.
   * @return array of boolean elements
   */
  static public boolean[] parseBoolean(int what[]) {
    boolean outgoing[] = new boolean[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (what[i] != 0);
    }
    return outgoing;
  }

  /*
  // removed, not necessary... if necessary, convert to int array first
  static public boolean[] parseBoolean(float what[]) {
    boolean outgoing[] = new boolean[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (what[i] != 0);
    }
    return outgoing;
  }
  */

  static public boolean[] parseBoolean(String what[]) {
    boolean outgoing[] = new boolean[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = Boolean.parseBoolean(what[i]);
    }
    return outgoing;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static public byte parseByte(boolean what) {
    return what ? (byte)1 : 0;
  }

  static public byte parseByte(char what) {
    return (byte) what;
  }

  static public byte parseByte(int what) {
    return (byte) what;
  }

  static public byte parseByte(float what) {
    return (byte) what;
  }

  /*
  // nixed, no precedent
  static public byte[] parseByte(String what) {  // note: array[]
    return what.getBytes();
  }
  */

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static public byte[] parseByte(boolean what[]) {
    byte outgoing[] = new byte[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = what[i] ? (byte)1 : 0;
    }
    return outgoing;
  }

  static public byte[] parseByte(char what[]) {
    byte outgoing[] = new byte[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (byte) what[i];
    }
    return outgoing;
  }

  static public byte[] parseByte(int what[]) {
    byte outgoing[] = new byte[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (byte) what[i];
    }
    return outgoing;
  }

  static public byte[] parseByte(float what[]) {
    byte outgoing[] = new byte[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (byte) what[i];
    }
    return outgoing;
  }

  /*
  static public byte[][] parseByte(String what[]) {  // note: array[][]
    byte outgoing[][] = new byte[what.length][];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = what[i].getBytes();
    }
    return outgoing;
  }
  */

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  /*
  static public char parseChar(boolean what) {  // 0/1 or T/F ?
    return what ? 't' : 'f';
  }
  */

  static public char parseChar(byte what) {
    return (char) (what & 0xff);
  }

  static public char parseChar(int what) {
    return (char) what;
  }

  /*
  static public char parseChar(float what) {  // nonsensical
    return (char) what;
  }

  static public char[] parseChar(String what) {  // note: array[]
    return what.toCharArray();
  }
  */

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  /*
  static public char[] parseChar(boolean what[]) {  // 0/1 or T/F ?
    char outgoing[] = new char[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = what[i] ? 't' : 'f';
    }
    return outgoing;
  }
  */

  static public char[] parseChar(byte what[]) {
    char outgoing[] = new char[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (char) (what[i] & 0xff);
    }
    return outgoing;
  }

  static public char[] parseChar(int what[]) {
    char outgoing[] = new char[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (char) what[i];
    }
    return outgoing;
  }

  /*
  static public char[] parseChar(float what[]) {  // nonsensical
    char outgoing[] = new char[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (char) what[i];
    }
    return outgoing;
  }

  static public char[][] parseChar(String what[]) {  // note: array[][]
    char outgoing[][] = new char[what.length][];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = what[i].toCharArray();
    }
    return outgoing;
  }
  */

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static public int parseInt(boolean what) {
    return what ? 1 : 0;
  }

  /**
   * Note that parseInt() will un-sign a signed byte value.
   */
  static public int parseInt(byte what) {
    return what & 0xff;
  }

  /**
   * Note that parseInt('5') is unlike String in the sense that it
   * won't return 5, but the ascii value. This is because ((int) someChar)
   * returns the ascii value, and parseInt() is just longhand for the cast.
   */
  static public int parseInt(char what) {
    return what;
  }

  /**
   * Same as floor(), or an (int) cast.
   */
  static public int parseInt(float what) {
    return (int) what;
  }

  /**
   * Parse a String into an int value. Returns 0 if the value is bad.
   */
  static public int parseInt(String what) {
    return parseInt(what, 0);
  }

  /**
   * Parse a String to an int, and provide an alternate value that
   * should be used when the number is invalid.
   */
  static public int parseInt(String what, int otherwise) {
    try {
      int offset = what.indexOf('.');
      if (offset == -1) {
        return Integer.parseInt(what);
      } else {
        return Integer.parseInt(what.substring(0, offset));
      }
    } catch (NumberFormatException e) { }
    return otherwise;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static public int[] parseInt(boolean what[]) {
    int list[] = new int[what.length];
    for (int i = 0; i < what.length; i++) {
      list[i] = what[i] ? 1 : 0;
    }
    return list;
  }

  static public int[] parseInt(byte what[]) {  // note this unsigns
    int list[] = new int[what.length];
    for (int i = 0; i < what.length; i++) {
      list[i] = (what[i] & 0xff);
    }
    return list;
  }

  static public int[] parseInt(char what[]) {
    int list[] = new int[what.length];
    for (int i = 0; i < what.length; i++) {
      list[i] = what[i];
    }
    return list;
  }

  static public int[] parseInt(float what[]) {
    int inties[] = new int[what.length];
    for (int i = 0; i < what.length; i++) {
      inties[i] = (int)what[i];
    }
    return inties;
  }

  /**
   * Make an array of int elements from an array of String objects.
   * If the String can't be parsed as a number, it will be set to zero.
   *
   * String s[] = { "1", "300", "44" };
   * int numbers[] = parseInt(s);
   *
   * numbers will contain { 1, 300, 44 }
   */
  static public int[] parseInt(String what[]) {
    return parseInt(what, 0);
  }

  /**
   * Make an array of int elements from an array of String objects.
   * If the String can't be parsed as a number, its entry in the
   * array will be set to the value of the "missing" parameter.
   *
   * String s[] = { "1", "300", "apple", "44" };
   * int numbers[] = parseInt(s, 9999);
   *
   * numbers will contain { 1, 300, 9999, 44 }
   */
  static public int[] parseInt(String what[], int missing) {
    int output[] = new int[what.length];
    for (int i = 0; i < what.length; i++) {
      try {
        output[i] = Integer.parseInt(what[i]);
      } catch (NumberFormatException e) {
        output[i] = missing;
      }
    }
    return output;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  /*
  static public float parseFloat(boolean what) {
    return what ? 1 : 0;
  }
  */

  /**
   * Convert an int to a float value. Also handles bytes because of
   * Java's rules for upgrading values.
   */
  static public float parseFloat(int what) {  // also handles byte
    return what;
  }

  static public float parseFloat(String what) {
    return parseFloat(what, Float.NaN);
  }

  static public float parseFloat(String what, float otherwise) {
    try {
      return new Float(what);
    } catch (NumberFormatException ignored) { }

    return otherwise;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  /*
  static public float[] parseFloat(boolean what[]) {
    float floaties[] = new float[what.length];
    for (int i = 0; i < what.length; i++) {
      floaties[i] = what[i] ? 1 : 0;
    }
    return floaties;
  }

  static public float[] parseFloat(char what[]) {
    float floaties[] = new float[what.length];
    for (int i = 0; i < what.length; i++) {
      floaties[i] = (char) what[i];
    }
    return floaties;
  }
  */

  static public float[] parseFloat(byte what[]) {
    float floaties[] = new float[what.length];
    for (int i = 0; i < what.length; i++) {
      floaties[i] = what[i];
    }
    return floaties;
  }

  static public float[] parseFloat(int what[]) {
    float floaties[] = new float[what.length];
    for (int i = 0; i < what.length; i++) {
      floaties[i] = what[i];
    }
    return floaties;
  }

  static public float[] parseFloat(String what[]) {
    return parseFloat(what, Float.NaN);
  }

  static public float[] parseFloat(String what[], float missing) {
    float output[] = new float[what.length];
    for (int i = 0; i < what.length; i++) {
      try {
        output[i] = new Float(what[i]);
      } catch (NumberFormatException e) {
        output[i] = missing;
      }
    }
    return output;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static public String str(boolean x) {
    return String.valueOf(x);
  }

  static public String str(byte x) {
    return String.valueOf(x);
  }

  static public String str(char x) {
    return String.valueOf(x);
  }

  static public String str(int x) {
    return String.valueOf(x);
  }

  static public String str(float x) {
    return String.valueOf(x);
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static public String[] str(boolean x[]) {
    String s[] = new String[x.length];
    for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
    return s;
  }

  static public String[] str(byte x[]) {
    String s[] = new String[x.length];
    for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
    return s;
  }

  static public String[] str(char x[]) {
    String s[] = new String[x.length];
    for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
    return s;
  }

  static public String[] str(int x[]) {
    String s[] = new String[x.length];
    for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
    return s;
  }

  static public String[] str(float x[]) {
    String s[] = new String[x.length];
    for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
    return s;
  }


  //////////////////////////////////////////////////////////////

  // INT NUMBER FORMATTING


  static public String nf(float num) {
    int inum = (int) num;
    if (num == inum) {
      return str(inum);
    }
    return str(num);
  }


  static public String[] nf(float[] num) {
    String[] outgoing = new String[num.length];
    for (int i = 0; i < num.length; i++) {
      outgoing[i] = nf(num[i]);
    }
    return outgoing;
  }


  /**
   * Integer number formatter.
   */
  static private NumberFormat int_nf;
  static private int int_nf_digits;
  static private boolean int_nf_commas;

  static public String[] nf(int num[], int digits) {
    String formatted[] = new String[num.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nf(num[i], digits);
    }
    return formatted;
  }

  /**
   * ( begin auto-generated from nf.xml )
   *
   * Utility function for formatting numbers into strings. There are two
   * versions, one for formatting floats and one for formatting ints. The
   * values for the <b>digits</b>, <b>left</b>, and <b>right</b> parameters
   * should always be positive integers.<br /><br />As shown in the above
   * example, <b>nf()</b> is used to add zeros to the left and/or right of a
   * number. This is typically for aligning a list of numbers. To
   * <em>remove</em> digits from a floating-point number, use the
   * <b>int()</b>, <b>ceil()</b>, <b>floor()</b>, or <b>round()</b>
   * functions.
   *
   * ( end auto-generated )
   * @ data:string_functions
   * @param num the number(s) to format
   * @param digits number of digits to pad with zero
   * @see PApplet#nfs(float, int, int)
   * @see PApplet#nfp(float, int, int)
   * @see PApplet#nfc(float, int)
   */
  static public String nf(int num, int digits) {
    if ((int_nf != null) &&
        (int_nf_digits == digits) &&
        !int_nf_commas) {
      return int_nf.format(num);
    }

    int_nf = NumberFormat.getInstance();
    int_nf.setGroupingUsed(false); // no commas
    int_nf_commas = false;
    int_nf.setMinimumIntegerDigits(digits);
    int_nf_digits = digits;
    return int_nf.format(num);
  }

/**
   * ( begin auto-generated from nfc.xml )
   *
   * Utility function for formatting numbers into strings and placing
   * appropriate commas to mark units of 1000. There are two versions, one
   * for formatting ints and one for formatting an array of ints. The value
   * for the <b>digits</b> parameter should always be a positive integer.
   * <br/> <br/>
   * For a non-US locale, this will insert periods instead of commas, or
   * whatever is apprioriate for that region.
   *
   * ( end auto-generated )
 * @ data:string_functions
 * @param num the number(s) to format
 * @see PApplet#nf(float, int, int)
 * @see PApplet#nfp(float, int, int)
 * @see PApplet#nfs(float, int, int)
 */
  static public String[] nfc(int num[]) {
    String formatted[] = new String[num.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfc(num[i]);
    }
    return formatted;
  }


  /**
   * nfc() or "number format with commas". This is an unfortunate misnomer
   * because in locales where a comma is not the separator for numbers, it
   * won't actually be outputting a comma, it'll use whatever makes sense for
   * the locale.
   */
  static public String nfc(int num) {
    if ((int_nf != null) &&
        (int_nf_digits == 0) &&
        int_nf_commas) {
      return int_nf.format(num);
    }

    int_nf = NumberFormat.getInstance();
    int_nf.setGroupingUsed(true);
    int_nf_commas = true;
    int_nf.setMinimumIntegerDigits(0);
    int_nf_digits = 0;
    return int_nf.format(num);
  }


  /**
   * number format signed (or space)
   * Formats a number but leaves a blank space in the front
   * when it's positive so that it can be properly aligned with
   * numbers that have a negative sign in front of them.
   */

  /**
   * ( begin auto-generated from nfs.xml )
   *
   * Utility function for formatting numbers into strings. Similar to
   * <b>nf()</b> but leaves a blank space in front of positive numbers so
   * they align with negative numbers in spite of the minus symbol. There are
   * two versions, one for formatting floats and one for formatting ints. The
   * values for the <b>digits</b>, <b>left</b>, and <b>right</b> parameters
   * should always be positive integers.
   *
   * ( end auto-generated )
  * @ data:string_functions
  * @param num the number(s) to format
  * @param digits number of digits to pad with zeroes
  * @see PApplet#nf(float, int, int)
  * @see PApplet#nfp(float, int, int)
  * @see PApplet#nfc(float, int)
  */
  static public String nfs(int num, int digits) {
    return (num < 0) ? nf(num, digits) : (' ' + nf(num, digits));
  }

  static public String[] nfs(int num[], int digits) {
    String formatted[] = new String[num.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfs(num[i], digits);
    }
    return formatted;
  }

  //

  /**
   * number format positive (or plus)
   * Formats a number, always placing a - or + sign
   * in the front when it's negative or positive.
   */
 /**
   * ( begin auto-generated from nfp.xml )
   *
   * Utility function for formatting numbers into strings. Similar to
   * <b>nf()</b> but puts a "+" in front of positive numbers and a "-" in
   * front of negative numbers. There are two versions, one for formatting
   * floats and one for formatting ints. The values for the <b>digits</b>,
   * <b>left</b>, and <b>right</b> parameters should always be positive integers.
   *
   * ( end auto-generated )
  * @ data:string_functions
  * @param num the number(s) to format
  * @param digits number of digits to pad with zeroes
  * @see PApplet#nf(float, int, int)
  * @see PApplet#nfs(float, int, int)
  * @see PApplet#nfc(float, int)
  */
  static public String nfp(int num, int digits) {
    return (num < 0) ? nf(num, digits) : ('+' + nf(num, digits));
  }

  static public String[] nfp(int num[], int digits) {
    String formatted[] = new String[num.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfp(num[i], digits);
    }
    return formatted;
  }



  //////////////////////////////////////////////////////////////

  // FLOAT NUMBER FORMATTING


  static private NumberFormat float_nf;
  static private int float_nf_left, float_nf_right;
  static private boolean float_nf_commas;

  static public String[] nf(float num[], int left, int right) {
    String formatted[] = new String[num.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nf(num[i], left, right);
    }
    return formatted;
  }
/**
 * @param left number of digits to the left of the decimal point
 * @param right number of digits to the right of the decimal point
 */
  static public String nf(float num, int left, int right) {
    if ((float_nf != null) &&
        (float_nf_left == left) &&
        (float_nf_right == right) &&
        !float_nf_commas) {
      return float_nf.format(num);
    }

    float_nf = NumberFormat.getInstance();
    float_nf.setGroupingUsed(false);
    float_nf_commas = false;

    if (left != 0) float_nf.setMinimumIntegerDigits(left);
    if (right != 0) {
      float_nf.setMinimumFractionDigits(right);
      float_nf.setMaximumFractionDigits(right);
    }
    float_nf_left = left;
    float_nf_right = right;
    return float_nf.format(num);
  }

/**
 * @param right number of digits to the right of the decimal point
 */
  static public String[] nfc(float num[], int right) {
    String formatted[] = new String[num.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfc(num[i], right);
    }
    return formatted;
  }


  static public String nfc(float num, int right) {
    if ((float_nf != null) &&
        (float_nf_left == 0) &&
        (float_nf_right == right) &&
        float_nf_commas) {
      return float_nf.format(num);
    }

    float_nf = NumberFormat.getInstance();
    float_nf.setGroupingUsed(true);
    float_nf_commas = true;

    if (right != 0) {
      float_nf.setMinimumFractionDigits(right);
      float_nf.setMaximumFractionDigits(right);
    }
    float_nf_left = 0;
    float_nf_right = right;
    return float_nf.format(num);
  }


 /**
  * @param left the number of digits to the left of the decimal point
  * @param right the number of digits to the right of the decimal point
  */
  static public String[] nfs(float num[], int left, int right) {
    String formatted[] = new String[num.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfs(num[i], left, right);
    }
    return formatted;
  }

  static public String nfs(float num, int left, int right) {
    return (num < 0) ? nf(num, left, right) :  (' ' + nf(num, left, right));
  }

 /**
  * @param left the number of digits to the left of the decimal point
  * @param right the number of digits to the right of the decimal point
  */
  static public String[] nfp(float num[], int left, int right) {
    String formatted[] = new String[num.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfp(num[i], left, right);
    }
    return formatted;
  }

  static public String nfp(float num, int left, int right) {
    return (num < 0) ? nf(num, left, right) :  ('+' + nf(num, left, right));
  }



  //////////////////////////////////////////////////////////////

  // HEX/BINARY CONVERSION


  /**
   * ( begin auto-generated from hex.xml )
   *
   * Converts a byte, char, int, or color to a String containing the
   * equivalent hexadecimal notation. For example color(0, 102, 153) will
   * convert to the String "FF006699". This function can help make your geeky
   * debugging sessions much happier.
   * <br/> <br/>
   * Note that the maximum number of digits is 8, because an int value can
   * only represent up to 32 bits. Specifying more than eight digits will
   * simply shorten the string to eight anyway.
   *
   * ( end auto-generated )
   * @ data:conversion
   * @param value the value to convert
   * @see PApplet#unhex(String)
   * @see PApplet#binary(byte)
   * @see PApplet#unbinary(String)
   */
  static public String hex(byte value) {
    return hex(value, 2);
  }

  static public String hex(char value) {
    return hex(value, 4);
  }

  static public String hex(int value) {
    return hex(value, 8);
  }
/**
 * @param digits the number of digits (maximum 8)
 */
  static public String hex(int value, int digits) {
    String stuff = Integer.toHexString(value).toUpperCase();
    if (digits > 8) {
      digits = 8;
    }

    int length = stuff.length();
    if (length > digits) {
      return stuff.substring(length - digits);

    } else if (length < digits) {
      return "00000000".substring(8 - (digits-length)) + stuff;
    }
    return stuff;
  }

 /**
   * ( begin auto-generated from unhex.xml )
   *
   * Converts a String representation of a hexadecimal number to its
   * equivalent integer value.
   *
   * ( end auto-generated )
   *
   * @ data:conversion
   * @param value String to convert to an integer
   * @see PApplet#hex(int, int)
   * @see PApplet#binary(byte)
   * @see PApplet#unbinary(String)
   */
  static public int unhex(String value) {
    // has to parse as a Long so that it'll work for numbers bigger than 2^31
    return (int) (Long.parseLong(value, 16));
  }

  //

  /**
   * Returns a String that contains the binary value of a byte.
   * The returned value will always have 8 digits.
   */
  static public String binary(byte value) {
    return binary(value, 8);
  }

  /**
   * Returns a String that contains the binary value of a char.
   * The returned value will always have 16 digits because chars
   * are two bytes long.
   */
  static public String binary(char value) {
    return binary(value, 16);
  }

  /**
   * Returns a String that contains the binary value of an int. The length
   * depends on the size of the number itself. If you want a specific number
   * of digits use binary(int what, int digits) to specify how many.
   */
  static public String binary(int value) {
    return binary(value, 32);
  }

  /*
   * Returns a String that contains the binary value of an int.
   * The digits parameter determines how many digits will be used.
   */

 /**
   * ( begin auto-generated from binary.xml )
   *
   * Converts a byte, char, int, or color to a String containing the
   * equivalent binary notation. For example color(0, 102, 153, 255) will
   * convert to the String "11111111000000000110011010011001". This function
   * can help make your geeky debugging sessions much happier.
   * <br/> <br/>
   * Note that the maximum number of digits is 32, because an int value can
   * only represent up to 32 bits. Specifying more than 32 digits will simply
   * shorten the string to 32 anyway.
   *
   * ( end auto-generated )
  * @ data:conversion
  * @param value value to convert
  * @param digits number of digits to return
  * @see PApplet#unbinary(String)
  * @see PApplet#hex(int,int)
  * @see PApplet#unhex(String)
  */
  static public String binary(int value, int digits) {
    String stuff = Integer.toBinaryString(value);
    if (digits > 32) {
      digits = 32;
    }

    int length = stuff.length();
    if (length > digits) {
      return stuff.substring(length - digits);

    } else if (length < digits) {
      int offset = 32 - (digits-length);
      return "00000000000000000000000000000000".substring(offset) + stuff;
    }
    return stuff;
  }


 /**
   * ( begin auto-generated from unbinary.xml )
   *
   * Converts a String representation of a binary number to its equivalent
   * integer value. For example, unbinary("00001000") will return 8.
   *
   * ( end auto-generated )
   * @ data:conversion
   * @param value String to convert to an integer
   * @see PApplet#binary(byte)
   * @see PApplet#hex(int,int)
   * @see PApplet#unhex(String)
   */
  static public int unbinary(String value) {
    return Integer.parseInt(value, 2);
  }
}
