package tests;

import java.awt.Dimension;

import javax.swing.JTextArea;

import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;

import runspl.RunSPL;
import smashed.Notepad;

public class NotepadTEST {
	public static NotepadTEST SINGLETON = new NotepadTEST();
	public boolean BASE___;
	public boolean BASEMENUBAR___;
	public boolean BASETOOLBAR___;
	public boolean EDIT___;
	public boolean EDITMENUBAR___;
	public boolean EDITTOOLBAR___;
	public boolean FORMAT___;
	public boolean FORMATMENUBAR___;
	public boolean FORMATTOOLBAR___;
	public boolean PERSISTENCE___;
	public boolean PERSISTENCEMENUBAR___;
	public boolean PERSISTENCETOOLBAR___;
	public boolean PRINT___;
	public boolean PRINTMENUBAR___;
	public boolean PRINTTOOLBAR___;
	public boolean SEARCH___;
	public boolean SEARCHMENUBAR___;
	public boolean SEARCHTOOLBAR___;
	public boolean UNDOREDO___;
	public boolean UNDOREDOMENUBAR___;
	public boolean UNDOREDOTOOLBAR___;
	public boolean WORDCOUNT___;
	public boolean WORDCOUNTMENUBAR___;
	public boolean WORDCOUNTTOOLBAR___;

	static String FEATURE_MODEL_PATH;
	static String EXECUTION_MODE;
	static String TEST_CASE;
		
	public static void main(String args[])
	{
		RunSPL.loadOneConfiguration(NotepadTEST.class);
		int testCase = Integer.parseInt(args[args.length-2]);
		
		runTest(testCase);
	}
	
	public static void runTest(final int testcase)
	{
		FailOnThreadViolationRepaintManager.install();
	 	  
		FrameFixture window;
	    Notepad frame = GuiActionRunner.execute(new GuiQuery<Notepad>() {
	        protected Notepad executeInEDT() {
	          Notepad n = new Notepad();
	          
	          if(testcase == 0)
	          {
	        	  // do nothing
	          }
	          else if(testcase == 1)
	          {
	        	  n.constructToolBar();
	          }
	          else if(testcase == 2)
	          {
	        	  n.constructMenu();
	          }
	          
	          return n;
	        }
	    });
	    window = new FrameFixture(frame);
	    window.show(new Dimension(500, 500));
	    	    
	    String text = "Hello";
	    GenericTypeMatcher<JTextArea> textAreaMatcher = new GenericTypeMatcher<JTextArea>(JTextArea.class) {
	    	  @Override protected boolean isMatching(JTextArea button) {
	    	    return true;
	    	  }  
	    	};
	    	
	    window.textBox(textAreaMatcher).enterText(text);
	    window.textBox(textAreaMatcher).requireText(text);
	  
	    window.cleanUp();
	}

	public void setBASE___(boolean bASE___) {
		BASE___ = bASE___;
	}

	public boolean isBASE___() {
		return BASE___;
	}

	public void setBASEMENUBAR___(boolean bASEMENUBAR___) {
		BASEMENUBAR___ = bASEMENUBAR___;
	}

	public boolean isBASEMENUBAR___() {
		return BASEMENUBAR___;
	}

	public void setBASETOOLBAR___(boolean bASETOOLBAR___) {
		BASETOOLBAR___ = bASETOOLBAR___;
	}

	public boolean isBASETOOLBAR___() {
		return BASETOOLBAR___;
	}

	public void setEDIT___(boolean eDIT___) {
		EDIT___ = eDIT___;
	}

	public boolean isEDIT___() {
		return EDIT___;
	}

	public void setEDITMENUBAR___(boolean eDITMENUBAR___) {
		EDITMENUBAR___ = eDITMENUBAR___;
	}

	public boolean isEDITMENUBAR___() {
		return EDITMENUBAR___;
	}

	public void setEDITTOOLBAR___(boolean eDITTOOLBAR___) {
		EDITTOOLBAR___ = eDITTOOLBAR___;
	}

	public boolean isEDITTOOLBAR___() {
		return EDITTOOLBAR___;
	}

	public void setFORMAT___(boolean fORMAT___) {
		FORMAT___ = fORMAT___;
	}

	public boolean isFORMAT___() {
		return FORMAT___;
	}

	public void setFORMATMENUBAR___(boolean fORMATMENUBAR___) {
		FORMATMENUBAR___ = fORMATMENUBAR___;
	}

