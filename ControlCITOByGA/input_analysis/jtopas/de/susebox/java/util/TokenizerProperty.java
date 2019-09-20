/*
 * TokenizerProperty.java: Various characteristics of Tokenizer.
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


//-----------------------------------------------------------------------------
// Class TokenizerProperty
//

/**
 * This class is used by {@link Tokenizer} implementations to return enumerations
 * of their various properties (keywords, special sequences etc.).
 *
 * @see Token
 * @see Tokenizer
 * @author Heiko Blau
 */
public class TokenizerProperty {
  
  //---------------------------------------------------------------------------
  // getter- and setter methods
  //
  
  /**
   * Setting the type of the <CODE>TokenizerProperty</CODE>. One of the constants
   * defined in {@link Token} is expected.
   *
   * @param type type of the tokenizer property
   */  
  public void setType(int type) {
    _type = type;
  }
    
  /**
   * Retrieving the type of this property.
   *
   * @return type of the property
   * @see #setType
   */  
  public int getType() {
    return _type;
  }

  /**
   * Setting flags. These flags are not specified here. They are used by the
   * {@link AbstractTokenizer} to store the specific parse flags for a token.
   *
   * @param flags   a bitmask
   */  
  public void setFlags(int flags) {
    _flags = flags;
  }
    
  /**
   * Retrieving the flags of this property.
   *
   * @return flags of the property
   * @see #setFlags
   */  
  public int getFlags() {
    return _flags;
  }

  /**
   * Values are quite different. Starting sequences of line comments, keywords 
   * and special sequences are strings representing only themselfes. Whitespaces
   * and separators are represented as string consisting of the single whitespace
   * and separator characters and / or character ranges.<br>
   * A block comment is represented an array of two strings. The first is the 
   * starting sequence, the second the finishing sequence.<br>
   */
  public void setValues(String[] values) {
    _values = values;
  }
    
  public String[] getValues() {
    return _values;
  }
    
  /**
   * Some token may have associated informations for the user of the <CODE>Token</CODE>.
   * A popular thing would be the association of an integer constant to a special
   * sequence or keyword to be used in fast <CODE>switch</CODE> statetents.
   *
   * @param companion the associated information for this token
   */  
  public void setCompanion(Object companion) {
    _companion = companion;
  }
    
  /**
   * Obtaining the associated information of the token. Can be <CODE>null</CODE>. See
   * {@link #setCompanion} for details.
   *
   * @return the associated information of this token
   */  
  public Object getCompanion() {
    return _companion;
  }
  
 
  //---------------------------------------------------------------------------
  // construction
  //
  
  /**
   * Default constructor.
   */  
  public TokenizerProperty() {
    this(Token.UNKNOWN, null, null);
  }
  
  /**
   * Constructs a <CODE>TokenizerProperty</CODE> where only the type is known so far.
   * For the type, one of the constants defined in {@link Token} must be used.
   *
   * @param type the property type
   */  
  public TokenizerProperty(int type) {
    this(type, null, null);
  }
  
  /**
   * Constructs a <CODE>TokenizerProperty</CODE> with type and value. For the type,
   * one of the constants defined in {@link Token} must be used.
   * @param type the property type
   * @param value the property value
   */  
  public TokenizerProperty(int type, String[] values) {
    this(type, values, null);
  }
  
  /**
   * Constructs a <CODE>TokenProperty</CODE> object with a complete set of type, value,
   * second value and companion.
   *
   */  
  public TokenizerProperty(int type, String[] values, Object companion) {
    this(type, values, companion, 0);
  }
  
  /**
   * Constructs a <CODE>TokenProperty</CODE> object with a complete set of type, value,
   * second value and companion.
   *
   */  
  public TokenizerProperty(int type, String[] values, Object companion, int flags) {
    setType(type);
    setValues(values);
    setCompanion(companion);
    setFlags(flags);
  }
  
  
  //---------------------------------------------------------------------------
  // members
  //
  protected int       _type;
  protected int       _flags;
  protected String[]  _values;
  protected Object    _companion;
}
