package cecs429.encode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VariableByteEncoding {
    public byte[] encodeNumber(int n){
        if (n == 0) {
            return new byte[]{0};
        }

        int size = (int)(Math.log(n) / Math.log(128)) + 1;
        byte[] result = new byte[size];
        int i = size - 1;
        do {
            result[i--] = (byte) (n % 128);
            if(n < 128)
                break;
            n /= 128;
        } while (i >= 0);
        result[size - 1] += 128;
        return result;
    }
    public byte[] encode(ArrayList<Integer> numbers) {
        ByteBuffer buf = ByteBuffer.allocate(numbers.size() * (Integer.SIZE / Byte.SIZE));
        for (Integer number : numbers) {
            buf.put(encodeNumber(number));
        }
        buf.flip();
        byte[] result = new byte[buf.limit()];
        buf.get(result);
        return result;
    }
    public List<Integer> decode(byte[] byteArray) {
        List<Integer> numbers = new ArrayList<>();
        int n = 0;
        if(byteArray[0]==0){
            numbers.add(0);
            return  numbers;
        }

        for (byte b : byteArray) {
            if ((b & 0xff) < 128) {
                n = 128 * n + (b & 0xff);
            } else {
                int num = (128 * n + ((b - 128) & 0xff));
                numbers.add(num);
                n = 0;
            }
        }
        return numbers;
    }
}
