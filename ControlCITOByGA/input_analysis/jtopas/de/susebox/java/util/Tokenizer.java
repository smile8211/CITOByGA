/*
 * Tokenizer.java: lexical parser interface.
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
import java.util.Iterator;


//-----------------------------------------------------------------------------
// Interface Tokenizer
//

/**<p>
 * The interface <CODE>Tokenizer</CODE> contains parse operations, control flags 
 * and comment, keyword and special sequence support. It is designed to 
 * enable a common approach for parsing texts like program code, annotated 
 * documents like HTML and so on.
 *</p><p>
 * To detect links in an HTML document, a tokenizer would be invoked like that:
 *<blockquote><pre>
 *
 * Vector    links     = new Vector();
 * Tokenizer tokenizer = new MyTokenizer();
 * Token     token;
 *
 * tokenizer.setParseFlags(Tokenizer.F_NO_CASE);
 * tokenizer.setSeparators("=");
 * tokenizer.addString("\"", "\"", "\\");
 * tokenizer.addBlockComment("&gt;", "&lt;");
 * tokenizer.addKeyword("HREF");
 *
 * while (tokenizer.hasMoreToken()) {
 *   token = tokenizer.nextToken();
 *   if (token.getType() == Token.KEYWORD) {
 *     tokenizer.nextToken();               // should be the '=' character
 *     links.addElement(tokenizer.next());
 *   }
 * }
 *
 *</pre></blockquote>
 * This is somewhat rough way to find links should work fine on syntactically
 * correct HTML code. It finds common links as well as mail, ftp links etc. Note
 * the block comment. It starts with the "&gt;" character, that is the closing
 * character for HTML tags and ends with the "&lt;" being the starting character
 * of HTML tags. The effect is that all the real text is treated as a comment.
 *</p><p>
 * To extract the contents of a HTML file, one would do:
 *<blockquote><pre>
 *
 * StringBuffer  contents  = new StringBuffer(4096);
 * Tokenizer     tokenizer = new MyTokenizer();
 * Token         token;
 *
 * tokenizer.setParseFlags(Tokenizer.F_NO_CASE);
 * tokenizer.addBlockComment("&gt;", "&lt;");
 * tokenizer.addBlockComment("&gt;HEAD&lt;", "&gt;/HEAD&lt;");
 * tokenizer.addBlockComment("&gt;!--;", "--&lt;");
 *    
 * while (tokenizer.hasMoreToken()) {
 *   token = tokenizer.nextToken();
 *   if (token.getType() != Token.BLOCK_COMMENT) {
 *     contents.append(token.getToken());
 *   }
 * }
 *
 *</pre></blockquote>
 * Here the block comment is the exact opposite of the first example. Now all the
 * HTML tags are skipped. Moreover, we declared the HTML-Header as a block
 * comment as well - the informations from the header are thus skipped alltogether.
 *</p><p>
 * Parsing (tokenizing) is done on a well defined priority scheme. See 
 * {@link #nextToken} for details.
 *</p><p>
 * NOTE: if a character sequence is registered for two categories of tokenizer
 * properties (e.g. as a line comments starting sequence as well as a special
 * sequence), the category with the highest priority wins (e.g. if the metioned
 * sequence is found, it is interpreted as a line comment). 
 *</p><p>
 * The tokenizer interface is clearly designed for "readable" data, say ASCII-
 * or UNICODE data. Parsing binary data has other characteristics that do not
 * nessecarily fit in a scheme of comments, keywords, strings, identifiers and 
 * operators.
 *</p>
 *
 * @see     Token
 * @see     TokenizerProperty
 * @see     AbstractTokenizer
 * @see     InputStreamTokenizer
 * @author  Heiko Blau
 */
public interface Tokenizer
{
  //---------------------------------------------------------------------------
  // default character classes
  //
  
  /** 
   * Whitespaces are portions of the text, that contain one or more characters 
   * that separate the significant parts of the text. Generally, a sequence of 
   * whitespaces is equally represented by one single whitespace character. That 
   * is the difference to separators.
   */  
  public static final String DEFAULT_WHITESPACES = " \t\r\n";
  
  /** 
   * Separators are otherwise not remarkable characters. An opening parenthesis 
   * might be nessecary for a syntactically correct text, but without any special 
   * meaning to the compiler, interpreter etc. after it has been detected.
   */  
  public static final String DEFAULT_SEPARATORS = "\u0021\u0023-\u002f\u003a-\u0040\u005b-\u005e\u0060\u007b-\u007e";
  
