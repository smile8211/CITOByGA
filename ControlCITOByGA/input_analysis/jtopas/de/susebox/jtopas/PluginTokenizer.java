/*
 * PluginTokenizer.java: Implementation of a Tokenizer with handler slots.
 *
 * Copyright (C) 2001 Heiko Blau
 *
 * This file belongs to the JTopas Library.
 * JTopas is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation; either version 2.1 of the License, or (at your 
 * option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with JTopas. If not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330, 
 *   Boston, MA 02111-1307 
 *   USA
 *
 * or check the Internet: http://www.fsf.org
 *
 * Contact:
 *   email: heiko@susebox.de 
 */

package de.susebox.jtopas;

//-----------------------------------------------------------------------------
// Imports
//
import de.susebox.java.util.Tokenizer;
import de.susebox.java.util.TokenizerProperty;
import de.susebox.java.util.TokenizerException;
import de.susebox.java.util.AbstractTokenizer;


//-----------------------------------------------------------------------------
// Class PluginTokenizer
//

/**<p>
 * This class is an extension to {@link de.susebox.java.util.AbstractTokenizer}.
 * It can be used instead of ({@link de.susebox.java.util.InputStreamTokenizer}
 * when that class is not approbriate for the situation of the user (for instance,
 * if its performance is to low).
 *</p><p>
 * There is another aspect to the <code>PluginTokenizer</code>: While the
 * basic interface {@link de.susebox.java.util.Tokenizer} requires to add 
 * special sequences, comments etc. prior to the tokenizing process, 
 * the <code>PluginTokenizer</code> allows a more lazy approach. It is possible
 * to start tokenizing without such properties and leave the
 * decision completely to the pluggins that can be associated with an instance
 * of this class. Only the enumeration methods like 
 * {@link de.susebox.java.util.Tokenizer#getSpecialSequences} won't generally 
 * return anything.
 *</p>
 *
 * @see     de.susebox.java.util.Tokenizer
 * @see     de.susebox.java.util.AbstractTokenizer
 * @author  Heiko Blau
 */
public class PluginTokenizer extends AbstractTokenizer {
  
  //---------------------------------------------------------------------------
  // Abstract methods
  //
  
  /**
   * Implements the abstract method {@link AbstractTokenizer#read} by wrapping 
   * the {@link java.io.InputStreamReader#read} call of the JDK standard class
   * {@link java.io.InputStreamReader}.
   *
   * @param cbuf      buffer to receive data
   * @param offset    position from where the data should be inserted in <CODE>cbuf</CODE>
   * @param maxChars  maximum number of characters to be read into <CODE>cbuf</CODE>
   * @return actually read characters or -1 on an end-of-file condition
   * @throws Exception anything that could happen during read, most likely {@link java.io.IOException}
   */
  protected int read(char[] cbuf, int offset, int maxChars) throws Exception {
    if (_source != null) {
      return _source.read(cbuf, offset, maxChars);
    } else {
      return -1;
    }
  }
  
  
  //---------------------------------------------------------------------------
  // Constructors
  //
  
  /**
   * Default constructor. Standard input {@link java.lang.System#in} is used
   * to construct the input stream reader.
   */  
  public PluginTokenizer() {
    this(null, 0);
  }

  /**
   * Constructor that takes an instance of type {@link TokenizerSource}. If 
   * <CODE>null</CODE> is given then a <code>TokenizerSource</code> working
   * on standard input is used ({@link java.lang.System#in}.
   *
   * @param dataSource   a {@link TokenizerSource} to provide data
   */
  public PluginTokenizer(TokenizerSource dataSource) {
    this(dataSource, 0);
  }
  
  /**
   * Constructor that takes a instantiated {@link java.io.InputStream} and the
   * tokenizer control flags.
   * If <CODE>null</CODE> is given for the stream then standard input is used 
   * ({@link java.lang.System#in}.
   * For the tokenizer control flags use a combination of the <CODE>F_...</CODE>
   * constants from the {@link Tokenizer} for this parameter.
   *
   * @param reader   input stream to be used for reading
   * @param flags    tokenizer control flags
   * @see   java.lang.InputStream
   * @see   Tokenizer
   */
  public PluginTokenizer(TokenizerSource dataSource, int flags) {
    super(flags);
    
    setSource((dataSource != null) ? dataSource: new InputStreamSource());
  }
  
  
  //---------------------------------------------------------------------------
  // Getter and setter
  //
  
