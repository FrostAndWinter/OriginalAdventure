/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2005-08 Ben Fry and Casey Reas

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

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


public interface PMatrix {
  
  void reset();
  
  /**
   * Returns a copy of this PMatrix.
   */
  PMatrix get();

  /**
   * Copies the matrix contents into a float array.
   * If target is null (or not the correct size), a new array will be created.
   */
  float[] get(float[] target);
  
  
  void set(PMatrix src);

  void set(float[] source);

  void set(float m00, float m01, float m02,
           float m10, float m11, float m12);

  void set(float m00, float m01, float m02, float m03,
           float m10, float m11, float m12, float m13,
           float m20, float m21, float m22, float m23,
           float m30, float m31, float m32, float m33);

  
  void translate(float tx, float ty);
  
  void translate(float tx, float ty, float tz);

  void rotate(float angle);

  void rotateX(float angle);

  void rotateY(float angle);

  void rotateZ(float angle);

  void rotate(float angle, float v0, float v1, float v2);

  void scale(float s);

  void scale(float sx, float sy);

  void scale(float x, float y, float z);
  
  void shearX(float angle);
  
  void shearY(float angle);

  /** 
   * Multiply this matrix by another.
   */
  void apply(PMatrix source);

  void apply(PMatrix2D source);

  void apply(PMatrix3D source);

  void apply(float n00, float n01, float n02,
             float n10, float n11, float n12);

  void apply(float n00, float n01, float n02, float n03,
             float n10, float n11, float n12, float n13,
             float n20, float n21, float n22, float n23,
             float n30, float n31, float n32, float n33);

  /**
   * Apply another matrix to the left of this one.
   */
  void preApply(PMatrix left);

  void preApply(PMatrix2D left);

  void preApply(PMatrix3D left);

  void preApply(float n00, float n01, float n02,
                float n10, float n11, float n12);

  void preApply(float n00, float n01, float n02, float n03,
                float n10, float n11, float n12, float n13,
                float n20, float n21, float n22, float n23,
                float n30, float n31, float n32, float n33);

  
  /** 
   * Multiply a PVector by this matrix. 
   */
  PVector mult(PVector source, PVector target);
  
  
  /** 
   * Multiply a multi-element vector against this matrix. 
   */
  float[] mult(float[] source, float[] target);
  
  
//  public float multX(float x, float y);
//  public float multY(float x, float y);
  
//  public float multX(float x, float y, float z);
//  public float multY(float x, float y, float z);
//  public float multZ(float x, float y, float z);  
  
  
  /**
   * Transpose this matrix.
   */
  void transpose();

  
  /**
   * Invert this matrix.
   * @return true if successful
   */
  boolean invert();
  
  
  /**
   * @return the determinant of the matrix
   */
  float determinant();
}