/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.sgl;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

import edu.mines.jtk.awt.*;
import edu.mines.jtk.dsp.Sampling;

/**
 * A simple frame for 3D graphics.
 * @author Chris Engelsma and Dave Hale, Colorado School of Mines.
 * @version 2009.07.20
 */
public class SimpleFrame extends JFrame {

  /**
   * Constructs a simple frame with default parameters.
   * Axes orientation defaults to x-right, y-top and z-down.
   */
  public SimpleFrame() {
    this(AxesOrientation.XRIGHT_YOUT_ZDOWN);
  }

  /**
   * Constructs a simple frame with specified axes orientation.
   * @param ao the axes orientation.
   */
  public SimpleFrame(AxesOrientation ao) {
    this(null, ao);
  }

  /**
   * Constructs a simple frame with specified world view and orientation.
   * @param world the world view.
   * @param ao the axes orientation.
   */
  public SimpleFrame(World world, AxesOrientation ao) {
    if (_world==null)
      _world = new World();
    _view = new OrbitView(_world);
    _view.setAxesOrientation(ao);
    ViewCanvas canvas = new ViewCanvas();
    canvas.setView(_view);

    ModeManager mm = new ModeManager();
    mm.add(canvas);
    OrbitViewMode ovm = new OrbitViewMode(mm);
    SelectDragMode sdm = new SelectDragMode(mm);

    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');
    Action exitAction = new AbstractAction("Exit") {
      public void actionPerformed(ActionEvent event) {
        System.exit(0);
      }
    };
    JMenuItem exitItem = fileMenu.add(exitAction);
    exitItem.setMnemonic('X');

    JMenu modeMenu = new JMenu("Mode");
    modeMenu.setMnemonic('M');
    JMenuItem ovmItem = new JMenuItem(ovm);
    modeMenu.add(ovmItem);
    JMenuItem sdmItem = new JMenuItem(sdm);
    modeMenu.add(sdmItem);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(modeMenu);

    JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
    toolBar.setRollover(true);
    JToggleButton ovmButton = new ModeToggleButton(ovm);
    toolBar.add(ovmButton);
    JToggleButton sdmButton = new ModeToggleButton(sdm);
    toolBar.add(sdmButton);

    ovm.setActive(true);

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setSize(new Dimension(SIZE,SIZE));
    this.add(canvas,BorderLayout.CENTER);
    this.add(toolBar,BorderLayout.WEST);
    this.setJMenuBar(menuBar);
    this.setVisible(true);
  }

  /**
   * Returns a new simple frame with a triangle group.
   * Triangles will be constructed as vertex normals.
   * @param xyz array of packed vertex coordinates.
   * @return a simple frame.
   */
  public static SimpleFrame asTriangles(float[] xyz) {
    return asTriangles(true,xyz);
  }

  /**
   * Returns a new simple frame with a triangle group.
   * @param vn true, for vertex normals; false, for triangle normals.
   * @param xyz array of packed vertex coordinates.
   * @return the simple frame.
   */
  public static SimpleFrame asTriangles(boolean vn, float[] xyz) {
    return asTriangles(vn,xyz,null);
  }

  /**
   * Returns a new simple frame with a triangle group.
   * Triangles will be constructed as vertex normals.
   * @param xyz array of packed vertex coordinates.
   * @param rgb array of packed color coordinates.
   * @return the simple frame.
   */
  public static SimpleFrame asTriangles(float[] xyz, float[] rgb) {
    return asTriangles(true,xyz,rgb);
  }
  /**
   * Returns a new simple frame with a triangle group.
   * @param vn true, for vertex normals; false, for triangle normals.
   * @param sx sampling of x coordinates; may be non-uniform.
   * @param sy sampling of y coordinates; may be non-uniform.
   * @param z array[nx][ny] of z coordinates z = f(x,y).
   */
  public SimpleFrame asTriangles(
    boolean vn, Sampling sx, Sampling sy, float[][] z)
  {
    return asTriangles(new TriangleGroup(vn,sx,sy,z));
  }

  /**
   * Returns a new simple frame with a triangle group.
   * @param vn true, for vertex normals; false, for triangle normals.
   * @param sx sampling of x coordinates; may be non-uniform.
   * @param sy sampling of y coordinates; may be non-uniform.
   * @param z array[nx][ny] of z coordinates z = f(x,y).
   * @param r array[nx][ny] of red color components.
   * @param g array[nx][ny] of green color components.
   * @param b array[nx][ny] of blue color components.
   */
  public SimpleFrame asTriangles(
    boolean vn, Sampling sx, Sampling sy, float[][] z,
    float[][] r, float[][] g, float[][] b)
  {
    return asTriangles(new TriangleGroup(vn,sx,sy,z,r,g,b));
  }

  /**
   * Returns a new simple frame with a triangle group.
   * @param vn true, for vertex normals; false, for triangle normals
   * @param xyz array of packed vertex coordinates.
   * @param rgb array of packed color coordinates.
   * @return the simple frame.
   */
  public static SimpleFrame asTriangles(boolean vn, float[] xyz, float[] rgb) {
    return asTriangles(new TriangleGroup(vn,xyz,rgb));
  }

  /**
   * Returns a new simple frame with a triangle group.
   * @param tg a triangle group.
   * @return the simple frame.
   */
  public static SimpleFrame asTriangles(TriangleGroup tg) {
    SimpleFrame sf = new SimpleFrame();
    sf.addTriangles(tg);
    sf.getOrbitView().setWorldSphere(tg.getBoundingSphere(true));
    return sf;
  }

