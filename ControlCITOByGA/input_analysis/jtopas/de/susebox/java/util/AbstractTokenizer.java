/*
 * AbstractTokenizer.java: core class for lexical parser.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.susebox.java.lang.ExtRuntimeException;
import de.susebox.java.lang.ExtIndexOutOfBoundsException;


//-----------------------------------------------------------------------------
// Class AbstractTokenizer
//

/** <p>
 * This is the core class for mainstream Tokenizers. It implements the {@link Tokenizer}
 * interface in a straightforward approach without too specialised parse
 * optimizations.
 * </p><p>
 * The class is abstract, so it cannot be used by itself. It defines one abstract
 * method, {@link #read}. This method is equivalent to the complex read methods of
 * the {@link java.io.InputStream} interface, which is most likely to be used for
 * implementing our read method.
 * </p><p>
 * Beside the {@link Tokenizer} interface, the class <CODE>AbstractTokenizer</CODE>
 * provides some basic features for cascading (nested) tokenizers. Conider the usual
 * HTML pages found today in the WWW. Most of them are a mixture of regular HTML,
 * cascading style sheets (CSS) and embedded JavaScript. These different languages
 * use different syntaxes, so one needs varous tokenizers on the same input stream.
 * </p><p>
 * There are also several methods suitable for own implementations in derived 
 * classes ({@link isWhitespace}, {@link isSeparator} and {@link isSequenceCommentOrString}).
 * Being a general approach to tokenizing, the <code>AbstractTokenizer</code> may
 * be not the fastest solution for ones specific situation. By implementing the
 * mentioned methods, one can improve the performance through exact knowledge of
 * what whitespaces, separators, comment starts and other special sequences are.
 *</p>
 *
 * @see Tokenizer
 * @see InputStreamTokenizer
 * @author Heiko Blau
 */
public abstract class AbstractTokenizer implements Tokenizer
{
  //---------------------------------------------------------------------------
  // Abstract methods
  //
  
  /**
   * This is the abstract method to be implemented by derived classes. It is supposed
   * to deliver data from some sort of input stream to the tokenizer. The simplest
   * implementation would use {@link java.io.InputStream#read}.
   *
   * @param cbuf      buffer to receive data
   * @param offset    position from where the data should be inserted in <CODE>cbuf</CODE>
   * @param maxChars  maximum number of characters to be read into <CODE>cbuf</CODE>
   * @return actually read characters or -1 on an end-of-file condition
   * @throws Exception anything that could happen during read, most likely {@link java.io.IOException}
   */
  protected abstract int read(char[] cbuf, int offset, int maxChars) throws Exception;
  
  
  /**
   * This method should be called by derived classes whenever the input buffer
   * has to be flushed. Such a situation may arise when the input source of the 
   * read method is changed.<br>
   * Position informations are reset to start. All data in the input buffer are
   * lost. So are the current token and any lookahead data. This is also true
   * for instances there the {@link Tokenizer#F_KEEP_DATA} flag is set.
   */
  protected void reset() {
    synchronized(this) {
      _currentReadPos   = 0;
      _currentWritePos  = 0;
      _rangeStart       = 0;
      _lineNumber       = -1;
      _columnNumber     = -1;
      _currentToken     = null;
      _lookAheadToken.setType(Token.UNKNOWN);
    }
  }
  
  
  //---------------------------------------------------------------------------
  // Constructors
  //
  
  /**
   * Default constructor that sets the tokenizer control flags as it would be
   * approbriate for C/C++ and Java. Found token images are copied. No line nor
   * column informations are provided. Nested comments are not allowed.
   *<br>
   * The tokenizer will use the {@link Tokenizer#DEFAULT_WHITESPACES} and 
   * {@link Tokenizer#DEFAULT_SEPARATORS} for whitespace and separator handling.
   */  
  public AbstractTokenizer() {
    this(0);
  }

  /**
   * This constructor takes the control flags to be used. It is a shortcut to:
   * <pre>
   *   Tokenizer t = new MyTokenizer();
   *
   *   t.setParseFlags(flags);
   * </pre>
   * See the {@link Tokenizer} interface for the supported flags.
   *<br>
   * The tokenizer will use the {@link Tokenizer#DEFAULT_WHITESPACES} and 
   * {@link Tokenizer#DEFAULT_SEPARATORS} for whitespace and separator handling
   * if no explicit calls to {@link #setWhitespaces} and {@link #setSeparators}
   * are made.
   *
   * @param flags tokenizer control flags
   */  
  public AbstractTokenizer(int flags) {
    this(flags, Tokenizer.DEFAULT_WHITESPACES, Tokenizer.DEFAULT_SEPARATORS);
  }
  
  
  /**
   * This constructor takes the control flags, whitespace and separator strings
   * to be used. It is a shortcut to:
   * <pre>
   *   Tokenizer t = new MyTokenizer();
   *
   *   t.setParseFlags(flags);
   *   t.setWhitespaces(ws);
   *   t.setSeparators(sep);
   * </pre>
   *
   * @param flags       tokenizer control flags
   * @param whitespaces the whitespace set
   * @param separators  the set of separating characters
   * @see   #setWhitespaces
   * @see   #setSeparators
   */  
  public AbstractTokenizer(int flags, String whitespaces, String separators) {
    setParseFlags(flags);
    setWhitespaces(whitespaces);
    setSeparators(separators);

    if ((_flags & Tokenizer.F_KEEP_DATA) != 0) {
      _inputBuffer = new char[0x10000];   // 64k
    } else {
      _inputBuffer = new char[0x2000];    // 8k
    }
  }
  
  
  //---------------------------------------------------------------------------
  // Methods of the Tokenizer interface
  //
  
  /**
   * Setting the control flags of the <code>Tokenizer</code>. Use a combination
   * of the <code>Tokenizer.F_...</code> flags for the parameter.
   *
   * @param flags the parser control flags
   */
  public void setParseFlags(int flags) {
    _flags = flags;
    
    // we would like to test flags on F_KEYWORDS_CASE
    if ((_flags & (Tokenizer.F_KEYWORDS_NO_CASE | Tokenizer.F_NO_CASE)) == 0) {
      _flags |= Tokenizer.F_KEYWORDS_CASE;
    } else if ((_flags & Tokenizer.F_KEYWORDS_CASE) != 0) {
      _flags &= ~Tokenizer.F_KEYWORDS_NO_CASE;
    }
    
    // when counting lines initialize the current line and column position
    if ((_flags & Tokenizer.F_COUNT_LINES) != 0) {
      _lineNumber   = 0;
      _columnNumber = 0;
    }
  }

   /**
    * Retrieving the parser control flags. A bitmask containing the <code>F_...</code>
    * constants is returned.
    * @return the current parser control flags
    * @see #setParseFlags
    */
  public int getParseFlags() {
    return _flags;
  }
  
  /**
   * Registering a string description. Strings are things like the primitive string 
   * literals in C/C++, SQL varchar literals, but also the character literals 
   * of C/C++ and Java.<br>
   * If the given string starting sequence is already known to the parser,
   * it will simply be re-registered. Using this method on a known string
   * with an associated companion will remove that companion.
   *
   * @param start     the starting sequence of a string
   * @param end       the finishing sequence of a string
   * @param escape    the escape sequence inside the string
   */
  public void addString(String start, String end, String escape) {
    addString(start, end, escape, null);
  }

  /**
   * Registering a the sequences that are used for string-like text parts.
   * This method supports also an information associated with the string,
   * called the companion.<br>
   * If the given string starting sequence is already known to the parser,
   * it will simply be re-registered. Using this method on a known string
   * with an associated companion will replace that companion against the given
   * one.
   *
   * @param start     the starting sequence of a string
   * @param end       the finishing sequence of a string
   * @param escape    the escape sequence inside the string
   * @param companion the associated information
   */
  public void addString(
    String start, 
    String end, 
    String escape, 
    Object companion
  )
  {
    addString(start, end, escape, companion, getParseFlags());
  }
  
  
  /**
   * Registering a the sequences that are used for string-like text parts.
   * This method supports also an information associated with the string,
   * called the companion.<br>
   * If the given string starting sequence is already known to the parser,
   * it will simply be re-registered. Using this method on a known string
   * with an associated companion will replace that companion against the given
   * one.
   * This version of <code>addString</code> supports a bitmask of the 
   * <code>F_...</code> flags to modify the general tokenizer settings (see
   * {@link #setParseFlags} for this special element.
   *
   * @param start     the starting sequence of a string
   * @param end       the finishing sequence of a string
   * @param escape    the escape sequence inside the string
   * @param companion the associated information
   * @param flags     modification flags 
   */
  public void addString(
    String start, 
    String end, 
    String escape, 
    Object companion, 
    int    flags
  )
  {
    addSpecialSequence(
      new TokenizerProperty(Token.STRING, new String[] { start, end, escape }, 
                            companion, flags)
    );
  }
  
  /**
   * Removing a string description.
   *
   * @param start     the starting sequence of a string
   */  
  public void removeString(String start) {
    removeSpecialSequence(start);
  }
  
    
  /**
   * Retrieving the information associated with a certain string. Only the 
   * starting sequence is nessecary to identify the string. If the string is not 
   * known to the parser, <CODE>null</CODE> will be returned.<br>
   * If one needs to know if a string exists without a companion or if the string
   * is unknown so far, use also the method {@link #stringExists}.
   *
   * @param start     the starting sequence of a string
   * @return the associated information or <CODE>null</CODE>
   */
  public Object getStringCompanion(String start) {
    return getSpecialSequenceCompanion(start);
  }
  

  /**
   * Checks if the given starting sequence of the string is known to the parser.
   *
   * @param start     the starting sequence of a string
   * @return <CODE>true</CODE> if the string is registered, 
   *         <CODE>false</CODE> otherwise
   */
  public boolean stringExists(String start) {
    return specialSequenceExists(start);
  }

  
  /**
   * This method returns an {@link java.util.Iterator} of {@link TokenizerProperty}
   * objects. Each <CODE>TokenizerProperty</CODE> object contains the starting,
   * finishing and escaping sequence of a string description and the companion if 
   * it exists.
   *
   * @return enumeration of {@link TokenizerProperty} objects
   */  
  public Iterator getStrings() {
    return new SpecialSequencesIterator(this, Token.STRING);
  }
  
  /**
   * Setting the whitespace character set of the tokenizer. It is possible to
   * use ranges like "a-z" when more than two whitespace characters are
   * neighbours in the UNICODE character set.<br>
   * Whitespaces are sequences that have the same syntactical meaning as one
   * single whitespace character would have. That means "     " (many spaces) is
   * the same as " " (one space).
   *
   * @param whitespaces the whitespace set
   */
  public void setWhitespaces(String whitespaces) {
    // set whitespaces and detect if end-of-line characters are part of them
    _whitespaces = (whitespaces != null) ? whitespaces : "";
    if (_whitespaces.indexOf('\n') >= 0 || _whitespaces.indexOf('\r') >= 0) {
      _newlineIsWhitespace = true;
    }
    
    // for fast whitespace scanning check for the most common ones
    if (   _whitespaces.equals(Tokenizer.DEFAULT_WHITESPACES)
        || (   _whitespaces.length()      == 4
            && _whitespaces.indexOf('\n') >= 0 
            && _whitespaces.indexOf('\r') >= 0
            && _whitespaces.indexOf(' ')  >= 0
            && _whitespaces.indexOf('\t') >= 0)) {
      _defaultWhitespaces = true;
    }
  }
  
