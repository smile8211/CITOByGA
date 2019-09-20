package smashed;//import the packages for using the classes in them into the program
import java.awt.*;
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
/*@(UndoRedo)*/
 


	@Feature___("UndoRedo")
	public class RedoAction extends AbstractAction{
/*@(UndoRedo)*/
 
      @Feature___("UndoRedo")
	protected Notepad notepad;
/*@(UndoRedo)*/
 

		@Feature___("UndoRedo")
		public RedoAction(Notepad notepad){
			super("Redo");
			putValue( Action.SMALL_ICON, 
				Notepad.getImageIcon("redo.gif"));
			setEnabled(false);
			this.notepad = notepad;
		}
/*@(UndoRedo)*/
 
		@Feature___("UndoRedo")
		public void actionPerformed(ActionEvent e){
			try{
				notepad.undo.redo();
			}
			catch (CannotRedoException ex){
				System.out.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			update();
			notepad.undoAction.update();
		}
/*@(UndoRedo)*/
 
		@Feature___("UndoRedo")
		protected void update(){
			if(notepad.undo.canRedo()){
				setEnabled(true);
				putValue("Redo", notepad.undo.getRedoPresentationName());
			}
			else{
				setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}
