/*
 * Plugin.java: Plugin for the PluginTokenizer.
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
// Interface Plugin
//

/**<p>
 * This is the base interface for all handler pluggins in the {@link PluginTokenizer}.
 *</p>
 *
 * @see     de.susebox.java.util.Tokenizer
 * @see     de.susebox.java.util.AbstractTokenizer
 * @author  Heiko Blau
 */
public interface Plugin {
  
  /**
   * When registering an instance that implements this interface, the 
   * {@link PluginTokenizer} will call this method to make itself known to
   * the <code>SeparatorHandler</code> instance in turn.
   *
   * @param tokenizer   the controlling {@link PluginTokenizer}
   */
  public void setTokenizer(PluginTokenizer tokenizer);
}