  /**
   * Obtaining the whitespace character set. The set may contain ranges.
   *
   * @see #setWhitespaces
   * @return the currently active whitespace set
   */
  public String getWhitespaces() {
    return _whitespaces;
  }
  
  /**
   * Setting the separator set. This set may contain ranges. A range is a
   * character (lower limit) followed by a '-' (minus) followed by a
   * second character (upper limit). A range of "a-z" means: all characters in
   * the UNICODE character set between and including 'a' and 'z'. Ranges should
   * be used whenever possible since they speed up the parsing process.<br>
   * Separators are characters that are significant for the syntax. A sequence
   * of separators is <i>NOT</i> equal to one single separator. Thats the
   * difference to whitespaces.
   *
   * @param separators the set of separating characters
   */
  public void setSeparators(String separators) {
    _separators = (separators != null) ? separators : "";
  }
  
  /**
   * Obtaining the separator set of the <code>Tokenizer</code>. The set may
   * contain ranges.
   *
   * @see #setSeparators
   * @return the currently used set of separating characters
   */
  public String getSeparators() {
    return _separators;
  }
  
  /**
   * Registering a the starting sequence of a line comment. The line comment is
   * a special type of whitespace. It starts with the given character sequence
   * and contains all characters up to and including the next end-of-line
   * character(s).<br>
   * Although most languages have only one line comment sequence, it is possible
   * to use more than one.<br>
   * If the given line comment starting sequence is already known to the parser,
   * it will simply be re-registered. Using this method on a known line comment
   * with an associated companion will effectively remove the companion.
   *
   * @param lineComment the starting sequence of the line comment
   */
  public void addLineComment(String lineComment) {
    addLineComment(lineComment, null);
  }

  /**
   * Registering a the starting sequence of a line comment. The line comment is
   * a special type of whitespace. It starts with the given character sequence
   * and contains all characters up to and including the next end-of-line
   * character(s).<br>
   * Although most languages have only one line comment sequence, it is possible
   * to use more than one.<br>
   * This method supports also an information associated with the line comment,
   * called the companion.<br>
   * If the given line comment starting sequence is already known to the parser,
   * it will simply be re-registered. Using this method on a known line comment
   * with an associated companion will replace that companion against the given
   * one.
   *
   * @param lineComment the starting sequence of a line comment
   * @param companion the associated information
   */
  public void addLineComment(String lineComment, Object companion) {
    addLineComment(lineComment, companion, getParseFlags());
  }

  
  /**
   * Registering a the starting sequence of a line comment. The line comment is
   * a special type of whitespace. It starts with the given character sequence
   * and contains all characters up to and including the next end-of-line
   * character(s).<br>
   * Although most languages have only one line comment sequence, it is possible
   * to use more than one.<br>
   * This method supports also an information associated with the line comment,
   * called the companion.<br>
   * If the given line comment starting sequence is already known to the parser,
   * it will simply be re-registered. Using this method on a known line comment
   * with an associated companion will replace that companion against the given
   * one.<br>
   * This version of <code>addLineComment</code> supports a bitmask of the
   * <code>F_...</code> flags to modify the general tokenizer settings (see
   * {@link #setParseFlags}) for this special element.
   *
   * @param lineComment the starting sequence of a line comment
   * @param companion the associated information
   * @param flags     modification flags
   */
  public void addLineComment(String lineComment, Object companion, int flags) {
    addSpecialSequence(
      new TokenizerProperty(Token.LINE_COMMENT, new String[] { lineComment }, 
                            companion, flags)
    );
  }  

  /**
   * Removing a certain line comment.
   *
   * @param lineComment the starting sequence of the line comment
   */  
  public void removeLineComment(String lineComment) {
    removeSpecialSequence(lineComment);
  }
  
  
  /**
   * Retrieving the associated object of a certain line comment. If the given
   * starting sequence of a line comment is not known to the parser, then the
   * method returns <CODE>null</CODE>.<br>
   * To distinguish between an unknown line comment and companion-less line
   * comment, use the method {@link #lineCommentExists}.
   *
   * @param lineComment the starting sequence of the line comment
   * @return the object associated with the line comment
   */  
  public Object getLineCommentCompanion(String lineComment) {
    return getSpecialSequenceCompanion(lineComment);
  }

  /**
   * Checks if the give line comment is known.
   *
   * @param lineComment the starting sequence of the line comment
   * @return <CODE>true</CODE> if the line comment is known, 
   *         <CODE>false</CODE> otherwise
   */  
  public boolean lineCommentExists(String lineComment) {
    return specialSequenceExists(lineComment);
  }
  
  /**
   * This method returns an {@link java.util.Iterator} of {@link TokenizerProperty}
   * objects. Each <CODE>TokenizerProperty</CODE> object contains one starting
   * sequence of a line comment and the companion if it exists.
   *
   * @return enumeration of {@link TokenizerProperty} objects
   */  
  public Iterator getLineComments() {
    return new SpecialSequencesIterator(this, Token.LINE_COMMENT);
  }
  
  /**
   * Registering a block comment with the parser. This version takes only the starting
   * and finishing sequence of the block comment.<br>
   * If the given starting sequence is already known to the parser, the block 
   * comment is simply re-registered. Using this method on a known block comment
   * with an associated companion will remove that companion.
   *
   * @param start the starting sequence of the block comment
   * @param end the finishing sequence of the block comment
   */  
  public void addBlockComment(String start, String end) {
    addBlockComment(start, end, null);
  }
  
  /**
   * Registering a block comment with the parser. Beside the obviously nessecary
   * starting and finishing sequence of the block comment, it takes an object that
   * is associated with the block comment, called the companion.<br>
   * If the given starting sequence is already known to the parser, the block
   * comment is simply re-registered. Using this method on a known block comment
   * with an associated companion will replace that companion against the given
   * one.
   *
   * @param start the starting sequence of the block comment
   * @param end the finishing sequence of the block comment
   * @param companion information object associated with this block comment
   */  
  public void addBlockComment(String start, String end, Object companion) {
    addBlockComment(start, end, companion, getParseFlags());
  }
  
  /**
   * Registering a block comment with the parser. Beside the obviously nessecary
   * starting and finishing sequence of the block comment, it takes an object that
   * is associated with the block comment, called the companion.<br>
   * If the given starting sequence is already known to the parser, the block
   * comment is simply re-registered. Using this method on a known block comment
   * with an associated companion will replace that companion against the given
   * one.<br>
   * This version of <code>addBlockComment</code> supports a bitmask of the 
   * <code>F_...</code> flags to modify the general tokenizer settings (see
   * {@link #setParseFlags}) for this special element.
   *
   * @param start     the starting sequence of the block comment
   * @param end       the finishing sequence of the block comment
   * @param companion information object associated with this block comment
   * @param flags     modification flags 
   */
  public void addBlockComment(String start, String end, Object companion, int flags) {
    addSpecialSequence(
      new TokenizerProperty(Token.BLOCK_COMMENT, new String[] { start, end }, 
                            companion, flags)
    );
  }
  
  /**
   * Removing a certain block comment. Only the starting sequence is nessecary
   * to identify the block comment.
   *
   * @param start the starting sequence of the block comment
   */  
  public void removeBlockComment(String start) {
    removeSpecialSequence(start);
  }
  
  /**
   * Retrieving a certain block comment. Only the starting sequence is nessecary
   * to identify the block comment. If the block comment is not known to the 
   * parser, then <CODE>null</CODE> is returned.<br>
   * To distinguish between an unknown line comment and companion-less line 
   * comment, use the method {@link #lineCommentExists}.
   *
   * @param start the starting sequence of the block comment
   * @return the associated object of the block comment
   */  
  public Object getBlockCommentCompanion(String start) {
    return getSpecialSequenceCompanion(start);
  }
  
  
  /**
   * Checks if the given block comment is known. Only the starting sequence is 
   * nessecary to identify the block comment.
   *
   * @param start the starting sequence of the block comment
   * @return <CODE>true</CODE> if the block comment is known, 
   *         <CODE>false</CODE> otherwise
   */  
  public boolean blockCommentExists(String start) {
    return specialSequenceExists(start);
  }
  
  
  /**
   * This method returns an {@link java.util.Iterator} of {@link TokenizerProperty}
   * objects. Each <CODE>TokenizerProperty</CODE> object contains the starting and
   * finishing sequence of a block comment and the companion if it exists.
   *
   * @return enumeration of {@link TokenizerProperty} objects
   */  
  public Iterator getBlockComments() {
    return new SpecialSequencesIterator(this, Token.BLOCK_COMMENT);
  }
  
  
  /**
   * Query the current row. The method can only be used if the flag <CODE>F_COUNT_LINES</CODE>
   * has been set.
   * Without this flag being set, the return value is undefined.
   * Note that row counting starts with 0, while editors often use 1 for the first
   * row.
   *
   * @return current row (starting with 0) 
   *         or -1 if the flag {@link Tokenizer#F_COUNT_LINES} is set
   */
  public int getCurrentLine() {
    return _lineNumber;
  }
  
  
  /**
   * Retrieve the current column. The method can only be used if the flag <CODE>F_COUNT_LINES</CODE>
   * has been set.
   * Without this flag being set, the return value is undefined.
   * Note that column counting starts with 0, while editors often use 1 for the first
   * column in one row.
   *
   * @return current column number (starting with 0)
   */
  public int getCurrentColumn() {
    return _columnNumber;
  }
  

