/*
 * SequenceHandler.java: Pluggin for the PluginTokenizer.
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
import de.susebox.java.util.TokenizerProperty;
import de.susebox.java.util.TokenizerException;


//-----------------------------------------------------------------------------
// Interface SequenceHandler
//

/**<p>
 * This interface must be implemented by classes that should be used as a 
 * special sequence and comment start sequence handler pluggin in the 
 * {@link PluginTokenizer}.
 *</p>
 *
 * @see     de.susebox.java.util.Tokenizer
 * @see     de.susebox.java.util.AbstractTokenizer
 * @author  Heiko Blau
 */
public interface SequenceHandler extends Plugin {
  
  /**
   * Return a {@link de.susebox.java.util.TokenizerProperty} if the character
   * starting at the given position comprise a special sequence (like the ++ operator
   * in C and Java or the &amp;bsp; in HTML), a comment starting sequence or
   * a string sign.<br>
   * Return <code>null</code> if no special sequence is present at the given
   * position.<br>
   * Use the {@link de.susebox.java.util.AbstractTokenizer#getCharUnchecked} to
   * retrieve a character from the tokenizers input buffer.
   *
   * @param  startingAtPos check from this position in the tokenizers input buffer
   * @throws {@link de.susebox.java.util.TokenizerException} for any problems
   * @return a <code>TokenizerProperty</code> instance describing the special sequence,
   *         comment etc. or <code>null</code> if no such thing was found. 
   */
  public TokenizerProperty isSequenceCommentOrString(int startingAtPos, int maxChars)     
    throws TokenizerException;
  
  /**
   * This method is called by the parent {@link PluginTokenizer} to learn how
   * many characters are needed by an instance of this interface to identify a
   * special sequence in the worst case. Usually that should be the length of
   * the longest possible special sequence, comment prefix etc.
   * The tokenizer will make sure that at least this number of characters is
   * available when {@link #isSequenceCommentOrString} is called. If less 
   * characters are provided, EOF is reached.
   *
   * @return  the number of characters needed in the worst case to identify a 
   *          special sequence
   */
  public int getSequenceMaxLength();
}
