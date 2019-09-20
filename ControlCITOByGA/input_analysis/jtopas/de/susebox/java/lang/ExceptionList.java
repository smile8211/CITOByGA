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

package de.susebox.java.lang;

//------------------------------------------------------------------------------
// Imports
//

/*-->
import java.text.MessageFormat;
 
import de.susebox.java.lang.ExceptionList;
-->*/


//------------------------------------------------------------------------------
// Interface ExceptionList
//

/**<p>
 * This interface should be implemented by exception classes that may contain
 * a stacked, additional or wrapped exception.
 *</p><p>
 * Such cases are common when
 *<ul><li>
 *   a method implements a certain interface method that allows for a specific
 *   exception like IOException, but the method itself may encounter a different
 *   exception type like SQLException (wrapped exception)
 *</li><li>
 *   one application layer catches an exception only to add its specific
 *   information in form of another exception (exception stack, nested exception
 *   like in SQLException or MessagingException).
 *</li></ul>
 *</p><p>
 * We provide the expected code in block comments starting with --&gt;, terminated
 * by --&gt;. Note that the provided code also includes a new implementation of
 * the base class method {@link java.lang.Throwable#getMessage} using the text
 * formatting capabilities of {@link java.text.MessageFormat}.
 *
 * @version	1.00, 2001/06/26
 * @author 	Heiko Blau
 */
public interface ExceptionList {
  /**
   * Method to traverse the exception list. By convention, <CODE>nextException</CODE>
   * returns the "earlier" exception. By walking down the exception list one gets the
   * the following meaning:<br>
   * this happened because nextException happened because nextException happened...
   *
   * @return the "earlier" exception
   */
  Exception nextException();
  /*-->
  {
    return _next;
  }
  -->*/
  
  
  /**
   * Check if <CODE>this</CODE> is only a exception that wraps the real one. This
   * might be nessecary to pass an exception incompatible to a method declaration.
   *
   * @return <CODE>true</CODE> if this is a wrapper exception,
   *         <CODE>false</CODE> otherwise
   */
  boolean isWrapperException();
  /*-->
  {
    return _isWrapper;
  }
  -->*/
  
  
  //---------------------------------------------------------------------------
  // implementation code templates
  //
  
  /**
   * This constructor should be used for wrapping another exception. While reading
   * data an IOException may occur, but a certain interface requires a
   * <CODE>SQLException</CODE>. Simply use:
   *<blockquote><pre>
   *   try {
   *     ...
   *   } catch (IOException ex) {
   *     throw new MyException(ex);
   *   }
   *</pre></blockquote>
   *
   * @param ex the exception to wrap
   */
  /*-->
  public <<WHICH>>Exception(Exception ex) {
    this(ex, null, null);
  }
  -->*/
  
  /**
   * If one likes to add ones own information to an exception, this constructor is
   * the easiest way to do so. By using such an approach a exception trace with useful
   * additional informations (which file could be found, what username is unknown)
   * can be realized:
   *<br><br><CODE>
   * try {                                                                      <br>&nbsp;
   *   ...                                                                      <br>
   * } catch (SQLException ex) {                                                <br>&nbsp;
   *   throw new MyException(ex, "while connecting to " + url);                 <br>
   * }
   *<br></CODE>
   *
   * @param ex    the inner exception
   * @param msg   exception message
   */
  /*-->
  public <<WHICH>>Exception(Exception ex, String msg) {
    this(ex, msg, null);
  }
  -->*/
  
  /**
   * This constructor takes a format string and its arguments. The format string
   * must have a form that can be used by {@link java.text.MessageFormat} methods.
   * That means:
   *<br><CODE>
   *    java.text.Message.format(fmt, args)
   *<br><CODE>
   * is similar to
   *<br><CODE>
   *    new MyException(fmt, args).getMessage();
   *<CODE>
   *
   * @param fmt   exception message
   * @param args  arguments for the given format string
   */
  /*-->
  public <<WHICH>>Exception(String fmt, Object[] args) {
    this(null, msg, args);
  }
  -->*/
  
  /**
   * This is the most complex way to construct an <CODE>ExceptionList</CODE>-
   * Exception.<br>
   * An inner exception is accompanied by a format string and its arguments.
   * Use this constructor in language-sensitive contexts or for formalized messages.
   * The meaning of the parameters is explained in the other constructors.
   *
   * @param ex    the inner exception
   * @param fmt   exception message
   * @param args  arguments for the given format string
   */
  /*-->
  public <<WHICH>>Exception(Exception ex, String fmt, Object[] args) {
    super(fmt);
   
    if (ex != null && fmt == null) {
      _isWrapper = true;
    } else {
      _isWrapper = false;
    }
    _next = ex;
  }
  -->
   
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
  /*-->
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
  -->*/
  
  //---------------------------------------------------------------------------
  // members
  //
  /*-->
  protected Object[]  _args       = null;
  protected Exception _next       = null;
  protected boolean   _isWrapper  = false;
  -->*/
}