  /**
   * Registering a special sequence of characters. Such sequences may be multicharacter
   * operators like the shift operators in Java.
   * Unlike keywords, special sequences act also as separators between other tokens.
   * If one special sequence is the prefix of other special sequences (in Java the
   * shift operator <CODE>&gt;&gt;</CODE> is the prefix of the shift operator
   * <CODE>&gt;&gt;&gt;</CODE>), always the longest possible match is returned.
   * Testing on special sequences takes place after whitespaces and comments are ruled
   * out, but before ordinary separators are tested.
   *
   * @param specSeq   special sequence to register
   * @see   #addKeyword
   * @see   #setSeparators
   */
  public void addSpecialSequence(String specSeq) {
    addSpecialSequence(specSeq, null);
  }
  
  
  /**
   * Registering a special sequence of characters. Such sequences may be multicharacter
   * operators like the shift operators in Java.
   * Unlike keywords, special sequences act also as separators between other tokens.
   * If one special sequence is the prefix of other special sequences (in Java the
   * shift operator <CODE>&gt;&gt;</CODE> is the prefix of the shift operator
   * <CODE>&gt;&gt;&gt;</CODE>), always the longest possible match is returned.
   * Testing on special sequences takes place after whitespaces and comments are ruled
   * out, but before ordinary separators are tested.
   * This form of <CODE>addSpecialSequence</CODE> also takes an object associated with
   * the special sequence, called the companion.
   *
   * @param specSeq     special sequence to register
   * @param companion information object associated with this special sequence
   * @see #addKeyword
   * @see #setSeparators
   */  
  public void addSpecialSequence(String specSeq, Object companion) {
    addSpecialSequence(specSeq, companion, getParseFlags());
  }

  
  /**
   * Registering a special sequence of characters. Such sequences may be multicharacter
   * operators like the shift operators in Java.
   * Unlike keywords, special sequences act also as separators between other tokens.
   * If one special sequence is the prefix of other special sequences (in Java the
   * shift operator <CODE>&gt;&gt;</CODE> is the prefix of the shift operator
   * <CODE>&gt;&gt;&gt;</CODE>), always the longest possible match is returned.
   * Testing on special sequences takes place after whitespaces and comments are ruled
   * out, but before ordinary separators are tested.
   * This form of <CODE>addSpecialSequence</CODE> also takes an object associated with
   * the special sequence, called the companion.
   * This version of <code>addSpecialSequence</code> supports a bitmask of the
   * <code>F_...</code> flags to modify the general tokenizer settings (see
   * {@link #setParseFlags} for this special element.
   *
   * @param specSeq     special sequence to register
   * @param companion   information object associated with this special sequence
   * @param flags       modification flags
   * @see #addKeyword
   * @see #setSeparators
   */
  public void addSpecialSequence(String specSeq, Object companion, int flags) {
    addSpecialSequence(
      new TokenizerProperty(Token.SPECIAL_SEQUENCE, new String[] { specSeq }, 
                            companion, flags)
    );
  }  
    

  /**
   * Deregistering a special sequence from the parser.
   *
   * @param specSeq   sequence to remove
   */  
  public void removeSpecialSequence(String specSeq) {
    if (specSeq != null) {
      try {
        for (int pos = 0; pos < _sequences.length; ++pos) {
          if (_sequences[pos] != null) {
            _sequences[pos].searchBinary(specSeq, 0, true);
          }
        }
      } catch (TokenizerException ex) {
        // this shouldn't happen at all since we do not read from the input stream
        throw new ExtRuntimeException(
                    ex,
                    "While trying to remove special sequence \"{0}\".", 
                    new Object[] { specSeq }
                  );
      }
    }
  }
  
  
  /**
   * Retrieving the companion of the given special sequence. If the special
   * sequence doesn't exist the method returns <CODE>null</CODE>.
   *
   * @param specSeq   sequence to remove
   * @return the object associated with the special sequence
   */
  public Object getSpecialSequenceCompanion(String specSeq) {
    if (specSeq != null) {
      TokenizerProperty prop = searchBinary(specSeq);

      if (prop != null) {
        return prop.getCompanion();
      }
    }
    
    // no special sequences at all or not found
    return null;
  }
  

  /**
   * This method returns an {@link java.util.Iterator} of {@link TokenizerProperty}
   * objects. Each <CODE>TokenizerProperty</CODE> object contains a special
   * sequence and the companion if it exists.
   *
   * @return enumeration of {@link TokenizerProperty} objects
   */  
  public Iterator getSpecialSequences() {
    return new SpecialSequencesIterator(this, Token.SPECIAL_SEQUENCE);
  }
  
  
  /**
   * Checks if the given special sequence is known to the <CODE>Tokenizer</CODE>.
   *
   * @param specSeq sequence to check
   * @return <CODE>true</CODE> if the block comment is known,
   *       <CODE>false</CODE> otherwise
   */  
  public boolean specialSequenceExists(String specSeq) {
    if (specSeq != null && searchBinary(specSeq) != null) {
      return true;
    } else {
      return false;
    }
  }
  
  
  /**
   * Registering a keyword. If the keyword is already known to the <CODE>Tokenizer</CODE>
   * then it is simply re-registered. If the known keyword has an associated 
   * companion it will be removed.
   *
   * @param keyword   keyword to register
   */
  public void addKeyword(String keyword) {
    addKeyword(keyword, null);
  }
  
  
  /**
   * Registering a keyword. If the keyword is already known to the <CODE>Tokenizer</CODE>
   * then it is simply re-registered. If the known keyword has an associated
   * companion it will be replaced against the given one.
   *
   * @param keyword   keyword to register
   * @param companion information object associated with this keyword
   */  
  public void addKeyword(String keyword, Object companion) {
    addKeyword(keyword, companion, getParseFlags());
  }
  
  
  /**
   * Registering a keyword. If the keyword is already known to the <CODE>Tokenizer</CODE>
   * then it is simply re-registered. If the known keyword has an associated
   * companion it will be replaced against the given one.<br>
   * This version of <code>addKeyword</code> supports a bitmask of the 
   * <code>F_...</code> flags to modify the general tokenizer settings (see
   * {@link #setParseFlags}) for this special element.
   *
   * @param keyword   keyword to register
   * @param companion information object associated with this keyword
   */  
  public void addKeyword(String keyword, Object companion, int flags) {
    // normalize flags
    int commonFlags = getParseFlags();
    
    if (flags != commonFlags) {
      if ((commonFlags & F_KEYWORDS_CASE) == 0) {
        if ((flags & F_KEYWORDS_CASE) != 0) {
          flags |= F_KEYWORDS_CASE;
        }
      } else {
        if ((flags & (F_KEYWORDS_NO_CASE | F_NO_CASE)) != 0) {
          flags &= ~F_KEYWORDS_CASE;
        }
      }
    }
    
    // there is a HashMap for case-sensitive and one for non case-sensitive
    // keywords
    // case-insensitive comparison must be done by comparing normalized strings
    // we choose the upper case (lower case would be fine as well)
    HashMap table;

    if ((flags & Tokenizer.F_KEYWORDS_CASE) != 0) {
      if (_keywords[0] == null) {
        _keywords[0] = new HashMap();
      }
      table = _keywords[0];
    } else {
      if (_keywords[1] == null) {
        _keywords[1] = new HashMap();
      }
      table   = _keywords[1];
      keyword = keyword.toUpperCase();
    }
    
    // put keyword and its property
    table.put(keyword, new TokenizerProperty(Token.KEYWORD, 
                                             new String[] { keyword }, 
                                             companion, flags));
  }
  
  
  /**
   * Deregistering a special sequence from the parser. If the keyword is not known
   * then the method does nothing.
   *
   * @param keyword   keyword to remove
   */  
  public void removeKeyword(String keyword) {
    if (keyword != null) {
      for (int pos = 0; pos < _keywords.length; ++pos) {
        if (_keywords[pos] != null) {
          _keywords[pos].remove(keyword);
        }
      }
    }
  }
  
  
  /**
   * Retrieving the companion of the given special sequence. If the special
   * sequence doesn't exist the method returns <CODE>null</CODE>.
   *
   * @param keyword   keyword thats companion is sought
   * @return the object associated with the keyword
   */
  public Object getKeywordCompanion(String keyword) {
    // case-sensitive keywords are prior to case-insensitive ones
    if (keyword != null) {
      for (int pos = 0; pos < _keywords.length; ++pos) {
        if (_keywords[pos] != null) {
          TokenizerProperty prop = (TokenizerProperty)_keywords[pos].get(keyword);
          
          if (prop != null) {
            return prop.getCompanion();
          }
        }
      }
    }
    
    // no keyword given, no keywords known, not found
    return null;
  }

  
  /**
   * This method returns an {@link java.util.Iterator} of {@link TokenizerProperty}
   * objects. Each <CODE>TokenizerProperty</CODE> object contains a keyword and 
   * the companion if it exists.
   *
   * @return iteration of {@link TokenizerProperty} objects
   */  
  public Iterator getKeywords() {
    return new KeywordIterator(this);
  }
  
  
  /**
   * Checks if the given keyword is known to the <CODE>Tokenizer</CODE>.
   *
   * @param keyword   keyword to search
   * @return <CODE>true</CODE> if the keyword is known,
   *        <CODE>false</CODE> otherwise
   */  
  public boolean keywordExists(String keyword) {
    if (keyword != null) {
      if (   (_keywords[0] != null && _keywords[0].containsKey(keyword)) 
          || (_keywords[1] != null && _keywords[1].containsKey(keyword.toUpperCase()))) {
        return true;
      }
    }
    return false;
  }
  
  
  /**
   * Checking if there are more tokens available. This method will return
   * <code>true</code> until and enf-of-file condition is encountered during a 
   * call to {@link #nextToken} or {@link #next}.<br>
   * That means, that the EOF is returned one time, afterwards <code>hasMoreToken</code>
   * will return <code>false</code>. Furthermore, that implies, that the method
   * will return <code>true</code> at least once, even if the input data stream
   * is empty.<br>
   * The method can be conveniently used in a while loop.
   *
   * @return  <code>true</code> if a call to {@link #nextToken} or {@link #next}
   *          will succed, <code>false</code> otherwise
   */
  public boolean hasMoreToken() {
    return _currentToken == null || _currentToken.getType() != Token.EOF;
  }
  
  
  /**
   * Retrieving the next {@link Token}. The method works in this order:<br>
   *<ol><li>
   *   Check for an end-of-file condition. If there is such a condition then
   *   return it.
   *</li><li>
   *   Try to collect a sequence of whitespaces. If such a sequence can be found
   *   return if the flag <CODE>F_RETURN_WHITESPACES</CODE> is set, or skip these
   *   whitespaces.
   *</li><li>
   *   Check the next characters against all known line and block comments. If
   *   a line or block comment starting sequence matches, return if the flag
   *   <CODE>F_RETURN_WHITESPACES</CODE> is set, or skip the comment.
   *   If comments are returned they include their starting and ending sequences
   *   (newline in the case of a line comment)
   *</li><li>
   *   Check the next characters against all known string starting sequences. If
   *   a string begin could be identified return the string until and including
   *   the closing sequence
   *</li><li>
   *   Check the next characters against all known special sequences. Especially,
   *   find the longest possible match. If a special sequence could be identified
   *   then return it.
   *</li><li>
   *   Check for ordinary separators. If one could be found return it.
   *</li><li>
   *   Check the next characters against all known keywords. If a keyword could
   *   be identified then return it.
   *</li><li>
   *   Return the text portion until the next whitespace, comment, special
   *   sequence or separator.
   *</li></ol>
   *
   * @return found {@link Token} including the EOF token
   * @throws TokenizerException generic exception (list) for all problems that may occur while parsing
   * (IOExceptions for instance)
   */
  public Token nextToken() throws TokenizerException {
    Token token = new Token();
    
    // there are a lot of read operations, adjustment of members etc. So it
    // might be a good idea to synchronize this core operation of the class
    synchronized(this) {
      
__MAIN_LOOP__:
      do {
        // we know the starting positions
        token.setStartPosition(getReadPosition());
        token.setStartLine(_lineNumber);
        token.setStartColumn(_columnNumber);

        // no lookahead available
        switch (_lookAheadToken.getType()) {
        case Token.UNKNOWN:
          token.setToken(null);
        
          // take care to use the right order to test for different token types
          if ( ! test4Whitespace(token)) {
            if ( ! test4SpecialSequence(token)) {
              if ( ! test4Separator(token)) {
                if ( ! test4Normal(token)) {
                  token.setType(Token.EOF);
                }
              }
            }
          }
          break;
          
        case Token.LINE_COMMENT:
        case Token.BLOCK_COMMENT:
        case Token.STRING:
        case Token.SPECIAL_SEQUENCE:
          completeSpecialSequence(token);
          _lookAheadToken.setType(Token.UNKNOWN);
          break;
        case Token.SEPARATOR:
          completeSeparator(token);
          _lookAheadToken.setType(Token.UNKNOWN);
          break;
        default:
          completeWhitespace(token);
          _lookAheadToken.setType(Token.UNKNOWN);
        }

        // more actions depending on the token
        switch (token.getType()) {
        case Token.WHITESPACE:
        case Token.LINE_COMMENT:
        case Token.BLOCK_COMMENT:
          if ((_flags & Tokenizer.F_RETURN_WHITESPACES) == 0) {
            token.setType(Token.UNKNOWN);   // see loop control (while)
            break;
          }
          /* no break; */
        default:
          if ((_flags & Tokenizer.F_TOKEN_POS_ONLY) == 0) {    
            token.setToken(new String(_inputBuffer, _currentReadPos, token.getLength()));
          }
        }
        
        // compute new line and column positions (if flag is set) and complete
        // the token
        adjustLineAndColumn(token.getType(), token.getLength());
        token.setEndLine(_lineNumber);
        token.setEndColumn(_columnNumber);

        // this is the one and only point where the current read position is
        // adjusted (except for the data shifting in readMoreData).
        _currentReadPos += token.getLength();
        
      } while (token.getType() == Token.UNKNOWN);
      
      // store the retrieved token
      _currentToken = token;
    }
    return token;
  }
  
 
  /**
   * This method is a convenience method. It returns only the next token image
   * without any informations about its type or associated information.
   *
   * @return the token image of the next token
   * @throws TokenizerException generic exception (list) for all problems that may occur while parsing
   * (IOExceptions for instance)
   */
  public String next() throws TokenizerException {
    nextToken();
    return current();
  }
 
  
  /**
   * Retrieve the {@link Token} that was found by the last call to {@link #nextToken}.
   * @return the token that was found by the last call to <CODE>nextToken</CODE>
   * or <CODE>next</CODE>
   */
  public Token currentToken() {
    return _currentToken;
  }
  
 
  /**
   * Convenience method to retrieve only the token image of the {@link Token} that
   * would be returned by {@link #currentToken}.
   *
   * @return the token image of the current token
   * @see #currentToken
   */
  public String current() {
    Token token = currentToken();
    
    if ((_flags & Tokenizer.F_TOKEN_POS_ONLY) == 0 || token.getToken() != null) {
      return token.getToken();
    } else {
      return getText(token.getStartPosition(), token.getLength());
    }
  }

  
  /**
   * If the flag {@link Tokenizer#F_COUNT_LINES} is set, this method will return the
   * line number starting with 0 in the input stream. Lines are terminated by one of
   * the following end-of-line sequences:
   * <br><ul><li>
   * Carriage Return (ASCII 13, '\r'). This EOL is used on Apple Macintosh
   * </li><li>
   * Linefeed (ASCII 10, '\n'). This is the UNIX EOL character.
   * </li><li>
   * Carriage Return + Linefeed ("\r\n"). This is used on MS Windows systems.
   * </li></ul>
   * Note that we didn't choose to use the system property "line.separator" since
   * today windows files are transfered to UNIX and Macs and vice versa. However,
   * a combination of '\n' with a subsequent '\r' is considered to be two lines.
   *
   * @return the current line number starting with 0 or -1 if no line numbers are supplied.
   * @see #getColumnNumber
   */  
  public int getLineNumber() {
    return _lineNumber;
  }
  
