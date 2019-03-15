package cs455.scaling.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

    private byte[] data;

    public Hash(byte[] data) {
        this.data = data;
    }

    public synchronized String SHA1FromBytes() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16);
    }

    public synchronized String checkSize(String hashedByteString) {
        StringBuilder pad = new StringBuilder(hashedByteString);

        if (hashedByteString.length() == 40)
            return hashedByteString;
        else {
            while (hashedByteString.length() < 40) {
                pad.insert(0, '0');
                hashedByteString = pad.toString();
            }
        }
        return hashedByteString;
    }
}
