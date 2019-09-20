/*
 * WhitespaceHandler.java: Pluggin for the PluginTokenizer.
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
import de.susebox.java.util.TokenizerException;


//-----------------------------------------------------------------------------
// Interface WhitespaceHandler
//

/**<p>
 * This interface must be implemented by classes that should be used as a 
 * whitespace handler pluggin in the {@link PluginTokenizer}.
 *</p>
 *
 * @see     de.susebox.java.util.Tokenizer
 * @see     de.susebox.java.util.AbstractTokenizer
 * @author  Heiko Blau
 */
public interface WhitespaceHandler extends Plugin {
  
  /**
   * This method checks if the character is a whitespace.
   *
   * @param testChar  check this character
   * @return <CODE>true</CODE> if the given character is a whitespace,
   *         <CODE>false</CODE> otherwise
   */
  public boolean isWhitespace(char testChar);

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
   * @param   startingAtPos  start checking for whitespace from this position
   * @param   maxChars  if there is no non-whitespace character, read up to this number of characters 
   * @return  number of whitespace characters starting from the given offset
   * @throws  TokenizerException failure while reading data from the input stream
   */
  public int readWhitespaces(int startingAtPos, int maxChars) throws TokenizerException;
}