  /**
   * If the flag {@link Tokenizer#F_COUNT_LINES} is set, this method will return the
   * current column positionstarting with 0 in the input stream.
   *
   * @return the current column position
   * @see #getLineNumber
   */  
  public int getColumnNumber() {
    return _columnNumber;
  }
  
  /**
   * This method returns the absolute offset in characters to the start of the
   * parsed stream. Together with {@link AbstractTokenizer#currentlyAvailable} 
   * it describes the currently available text "window".
   *
   * @return the absolute offset of the current text window in characters from 
   *         the start of the data source of the Tokenizer
   */
  public int getRangeStart() {
    return _rangeStart;
  }
  
  /**
   * Getting the current read offset. This is the absolute position where the
   * next call to <CODE>nextToken</CODE> or <CODE>next</CODE> will start.
   *
   * @return the absolute offset in characters from the start of the data source 
   *         of the Tokenizer where reading will be continued
   */
  public int getReadPosition() {
    return _rangeStart + _currentReadPos;
  }
  
  /**
   * Retrieving the number of the currently available characters. This includes
   * both characters already parsed by the <CODE>Tokenizer</CODE> and characters
   * still to be analyzed.<br>
   *
   * @return number of currently available characters
   */
  public int currentlyAvailable() {
    return _currentWritePos;
  }
  
  
  /**
   * Try to read more data into the text buffer of the tokenizer. This can be
   * useful when a method needs to look ahead of the available data or a skip
   * operation should be performed.<br>
   * The method returns the same value than an immediately following call to 
   * {@link currentlyAvailable} would return.<br>
   *
   * @return  the number of character now available
   * @throws  TokenizerException generic exception (list) for all problems that 
   *          may occur while reading (IOExceptions for instance)
   */
  public int readMore() throws TokenizerException {
    readMoreData();
    return currentlyAvailable();
  }
  
  
  /**
   * This method tells the tokenizer to skip the given number of characters
   * starting on the current read position as can be retrieved by {@link #getReadPosition}.
   * The given number of characters must be less or equal to 
   * {@link #currentlyAvailable} - ({@link #getReadPosition} - {@link #getRangeStart}).
   *
   * @param numberOfChars   Number of characters to skip
   */
  public void skip(int numberOfChars) throws IndexOutOfBoundsException {
    int available = _currentWritePos - _currentReadPos;
    
    if (numberOfChars > available) {
      throw new ExtIndexOutOfBoundsException(
                  "Number of characters to skip ({0}) exceeds the available number ({1}).", 
                  new Object[] { new Integer(numberOfChars), new Integer(available) } 
                );
    } else if (numberOfChars < 0) {
      throw new ExtIndexOutOfBoundsException(
                  "Number of characters to skip ({0}) is negative.", 
                  new Object[] { new Integer(numberOfChars) } 
                );
    }
    
    _currentReadPos += numberOfChars;
    _lookAheadToken  = null;
  }
  

  /**
   * Retrieve text from the currently available range. The start and length
   * parameters must be inside {@link #getRangeStart} and
   * <CODE>getRangeStart + {@link #currentlyAvailable}</CODE>.
   *
   * @param   start   position where the text begins
   * @param   len     length of the text
   * @return  the text beginning at the given position ith the given length
   * @throws  IndexOutOfBoundsException if the starting position or the length 
   *          is out of the current text window
   */
  public String getText(int start, int len) throws IndexOutOfBoundsException {
    if (start < _rangeStart) {
      throw new ExtIndexOutOfBoundsException(
                  "Start position {0} lower than the current text window start {1}.", 
                  new Object[] { new Integer(start), new Integer(_rangeStart) } 
                );
    } else if (start + len > _rangeStart + _currentWritePos) {
      throw new ExtIndexOutOfBoundsException(
                  "required text starting at position {0} with length {1} exceeds current text window starting at {2} with length {3}.", 
                  new Object[] { 
                    new Integer(start), new Integer(len), 
                    new Integer(_rangeStart),  new Integer(currentlyAvailable()) 
                  }
                );
    }
    return new String(_inputBuffer, start - _rangeStart, len);
  }
  
  /**
   * Get a single character from the current text range.
   *
   * @param pos position of the required character
   * @return the character at the specified position
   * @throws IndexOutOfBoundsException if the parameter <CODE>pos</CODE> is not 
   *         in the available text range (text window)
   */
  public char getChar(int pos) throws IndexOutOfBoundsException {
    if (pos < _rangeStart || pos >= _currentWritePos) {
      throw new ExtIndexOutOfBoundsException(
                  "Given position {0} is out of current text window starting at {2} with length {3}.", 
                  new Object[] { 
                    new Integer(pos), new Integer(_rangeStart), new Integer(currentlyAvailable())
                  } 
                );
    }
    return _inputBuffer[pos - _rangeStart];
  }

  
  /**
   * This is a method designed to be used by those who can guarantee to be in a 
   * valid position range. It is equivalent to the {@link #getChar}
   * method above except that it does not check for a invalid position.
   *<br>
   * When passing an invalid position value, two situations may occure: The
   * invalid position is really out of bounds of the input buffer (an unchecked 
   * exception will be thrown) or it points into an area where old or even 
   * initial values lay.
   *
   * @param pos position of the required character
   * @return the character at the specified position
   */
  public char getCharUnchecked(int pos) {
    return _inputBuffer[pos - _rangeStart];
  }

  
  //---------------------------------------------------------------------------
  // embedded tokenizer support
  //
  
