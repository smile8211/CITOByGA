package smashed;import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import annotationclasses___.Feature___;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.awt.print.*;
/*@(Base___)*/
 


/**
 *A PUBLIC CLASS FOR CENTER.JAVA
 */
@Feature___("Base___")
public class Center
{
/*@(Base___)*/
 
   @Feature___("Base___")
protected Notepad n;
/*@(Base___)*/
  //for using the object in the Notepad.java

	@Feature___("Base___")
	public Center()
	{
	}
/*@(Base___)*/
 

   @Feature___("Base___")
public Center(Notepad n)
   {
      this.n = n;
   }
/*@(Base___)*/
 

   @Feature___("Base___")
public void nCenter()
   {
      //Centering the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      n.setLocation((screenSize.width-n.getWidth())/2,
	       (screenSize.height-n.getHeight())/2);
   }
/*@(Format)*/
 
	@Feature___("Format")
	protected Fonts f;
/*@(Format)*/
  //for using the object in the Fonts.java

	@Feature___("Format")
	public Center(Fonts f)
	{
		this.f = f;
	}
/*@(Format)*/
 

	@Feature___("Format")
	public void fCenter()
	{
		//Centering the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation((screenSize.width-f.getWidth())/2,
			(screenSize.height-f.getHeight())/2);
	}
}