  /** 
   * Setting the source for the tokenizer. Note that any data read from a previous
   * source are lost. Position, line and column counting will start from 0.
   * With this method it is possible to use one tokenizer on more than one
   * source. A common example is a list of files that are to be processed.
   *
   * @param reader    the new source for the {@link de.susebox.java.util.AbstractTokenizer#read} method
   */
  public void setSource(TokenizerSource dataSource) {
    if ((_source = dataSource) != null) {
      _source.setTokenizer(this);
    }
    reset();
  }
  
  /**
   * Retrieving the current source. Note that the Tokenizer probably read ahead a
   * huge part or even all data from this source.
   *
   * @return the current source of the tokenizer
   */
  public TokenizerSource getSource() {
    return _source;
  }
  
  
  /**
   * Setting a new {@link WhitespaceHandler} or removing any previously installed
   * <code>WhitespaceHandler</code>. If <code>null</code> is passed, the tokenizer
   * will fall back to the base implementation.
   *
   * @param handler   the (new) whitespace handler to use or null to remove it
   */
  public void setWhitespaceHandler(WhitespaceHandler handler) {
    if ((_whitespaceHandler = handler) != null) {
      _whitespaceHandler.setTokenizer(this);
    }
  }
  
  /**
   * Retrieving the current {@link WhitespaceHandler}. The method may return
   * <code>null</code> if there isn't any handler installed. That does not
   * mean, that whitespaces are not dealt with, but that whitespace parsing is
   * done by the base method of {@link de.susebox.java.util.AbstractTokenizer}.
   *
   * @return  the currently active whitespace handler or null, if the base
   *          implementation is working
   */
  public WhitespaceHandler getWhitespaceHandler() {
    return _whitespaceHandler;
  }
  
  
  /**
   * Setting a new {@link SeparatorHandler} or removing any previously installed
   * <code>SeparatorHandler</code>. If <code>null</code> is passed, the tokenizer
   * will fall back to the base implementation.
   *
   * @param handler   the (new) separator handler to use or null to remove it
   */
  public void setSeparatorHandler(SeparatorHandler handler) {
    if ((_separatorHandler = handler) != null) {
      _separatorHandler.setTokenizer(this);
    }
  }
  
  /**
   * Retrieving the current {@link SeparatorHandler}. The method may return
   * <code>null</code> if there isn't any handler installed. That does not
   * mean, that separators are not dealt with, but that separator parsing is
   * done by the base method of {@link de.susebox.java.util.AbstractTokenizer}.
   *
   * @return  the currently active {@link SeparatorHandler} or null, if the base
   *          implementation is working
   */
  public SeparatorHandler getSeparatorHandler() {
    return _separatorHandler;
  }
  
  
  /**
   * Setting a new {@link SequenceHandler} or removing any previously installed
   * one. If <code>null</code> is passed, the tokenizer will fall back to the 
   * base implementation.
   *
   * @param handler   the (new) {@link SequenceHandler} to use or null to remove it
   */
  public void setSequenceHandler(SequenceHandler handler) {
    if ((_sequenceHandler = handler) != null) {
      _sequenceHandler.setTokenizer(this);
    }
  }
  
  /**
   * Retrieving the current {@link SequenceHandler}. The method may return
   * <code>null</code> if there isn't any handler installed. That does not
   * mean, that special sequences, comments and strings are not dealt with, but 
   * that parsing is done by the base method of {@link de.susebox.java.util.AbstractTokenizer}.
   *
   * @return  the currently active {@link SequenceHandler} or null, if the base
   *          implementation is working
   */
  public SequenceHandler getSequenceHandler() {
    return _sequenceHandler;
  }
  
  
  //---------------------------------------------------------------------------
  // Overridden methods of AbstractTokenizer
  //
  