  /**
   * Adding an embedded tokenizer. Embedded tokenizer work on the same input 
   * buffer as their base tokenizer. A situation where embedded tokenizer could
   * be applied, is a HTML stream with cascading style sheet (CSS) and JavaScript
   * parts.<br>
   * There are no internal means of switching from one tokenizer to another. 
   * This should be done by the caller using the method {@link #switchTo}.
   *
   * @param  tokenizer   an embedded tokenizer
   */
  public void addTokenizer(AbstractTokenizer tokenizer) {
    AbstractTokenizer curr = this;
    
    while (curr._nextTokenizer != null) {
      curr = curr._nextTokenizer;
    }
    curr._nextTokenizer      = tokenizer;
    tokenizer._prevTokenizer = curr;
    
    // share the input buffer of the base tokenizer
    tokenizer._inputBuffer = getBaseTokenizer(this)._inputBuffer;
  }

  
  /**
   * Changing fron one tokenizer to another. If the given tokenizer has not been
   * added with {@link #addTokenizer}, an exception is thrown.<br>
   * The <CODE>switchTo</CODE> method does the nessecary synchronisation between
   * <CODE>this</CODE> and the given tokenizer. The user is therefore responsible
   * to use <CODE>switchTo</CODE> whenever a tokenizer change is nessecary. It
   * must be done this way:
   *<blockquote><pre>
   *   Tokenizer base     = new MyTokenizer(...)
   *   Tokenizer embedded = new MyTokenizer(...)
   *
   *   // setting properties (comments, keywords etc.)
   *   ...
   *
   *   // embedding a tokenizer
   *   base.addTokenizer(embedded);
   *   
   *   // tokenizing with base
   *   ...
   *   if (<i>switch_condition</i>) {
   *     base.switchTo(embedded);
   *   }
   *
   *   // tokenizing with embedded
   *   ...
   *   if (<i>switch_condition</i>) {
   *     embedded.switchTo(base);
   *   }
   *</pre></blockquote>
   * That way we avoid a more complex synchronisation between tokenizers whenever
   * one of them parses the next data in the input stream. However, the danger
   * of not synchronized tokenizers remains, so take care.
   *
   * @param tokenizer   the tokenizer that should be used from now on
   */
  public void switchTo(AbstractTokenizer tokenizer) 
    throws TokenizerException
  {
    if (tokenizer._inputBuffer != _inputBuffer) {
      throw new TokenizerException("Trying to switch to an alien tokenizer (not added with addTokenizer).", null);
    }
    tokenizer._currentReadPos  = this._currentReadPos;
    tokenizer._currentWritePos = this._currentWritePos;
    tokenizer._columnNumber    = this._columnNumber;
    tokenizer._lineNumber      = this._lineNumber;
    tokenizer._rangeStart      = this._rangeStart;
  }


  //---------------------------------------------------------------------------
  // Methods that may be overwritten in derived classes
  //
  
  /**
   * This method checks if the character is a whitespace. Implement Your own
   * code for situations where this default implementation is not fast enough
   * or otherwise not really good.
   *
   * @param testChar  check this character
   * @return <CODE>true</CODE> if the given character is a whitespace,
   *         <CODE>false</CODE> otherwise
   */
  protected boolean isWhitespace(char testChar) {
    int idx = -1;
    
    if (_defaultWhitespaces) {
      switch (testChar) {
        case ' ':
        case '\n':
        case '\r':
        case '\t':
          idx = 0;
          break;
      }
          
    } else {
      idx = indexInSet(testChar, _whitespaces);
    }
    return (idx >= 0);
  }
      
 
  /**
   * This method detects the number of whitespace characters starting at the given
   * position. It should use {@link #getChar} or {@link #getCharUnchecked} to 
   * retrieve a character to check.
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
  protected int readWhitespaces(int startingAtPos, int maxChars) throws TokenizerException {
    int len   = 0;
    
    while (len < maxChars) {
      if ( ! isWhitespace(getCharUnchecked(startingAtPos + len))) {
        break;
      }
      len++;
    }
    return len;
  }
  
  
  /**
   * This method reads characters in a string until it detects the end of
   * the string (this is the end sequence of the string, '"' in C and Java)
   * It should use {@link #getChar} or {@link #getCharUnchecked} to retrieve a 
   * character to check.
   *<br>
   * The method should return the number of characters identified as parts of the
   * string starting from and including the given start position.
   *<br>
   * Do not attempt to actually read more data or do anything that leads to the
   * change of the data source or to tokenizer switching. This is done by the 
   * tokenizer framework.
   *
   * @param   startingAtPos   start reading the string from this position
   * @param   maxChars        check up to this number of characters 
   * @return  number of characters being part of the line comment
   * @throws  TokenizerException something happened
   */
  protected int readString(int startingAtPos, int maxChars) throws TokenizerException {
    return 0;
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
    return indexInSet(testChar, _separators) >= 0;
  }

  
  /**
   * This method checks at the given position if it contains a a special sequence. 
   * Unlike the method {@link #test4SpecialSequence} it does nothing more.
   * It should use {@link #getChar} or {@link #getCharUnchecked} to retrieve 
   * characters at need.
   *
   * @param  startingAtPos  check at this position
   * @throws TokenizerException failure while reading data from the input stream
   * @return <CODE>true</CODE> if a special sequence was found at the given offset,
   *         <CODE>false</CODE> otherwise
   */
  protected TokenizerProperty isSequenceCommentOrString(int startingAtPos) 
    throws TokenizerException 
  {
    // we need the longest possible match
    TokenizerProperty prop   = null;
    int               offset = startingAtPos - (_currentReadPos + _rangeStart);
    
    for (int pos = 0; pos < _sequences.length; ++pos) {
      if (_sequences[pos] != null) {
        TokenizerProperty p = _sequences[pos].searchBinary(null, offset, false);
        
        if (   p != null 
            && (   prop == null
                || p.getValues()[0].length() > prop.getValues()[0].length())) {
          prop = p;
        }
      }
    }
    return prop;
  }

  
  //---------------------------------------------------------------------------
  // Implementation
  //

  /**
   * This method creates the sorted arrays to store the case-sensitive and
   * -insensitive special sequences, comments, strings etc.
   *
   * @param prop  the description of the new sequence
   */
  protected void addSpecialSequence(TokenizerProperty prop) {
    int arrayIdx;
    int flags = prop.getFlags();
    
    if ((flags & Tokenizer.F_NO_CASE) == 0) {
      if (_sequences[0] == null) {
        _sequences[0] = new SortedArray(this, flags);
      }
      arrayIdx = 0;
    } else {
      if (_sequences[1] == null) {
        _sequences[1] = new SortedArray(this, flags);
      }
      arrayIdx = 1;
    }
    _sequences[arrayIdx].addSpecialSequence(prop);
  }
  
  
  /**
   * Search a special sequence for the getter methods.
   *
   * @param   specSeq   (starting) sequence to be found
   * @return  the {@link TokenizerProperty} of the sequence or <code>null</code>
   */
  protected TokenizerProperty searchBinary(String specSeq) {
    try {
      for (int pos = 0; pos < _sequences.length; ++pos) {
        TokenizerProperty prop;
        
        if (   _sequences[pos] != null 
            && (prop = _sequences[pos].searchBinary(specSeq, 0, false)) != null) {
          return prop;
        }
      } 
    } catch (TokenizerException ex) {
      // this shouldn't happen at all since we do not read from the input stream
      throw new ExtRuntimeException(
                  ex,
                  "While trying to retrieve the companion of special sequence \"{0}\".", 
                  new Object[] { specSeq }
                );
    }
    
    // no sequences or not found
    return null;
  }
  
  
  /**
   * Embedded tokenizers have their base tokenizer they share the input stream
   * with.
   *
   * @param t   the tokenizer thats base tokenizer should be found
   * @return the base tokenizer (the one owning the input stream and text buffer)
   */
  protected AbstractTokenizer getBaseTokenizer(AbstractTokenizer t) {
    while (t._prevTokenizer != null) {
      t = t._prevTokenizer;
    }
    return t;
  }
  
  /**
   * This method organizes the input buffer. It moves the current text window if
   * nessecary or allocates more space, if data should be kept completely (see the
   * {@link Tokenizer#F_KEEP_DATA} flag).
   * Its main purpose is to call the {@link #read} method of the implementing class.
   *
   * @return number of read bytes or -1 if an end-of-file condition occured
   * @throws TokenizerException wrapped exceptions from the {@link #read} method
   */
  protected int readMoreData() throws TokenizerException  {
    // its always the base tokenizer doing the reading
    int               bytes = 0;
    AbstractTokenizer base  = getBaseTokenizer(this);
    
    if (base != this) {
      return base.readMoreData();
    }
    
    // this is a good moment to move already read data if the write position is
    // near the end of the buffer and there is a certain space before the current
    // read position
    int readOffset = 0;
    
    if ((_flags & Tokenizer.F_KEEP_DATA) == 0) {
      if (   _currentReadPos  > _inputBuffer.length / 4
          && _currentWritePos > (3 * _inputBuffer.length) / 4) {
        System.arraycopy(_inputBuffer, _currentReadPos, _inputBuffer, 0, _currentWritePos - _currentReadPos);
        readOffset        = _currentReadPos;
        _rangeStart      += _currentReadPos;
        _currentWritePos -= _currentReadPos;
        _currentReadPos   = 0;
      }
    }
    
    // if there is no space any more and data couldn't be moved (see above)
    // we need a new input buffer
    if (_currentWritePos >= _inputBuffer.length) {
      char[] newBuffer = new char[_inputBuffer.length * 2];
      
      if ((_flags & Tokenizer.F_KEEP_DATA) != 0) {
        System.arraycopy(_inputBuffer, 0, newBuffer, 0, _currentWritePos);
      } else {
        System.arraycopy(_inputBuffer, _currentReadPos, newBuffer, 0, _currentWritePos - _currentReadPos);
      }
      _inputBuffer = newBuffer;
    }
    
    // now read more data. We need to block the potentially non-blocking read 
    // method of the implementing class
    // Note that a bytes can only be != 0 with <0 for EOF
    while (bytes == 0) {
      try {
        bytes = read(_inputBuffer, _currentWritePos, _inputBuffer.length - _currentWritePos);
      } catch (Exception ex) {
        throw new TokenizerException(ex);
      }
    }
    if (bytes > 0) {
      _currentWritePos += bytes;
    }
    
    // Inform all embedded tokenizers about input buffer changes
    base.synchronizeAll(readOffset);
    return bytes;
  }
  

  /**
   * When the method {@link readMoreData} changes the contents of the input buffer 
   * or the input buffer itself, all embedded tokenizers must be synchronized.
   * That means their member variables are adjusted to the base tokenizer.
   *
   * @param readPosOffset   add this (negative) offset to all current read positions
   */
  protected void synchronizeAll(int readPosOffset) {
    AbstractTokenizer base     = getBaseTokenizer(this);
    AbstractTokenizer embedded = base;

    while ((embedded = embedded._nextTokenizer) != null) {
      embedded._inputBuffer     = base._inputBuffer;
      embedded._currentWritePos = base._currentWritePos;
      embedded._currentReadPos += readPosOffset;
    }
  }

