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

public class Utility {

    public static void longToBytes(long n, byte b[], int offset) {
        for (int i = 0; i < 8; i++) {
                b[offset + i] = (byte)(n & 0xff);
                n = n >> 8;
        }
    }

    public static long bytesToLong(byte b[], int offset) {  
        long n = 0;
        for (int i = 7; i >= 0; i--) {
                n = (n << 8) + (b[offset + i] & 0xff);
        }
        return n;
    }
}