  /**
   * This method checks if the character is a whitespace. It will use an installed
   * {@link WhitespaceHandler} or switch back to the base implementation.
   *
   * @param testChar  check this character
   * @return <CODE>true</CODE> if a whitespace sequence was found at the given offset,
   *         <CODE>false</CODE> otherwise
   * @see #setWhitespaceHandler
   */
  protected boolean isWhitespace(char testChar) {
    if (_whitespaceHandler != null) {
      return _whitespaceHandler.isWhitespace(testChar);
    } else {
      return super.isWhitespace(testChar);
    }
  }
  
  
  /**
   * This method detects the number of whitespace characters starting at the given
   * position. It should use {@link getChar} to retrieve a character to check.
   *<br>
   * The method should return the number of characters identified as whitespaces
   * starting from and including the given start position.
   *<br>
   * Do not attempt to actually read more data or do anything that leads to the
   * change of the data source or to tokenizer switching. This is done by the 
   * tokenizer framework.
   *
   * @param   startingAtPos start checking for whitespace from this position
   * @param   maxChars      if there is no non-whitespace character, read up to this number of characters 
   * @return  number of whitespace characters starting from the given offset
   * @throws  TokenizerException failure while reading data from the input stream
   */
  protected int readWhitespaces(int startingAtPos, int maxChars) 
    throws TokenizerException 
  {
    if (_whitespaceHandler != null) {
      return _whitespaceHandler.readWhitespaces(startingAtPos, maxChars);
    } else {
      return super.readWhitespaces(startingAtPos, maxChars);
    }
  }
  
  
  /**
   * This method checks the given character if it is a separator.
   * Implement Your own code for situations where this default implementation 
   * is not fast enough or otherwise not really good.
   *
   * @param testChar  check this character
   * @return <CODE>true</CODE> if the given character is a separator,
   *         <CODE>false</CODE> otherwise
   */
  protected boolean isSeparator(char testChar) {
    if (_separatorHandler != null) {
      return _separatorHandler.isSeparator(testChar);
    } else {
      return super.isSeparator(testChar);
    }
  }

  
  /**
   * This method checks at the given position if it contains a a special sequence. 
   * If a {@link SequenceHandler} is installed then it will be used to identify 
   * the starting sequences of comments, strings or single special sequences.
   * The <code>SequenceHandler</code> informs the <code>Tokenizer</code> about the
   * maximum possible length of any of these sequences 
   * (see {@link SequenceHandler#getSequenceMaxLength}).
   *<br>
   * That way the tokenizer is able to provide a sufficient amount of data to the 
   * <code>SequenceHandler</code>. If there are less characters than the maximum 
   * length provided, the <code>Tokenizer</code> is near EOF.
   *<br>
   * The 
   *
   * @param  startingAtPos  start checking for sequences from this position
   * @throws TokenizerException failure while reading data from the input stream
   * @return a {@link de.susebox.java.util.TokenizerProperty} if a special sequence 
   *         was found at the given position or <CODE>null</CODE> otherwise
   */
  protected TokenizerProperty isSequenceCommentOrString(int startingAtPos) throws TokenizerException {
    if (_sequenceHandler != null) {
      int available = currentlyAvailable() - (startingAtPos - getRangeStart());
      int maxLength = _sequenceHandler.getSequenceMaxLength();
      
      if (maxLength > 0 && available < maxLength) {
        readMore();
        available = currentlyAvailable() - (startingAtPos - getRangeStart());
      }
      return _sequenceHandler.isSequenceCommentOrString(startingAtPos, available);
    } else {
      return super.isSequenceCommentOrString(startingAtPos);
    }
  }

  
    
  //---------------------------------------------------------------------------
  // Members
  //
  private TokenizerSource   _source            = null;
  private WhitespaceHandler _whitespaceHandler = null;
  private SeparatorHandler  _separatorHandler  = null;
  private SequenceHandler   _sequenceHandler   = null;
}