  /**
   * The number of characters until the next comment, whitespace, string, special
   * sequence or separator are determined.
   *
   * @param token buffer to receive information about the keyword or normal token
   * @return <CODE>true</CODE> if a keyword or normal text (e.g. an identifier) 
   *         has been found, <CODE>false</CODE> otherwise
   * @throws TokenizerException failure while reading data from the input stream
   */
  protected boolean test4Normal(Token token) throws TokenizerException {
    // find out the return value (length of normal token)
    int len = 0;
    int pos;
    
    while (_currentReadPos + len < _currentWritePos || readMoreData() > 0) {
      if (   isWhitespace(len) 
          || isSpecialSequence(len) 
          || isSeparator(len)) {
        break;
      }
      len++;
    }
    
    // something else found (whitespace, separator, EOF, ...)
    if (len <= 0) {
      return false;
    }
    
    // test on keyword
    if (_keywords[0] != null || _keywords[1] != null) {
      TokenizerProperty prop    = null;
      String            keyword = new String(_inputBuffer, _currentReadPos, len);
      
      if (_keywords[0] != null) {
        prop = (TokenizerProperty)_keywords[0].get(keyword);
      }
      if (prop == null && _keywords[1] != null) {
        keyword = keyword.toUpperCase();
        prop    = (TokenizerProperty)_keywords[1].get(keyword);
      }
      if (prop != null) {
        token.setType(Token.KEYWORD); 
        token.setLength(keyword.length());
        token.setCompanion(prop.getCompanion());
      } else {
        token.setType(Token.NORMAL);
        token.setLength(len);
      }
    } else {
      token.setType(Token.NORMAL);
      token.setLength(len);
    }
    return true;
  }
  
  
  /**
   * Check if the current read position contains a whitespace. If so retrieve
   * the whole sequence of whitespaces.
   *
   * @param token   add information to this buffer
   * @throws TokenizerException failure while reading data from the input stream
   * @return <CODE>true</CODE> if a whitespace sequence was found
   *         <CODE>false</CODE> otherwise
   */
  protected boolean test4Whitespace(Token token) throws TokenizerException {
    if (_currentReadPos < _currentWritePos ||  readMoreData() > 0) {
      if (isWhitespace(_inputBuffer[_currentReadPos])) {
        completeWhitespace(token);
        return true;
      }
    }
    return false;
  }
  
  /**
   * After having identified a whitespace, this method continues to read data
   * until it detects a non-whitespace.<br>
   * The method is expected to fill in the type and the length of the whitespace
   * token using the methods {@link Token#setType} and {@link Token#setLength}.
   *
   * @param token   add information to this buffer
   * @throws TokenizerException failure while reading data from the input stream
   */
  protected void completeWhitespace(Token token) throws TokenizerException {
    int start     = _currentReadPos + 1;  // the first whitespace we have already
    int available = _currentWritePos - start;
    int len       = readWhitespaces(_rangeStart + start, available);
    
    while (len == available) {
      if (readMoreData() <= 0) {
        break;
      }
      start    += len;
      available = _currentWritePos - start;
      len      += readWhitespaces(_rangeStart + start, available);
    }

    token.setType(Token.WHITESPACE);
    token.setLength(len + 1);           // the first whitespace we had already
  }
  
