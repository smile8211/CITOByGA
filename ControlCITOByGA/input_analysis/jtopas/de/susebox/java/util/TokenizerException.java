/*
 * ExceptionList.java: Interface for exception stacks
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

//------------------------------------------------------------------------------
// Imports
//

import java.text.MessageFormat;
import de.susebox.java.lang.ExceptionList;


//------------------------------------------------------------------------------
// TokenizerException - definition
//

/**<p>
 * Wrapper exception for all the problems that may occur while parsing. There
 * are IOExceptions, SQLExceptions etc. that can all happen when a {@link Tokenizer}
 * tries to extract the next token.
 *</p><p>
 * The class supports formats and format arguments beside the usual plain 
 * exception message string.
 *</p>
 *
 * @version	1.00, 2001/07/10
 * @author 	Heiko Blau
 */
public class TokenizerException 
  extends     Exception 
  implements  ExceptionList 
{
  //---------------------------------------------------------------------------
  // methods of the ExceptionList interface
  //
  
  /**
   * Method to traverse the exception list. By convention, <CODE>nextException</CODE>
   * returns the "earlier" exception. By walking down the exception list one gets the
   * the following meaning:<br>
   * this happened because nextException happened because nextException happened...
   *
   * @return the "earlier" exception
   */  
  public Exception nextException() {
		return _next;
	}

  /**
   * Check if <CODE>this</CODE> is only a exception that wraps the real one. This
   * might be nessecary to pass an exception incompatible to a method declaration.
   *
   * @return <CODE>true</CODE> if this is a wrapper exception,
   *         <CODE>false</CODE> otherwise
   */  
	public boolean isWrapperException() {
		return _isWrapper;
	}

  
  //---------------------------------------------------------------------------
  // constructors
  //
  
  /**
   * This constructor should be used for wrapping another exception. While reading
   * data an IOException may occur, but the {@link Tokenizer} interface requires a
   * <code>TokenizerException</code>. Simply use:
   *<blockquote><pre>
   * try {
   *   ...
   * } catch (IOException ex) {
   *   throw new TokenizerException(ex);
   * }
   *</pre></blockquote>
   *
   * @param ex the exception to wrap
   */  
	public TokenizerException(Exception ex) {
		this(ex, null, null);
	}

  /**
   * If one likes to add ones own information to an exception, this constructor is
   * the easiest way to do so. By using such an approach a exception trace with useful
   * additional informations (which file could be found, what username is unknown)
   * can be realized:
   *<blockquote><pre>
   * try {
   *   ...
   * } catch (IOException ex) {
   *   throw new TokenizerException(ex, "while tokenizing " + path);
   * }
   *</pre></blockquote>
   *
   * @param ex    the inner exception
   * @param msg   exception message
   */  
	public TokenizerException(Exception ex, String msg) {
		this(ex, msg, null);
	}

  /**
   * This constructor takes a format string and its arguments. The format string
   * must have a form that can be used by {@link java.text.MessageFormat} methods.
   * That means:
   *<blockquote><pre>
   *    java.text.Message.format(fmt, args)
   *</pre></blockquote>
   * is similar to
   *<blockquote><pre>
   *    new TokenizerException(fmt, args).getMessage();
   *</pre></blockquote>
   *
   * @param fmt   exception message
   * @param args  arguments for the given format string
   */  
	public TokenizerException(String fmt, Object[] args) {
    this(null, fmt, args);
	}

  /**
   * This is the most complex way to construct a <CODE>TokenizerException</CODE>.
   * An inner exception is accompanied by a format string and its arguments.
   * Use this constructor in language-sensitive contexts or for formalized messages.
   * The meaning of the parameters is explained in the other constructors.
   *
   * @param ex    the inner exception
   * @param fmt   exception message
   * @param args  arguments for the given format string
   */  
	public TokenizerException(Exception ex, String fmt, Object[] args) {
		super(fmt);
    
    if (ex != null && fmt == null) {
      _isWrapper = true;
    } else {
      _isWrapper = false;
    }
    _next = ex;
	}


  //---------------------------------------------------------------------------
  // overridden methods
  //
  
  /**
   * Implementation of the standard {@link java.Throwable#getMessage} method to 
   * meet the requirements of formats and format arguments as well as wrapper
   * exceptions.
   * If this is a wrapper exception then the <CODE>getMessage</CODE> of the wrapped 
   * exception is returned.
   * If no arguments were given in the constructor then the format parameter is
   * taken as the formatted message itself. Otherwise it is treated like the
   * patter for the {@link java.text.MessageFormat#format} method.
   *
   * @return  the formatted exception message
   * @see     java.text.MessageFormat
   */  
	public String getMessage() {
    if (isWrapperException()) {
      return nextException().getMessage();
    } else {
      String fmt = super.getMessage();

      if (_args == null) {
        return fmt;
      } else {
        return MessageFormat.format(fmt, _args);
      }
    }
	}

  
  
  //---------------------------------------------------------------------------
  // members
  //
  protected Object[]  _args       = null;
  protected Exception _next       = null;
  protected boolean   _isWrapper  = false;
}
