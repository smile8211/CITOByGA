/*
 * InputStreamTokenizer.java: Implementation of a Tokenizer for input streams.
 *
 * Copyright (C) 2001 Heiko Blau
 *
 * This file belongs to the Susebox Java Core Library (Susebox JCL).
 * The Susebox JCL is free software; you can redistribute it and/or modify it 
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
 * with the Susebox JCL. If not, write to the
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

package de.susebox.java.util;

//-----------------------------------------------------------------------------
// Imports
//
import java.io.Reader;
import java.io.InputStreamReader;


//-----------------------------------------------------------------------------
// Class InputStreamTokenizer
//

/**<p>
 * The class extends the {@link AbstractTokenizer} and provides the {@link Tokenizer}
 * functionality for {@link java.io.Reader} sources, e.g. {@link java.io.InputStreamReader}
 * or {@link java.io.StringReader}.
 *</p><p>
 * The name is somewhat misleading since the class does actually work with 
 * every <code>Reader</code> implementation, but {@link java.io.InputStreamReader} 
 * are probably the most important ones.
 *</p>
 *
 * @see     Tokenizer
 * @see     AbstractTokenizer
 * @see     java.io.Reader
 * @see     java.io.InputStreamReader
 * @author  Heiko Blau
 */
public class InputStreamTokenizer extends AbstractTokenizer {
  
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
    if (_reader != null) {
      return _reader.read(cbuf, offset, maxChars);
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
  public InputStreamTokenizer() {
    this(null, 0);
  }

  /**
   * Constructor that takes a instantiated {@link java.io.InputStream}. If 
   * <CODE>null</CODE> is given then standard input is used ({@link java.lang.System#in}.
   *
   * @param Reader   input stream to be used for reading
   * @see   java.lang.InputStream
   */
  public InputStreamTokenizer(Reader reader) {
    this(reader, 0);
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
  public InputStreamTokenizer(Reader reader, int flags) {
    super(flags);
    
    setSource((reader != null) ? reader : new InputStreamReader(System.in));
  }
  
  
  /**
   * Constructor that takes a instantiated {@link java.io.InputStream}, the
   * tokenizer control flags, whitespace and separator strings.
   * If <CODE>null</CODE> is given for the stream then standard input is used 
   * ({@link java.lang.System#in}.
   * For the tokenizer control flags use a combination of the <CODE>F_...</CODE>
   * constants from the {@link Tokenizer} for this parameter.
   *
   * @param reader      input stream to be used for reading
   * @param flags       tokenizer control flags
   * @param whitespaces the whitespace set
   * @param separators  the set of separating characters
   * @see   java.lang.InputStream
   * @see   Tokenizer
   */
  public InputStreamTokenizer(Reader reader, int flags, String whitespaces, String separators) {
    super(flags);
    
    setSource((reader != null) ? reader : new InputStreamReader(System.in));
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
   * @param reader    the new source for the {@link #read} method
   */
  public void setSource(Reader reader) {
    _reader = reader;
    reset();
  }
  
  /**
   * Retrieving the current source. Note that the Tokenizer probably read ahead a
   * huge part or even all data from this source.
   *
   * @return the current source of the tokenizer
   */
  public Reader getSource() {
    return _reader;
  }
  
  
  //---------------------------------------------------------------------------
  // Members
  //
  protected Reader _reader = null;
}