  /**
   * This method checks at the given offset if it is a whitespace. Unlike the
   * method {@link #test4Whitespace} it does nothing more.
   *
   * @param offset  check at this position relative to the current read position
   * @throws TokenizerException failure while reading data from the input stream
   * @return <CODE>true</CODE> if a whitespace sequence was found at the given offset,
   *         <CODE>false</CODE> otherwise
   */
  protected boolean isWhitespace(int offset) throws TokenizerException {
    // enough data ?
    if (_currentReadPos + offset >= _currentWritePos && readMoreData() < 0) {
      return false;
    }
    
    // did we find a whitespace?
    if (isWhitespace(_inputBuffer[_currentReadPos + offset])) {
      _lookAheadToken.setType(Token.WHITESPACE);
      return true;
    } else {
      return false;
    }
  }
      
 
  /**
   * Check if the current read position contains a separator. If so retrieve
   * the separator.
   *
   * @param token   add information about the separator to this buffer
   * @throws TokenizerException failure while reading data from the input stream
   * @return <CODE>true</CODE> if a separator was found,
   *         <CODE>false</CODE> otherwise
   */
  protected boolean test4Separator(Token token) throws TokenizerException {
    if (isSeparator(0)) {
      _lookAheadToken.setType(Token.UNKNOWN);
      completeSeparator(token);
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * After having identified a separator, this method stores information about
   * it in the given <code>Token</code>. Separators are always single characters.
   *
   * @param token   add information about the separator to this buffer
   * @throws TokenizerException failure while reading data from the input stream
   */
  protected void completeSeparator(Token token) throws TokenizerException {
    token.setType(Token.SEPARATOR);
    token.setLength(1);
  }
  
  
  /**
   * This method checks at the given offset if it contains a separator. Unlike the
   * method {@link #test4Separator} it does nothing more.
   *
   * @param offset  check at this position relative to the current read position
   * @throws TokenizerException failure while reading data from the input stream
   * @return <CODE>true</CODE> if a separator was found at the given offset,
   *         <CODE>false</CODE> otherwise
   */
  protected boolean isSeparator(int offset) throws TokenizerException {
    if (_currentReadPos + offset < _currentWritePos ||  readMoreData() > 0) {
      int idx = indexInSet(_inputBuffer[_currentReadPos + offset], _separators);
      
      if (idx >= 0) {
        _lookAheadToken.setType(Token.SEPARATOR);
        return true;
      }
    }
    return false;
  }
  
  
  /**
   * Check if the current read position is the start of a special sequence. If 
   * so retrieve the special sequence.
   *
   * @param token   add information about the special sequence to this buffer
   * @throws TokenizerException failure while reading data from the input stream
   * @return <CODE>true</CODE> if a special sequence was found,
   *         <CODE>false</CODE> otherwise
   */
  protected boolean test4SpecialSequence(Token token) throws TokenizerException {
    TokenizerProperty prop = isSequenceCommentOrString(_rangeStart + _currentReadPos);
    
    if (prop != null) {
      _lookAheadToken.setCompanion(prop);
      completeSpecialSequence(token);
      return true;
    } else {
      return false;
    }
  }
  
  
  /**
   * After having identified a special sequence, this method completes this sequence
   * and stores information about it in the given <code>Token</code>. Note that 
   * a special sequence is not identical to the token image that comprises the
   * special sequence. For instance, a line comment start with the character
   * sequence that identifies the line comment but contains also the data up to 
   * and including the end-of-line combination.
   *
   * @param token   add information about the separator to this buffer
   */
  protected void completeSpecialSequence(Token token) throws TokenizerException {
    TokenizerProperty prop = (TokenizerProperty)_lookAheadToken.getCompanion();
    String            seq  = prop.getValues()[0];
      
    token.setType(prop.getType());
    token.setCompanion(prop.getCompanion());
      
    switch (prop.getType()) {
    case Token.STRING:
      token.setLength(completeString(seq.length(), prop));
      break;
    case Token.BLOCK_COMMENT:
      token.setLength(completeBlockComment(seq.length(), prop));
      break;
    case Token.LINE_COMMENT:
      token.setLength(completeLineComment(seq.length()));
      break;
    default:
      token.setLength(seq.length());
    }          
  }
  
  /**
   * This method checks at the given offset if it contains a a special sequence. 
   * Unlike the method {@link #test4SpecialSequence} it does nothing more.
   *
   * @param offset  check at this position relative to the current read position
   * @throws TokenizerException failure while reading data from the input stream
   * @return <CODE>true</CODE> if a special sequence was found at the given offset,
   *         <CODE>false</CODE> otherwise
   */
  protected boolean isSpecialSequence(int offset) throws TokenizerException {
    TokenizerProperty prop = isSequenceCommentOrString(_rangeStart + _currentReadPos + offset);
    
    if (prop != null) {
      _lookAheadToken.setType(Token.SPECIAL_SEQUENCE);
      _lookAheadToken.setCompanion(prop);
      return true;
    } else {
      return false;
    }
  }
  
  
  /**
   * Completing a line comment. After a line comment sequence has been found, all
   * characters up to and including the end-of-line combination belong to the 
   * line comment. Note that on reaching end-of-file a line comment does not 
   * nessecarily ends with an end-of-line sequence (linefeed for example).
   *
   * @param  offset  start completing at this position relative to the current read position
   * @throws TokenizerException failure while reading data from the input stream
   * @return length of the line comment including start and terminating newline
   */
  protected int completeLineComment(int offset) 
    throws TokenizerException 
  {
    int len = offset;

    while (_currentReadPos + len < _currentWritePos || readMoreData() > 0) {
      switch (_inputBuffer[_currentReadPos + len]) {
      case '\r':
        len++;
        if (_currentReadPos + len < _currentWritePos || readMoreData() > 0) {
          if (_inputBuffer[_currentReadPos + len] == '\n') {
            len++;
          }
        }
        return len;       // this should be one of the usual return points
      case '\n':
        return len + 1;     // this should be the other of the usual return points
      default:
        len++;
      }
    }
    
    // this is reached on EOF
    return len;
  }
  
  
  /**
   * Completing a block comment. After a block comment sequence has been found, all
   * characters up to and including the end sequence of the block comment belong 
   * to the block comment. Note that on reaching end-of-file a block comment does 
   * not nessecarily ends with an end-of-block-comment sequence.
   *
   * @param offset  start completing at this position relative to the current read position
   * @throws TokenizerException failure while reading data from the input stream
   * @return length of the block comment including start and end sequence
   */
  protected int completeBlockComment(int offset, TokenizerProperty prop) 
    throws TokenizerException 
  {
    String  start  = prop.getValues()[0];
    String  end    = prop.getValues()[1];
    boolean noCase = (prop.getFlags() & F_NO_CASE) != 0;
    int     len    = offset;
    int     level  = 0;

  __LOOP__:
    do {
      // test on nested comments: we take only care for nesting the same
      // block comment
      if (   (_flags & Tokenizer.F_ALLOW_NESTED_COMMENTS) != 0) {
        switch (comparePrefix(len, start, noCase)) {
        case 0:     // comment start identified
          level++;
          len += start.length();
          continue __LOOP__;
        case -1:    // EOF reached
          return _currentWritePos - _currentReadPos;   
        }
      }
      
      // is it the end ?
      switch (comparePrefix(len, end, noCase)) {
      case 0:       // comment end identified
        level--;
        len += end.length();
        break;
      case -1:      // EOF reached
        return _currentWritePos - _currentReadPos;
      default:
        len++;
      }
    } while (level >= 0);
    
    // this is the regular return point
    return len;
  }
  
  
  /**
   * Completing a string. After a string start sequence has been found, all
   * characters up to and including the end-of-string sequence belong to the
   * string. Note that on reaching end-of-file a string does not nessecarily ends 
   * with an end-of-string sequence.
   *
   * @param offset  start completing at this position relative to the current read position
   * @throws TokenizerException failure while reading data from the input stream
   * @return length of the string including start and end sequence
   */
  protected int completeString(int offset, TokenizerProperty prop) 
    throws TokenizerException 
  {
    String  end           = prop.getValues()[1];
    String  esc           = prop.getValues()[2];
    boolean noCase        = (prop.getFlags() & F_NO_CASE) != 0;
    boolean escEqualsEnd  =    ( ! noCase && esc.compareTo(end)           == 0)
                            || (   noCase && esc.compareToIgnoreCase(end) == 0);
    int     len           = offset;

    while (true) {
      // test on escape
      if (esc != null) {
        switch (comparePrefix(len, esc, noCase)) {
        case 0:       // escape found
          len += esc.length();
          if (escEqualsEnd) {
            switch (comparePrefix(len, end, noCase)) {
            case 0:
              len += end.length();
              break;
            case -1:      // EOF reached
              return _currentWritePos - _currentReadPos;   
            default:
              return len; // this is the regular return point if the esc is the string end
            }
          } else {
            len++;        // esc != string end: skip the next character
          }
          continue;
        case -1:          // EOF reached
          return _currentWritePos - _currentReadPos;   
        }
      }

      // test on end sequence
      switch (comparePrefix(len, end, noCase)) {
      case 0:             // this is the regular return point if esc != string end
        len += end.length();    
        return len;
      case -1:            // EOF reached    
        return _currentWritePos - _currentReadPos;   
      }
     
      len++;
    }
  }

  
  /**
   * This method compares the characters at the given offset (from the current
   * read position) with the given prefix.
   *
   * @param   offset  start comparing at this offset from the current read position
   * @param   prefic  compare read data with this prefix
   * @param   noCase  case- or not case-sensitive comparison
   * @throws  TokenizerException failure while reading data from the input stream
   * @return  0 if the the given prefix matches the input stream, -1 on EOF and
   *          1 if not matching
   */
  protected int comparePrefix(int offset, String prefix, boolean noCase) 
    throws TokenizerException 
  {
    // compare
    int len = prefix.length();
    
    for (int pos = offset; pos < offset + len; ++pos) {
      // do we have enough data
      if (_currentReadPos + pos >= _currentWritePos && readMoreData() < 0) {
        return -1;
      }
      
      // compare single character
      char c1 = prefix.charAt(pos - offset);
      char c2 = _inputBuffer[_currentReadPos + pos];
      
      if (   c1 != c2
          && (! noCase || Character.toUpperCase(c1) != Character.toUpperCase(c2))) {
        return 1;
      }
    }
    
    // found
    return 0;
  }
  

  /**
   * A given character is searched in the given character set. This set may
   * contain ranges, for example "a-z" for all lowercase alpha characters. To use
   * the minus sign itself, escape it by "\-".
   *
   * @return  index in the given character set where the given character
   *          was found or -1 when not ound
   */
  protected int indexInSet(char ch, String set) {
    int  len = (set != null) ? set.length() : 0;
    char start, end, setChar;
    char chUp = 0;
    
    if ((_flags & Tokenizer.F_NO_CASE) != 0){
      chUp = Character.toUpperCase(ch);
    }
    
    for (int ii = 0; ii < len; ++ii)  {
      switch (setChar = set.charAt(ii)) {
      case '-':
        start = (ii > 0) ? set.charAt(ii - 1) : 0;
        end   = (ii < len - 1) ? set.charAt(ii + 1) : 0xFFFF;
        if (ch >= start && ch <= end) {
          return ii;
        }
        ii += 2; 
        break;
        
      case '\\':
        setChar = (ii + 1 >= len) ? 0 : set.charAt(ii + 1);
        ii++;
        /* no break */
        
      default:
        if (   ch == setChar
            || ((_flags & Tokenizer.F_NO_CASE) != 0 && chUp == Character.toUpperCase(setChar))) {
          return ii;
        }
      }
    }
    
    // not found
    return -1;
  }
  
  /**
   * The method recomputes the line and column position of the tokenizer, if the 
   * flag {@link Tokenizer#F_COUNT_LINES} is set. It gets the token type of the
   * {@link Token} that has been retrieved by the calling {@link #nextToken}.
   * Using the tokenizer control flags and certain other information it tries to
   * to find end-of-line sequences as fast as possible. For example, a line 
   * comment should always contain a end-of-line sequence, so we can simply 
   * increase the line count and set the column count to 0.
   *
   * @param type    the type of the current token
   * @param length  the length of the current token
   */
  protected void adjustLineAndColumn(int type, int length) {
    // line and column counting not required
    if ((_flags & Tokenizer.F_COUNT_LINES) == 0) {
      return;
    }
    
    // there might be a simple way to determine the current line and column position
    switch (type) {
    case Token.EOF:
      return;
        
    case Token.LINE_COMMENT:        // a line comment always ends with a newline
      _lineNumber++;
      _columnNumber = 0;
      return;
      
    case Token.SPECIAL_SEQUENCE:
    case Token.NORMAL:
    case Token.KEYWORD:
      if (_newlineIsWhitespace) {   // newline is a whitespace character
        _columnNumber += length;    // it does therefore not occure in other
        return;                     // tokens
      }
      break;
        
    case Token.WHITESPACE:
      if (!_newlineIsWhitespace) {  // newline is not a whitespace; we do not
        _columnNumber += length;    // have to test for it in the current 
        return;                     // token
      }
      break;
    }
    
    // count it
    for (int pos = _currentReadPos; pos < _currentReadPos + length; ++pos) {
      switch (_inputBuffer[pos]) {
      case '\r':
        if (pos + 1 >= _currentReadPos + length || _inputBuffer[pos + 1] != '\n') {
          _lineNumber++;
          _columnNumber = 0;
          break;
        }
        pos++;
        /* no break; */
      case '\n':
        _lineNumber++;
        _columnNumber = 0;
        break;
        
      default:
        _columnNumber++;
      }
    }
  }
  

  //---------------------------------------------------------------------------
  // Members
  //
  
  /**
   * overall tokenizer flags.
   */
  protected int _flags = 0;
  
  /**
   * current whitespace characters including character ranges.
   */
  protected String _whitespaces = Tokenizer.DEFAULT_WHITESPACES;
  
  /**
   * current separator characters including character ranges.
   */
  protected String _separators = Tokenizer.DEFAULT_SEPARATORS;
  
  /**
   * The first element is the <code>SortedArray</code> for the case-sensitive 
   * sequences, the second is for the case-insensitive ones.
   */
  protected SortedArray[] _sequences = new SortedArray[2];
  
  /**
   * Like the array {@link #_sequences} this two-element Array contains two
   * {@link java.util.HashMap}, the first for the case-sensitive keywords, the
   * second for the case-insensitive ones.
   * With Java2, replace with {@link java.util.HashMap}.
   *
   * @deprecated
   */
  protected HashMap[] _keywords = new HashMap[2];
  
  /**
   * this flag speeds up the line and column counting
   */
  protected boolean _newlineIsWhitespace  = false;
  
  /**
   * This buffer holds the currently read data. Dont use a buffered reader, since
   * we do buffering here.
   */
  protected char[] _inputBuffer = null;

  /**
   * index in {@link #_inputBuffer} there {@link #nextToken} will start parsing.
   */
  protected int _currentReadPos = 0;

  /**
   * index in {@link #_inputBuffer} there {@link #readMoreData} will fill in new data.
   */
  protected int _currentWritePos = 0;
  
  /**
   * Mapping of index 0 of {@link #_inputBuffer} to the absolute start of the 
   * input stream.
   */
  protected int _rangeStart = 0;
  
  /**
   * if line counting is enabled, this contains the current line number starting
   * with 0.
   */
  protected int _lineNumber = -1;

  /**
   * if line counting is enabled, this contains the current column number starting
   * with 0.
   */
  protected int _columnNumber = -1;
  
  /**
   * Token found by the last call to {@link #nextToken}
   */
  protected Token _currentToken = null;
  
  /**
   * If a mthod could detect the tokewn after the currently assembled one, infomation
   * regarding that token are stored here.
   */
  protected Token _lookAheadToken = new Token();
  
  /**
   * For embedded tokenizers: this is the list of the succeding tokenizers
   */
  protected AbstractTokenizer _nextTokenizer = null;

  /**
   * For embedded tokenizers: this is the list of the previous tokenizers
   */
  protected AbstractTokenizer _prevTokenizer = null;
  
  /**
   * For fast whitespace scans, check the common whitespaces first
   */
  private boolean _defaultWhitespaces = false;
}



//---------------------------------------------------------------------------
// inner classes
//

/**
 * This hidden class implements a binary tree for the special sequences. It is
 * designed as a sorted array.
 */
final class SortedArray {

  /**
   * Constructor needs the Tokenizer and the flags (compare case-sensitive or
   * not).
   *
   * @param parent    the Tokenizer constructing me
   * @param flags     <code>F_...</code> flags as defined in {@link Tokenizer}
   */
  public SortedArray(AbstractTokenizer parent, int flags) {
    _parent = parent;
    _flags  = flags;
  }

  /**
   * Binary search in the special sequence tree. There are two results. First,
   * the method returns a value 0, >0 or <0 for the binary search, and in the
   * <code>nearestMatch</code> the index in the tree, where a new entry should be
   * placed.<br>
   * If the returned value is 0, a new entry goes to the list in nearestMatch[0].
   *<br>
   * If the returned value is <0, a new list at nearestMatch[0] + 1 will be
   * created.<br>
   * If the returned value is >0, a new list at nearestMatch[0] will be created.
   *
   * @param startChar search this character
   * @param nearestMatch  in this one-element array the method returns the 
   *                      index next to which new entries should be inserted
   * @return 
   */
  private int searchIndex(char startChar, int[] nearestMatch) {
    // do the binary search
    char upChar = Character.toUpperCase(startChar);
    int  left   = 0;
    int  right  = _array.size() - 1;
    int  res    = -1;

    nearestMatch[0] = -1;

    // typical binary search for the matching start character
  __MAIN_LOOP__:
    while (left <= right) {
      int idx;

      // do we really have a new one
      if ((idx = (right + left) / 2) == nearestMatch[0]) {
        break;
      }
      nearestMatch[0] = idx;

      // compare the starting characters
      SpecialSequence   listElem = (SpecialSequence)_array.get(idx);
      TokenizerProperty existing = listElem._property;
      String            exSeq    = existing.getValues()[0];

      if ((_flags & Tokenizer.F_NO_CASE) != 0) {
        res = Character.toUpperCase(exSeq.charAt(0)) - upChar;
      } else {
        res = exSeq.charAt(0) - startChar; 
      }

      // binary branching
      if (res == 0) { 
        break;                // found the list
      } else if (res < 0) {   
        left = idx + 1;       // take the upper half 
      } else {                
        right = idx - 1;      // take the lower half
      }
    }

    // res == 0 means found
    return res;
  }

  /**
   * Retrieving a special sequence at the given position. The method does not
   * check the given positionen for being inside the array boundaries.
   *
   * @param index   the position
   * @return the special sequence 
   */
  public SpecialSequence get(int index) {
    return (SpecialSequence)_array.get(index);
  }


  /**
   * Retrieving the size of this sorted array.
   *
   * @return the size of this array
   */
  public int size() {
    return _array.size();
  }


  /**
   * Search a given sequence in the special sequences vector. This is a combined
   * method. It can search for a given sequence or take the current reading
   * position to read a potential special sequence.
   * After having found a match in the list of special sequences, the method 
   * can be advised to remove the matching element.
   * Searching is done in a hopefully very effective way by binary search and 
   * length-ordered sequences with the same first character.
   *
   * @param specSeq   candidate for special sequence or <CODE>null</CODE> if the
   *                  input buffer should be read
   * @param offset    (only used when <CODE>specSeq</CODE> is null) offset from the
   *                  current read position
   */
  protected TokenizerProperty searchBinary(String specSeq, int offset, boolean remove) 
    throws TokenizerException
  {
    // no special sequences known or no more data
    if (  _array == null 
       || (specSeq == null
           && _parent._currentReadPos + offset >= _parent._currentWritePos 
           && _parent.readMoreData() <= 0)) {
      return null;
    }

    // do the binary search
    boolean fromStream   = (specSeq == null);
    int[]   nearestMatch = { -1 };
    char    startChar;

    if (specSeq == null) {
      startChar = _parent._inputBuffer[_parent._currentReadPos + offset];
    } else {
      startChar = specSeq.charAt(0);
    }
    if (searchIndex(startChar, nearestMatch) != 0) {
      return null;
    }

    // does the sequence come from the input stream ?
    SpecialSequence   listElem   = (SpecialSequence)_array.get(nearestMatch[0]);
    TokenizerProperty existing   = listElem._property;
    String            exSeq      = existing.getValues()[0];

    if (specSeq == null) {
      int len = exSeq.length();

      if (_parent._currentReadPos + offset + len >= _parent._currentWritePos) {
        _parent.readMoreData();

        if ((_parent._currentWritePos - (_parent._currentReadPos + offset)) < len) {
          len = _parent._currentWritePos - (_parent._currentReadPos + offset);
        }
        if (len <= 0) {
          return null;
        }
      }
      specSeq = new String(_parent._inputBuffer, _parent._currentReadPos + offset, len);
    }

    // try to find the longest match in the list of equaly starting sequences
    SpecialSequence prevElem = null;

    do {
      existing = listElem._property;
      exSeq    = existing.getValues()[0];

      // given sequences should be found as they are. Potential sequences from
      // the input stream are shortened approbriately as the list elements 
      // become shorter.
      if (specSeq.length() > exSeq.length()) {
        if (fromStream) {
          specSeq = specSeq.substring(0, exSeq.length());
        } else {
          return null;
        }
      }

      // only need to compare on equal lengths
      if (specSeq.length() == exSeq.length()) {
        int res;

        if ((_flags & Tokenizer.F_NO_CASE) != 0) {
          res = exSeq.compareToIgnoreCase(specSeq);
        } else {
          res = exSeq.compareTo(specSeq); 
        }
        if (res == 0) {
          if (remove) {
            if (prevElem == null) {
              if (listElem._next == null) {
                _array.remove(nearestMatch[0]);
              } else {
                listElem = listElem._next;
                _array.set(nearestMatch[0], listElem);
              }
            } else {
              prevElem._next = listElem._next;
            }
            return null;
          } else {
            return listElem._property;
          }
        } else if (res < 0) {   
          // no need to compare the spec.seq with the same length as
          // the current exSeq.
          specSeq = specSeq.substring(0, specSeq.length() - 1);
        }
      }
      prevElem = listElem;
      listElem = listElem._next;
    } while (listElem != null);

    // still not found
    return null;
  }


  /**
   * Inner method that controls the special sequence store. This store is 
   * implemented as a binary tree for fast search operations.
   *
   * @param prop  the special sequence to add
   */
  public void addSpecialSequence(TokenizerProperty prop) {
    // first call
    if (_array == null) {
      _array = new ArrayList();
    }

    // binary search for the right position of the token
    String  seq           = prop.getValues()[0];
    int[]   nearestMatch  = { -1 };
    int     res           = searchIndex(seq.charAt(0), nearestMatch);

    // where to insert: < 0 means new sequence is greater than neighbouring 
    // existing one
    int idx = nearestMatch[0];

    if (res < 0) {
      idx++;
    }

    // new entry or replace existing one
    if (res != 0) {
      if (_array.size() > idx) {
        _array.add(idx, new SpecialSequence(prop));
      } else {        
        _array.add(new SpecialSequence(prop));
      }
    } else {
      SpecialSequence   listElem = (SpecialSequence)_array.get(idx);
      TokenizerProperty existing;
      String            exSeq;

      while (listElem != null) {
        existing = listElem._property;
        exSeq    = existing.getValues()[0];

        if (seq.length() > exSeq.length()) {
          listElem._next     = new SpecialSequence(existing, listElem._next);
          listElem._property = prop;
          break;
        } else if (seq.length() == exSeq.length()) {
          if ((_flags & Tokenizer.F_NO_CASE) != 0) {
            res = exSeq.compareToIgnoreCase(seq);
          } else {
            res = exSeq.compareTo(seq); 
          }
          if (res == 0) {
            listElem._property = prop;
          } else if (res < 0) {
            listElem._next     = new SpecialSequence(existing, listElem._next);
            listElem._property = prop;
          } else if (listElem._next == null) {
            listElem._next     = new SpecialSequence(prop);
          }
          break;
        } else if (listElem._next == null) {
          listElem._next = new SpecialSequence(prop);
          break;
        }
        listElem = listElem._next;
      }
    }
  }

  // Members
  private AbstractTokenizer _parent = null;
  private ArrayList         _array  = null;
  int                       _flags  = 0;
}


/**
 * List for equaly starting special sequences
 */
final class SpecialSequence {
  SpecialSequence(TokenizerProperty prop) {
    this(prop, null);
  }

  SpecialSequence(TokenizerProperty prop, SpecialSequence next) {
    _property = prop;
    _next     = next;
  }

  public SpecialSequence    _next;
  public TokenizerProperty  _property;
}
  
  
/**
 * Instances of this inner class are returned when a call to {@link getKeywords}.
 * Each element of the enumeration contains a {@link TokenizerProperty} element,
 * that in turn has the keyword with its companion
 */
final class KeywordIterator implements Iterator {

  /**
   * constructor taking the calling <code>Tokenizer</code> and the type of the
   * {@link TokenizerProperty}.
   *
   * @param parent  the calling tokenizer
   * @param type    type of the <code>TokenizerProperty</code> 
   */
  public KeywordIterator(AbstractTokenizer parent) {
    if (parent._keywords[0] != null) {
      _iterators[0] = parent._keywords[0].values().iterator();
    }
    if (parent._keywords[1] != null) {
      _iterators[1] = parent._keywords[1].values().iterator();
    }
  }

  /**
   * the well known method from the {@link java.util.Iterator} interface.
   *
   * @return <code>true</code> if there are more {@link TokenizerProperty}
   *         elements, <code>false</code> otherwise
   */
  public boolean hasNext() {
    // check the current array
    if (_iterators[0] != null) {
      if (_iterators[0].hasNext()) {
        return true;
      } else {
        _iterators[0] = null;
      }
    }
    if (_iterators[1] != null) {
      if (_iterators[1].hasNext()) {
        return true;
      } else {
        _iterators[1] = null;
      }
    }
    return false;
  }

  /**
   * Retrieve the next {@link TokenizerProperty} in this enumeration. 
   *
   * @return  the next keyword as a <code>TokenizerProperty</code>
   */
  public Object next() {
    if (hasNext()) {
      if (_iterators[0] != null) {
        return _iterators[0].next();
      } else {
        return _iterators[1].next();
      }
    }
    return null;
  }
  
  /**
   * This method is similar to {@link Tokenizer#removeKeyword}
   */
  public void remove() {
    if (_iterators[0] != null) {
      _iterators[0].remove();
    } else {
      _iterators[1].remove();
    }
  }

  // members
  private Iterator[] _iterators = new Iterator[2];
}



/**
 * Iterator for comments.
 * Instances of this inner class are returned when a call to one of the methods
 *<ul><li>
 *    {@link #getBlockComments}
 *</li><li>
 *    {@link #getLineComments}
 *</li><li>
 *    {@link #getStrings}
 *</li><li>
 *    {@link #getSpecialSequences}
 *</li></ul>
 * is done. Each element of the enumeration contains a {@link TokenizerProperty}
 * element, that in turn has the comment, special sequence etc. together with
 * its companion
 */
final class SpecialSequencesIterator implements Iterator {

  /**
   * constructor taking the calling <code>Tokenizer</code> and the type of the
   * {@link TokenizerProperty}.
   *
   * @param parent  the calling tokenizer
   * @param type    type of the <code>TokenizerProperty</code> 
   */
  public SpecialSequencesIterator(AbstractTokenizer parent, int type) {
    _type      = type;
    _arrays[0] = parent._sequences[0];
    _arrays[1] = parent._sequences[1];
  }

  /**
   * Checking for the next element in a special sequence list, that has the
   * required type. This method is the one that ultimately decides if there are
   * more elements or not.
   *
   * @return <code>true</code> if there is a matching {@link TokenizerProperty}
   *         element, <code>false</code> otherwise
   */
  private boolean listHasNext() {
    while (_currentElem != null) {
      if (_currentElem._property.getType() == _type) {
        return true;
      }
      _currentElem = _currentElem._next;
    }
    return false;
  }

  /**
   * the well known method from the {@link java.util.Iterator} interface.
   *
   * @return <code>true</code> if there are more {@link TokenizerProperty}
   *         elements, <code>false</code> otherwise
   */
  public boolean hasNext() {
    // simple: check the current list for a successor
    if (listHasNext()) {
      return true;
    }

    // which is the current array ?
    SortedArray array = null;

    if (_arrays[0] != null) {
      array = _arrays[0];
    } else {
      array = _arrays[1];
    }

    // check the current array 
    if (array != null) {
      int size = array.size();        

      while (++_currentIndex < size) {
        _currentElem = array.get(_currentIndex);
        if (listHasNext()) {
          return true;
        }
      }

      // possible to switch to the no-case array ?
      if (array == _arrays[0]) {
        _arrays[0]    = null;
        _currentElem  = null;
        _currentIndex = -1;
        return hasNext();
      }
    }

    // no (more) sequences
    return false;
  }

  /**
   * Retrieve the next {@link TokenizerProperty} in this enumeration.
   *
   * @return a {@link TokenizerProperty} of the desired type or <code>null</code>
   */
  public Object next() {
    if (! hasNext()) {
      return null;
    } else {
      TokenizerProperty prop = _currentElem._property;
      _currentElem = _currentElem._next;
      return prop;
    }
  }
  
  /**
   * Remove the current special sequence entry from the collection. This is an
   * alternative to {@link Tokenizer#removeSpecialSequence}.
   */
  public void remove() {
    // which is the current array ?
    SortedArray array = null;

    if (_arrays[0] != null) {
      array = _arrays[0];
    } else {
      array = _arrays[1];
    }

    try {
      TokenizerProperty prop = _currentElem._property;
      
      _currentElem = _currentElem._next;
      array.searchBinary(prop.getValues()[0], 0, true);
    } catch (Exception ex) {
      throw new ExtRuntimeException(ex, "While trying to remove current element of a SpecialSequencesIterator.");
    }
  }


  // members
  private SortedArray[]   _arrays       = new SortedArray[2];
  private SpecialSequence _currentElem  = null;
  private int             _currentIndex = -1;
  private int             _type         = Token.UNKNOWN;
}