  /**
   * Returns a new simple frame with an image panel group.
   * @param s1 sampling in the 1st dimension (Z).
   * @param s2 sampling in the 2nd dimension (Y).
   * @param s3 sampling in the 3rd dimension (X).
   * @param f a 3D array.
   * @return the simple frame.
   */
  public static SimpleFrame asImagePanels(
          Sampling s1, Sampling s2, Sampling s3, float[][][] f) {
    return asImagePanels(new ImagePanelGroup(s1,s2,s3,f));
  }


  /**
   * Returns a new simple frame with an image panel group.
   * @param ipg an image panel group.
   * @return the simple frame.
   */
  public static SimpleFrame asImagePanels(ImagePanelGroup ipg) {
    SimpleFrame sf = new SimpleFrame();
    sf.addImagePanelGroup(ipg);
    sf.getOrbitView().setWorldSphere(ipg.getBoundingSphere(true));
    return sf;
  }

  /**
   * Adds a triangle group with specified vertex coordinates.
   * @param xyz array of packed vertex coordinates.
   * @return the triangle group.
   */
  public TriangleGroup addTriangles(float[] xyz) {
    return addTriangles(new TriangleGroup(true,xyz,null));
  }

  /**
   * Adds a triangle group with specified vertex coordinates and colors.
   * @param xyz array of packed vertex coordinates.
   * @param rgb array of packed color components.
   * @return the triangle group.
   */
  public TriangleGroup addTriangles(float[] xyz, float[] rgb) {
    return addTriangles(new TriangleGroup(true,xyz,rgb));
  }

  /**
   * Adds a triangle group for a sampled function z = f(x,y).
   * @param sx sampling of x coordinates; may be non-uniform.
   * @param sy sampling of y coordinates; may be non-uniform.
   * @param z array[nx][ny] of z coordinates z = f(x,y).
   */
  public TriangleGroup addTriangles(Sampling sx, Sampling sy, float[][] z) {
    return addTriangles(new TriangleGroup(true,sx,sy,z));
  }

  /**
   * Adds a triangle group to a simple frame from a given triangle group.
   * @param tg a triangle group.
   * @return the attached triangle group.
   */
  public TriangleGroup addTriangles(TriangleGroup tg) {
    _world.addChild(tg);
    return tg;
  }

  /**
   * Adds an image panel to a simple frame from a given image panel.
   * @param ip an image panel.
   * @return the attached image panel.
   */
  public ImagePanel addImagePanel(ImagePanel ip) {
    _world.addChild(ip);
    return ip;
  }

  /**
   * Adds an image panel group to a simple frame from a given 3D array
   * @param f a 3D array.
   * @return the image panel group.
   */
  public ImagePanelGroup addImagePanelGroup(float[][][] f) {
    return addImagePanelGroup(new Sampling(f[0][0].length),
                              new Sampling(f[0].length),
                              new Sampling(f.length),
                              f);
  }

  /**
   * Adds an image panel group to a simple frame from given samplings and
   * a 3D array.
   * @param s1 sampling in the 1st dimension (Z).
   * @param s2 sampling in the 2nd dimension (Y).
   * @param s3 sampling in the 3rd dimension (X).
   * @param f a 3D array.
   * @return the image panel group.
   */
  public ImagePanelGroup addImagePanelGroup(
          Sampling s1, Sampling s2, Sampling s3, float[][][] f) {
    return addImagePanelGroup(new ImagePanelGroup(s1,s2,s3,f));
  }

  /**
   * Adds an image panel group to a simple frame from a given image panel
   * group.
   * @param ipg an image panel group.
   * @return the attached image panel group.
   */
  public ImagePanelGroup addImagePanelGroup(ImagePanelGroup ipg) {
    _world.addChild(ipg);
    return ipg;
  }

  /**
   * Gets the orbit view for this frame.
   * @return the orbit view.
   */
  public OrbitView getOrbitView() {
    return _view;
  }

  /**
   * Gets the world for this frame.
   * @return the world.
   */
  public World getWorld() {
    return _world;
  }

  /**
   * Sets the bounding sphere of the frame with a given center point and
   * radius.
   * @param p the center point.
   * @param r the radius.
   */
  public void setWorldSphere(Point3 p, int r) {
    setWorldSphere(new BoundingSphere(p,r));
  }

  /**
   * Sets the bounding sphere of the frame with a given center x, y, z and
   * radius.
   * @param x the center X-coordinate.
   * @param y the center Y-coordinate.
   * @param z the center Z-coordinate.
   * @param r the radius.
   */
  public void setWorldSphere(double x, double y, double z, double r) {
    setWorldSphere(new BoundingSphere(x,y,z,r));
  }

  /**
   * Sets the bounding sphere of the frame.
   * @param xmin the minimum x coordinate.
   * @param ymin the minimum y coordinate.
   * @param zmin the minimum z coordinate.
   * @param xmax the maximum x coordinate.
   * @param ymax the maximum y coordinate.
   * @param zmax the maximum z coordinate.
   */
  public void setWorldSphere(
    double xmin, double ymin, double zmin,
    double xmax, double ymax, double zmax)
  {
    setWorldSphere(
      new BoundingSphere(
        new BoundingBox(xmin,ymin,zmin,xmax,ymax,zmax)));
  }

  /**
   * Sets the bounding sphere of the frame.
   * @param bs the bounding sphere.
   */
  public void setWorldSphere(BoundingSphere bs) {
    _view.setWorldSphere(bs);
  }

/////////////////////////////////////////////////////////////////////////////
// private

  private OrbitView _view;
  private World _world;
  private static final int SIZE = 600;

}