/*
 * InputStreamSource.java: Implementation of a TokenizerSource.
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
import java.io.Reader;
import java.io.InputStreamReader;


//-----------------------------------------------------------------------------
// Class InputStreamSource
//

/**<p>
 * Implementation of the {@link TokenizerSource} interface for {@link java.io.Reader}
 * sources, e.g. {@link java.io.InputStreamReader}.
 *</p><p>
 * The class can be compared with {@link de.susebox.java.util.InputStreamTokenizer},
 * that has approximately the same (own) method set. While the <code>InputStreamTokenizer</code>
 * is a class directly derived from {@link de.susebox.java.util.AbstractTokenizer},
 * <code>InputStreamSource</code> is used in conjunction with the {@link PluginTokenizer}.
 * That way it is possible to switch from one <code>Reader</code> to
 * another dynamically.
 *</p>
 *
 * @see     de.susebox.java.util.Tokenizer
 * @see     de.susebox.java.util.AbstractTokenizer
 * @see     PluginTokenizer
 * @see     java.io.Reader
 * @author  Heiko Blau
 */
public class InputStreamSource implements TokenizerSource {
  
  //---------------------------------------------------------------------------
  // Interface methods
  //
  
  /**
   * A basic method to supply data to a {@link de.susebox.java.util.Tokenizer}.
   *
   * @param cbuf      buffer to receive data
   * @param offset    position from where the data should be inserted in <CODE>cbuf</CODE>
   * @param maxChars  maximum number of characters to be read into <CODE>cbuf</CODE>
   * @return actually read characters or -1 on an end-of-file condition
   * @throws Exception anything that could happen during read, most likely {@link java.io.IOException}
   */
  public int read(char[] cbuf, int offset, int maxChars) throws Exception {
    if (_reader != null) {
      return _reader.read(cbuf, offset, maxChars);
    } else {
      return -1;
    }
  }

  /**
   * When registering an instance that implements this interface, the 
   * {@link PluginTokenizer} will call this method to make itself known to
   * the <code>SeparatorHandler</code> instance in turn.
   *
   * @param tokenizer   the controlling {@link PluginTokenizer}
   */
  public void setTokenizer(PluginTokenizer tokenizer) {}
  
  
  //---------------------------------------------------------------------------
  // Constructors
  //
  
  /**
   * Default constructor. Standard input {@link java.lang.System#in} is used
   * to construct the input stream reader.
   */  
  public InputStreamSource() {
    this(null, 0);
  }

  /**
   * Constructor that takes a instantiated {@link java.io.Reader}. If 
   * <CODE>null</CODE> is given then standard input is used (see {@link java.lang.System#in}).
   *
   * @param Reader   input stream to be used for reading
   * @see   java.io.Reader
   */
  public InputStreamSource(Reader reader) {
    this(reader, 0);
  }
  
  /**
   * Constructor that takes a instantiated {@link java.io.Reader} and the
   * tokenizer control flags.
   * If <CODE>null</CODE> is given for the stream then standard input is used 
   * (see {@link java.lang.System#in}).
   * For the tokenizer control flags use a combination of the <CODE>F_...</CODE>
   * constants from the {@link Tokenizer} for this parameter.
   *
   * @param reader   input stream to be used for reading
   * @param flags    tokenizer control flags
   * @see   java.lang.InputStream
   * @see   Tokenizer
   */
  public InputStreamSource(Reader reader, int flags) {
    setReader((reader != null) ? reader : new InputStreamReader(System.in));
  }
  
  
  //---------------------------------------------------------------------------
  // Getter and setter
  //
  
  /** 
   * Setting the source for the {@link #read} method. Using this method instead
   * of {@link PluginTokenizer#setSource} will exchange an input stream without
   * the <code>Tokenizer</code> realizing the fact.
   *
   * @param reader    the new source for the {@link #read} method
   */
  public void setReader(Reader reader) {
    _reader = reader;
  }
  
  /**
   * Retrieving the current {@link java.io.InputStreamReader}. Note that the 
   * {@link de.susebox.java.util.Tokenizer} probably read ahead a huge part or 
   * even all data from this source. So dont try to be smart and cache data while 
   * the <code>Tokenizer</code> does the same. Unless, of course, You know why
   * You like to read ahead Yourself.
   *
   * @return the current source of the tokenizer
   */
  public Reader getReader() {
    return _reader;
  }
  
  
  //---------------------------------------------------------------------------
  // Members
  //
  protected Reader _reader = null;
}
