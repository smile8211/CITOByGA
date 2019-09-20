/* 
 * This file is part of the Daisy distribution.  This software is
 * distributed 'as is' without any guarantees whatsoever. It may be
 * used freely for research but may not be used in any commercial
 * products.  The contents of this distribution should not be posted
 * on the web or distributed without the consent of the authors.
 *
 * Authors: Cormac Flanagan, Stephen N. Freund, Shaz Qadeer 
 * Contact: Shaz Qadeer (qadeer@microsoft.com)
 * 
 * Modified by Edwin Rodr&iacute;guez to use byte[] for the disk.
 */

package daisy;

import java.io.*;
import java.util.*;
import util.*;

public class Petal {

    private static final int INITIAL_DISK_SIZE = 3;
    static private byte[] disk = null;

    public static void init(boolean keepOld) {
    	disk = new byte[INITIAL_DISK_SIZE];
    	Daisy.init();
    }

    public static void write(long n, byte b) {
    	int iLoc = (int)n;
    	while(iLoc > disk.length) {
    		resizeDisk();
    	}
    	disk[iLoc] = b;
    }

    public static void write(long loc, byte b[], int n) {
    	int iLoc = (int)loc;
    	for(int i = iLoc; i < n; ++i) {
        	while(i > disk.length) {
        		resizeDisk();
        	}
    		disk[i] = b[i -iLoc];
    	}
    }


    public static void read(long loc, byte b[], int n) {
    	int iLoc = (int)loc;
    	while(iLoc > disk.length) {
    		resizeDisk();
    	}
    	for(int i = iLoc; i < n; ++i) {
    		b[i - iLoc] = disk[i];
    	}
    }

    public static byte read(long n) {
    	int iLoc = (int)n;
    	while(iLoc > disk.length) {
    		resizeDisk();
    	}
    	return disk[iLoc];
    }

    public static void writeLong(long n, long num) {
	for (int i = 0; i < 8; i++) {
	    write(n + i, (byte)(num & 0xff));
	    num = num >> 8;
	}
    }

    static public long readLong(long n) {
	long num = 0;
	for (int i = 7; i >= 0; i--) {
	    byte x = read (n + i);
	    num = (num << 8) + (x & 0xff);
	}
	return num;
    }
    
    private static void resizeDisk() {
    	int oldLength = disk.length;
    	int newLength = oldLength * 2;
    	byte[] newDisk = new byte[newLength];
    	System.arraycopy(disk, 0, newDisk, 0, oldLength);
    	disk = newDisk;
    }

}