  /**
   * Default starting sequence of a block comment (Java, C/C++).
   */  
  public static final String DEFAULT_BLOCK_COMMENT_START = "/*";

  /**
   * Default end sequence of a block comment (Java, C/C++).
   */  
  public static final String DEFAULT_BLOCK_COMMENT_END = "*/";
  
  /**
   * Default line comment seqence (Java, C++)
   */  
  public static final String DEFAULT_LINE_COMMENT = "//";
  
  /**
   * The well-known string starting sequence of C/C++, Java and other languages.
   */  
  public static final String DEFAULT_STRING_START  = "\"";
  
  /**
   * The well-known string ending sequence of C/C++, Java and other languages.
   */  
  public static final String DEFAULT_STRING_END    = DEFAULT_STRING_START;
  
  /**
   * The well-known escape sequence for strings in C/C++, Java and other languages.
   */  
  public static final String DEFAULT_STRING_ESCAPE = "\\";
  
  /**
   * The well-known character starting sequence of C/C++, Java and other languages.
   */  
  public static final String DEFAULT_CHAR_START  = "'";
  
  /**
   * The well-known character ending sequence of C/C++, Java and other languages.
   */  
  public static final String DEFAULT_CHAR_END    = DEFAULT_CHAR_START;

  /**
   * The well-known escape sequence for character literals in C/C++, Java and other
   * languages.
   */  
  public static final String DEFAULT_CHAR_ESCAPE = DEFAULT_STRING_ESCAPE;
  
  
  //---------------------------------------------------------------------------
  // common control flags
  //
  
  /**
   * General compare operations are case-sensitive, that means 'A' equals 'A' 
   * but not 'a'.<br>
   * Without this flag, comparison is done like in Java or C/C++. When the flag 
   * is set, the tokenizer compares tokens like in HTML or PL/SQL.
   */
  public static final int F_NO_CASE               = 0x0001;

  /**
   * In case that the <CODE>F_NO_CASE</CODE> flag is set, this flag is used to
   * alter the behaviour of the tokenizer for keyword comparison.
   * The combination between <CODE>F_NO_CASE</CODE> and <CODE>F_KEYWORDS_CASE</CODE>
   * means, that keywords are case-sensitive but everything else is not.
   */
  public static final int F_KEYWORDS_CASE         = 0x0002;
  
  /**
   * In case that the <CODE>F_NO_CASE</CODE> flag is not set, this flag is used 
   * to alter the behaviour of the tokenizer for keyword comparison.
   * The flag <CODE>F_KEYWORDS_NO_CASE</CODE> means, that keywords are not 
   * case-sensitive but everything else is.
   */
  public static final int F_KEYWORDS_NO_CASE      = 0x0004;
  
  /**
   * In many cases, parsers are not interested in whitespaces. If You are, use
   * this flag to force the tokenizer to return whitespace sequences as a token.
   */
  public static final int F_RETURN_WHITESPACES    = 0x0008;
  
  /**
   * For perfomance and memory reasons, this flag is used to avoid copy operations
   * for every token. The token image itself is not returned in a {@link Token}
   * instance, only its position and length in the input stream.
   */
  public static final int F_TOKEN_POS_ONLY        = 0x0010;

  /**
   * Set this flag to let the tokenizer buffer all data. Normally, a tokenizer
   * keeps only a certain amount of periodically changing data in its internal 
   * buffer.
   */
  public static final int F_KEEP_DATA             = 0x0020;

  /**
   * Tells the tokenizer to count lines and columns. The tokenizer may use
   * System.getProperty("line.separator") to obtain the end-of-line sequence.
   */
  public static final int F_COUNT_LINES           = 0x0040;

  /**
   * Nested block comments are normally not allowed. This flag changes the 
   * default behaviour
   */
  public static final int F_ALLOW_NESTED_COMMENTS = 0x0080;
  
  /**
   * With this flag, the tokenizer tries to identify numbers.
   */
  public static final int F_PARSE_NUMBERS         = 0x0100;


  //---------------------------------------------------------------------------
  // trivial property methods
  //
  
  /**
   * Setting the control flags of the <code>Tokenizer</code>. Use a combination
   * of the <code>F_...</code> flags for the parameter.
   * @param flags the parser control flags
   */
  public void setParseFlags(int flags);