	public boolean isFORMATMENUBAR___() {
		return FORMATMENUBAR___;
	}

	public void setFORMATTOOLBAR___(boolean fORMATTOOLBAR___) {
		FORMATTOOLBAR___ = fORMATTOOLBAR___;
	}

	public boolean isFORMATTOOLBAR___() {
		return FORMATTOOLBAR___;
	}

	public void setPERSISTENCE___(boolean pERSISTENCE___) {
		PERSISTENCE___ = pERSISTENCE___;
	}

	public boolean isPERSISTENCE___() {
		return PERSISTENCE___;
	}

	public void setPERSISTENCEMENUBAR___(boolean pERSISTENCEMENUBAR___) {
		PERSISTENCEMENUBAR___ = pERSISTENCEMENUBAR___;
	}

	public boolean isPERSISTENCEMENUBAR___() {
		return PERSISTENCEMENUBAR___;
	}

	public void setPERSISTENCETOOLBAR___(boolean pERSISTENCETOOLBAR___) {
		PERSISTENCETOOLBAR___ = pERSISTENCETOOLBAR___;
	}

	public boolean isPERSISTENCETOOLBAR___() {
		return PERSISTENCETOOLBAR___;
	}

	public void setPRINT___(boolean pRINT___) {
		PRINT___ = pRINT___;
	}

	public boolean isPRINT___() {
		return PRINT___;
	}

	public void setPRINTMENUBAR___(boolean pRINTMENUBAR___) {
		PRINTMENUBAR___ = pRINTMENUBAR___;
	}

	public boolean isPRINTMENUBAR___() {
		return PRINTMENUBAR___;
	}

	public void setPRINTTOOLBAR___(boolean pRINTTOOLBAR___) {
		PRINTTOOLBAR___ = pRINTTOOLBAR___;
	}

	public boolean isPRINTTOOLBAR___() {
		return PRINTTOOLBAR___;
	}

	public void setSEARCH___(boolean sEARCH___) {
		SEARCH___ = sEARCH___;
	}

	public boolean isSEARCH___() {
		return SEARCH___;
	}

	public void setSEARCHMENUBAR___(boolean sEARCHMENUBAR___) {
		SEARCHMENUBAR___ = sEARCHMENUBAR___;
	}

	public boolean isSEARCHMENUBAR___() {
		return SEARCHMENUBAR___;
	}

	public void setSEARCHTOOLBAR___(boolean sEARCHTOOLBAR___) {
		SEARCHTOOLBAR___ = sEARCHTOOLBAR___;
	}

	public boolean isSEARCHTOOLBAR___() {
		return SEARCHTOOLBAR___;
	}

	public void setUNDOREDO___(boolean uNDOREDO___) {
		UNDOREDO___ = uNDOREDO___;
	}

	public boolean isUNDOREDO___() {
		return UNDOREDO___;
	}

	public void setUNDOREDOMENUBAR___(boolean uNDOREDOMENUBAR___) {
		UNDOREDOMENUBAR___ = uNDOREDOMENUBAR___;
	}

	public boolean isUNDOREDOMENUBAR___() {
		return UNDOREDOMENUBAR___;
	}

	public void setUNDOREDOTOOLBAR___(boolean uNDOREDOTOOLBAR___) {
		UNDOREDOTOOLBAR___ = uNDOREDOTOOLBAR___;
	}

	public boolean isUNDOREDOTOOLBAR___() {
		return UNDOREDOTOOLBAR___;
	}

	public void setWORDCOUNT___(boolean wORDCOUNT___) {
		WORDCOUNT___ = wORDCOUNT___;
	}

	public boolean isWORDCOUNT___() {
		return WORDCOUNT___;
	}

	public void setWORDCOUNTMENUBAR___(boolean wORDCOUNTMENUBAR___) {
		WORDCOUNTMENUBAR___ = wORDCOUNTMENUBAR___;
	}

	public boolean isWORDCOUNTMENUBAR___() {
		return WORDCOUNTMENUBAR___;
	}

	public void setWORDCOUNTTOOLBAR___(boolean wORDCOUNTTOOLBAR___) {
		WORDCOUNTTOOLBAR___ = wORDCOUNTTOOLBAR___;
	}

	public boolean isWORDCOUNTTOOLBAR___() {
		return WORDCOUNTTOOLBAR___;
	}
}
