/****************************************************************************
Copyright (c) 2004, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.mosaic;

import static edu.mines.jtk.util.M.*;
import edu.mines.jtk.util.*;

/**
 * Converts (projects) world coordinates v to/from normalized coordinates u.
 * The projection is a simple scale and translation, such that specified
 * world coordinates (v0,v1) correspond to specified normalized coordinates 
 * (u0,u1).
 * <p>
 * Specifically, a projector computes u = shift+scale*v, where scale = 
 * (u1-u0)/(v1-v0), and shift = u0-scale*v0. The projection exists only 
 * for v1 != v0. However, v0 and v1 are otherwise unconstrained. v0 may 
 * be greater than v1.
 * <p>
 * The projection from normalized coordinates u to world coordinates v is 
 * simply the inverse, and this inverse exists only for u1 != u0.
 * <p> 
 * By definition, u0 is closest to normalized coordinate u=0, and u1 is 
 * closest to normalized coordinate u=1. These coordinates must satisfy
 * the constraints 0.0 &lt;= u0 &lt; u1 &lt;= 1.0.
 * <p>
 * Typically, the coordinates (v0,v1) represent bounds in world coordinate 
 * space. Then, the gaps in normalized coordinate space [0,u0) and (u1,1] 
 * represent margins, extra space needed for graphic rendering. The amount 
 * of extra space required varies, depending on the graphics. Accounting for 
 * this varying amount of extra space is a complex but important aspect of 
 * aligning the coordinate systems of two or more graphics.
 * <p>
 * Alignment is accomplished by simply rendering all graphics using
 * the same projector. We obtain this shared projector by merging the 
 * preferred projectors of each graphic. A preferred projector is one
 * that a graphic might use if it were the only one being rendered.
 * <p>
 * We assume that each graphic has a preferred projector that indicates 
 * the world coordinate span [v0,v1] and margins [0,u0) and (u1,1] that
 * it would prefer if it were the only graphic rendered. We then merge
 * two projectors into one so that the merged projector contains the 
 * union of the two world coordinate spans and has adequate margins. 
 * <p>
 * A projector has a sign, which is the sign of v1-v0. Note that this sign 
 * is never ambiguous, because v1 never equals v0. When merging a projector 
 * B into into a projector A, we preserve the sign of projector A.
 *
 * @author Dave Hale, Colorado School of Mines
 * @version 2005.01.01
 */
public class Projector implements Cloneable {

  /**
   * Constructs a projector with specified v and u values. The
   * parameters u0 and u1 determine the margins of the projector.
   * The world coordinate v0 corresponds to normalized coordinate u0;
   * the world coordinate v1 corresponds to normalized coordinate u1.
   * @param v0 the v coordinate that corresponds to u coordinate u0;
   *  v0 != v1 is required.
   * @param v1 the v coordinate that corresponds to u coordinate u1;
   *  v0 != v1 is required.
   * @param u0 the u coordinate closest to normalized coordinate 0;
   *  0.0 &lt;= u0 &lt; u1 is required.
   * @param u1 the u coordinate closest to normalized coordinate 1;
   *  u0 &lt; u1 &lt;= 1.0 is required.
   */
  public Projector(double v0, double v1, double u0, double u1) {
    Check.argument(0.0<=u0,"0.0 <= u0");
    Check.argument(u0<u1,"u0 < u1");
    Check.argument(u1<=1.0,"u1 <= 1.0");
    Check.argument(v0!=v1,"v0 != v1");
    _u0 = u0;
    _u1 = u1;
    _v0 = v0;
    _v1 = v1;
    computeShiftsAndScales();
  }

  /**
   * Returns a clone of this projector.
   * @return the clone.
   */
  public Projector clone() {
    return new Projector(_v0,_v1,_u0,_u1);
  }

  /**
   * Returns normalized coordinate u corresponding to world coordinate v.
   * @param v world coordinate v.
   * @return normalized coordinate u.
   */
  public double u(double v) {
    return _vshift+_vscale*v;
  }

  /**
   * Returns world coordinate v corresponding to normalized coordinate u.
   * @param u normalized coordinate u.
   * @return world coordinate v.
   */
  public double v(double u) {
    return _ushift+_uscale*u;
  }

  /**
   * Returns the u-coordinate bound closest to u=0.
   * @return the u-coordinate bound.
   */
  public double u0() {
    return _u0;
  }

  /**
   * Returns the u-coordinate bound closest to u=1.
   * @return the u-coordinate bound.
   */
  public double u1() {
    return _u1;
  }