   /**
    * Retrieving the parser control flags. A bitmask containing the <code>F_...</code>
    * constants is returned.
    * @return the current parser control flags
    * @see #setParseFlags
    */
  public int getParseFlags();
  
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
  public void addString(String start, String end, String escape);

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
  public void addString(String start, String end, String escape, Object companion);
  
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
  );
  
  /**
   * Removing a string description.
   *
   * @param start     the starting sequence of a string
   */  
  public void removeString(String start);
  
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
  public Object getStringCompanion(String start);
  
  /**
   * Checks if the given starting sequence of the string is known to the parser.
   *
   * @param start     the starting sequence of a string
   * @return <CODE>true</CODE> if the string is registered, 
   *         <CODE>false</CODE> otherwise
   */
  public boolean stringExists(String start);

  /**
   * This method returns an {@link java.util.Iterator} of {@link TokenizerProperty}
   * objects. Each <CODE>TokenizerProperty</CODE> object contains the starting,
   * finishing and escaping sequence of a string description and the companion if 
   * it exists.
   *
   * @return enumeration of {@link TokenizerProperty} objects
   */  
  public Iterator getStrings();
  
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
  public void setWhitespaces(String whitespaces);
  
  /**
   * Obtaining the whitespace character set. The set may contain ranges.
   *
   * @see #setWhitespaces
   * @return the currently active whitespace set
   */
  public String getWhitespaces();
  
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
  public void setSeparators(String separators);
  
  /**
   * Obtaining the separator set of the <code>Tokenizer</code>. The set may
   * contain ranges.
   *
   * @see #setSeparators
   * @return the currently used set of separating characters
   */
  public String getSeparators();
  
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
  public void addLineComment(String lineComment);

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
  public void addLineComment(String lineComment, Object companion);

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
  public void addLineComment(String lineComment, Object companion, int flags);
  
  /**
   * Removing a certain line comment.
   *
   * @param lineComment the starting sequence of the line comment
   */  
  public void removeLineComment(String lineComment);
  
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
  public Object getLineCommentCompanion(String lineComment);

  /**
   * Checks if the give line comment is known.
   *
   * @param lineComment the starting sequence of the line comment
   * @return <CODE>true</CODE> if the line comment is known, 
   *         <CODE>false</CODE> otherwise
   */  
  public boolean lineCommentExists(String lineComment);
  
  /**
   * This method returns an {@link java.util.Iterator} of {@link TokenizerProperty}
   * objects. Each <CODE>TokenizerProperty</CODE> object contains one starting
   * sequence of a line comment and the companion if it exists.
   *
   * @return enumeration of {@link TokenizerProperty} objects
   */  
  public Iterator getLineComments();
  
  /**
   * Registering a block comment with the parser. This version takes only the starting
   * and finishing sequence of the block comment.<br>
   * If the given starting sequence is already known to the parser, the block 
   * comment is simply re-registered. Using this method on a known block comment
   * with an associated companion will remove that companion.
   *
   * @param start the starting sequence of the block comment
   * @param end   the finishing sequence of the block comment
   */  
  public void addBlockComment(String start, String end);
  
  /**
   * Registering a block comment with the parser. Beside the obviously nessecary
   * starting and finishing sequence of the block comment, it takes an object that
   * is associated with the block comment, called the companion.<br>
   * If the given starting sequence is already known to the parser, the block
   * comment is simply re-registered. Using this method on a known block comment
   * with an associated companion will replace that companion against the given
   * one.
   *
   * @param start     the starting sequence of the block comment
   * @param end       the finishing sequence of the block comment
   * @param companion information object associated with this block comment
   */  
  public void addBlockComment(String start, String end, Object companion);
  
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
  public void addBlockComment(String start, String end, Object companion, int flags);
  
  /**
   * Removing a certain block comment. Only the starting sequence is nessecary
   * to identify the block comment.
   *
   * @param start the starting sequence of the block comment
   */  
  public void removeBlockComment(String start);
  
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
  public Object getBlockCommentCompanion(String start);
  
  /**
   * Checks if the give block comment is known. Only the starting sequence is 
   * nessecary to identify the block comment.
   *
   * @param start the starting sequence of the block comment
   * @return <CODE>true</CODE> if the block comment is known, 
   *         <CODE>false</CODE> otherwise
   */  
  public boolean blockCommentExists(String start);
  
  /**
   * This method returns an {@link java.util.Iterator} of {@link TokenizerProperty}
   * objects. Each <CODE>TokenizerProperty</CODE> object contains the starting and
   * finishing sequence of a block comment and the companion if it exists.
   *
   * @return enumeration of {@link TokenizerProperty} objects
   */  
  public Iterator getBlockComments();
  
  /**
   * Query the current row. The method can only be used if the flag <CODE>F_COUNTLINES</CODE>
   * has been set.
   * Without this flag being set, the return value is undefined.
   * Note that row counting starts with 0, while editors often use 1 for the first
   * row.
   *
   * @return current row (starting with 0)
   */
  public int getCurrentLine();
  
  /**
   * Retrieve the current column. The method can only be used if the flag <CODE>F_COUNTLINES</CODE>
   * has been set.
   * Without this flag being set, the return value is undefined.
   * Note that column counting starts with 0, while editors often use 1 for the first
   * column in one row.
   *
   * @return current column number (starting with 0)
   */
  public int getCurrentColumn();
  

  //---------------------------------------------------------------------------
  // properties for sophisticated parser operations
  //
  
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
  public void addSpecialSequence(String specSeq);
  
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
   * @param companion   information object associated with this special sequence
   * @see #addKeyword
   * @see #setSeparators
   */  
  public void addSpecialSequence(String specSeq, Object companion);
  
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
   * the special sequence, called the companion.<br>
   * This version of <code>addSpecialSequence</code> supports a bitmask of the 
   * <code>F_...</code> flags to modify the general tokenizer settings (see
   * {@link #setParseFlags}) for this special element.
   *
   * @param specSeq     special sequence to register
   * @param companion   information object associated with this special sequence
   * @param flags       modification flags 
   * @see #addKeyword
   * @see #setSeparators
   */
  public void addSpecialSequence(String specSeq, Object companion, int flags);
  
  
  /**
   * Deregistering a special sequence from the parser.
   *
   * @param specSeq   sequence to remove
   */  
  public void removeSpecialSequence(String specSeq);
  
  /**
   * Retrieving the companion of the given special sequence. If the special
   * sequence doesn't exist the method returns <CODE>null</CODE>.
   *
   * @param specSeq   sequence to remove
   * @return the object associated with the special sequence
   */
  public Object getSpecialSequenceCompanion(String specSeq);

  /**
   * This method returns an {@link java.util.Iterator} of {@link TokenizerProperty}
   * objects. Each <CODE>TokenizerProperty</CODE> object contains a special
   * sequence and the companion if it exists.
   *
   * @return enumeration of {@link TokenizerProperty} objects
   */  
  public Iterator getSpecialSequences();
  
  /**
   * Checks if the given special sequence is known to the <CODE>Tokenizer</CODE>.
   *
   * @param specSeq sequence to check
   * @return <CODE>true</CODE> if the block comment is known,
   *       <CODE>false</CODE> otherwise
   */  
  public boolean specialSequenceExists(String specSeq);
  
  /**
   * Registering a keyword. If the keyword is already known to the <CODE>Tokenizer</CODE>
   * then it is simply re-registered. If the known keyword has an associated 
   * companion it will be removed.
   *
   * @param keyword   keyword to register
   */
  public void addKeyword(String keyword);
  
  /**
   * Registering a keyword. If the keyword is already known to the <CODE>Tokenizer</CODE>
   * then it is simply re-registered. If the known keyword has an associated
   * companion it will be replaced against the given one.
   *
   * @param keyword   keyword to register
   * @param companion information object associated with this keyword
   */  
  public void addKeyword(String keyword, Object companion);
  
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
   * @param flags       modification flags 
   */  
  public void addKeyword(String keyword, Object companion, int flags);
  
  /**
   * Deregistering a keyword from the parser. If the keyword is not known
   * then the method does nothing.
   *
   * @param keyword   keyword to remove
   */  
  public void removeKeyword(String keyword);
  
  /**
   * Retrieving the companion of the given special sequence. If the special
   * sequence doesn't exist the method returns <CODE>null</CODE>.
   *
   * @param keyword   keyword thats companion is sought
   * @return the object associated with the keyword
   */
  public Object getKeywordCompanion(String keyword);

  /**
   * This method returns an {@link java.util.Iterator} of {@link TokenizerProperty}
   * objects. Each <CODE>TokenizerProperty</CODE> object contains a keyword and 
   * the companion if it exists.
   *
   * @return enumeration of {@link TokenizerProperty} objects
   */  
  public Iterator getKeywords();
  
  /**
   * Checks if the given keyword is known to the <CODE>Tokenizer</CODE>.
   *
   * @param keyword   keyword to search
   * @return <CODE>true</CODE> if the keyword is known,
   *        <CODE>false</CODE> otherwise
   */  
  public boolean keywordExists(String keyword);
  
  
  //---------------------------------------------------------------------------
  // parser operations
  //

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
  public boolean hasMoreToken();
  
  
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
  public Token nextToken() throws TokenizerException;
 
  /**
   * This method is a convenience method. It returns only the next token image
   * without any informations about its type or associated information.
   *
   * @return the token image of the next token
   * @throws TokenizerException generic exception (list) for all problems that may occur while parsing
   * (IOExceptions for instance)
   */
  public String next() throws TokenizerException;
 
  /**
   * Retrieve the {@link Token} that was found by the last call to {@link #nextToken}.
   * @return the token that was found by the last call to <CODE>nextToken</CODE>
   * or <CODE>next</CODE>
   */
  public Token currentToken();
 
  /**
   * Convenience method to retrieve only the token image of the {@link Token} that
   * would be returned by {@link #currentToken}.
   *
   * @return the token image of the current token
   * @see #currentToken
   */
  public String current();

  
  //---------------------------------------------------------------------------
  // line and column positions
  //
  
  /**
   * If the flag {@link Tokenizer#F_COUNT_LINES} is set, this method will return the
   * line number starting with 0 in the input stream. The implementation of the
   * <CODE>Tokenizer</CODE> interface can decide which end-of-line sequences should
   * be recognized. The most flexible approach is to process the following 
   * end-of-line sequences:
   * <br><ul><li>
   * Carriage Return (ASCII 13, '\r'). This EOL is used on Apple Macintosh
   * </li><li>
   * Linefeed (ASCII 10, '\n'). This is the UNIX EOL character.
   * </li><li>
   * Carriage Return + Linefeed ("\r\n"). This is used on MS Windows systems.
   * </li></ul>
   * Another legitime and in many cases satisfying way is to use the system 
   * property "line.separator".
   *
   * @return the current line number starting with 0 or -1 if no line numbers are supplied.
   * @see #getColumnNumber
   */  
  public int getLineNumber();
  
  /**
   * If the flag {@link Tokenizer#F_COUNT_LINES} is set, this method will return the
   * current column positionstarting with 0 in the input stream.
   *
   * @return the current column position
   * @see #getLineNumber
   */  
  public int getColumnNumber();
  
  //---------------------------------------------------------------------------
  // text range operations
  //
  
  /**
   * This method returns the absolute offset in characters to the start of the
   * parsed stream. Together with {@link #currentlyAvailable} it describes the
   * currently available text "window".<br>
   * The position returned by this method and also by {@link getReadPosition}
   * are absolute rather than relative in a text buffer to give the tokenizer
   * the full control of how and when to refill its text buffer.
   *
   * @return the absolute offset of the current text window in characters from 
   *         the start of the data source of the Tokenizer
   */
  public int getRangeStart();
  
  /**
   * Getting the current read offset. This is the absolute position where the
   * next call to <CODE>nextToken</CODE> or <CODE>next</CODE> will start. It is
   * therefore <b><k>not</k></b> the same as the position returned by 
   * {@link Token#getStartPosition} of the current token ({@link #currentToken}). 
   *<br>
   * The position returned by this method and also by {@link getRangeStart}
   * are absolute rather than relative in a text buffer to give the tokenizer
   * the full control of how and when to refill its text buffer.
   *
   * @return the absolute offset in characters from the start of the data source 
   *         of the Tokenizer where reading will be continued
   */
  public int getReadPosition();
  
  /**
   * Retrieving the number of the currently available characters. This includes
   * both characters already parsed by the <CODE>Tokenizer</CODE> and characters
   * still to be analyzed.<br>
   *
   * @return number of currently available characters
   */
  public int currentlyAvailable();
  
  /**
   * Retrieve text from the currently available range. The start and length
   * parameters must be inside {@link getRangeStart} and
   * {@link getRangeStart} + {@link currentlyAvailable}.
   *
   * @param start position where the text begins
   * @param length length of the text
   * @return the text beginning at the given position ith the given length
   * @throws IndexOutOfBoundsException if the starting position or the length is out of the current
   * text window
   */
  public String getText(int start, int length) throws IndexOutOfBoundsException;
  
  /**
   * Get a single character from the current text range.
   *
   * @param pos position of the required character
   * @return the character at the specified position
   * @throws IndexOutOfBoundsException if the parameter <CODE>pos</CODE> is not 
   *         in the available text range (text window)
   */
  public char getChar(int pos) throws IndexOutOfBoundsException;
  
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
  public int readMore() throws TokenizerException;
  
  /**
   * This method tells the tokenizer to skip the given number of characters
   * starting on the current read position as can be retrieved by {@link getReadPosition}.
   * The given number of characters must be less or equal to 
   * {@link currentlyAvailable} - ({@link getReadPosition} - {@link getRangeStart}).
   *
   * @param numberOfChars   Number of characters to skip
   */
  public void skip(int numberOfChars) throws IndexOutOfBoundsException;
}
