import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Timer;

/**
 * @author mikhail.bezoyan
 */
public class Runner {

    private Process process;
    private InputStream out;
    private OutputStream in;
    private byte[] result;

    public Runner() {
    }

    public void init() throws IOException, InterruptedException {

    }

    public byte[] transform(byte[] img) throws IOException {
        IOUtils.write(img, in);
//        IOUtils.write(img, in);
        in.flush();
        return IOUtils.toByteArray(out);
    }


/*
    public byte[] execute(String command) throws ContentUploadServerException, InterruptedException, IOException {
        InputStream is = null;
        InputStream errStream = null;
        OutputStream os = null;
        Process process = null;
        Timer timer = null;
        try {
            byte[] out = null;

            int exitVal = process.waitFor();
            if (0 != exitVal) {
                String errStr = null;
                if (null != errStream) {
                    byte[] errOut = IOUtils.toByteArray(errStream);
                    if (null != errOut) {
                        errStr = new String(errOut);
                    }
                }
                throw ContentUploadServerException.IMAGE_INVALID_FORMAT;
            } else {
                if (null != is) {
                    out = IOUtils.toByteArray(is);
                }

            }
            return out;
        } catch (InterruptedException e) {
            try {
                process.destroy();
            } catch (Exception e2) {
            }
            log.error("Command [" + command + "] did not return after " + this.timeout + " milliseconds");
            throw e;
        } finally {
            try {
                if (null != timer) {
                    timer.cancel();
                }
            } catch (Exception e) {
            }
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(errStream);
        }
    }
*/

}
