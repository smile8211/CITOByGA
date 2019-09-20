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
 *A CLASS FOR CREATING ABOUT PANEL
 */
@Feature___("Base___")
public class About extends JPanel
{
/*@(Base___)*/
 
   @Feature___("Base___")
public About()
   {
      //Create a Label & an image icon in it
      JLabel label1 = new JLabel
		  (Notepad.getImageIcon("java.gif"));
      
      //adding label1 to the JPanel
      this.add(label1);
      
      //Create a Label & put a HTML script
      JLabel label2 = new JLabel("<html><li>JAVA(TM) Notepad</li><li><p>Ver# 2.0</li>"
	 +"<li><p>Coded by: Salah Al-Thubaiti</li><li><p>KFUPM, CS</li><li>"
	 +"<p>CopyRight© 2001-2002</li></html>");
   
      //adding label2 to the JPanel
      this.add(label2);
   }
}