  /**
   * Returns the v-coordinate bound closest to u=0.
   * @return the v-coordinate bound.
   */
  public double v0() {
    return _v0;
  }

  /**
   * Returns the v-coordinate bound closest to u=1.
   * @return the v-coordinate bound.
   */
  public double v1() {
    return _v1;
  }

  /**
   * Merges the specified projector into this projector. 
   * @param p the projector.
   */
  public void merge(Projector p) {

    // Ignore null projectors.
    if (p==null)
      return;

    // Parameters for this projector A.
    double u0a = _u0;
    double u1a = _u1;
    double v0a = _v0;
    double v1a = _v1;

    // Parameters for that projector B.
    double u0b = p._u0;
    double u1b = p._u1;
    double v0b = p._v0;
    double v1b = p._v1;

    // Merged world coordinate bounds. Preserve sign of projector A.
    double vmin = min(min(v0a,v1a),min(v0b,v1b));
    double vmax = max(max(v0a,v1a),max(v0b,v1b));
    _v0 = (v0a<v1a)?vmin:vmax;
    _v1 = (v0a<v1a)?vmax:vmin;

    // If sign of B does not equal sign of A, flip B.
    double ra = (v1a-v0a)/(_v1-_v0); 
    double rb = (v1b-v0b)/(_v1-_v0);
    if (rb<0.0) {
      rb = -rb;
      double u0t = u0b;
      u0b = 1.0-u1b;
      u1b = 1.0-u0t;
      double v0t = v0b;
      v0b = v1b;
      v1b = v0t;
    }

    // Merged normalized coordinate bounds. This is tricky. The merged 
    // bound u0 equals the maximum of sa*u0a and sb*u0b, where sa and sb 
    // are scale factors that depend on the difference u1-u0. Specifically 
    // sa = ra*(u1-u0)/(u1a-u0a), and sb = rb*(u1-u0)/(u1b-u0b). It seems 
    // that we cannot get u0 without knowing u0 and u1. However, we also 
    // know that the merged margin 1-u1 equals the maximum of sa*(1-u1a)
    // and sb*(1-u1b). So we have two linear equations that we solve for 
    // the two unknowns u0 and u1. Note: 0 <= u0 < u1 <= 1 is invariant.
    double ca = u0a*ra/(u1a-u0a);
    double cb = u0b*rb/(u1b-u0b);
    double c = max(ca,cb);
    double da = (1.0-u1a)*ra/(u1a-u0a);
    double db = (1.0-u1b)*rb/(u1b-u0b);
    double d = max(da,db);
    _u0 = c/(1.0+c+d);
    _u1 = 1.0-d/(1.0+c+d);

    // Recompute shifts and scales.
    computeShiftsAndScales();
  }

  /**
   * Gets the scale ratio for this projector and a specified projector.
   * Recall that a projector converts world coordinates v to normalized 
   * coordinates u by a simple scaling and translation: u = shift+scale*v. 
   * This method returns the scale for this projector divided by the scale 
   * for the specified projector.
   * <p>
   * This method is typically used to account for the effects of merging
   * two or more projectors. For example, after merging, parameters that 
   * are proportional to the sizes of margins [0,u0) or (u1,1] in the
   * specified projector should be scaled by this ratio before being used
   * with this projector.
   * @return the scale factor.
   */
  public double getScaleRatio(Projector p) {
    return _vscale/p._vscale;
  }

  public boolean equals(Object obj) {
    if (this==obj)
      return true;
    if (obj==null || this.getClass()!=obj.getClass())
      return false;
    Projector that = (Projector)obj;
    return this._u0==that._u0 && 
           this._u1==that._u1 &&
           this._v0==that._v0 &&
           this._v1==that._v1;
  }

  public int hashCode() {
    long u0bits = Double.doubleToLongBits(_u0);
    long u1bits = Double.doubleToLongBits(_u1);
    long v0bits = Double.doubleToLongBits(_v0);
    long v1bits = Double.doubleToLongBits(_v1);
    return (int)(u0bits^(u0bits>>>32) ^
                 u1bits^(u1bits>>>32) ^
                 v0bits^(v0bits>>>32) ^
                 v1bits^(v1bits>>>32));
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private double _u0,_u1;
  private double _v0,_v1;
  private double _ushift,_uscale;
  private double _vshift,_vscale;

  private void computeShiftsAndScales() {
    _uscale = (_v1-_v0)/(_u1-_u0);
    _ushift = _v0-_uscale*_u0;
    _vscale = (_u1-_u0)/(_v1-_v0);
    _vshift = _u0-_vscale*_v0;
  }
}