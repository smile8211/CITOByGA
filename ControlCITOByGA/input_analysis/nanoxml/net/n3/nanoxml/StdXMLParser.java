/* StdXMLParser.java                                               NanoXML/Java
 *
 * $Revision: 1.6 $
 * $Date: 2001/03/28 18:47:24 $
 * $Name: PRERELEASE_2_0_20010329 $
 *
 * This file is part of NanoXML 2 for Java.
 * Copyright (C) 2001 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 */


package net.n3.nanoxml;


import java.io.IOException;
import java.io.CharArrayReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Properties;


/**
 * StdXMLParser is the core parser of NanoXML.
 *
 * @author Marc De Scheemaecker
 * @version $Name: PRERELEASE_2_0_20010329 $, $Revision: 1.6 $
 */
public class StdXMLParser
    implements IXMLParser
{

    /**
     * The builder which creates the logical structure of the XML data.
     */
    protected IXMLBuilder builder;
    
    
    /**
     * The reader from which the parser retrieves its data.
     */
    protected IXMLReader reader;
    
    
    /**
     * The validator that will process entity references and validate the XML
     * data.
     */
    protected IXMLValidator validator;
    
    
    /**
     * Creates a new parser.
     */
    public StdXMLParser()
    {
        this.builder = null;
        this.validator = null;
        this.reader = null;
    }
    
    
    /**
     * Sets the builder which creates the logical structure of the XML data.
     *
     * @param builder the non-null builder
     */
    public void setBuilder(IXMLBuilder builder)
    {
        this.builder = builder;
    }
    
    
    /**
     * Sets the validator that will process entity references and validate the
     * XML data.
     *
     * @param validator the non-null validator
     */
    public void setValidator(IXMLValidator validator)
    {
        this.validator = validator;
    }
    
    
    /**
     * Sets the reader from which the parser retrieves its data.
     *
     * @param reader the non-null reader
     */
    public void setReader(IXMLReader reader)
    {
        this.reader = reader;
    }
    
    
    /**
     * Parses the data and lets the builder create the logical data structure.
     *
     * @return the logical structure built by the builder
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    public Object parse()
        throws IOException
    {
        this.builder.startBuilding(this.reader.getLineNr());
        this.scanData();
        return this.builder.getResult();
    }
    
    
    /**
     * Scans the XML data for elements.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void scanData()
        throws IOException
    {
        while ((! this.reader.atEOF()) && (this.builder.getResult() == null)) {
            char ch = this.read(null);
            
            switch (ch) {
                case '<':
                    this.scanSomeTag(false /*don't allow CDATA*/);
                    break;
                    
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    // skip whitespace
                    break;
                    
                default:
                    this.errorInvalidInput("`" + ch + "'");
            }
        }
    }
    
    
    /**
     * Scans an XML tag.
     *
     * @param allowCDATA true if CDATA sections are allowed at this point
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void scanSomeTag(boolean allowCDATA)
        throws IOException
    {
        char ch = this.read(null);
        
        switch (ch) {
            case '?':
                this.processPI();
                break;
                
            case '!':
                this.processSpecialTag(allowCDATA);
                break;
                
            default:
                this.reader.unread(ch);
                this.processElement();
        }
    }
    
    
    /**
     * Skips the remainder of a comment.
     * It is assumed that &lt;!- is already read.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void skipComment()
        throws IOException
    {
        if (this.read(null) != '-') {
            this.skipTag();
            return;
        }
        
        int dashesRead = 0;
        
        for (;;) {
            char ch = this.read(null);
            
            switch (ch) {
                case '-':
                    dashesRead++;
                    break;
                    
                case '>':
                    if (dashesRead == 2) {
                        return;
                    }
                    
                default:
                    dashesRead = 0;
            }
        }
    }
    
    
    /**
     * Skips the remainder of the current XML tag.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void skipTag()
        throws IOException
    {
        int level = 1;
        
        while (level > 0) {
            char ch = this.read(null);
            
            switch (ch) {
                case '<':
                    ++level;
                    break;
                    
                case '>':
                    --level;
                    break;
            }
        }
    }
    
    
    /**
     * Processes a "processing instruction".
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processPI()
        throws IOException
    {
        this.skipWhitespace(null, null);
        String target = this.scanIdentifier();
        this.skipWhitespace(null, null);
        Reader reader = new ContentReader(">?", true, "");
        
        if (! target.equalsIgnoreCase("xml")) {
            this.builder.newProcessingInstruction(target, reader);
        }
        
        reader.close();
    }
    
    
    /**
     * Processes a tag that starts with a bang (<!...>).
     *
     * @param allowCDATA true if CDATA sections are allowed at this point
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processSpecialTag(boolean allowCDATA)
        throws IOException
    {
        char ch = this.read(null);
        
        switch (ch) {
            case '[':
                if (allowCDATA) {
                    this.processCDATA();
                } else {
                    this.skipTag();
                }
                
                return;
                
            case 'D':
                this.processDocType();
                return;
                
            case '-':
                this.skipComment();
                return;
        }        
    }
    
    
    /**
     * Processes a CDATA section.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processCDATA()
        throws IOException
    {
        if (this.read(null) != 'C') { this.skipTag(); }
        if (this.read(null) != 'D') { this.skipTag(); }
        if (this.read(null) != 'A') { this.skipTag(); }
        if (this.read(null) != 'T') { this.skipTag(); }
        if (this.read(null) != 'A') { this.skipTag(); }
        if (this.read(null) != '[') { this.skipTag(); }
        this.validator.PCDataAdded(this.reader.getLineNr());
        Reader reader = new ContentReader(">]]", true, "");
        this.builder.addPCData(reader, this.reader.getLineNr());
        reader.close();
    }
        

    /**
     * Processes a document type declaration.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processDocType()
        throws IOException
    {
        if (this.read(null) != 'O') { this.skipTag(); }
        if (this.read(null) != 'C') { this.skipTag(); }
        if (this.read(null) != 'T') { this.skipTag(); }
        if (this.read(null) != 'Y') { this.skipTag(); }
        if (this.read(null) != 'P') { this.skipTag(); }
        if (this.read(null) != 'E') { this.skipTag(); }
        this.skipWhitespace(null, null);
        String systemID = null;
        StringBuffer publicID = new StringBuffer();
        String rootElement = this.scanIdentifier();
        this.skipWhitespace(null, null);
        char ch = this.read(null);
        
        if (ch == 'P') {
            systemID = this.scanPublicID(publicID);
            this.skipWhitespace(null, null);
            ch = this.read(null);
        } else if (ch == 'S') {
            systemID = this.scanSystemID();
            this.skipWhitespace(null, null);
            ch = this.read(null);
        }
        
        if (ch == '[') {
            this.validator.parseDTD(publicID.toString(),
                                    this.reader);
            this.skipWhitespace(null, null);
            ch = this.read(null);
        }
        
        if (ch != '>') {
            this.errorExpectedInput("`>'");
        }
        
        if (systemID != null) {
            Reader reader
                    = this.reader.openStream(publicID.toString(), systemID);
            this.reader.startNewStream(reader);
            this.validator.parseDTD(publicID.toString(),
                                    this.reader);
        }
    }
    

    /**
     * Scans a public ID.
     *
     * @param publicID will contain the public ID
     *
     * @return the system ID
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected String scanPublicID(StringBuffer publicID)
        throws IOException
    {
        if (this.read(null) != 'U') { return null; }
        if (this.read(null) != 'B') { return null; }
        if (this.read(null) != 'L') { return null; }
        if (this.read(null) != 'I') { return null; }
        if (this.read(null) != 'C') { return null; }
        
        this.skipWhitespace(null, null);
        publicID.append(this.scanString(false));
        this.skipWhitespace(null, null);
        return this.scanString(false);
    }
    

    /**
     * Scans a system ID.
     *
     * @return the system ID
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected String scanSystemID()
        throws IOException
    {
        if (this.read(null) != 'Y') { return null; }
        if (this.read(null) != 'S') { return null; }
        if (this.read(null) != 'T') { return null; }
        if (this.read(null) != 'E') { return null; }
        if (this.read(null) != 'M') { return null; }
        
        this.skipWhitespace(null, null);
        return this.scanString(false);
    }
    
    
    /**
     * Processes a regular element.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processElement()
        throws IOException
    {
        String name = this.scanIdentifier();
        this.skipWhitespace(null, null);
        this.validator.elementStarted(name, this.reader.getLineNr());
        this.builder.startElement(name, this.reader.getLineNr());
        char ch;
        
        for (;;) {
            ch = this.read(null);
            
            if ((ch == '/') || (ch == '>')) {
                break;
            }
            
            this.reader.unread(ch);
            this.processAttribute();
            this.skipWhitespace(null, null);
        }
        
        if (ch == '/') {
            if (this.read(null) != '>') {
                this.errorExpectedInput("`>'");
            }
            
            Properties extraAttributes = new Properties();
            this.validator.elementEnded(name, extraAttributes);
            Enumeration _enum = extraAttributes.keys();
            
            while (_enum.hasMoreElements()) {
                String key = (String) _enum.nextElement();
                String value = extraAttributes.getProperty(key);
                this.builder.addAttribute(key, value);
            }
            
            this.builder.endElement(name);
            return;
        }
        
        StringBuffer whitespaceBuffer = new StringBuffer(16);
        
        for (;;) {
            whitespaceBuffer.setLength(0);
            boolean fromEntity[] = new boolean[1];
            this.skipWhitespace(whitespaceBuffer, fromEntity);
            ch = this.read(null);
            
            if ((ch == '<') && (! fromEntity[0])) {
                ch = reader.read();
                
                if (ch == '/') {
                    this.skipWhitespace(null, null);
                    String str = this.scanIdentifier();
                    
                    if (! str.equals(name)) {
                        this.errorWrongClosingTag(name, str);
                    }
                    
                    this.skipWhitespace(null, null);
                    
                    if (this.read(null) != '>') {
                        this.errorClosingTagNotEmpty();
                    }
                    
                    Properties extraAttributes = new Properties();
                    this.validator.elementEnded(name, extraAttributes);
                    Enumeration _enum = extraAttributes.keys();
                    
                    while (_enum.hasMoreElements()) {
                        String key = (String) _enum.nextElement();
                        String value = extraAttributes.getProperty(key);
                        this.builder.addAttribute(key, value);
                    }
                    
                    this.builder.endElement(name);
                    break;
                } else {
                    this.reader.unread(ch);
                    this.scanSomeTag(true /*CDATA allowed*/);
                }
            } else {
                this.validator.PCDataAdded(this.reader.getLineNr());
                this.reader.unread(ch);
                //whitespaceBuffer.append(ch);
                Reader reader = new ContentReader("<", false,
                                                  whitespaceBuffer.toString());
                this.builder.addPCData(reader, this.reader.getLineNr());
                reader.close();
                this.reader.unread('<');
            }
        }
    }
    

    /**
     * Processes an attribute of an element.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processAttribute()
        throws IOException
    {
        String key = this.scanIdentifier();
        this.skipWhitespace(null, null);
        
        if (this.read(null) != '=') {
            this.errorExpectedInput("`='");
        }
        
        String value = this.scanString(true);
        this.validator.attributeAdded(key, value);
        this.builder.addAttribute(key, value);
    }
    
    
    /**
     * Retrieves an identifier from the data.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected String scanIdentifier()
        throws IOException
    {
        StringBuffer result = new StringBuffer();
        
        for (;;) {
            char ch = this.read(null);
            
            if ((ch == '_') || (ch == ':') || (ch == '-') || (ch == '.')
                    || ((ch >= 'a') && (ch <= 'z'))
                    || ((ch >= 'A') && (ch <= 'Z'))
                    || ((ch >= '0') && (ch <= '9')) || (ch > '\u007E')) {
                result.append(ch);
            } else {
                this.reader.unread(ch);
                break;
            }
        }
        
        return result.toString();
    }
    
    
    /**
     * Retrieves a delimited string from the data.
     *
     * @param normalizeWhitespace if all whitespace chars need to be converted
     *                            to spaces
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected String scanString(boolean normalizeWhitespace)
        throws IOException
    {
        StringBuffer result = new StringBuffer();
        boolean isEntity[] = new boolean[1];
        char delim = this.read(null);
        
        if ((delim != '\'') && (delim != '"')) {
            this.errorExpectedInput("delimited string");
        }
        
        for (;;) {
            char ch = this.read(isEntity);
            
            if ((! isEntity[0]) && (ch == '&')) {
                this.reader.startNewStream(this.scanEntity(isEntity));
                ch = this.reader.read();
            }
                    
            if ((! isEntity[0]) && (ch == delim)) {
                break;
            } else if (normalizeWhitespace && (ch < ' ')) {
                result.append(' ');
            } else {
                result.append(ch);
            }
        }
        
        return result.toString();
    }


    /**
     * Processes an entity.
     *
     * @return a reader from which the entity value can be retrieved
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected Reader scanEntity(boolean[] isCharLiteral)
        throws IOException
    {
        char ch = this.reader.read();
        StringBuffer keyBuf = new StringBuffer();
        
        while (ch != ';') {
            keyBuf.append(ch);
            ch = this.reader.read();
        }
        
        String key = keyBuf.toString();
        
        if (key.charAt(0) == '#') {
            if (isCharLiteral != null) {
                isCharLiteral[0] = true;
            }
            
            char[] chArr = new char[1];
            
            if (key.charAt(1) == 'x') {
                chArr[0] = (char) Integer.parseInt(key.substring(2), 16);
            } else {
                chArr[0] = (char) Integer.parseInt(key.substring(1), 10);
            }
            
            return new CharArrayReader(chArr);
        } else {
            Reader entityReader = this.validator.getEntity(this.reader, key);
            
            if (entityReader == null) {
                this.errorInvalidEntity(key);
            }
            
            return entityReader;
        }
    }
    
    
    /**
     * Skips whitespace from the reader.
     *
     * @param buffer where to put the whitespace; null if the whitespace does
     *               not have to be stored.
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void skipWhitespace(StringBuffer buffer,
                                  boolean[]    isEntity)
        throws IOException
    {
        char ch;
        
        if (buffer == null) {
            do {
                ch = this.reader.read();
            } while ((ch == ' ') || (ch == '\t') || (ch == '\n')
                     || (ch == '\r'));
        } else {
            for (;;) {
                ch = this.reader.read();
            
                if ((ch != ' ') && (ch != '\t') && (ch != '\n')
                        && (ch != '\r')) {
                    break;
                }
                
                buffer.append(ch);
            }
        }
        
        this.reader.unread(ch);
    }
    

    /**
     * Reads a character from the reader.
     *
     * @param isEntityValue if the character is the first character in an
     *                      entity
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected char read(boolean[] isEntityValue)
        throws IOException
    {
        char ch = this.reader.read();
        
        if (isEntityValue != null) {
            isEntityValue[0] = false;
        }
        
        if (ch == '&') {
            boolean[] charLiteral = new boolean[1];
            this.reader.startNewStream(this.scanEntity(charLiteral));
            
            if (charLiteral[0]) {
                ch = this.reader.read();
                
                if (isEntityValue != null) {
                    isEntityValue[0] = true;
                }
            } else {
                ch = this.read(null);
            }
            
            /*if (isEntityValue != null) {
                if ((ch == '&') && (! isEntityValue[0])) { // escape char
                    ch = this.reader.read();
                    
                    if ((ch == '<') || (ch == '&')) { // only escape &, <
                        isEntityValue[0] = true;
                    } else {
                        this.reader.unread(ch);
                        ch = '&';
                    }
                }
            }*/
        }
                    
        return ch;
    }
    
    
    /**
     * Throws an XMLParseException to indicate that an expected string is not
     * encountered.
     *
     * @param expectedString the string that is expected
     */
    protected void errorExpectedInput(String expectedString)
    {
        throw new XMLParseException(this.reader.getLineNr(),
                                    "Expected: " + expectedString);
    }
    
    
    /**
     * Throws an XMLParseException to indicate that a string is not expected
     * at this point.
     *
     * @param unexpectedString the string that is unexpected
     */
    protected void errorInvalidInput(String unexpectedString)
    {
        throw new XMLParseException(this.reader.getLineNr(),
                                    "Invalid input: " + unexpectedString);
    }
    
    
    /**
     * Throws an XMLParseException to indicate that the closing tag of an
     * element does not match the opening tag.
     *
     * @param expectedName the name of the opening tag
     * @param wrongName the name of the closing tag
     */
    protected void errorWrongClosingTag(String expectedName,
                                        String wrongName)
    {
        throw new XMLParseException(this.reader.getLineNr(),
                                    "Closing tag does not match opening tag: `"
                                    + wrongName + "' != `" + expectedName
                                    + "'");
    }


    /**
     * Throws an XMLParseException to indicate that extra data is encountered
     * in a closing tag.
     */
    protected void errorClosingTagNotEmpty()
    {
        throw new XMLParseException(this.reader.getLineNr(),
                                    "Closing tag must be empty");
    }
    
    
    /**
     * Throws an XMLParseException to indicate that an entity could not be
     * resolved.
     *
     * @param key the name of the entity
     */
    protected void errorInvalidEntity(String key)
    {
        throw new XMLParseException(this.reader.getLineNr(),
                                    "Invalid entity: `&" + key + ";'");
    }
        
    
    /**
     * This reader reads data from another reader until a certain string is
     * encountered.
     *
     * @author Marc De Scheemaecker
     * @version $Name: PRERELEASE_2_0_20010329 $, $Version$
     */
    protected class ContentReader
        extends Reader
    {
        
        /**
         * The delimiter that will indicate the end of the stream.
         */
        private String delimiter;
        
        
        /**
         * The characters that have been read too much.
         */
        private String charsReadTooMuch;
        
        
        /**
         * The number of characters in the delimiter that stil need to be
         * scanned.
         */
        private int charsToGo;
        
        
        /**
         * True if &amp; needs to be left untouched.
         */
        private boolean useLowLevelReader;
        
        
        /**
         * True if we are passed the initial prefix.
         */
        private boolean pastInitialPrefix;
        
        
        /**
         * Creates the reader.
         *
         * @param delimiter the delimiter, as a backwards string, that will 
         *                  indicate the end of the stream
         * @param useLowLevelReader true if &amp; needs to be left untouched;
         *                          false if entities need to be processed
         */
        ContentReader(String delimiter,
                      boolean useLowLevelReader,
                      String prefix)
        {
            this.delimiter = delimiter;
            this.charsToGo = this.delimiter.length();
            this.charsReadTooMuch = prefix;
            this.useLowLevelReader = useLowLevelReader;
            this.pastInitialPrefix = false;
        }
        
        
        /**
         * Reads a block of data.
         *
         * @param buffer where to put the read data
         * @param offset first position in buffer to put the data
         * @param size maximum number of chars to read
         *
         * @return the number of chars read, or -1 if at EOF
         *
         * @throws java.io.IOException
         *		if an error occurred reading the data
         */
        public int read(char[] buffer,
                        int    offset,
                        int    size)
            throws IOException
        {
            int charsRead = 0;
            boolean isEntity[] = new boolean[1];
            isEntity[0] = false;
            
            if ((offset + size) > buffer.length) {
                size = buffer.length - offset;
            }
            
            while ((this.charsToGo > 0) && (charsRead < size)) {
                char ch;
                
                if (this.charsReadTooMuch.length() > 0) {
                    ch = this.charsReadTooMuch.charAt(0);
                    this.charsReadTooMuch = this.charsReadTooMuch.substring(1);
                } else {
                    this.pastInitialPrefix = true;

                    if (useLowLevelReader) {
                        ch = StdXMLParser.this.reader.read();
                    } else {
                        ch = StdXMLParser.this.read(isEntity);
                        
                        if (! isEntity[0]) {
                            if (ch == '&') {
                                StdXMLParser.this.reader.startNewStream
                                    (StdXMLParser.this.scanEntity(isEntity));
                                ch = StdXMLParser.this.reader.read();
                            }
                        }
                    }
                }
                
                if (isEntity[0]) {
                    buffer[offset + charsRead] = ch;
                    charsRead++;
                } else {
                    if ((ch == (this.delimiter.charAt(this.charsToGo - 1)))
                            && pastInitialPrefix) {
                        --this.charsToGo;
                    } else if (this.charsToGo < this.delimiter.length()) {
                        this.charsReadTooMuch
                                = this.delimiter.substring(this.charsToGo + 1)
                                    + ch;
                        this.charsToGo = this.delimiter.length();
                        buffer[offset + charsRead]
                                = this.delimiter.charAt(this.charsToGo - 1);
                        charsRead++;
                    } else {
                        buffer[offset + charsRead] = ch;
                        charsRead++;
                    }
                }
            }
            
            if (charsRead == 0) {
                charsRead = -1;
            }
            
            return charsRead;
        }
        
        
        /**
         * Skips remaining data and closes the stream.
         *
         * @throws java.io.IOException
         *		if an error occurred reading the data
         */
        public void close()
            throws IOException
        {
            while (this.charsToGo > 0) {
                char ch;
                
                if (this.charsReadTooMuch.length() > 0) {
                    ch = this.charsReadTooMuch.charAt(0);
                    this.charsReadTooMuch = this.charsReadTooMuch.substring(1);
                } else {
                    if (useLowLevelReader) {
                        ch = StdXMLParser.this.reader.read();
                    } else {
                        ch = StdXMLParser.this.read(null);
                    }
                }
                
                if (ch == (this.delimiter.charAt(this.charsToGo - 1))) {
                    --this.charsToGo;
                } else if (this.charsToGo < this.delimiter.length()) {
                    this.charsReadTooMuch
                            = this.delimiter.substring(this.charsToGo + 1)
                                + ch;
                    this.charsToGo = this.delimiter.length();
                }
            }
        }
    }
    
}
