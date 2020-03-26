import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RandomFadeFilterStream extends FilterInputStream {

    RandomFadeFilterStream(InputStream in) {
        super(new BufferedInputStream(in));
    }

    public short getSample(byte[] buffer, int position) {
        return (short) (((buffer[position + 1] & 0xff) << 8) |
                (buffer[position] & 0xff));
    }

    public void setSample(byte[] buffer, int position, short sample) {
        buffer[position] = (byte) (sample & 0xFF);
        buffer[position + 1] = (byte) ((sample >> 8) & 0xFF);
    }

    public int read(byte[] sample, int offset, int length) throws IOException {
        int bytesRead = super.read(sample, offset, length);
        float volume = 1.0f;
        short amp;

        for (int p = 0; p < bytesRead; p = p + 2) {
            amp = getSample(sample, p);
            amp = (short) ((float) amp * volume);
            setSample(sample, p, amp);
            volume = volume - (float) Math.random() * 0.0001f;
        }
        return length;
    }
}
