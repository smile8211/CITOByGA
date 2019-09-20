/* 
 * This file is part of the Daisy distribution.  This software is
 * distributed 'as is' without any guarantees whatsoever. It may be
 * used freely for research but may not be used in any commercial
 * products.  The contents of this distribution should not be posted
 * on the web or distributed without the consent of the authors.
 *
 * Authors: Cormac Flanagan, Stephen N. Freund, Shaz Qadeer 
 * Contact: Shaz Qadeer (qadeer@microsoft.com)
 */

package daisy;

//@ thread_local
public class FileHandle {
    public long inodenum;

    public static boolean equal(FileHandle fd1, FileHandle fd2) {
	return fd1.inodenum == fd2.inodenum;
    }
        
    public String toString() {
        return "[inodenum=" + this.inodenum + "]";
    }
}
