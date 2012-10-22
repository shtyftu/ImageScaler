import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author mikhail.bezoyan
 */
public class Scaler2 implements Callable<byte[]> {
    private static final AtomicLong total = new AtomicLong();

    private byte[] data;


    public Scaler2(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] call() throws Exception {
        Process process = new ProcessBuilder(
                Arrays.asList("/opt/local/bin/convert", "fd:0", "-scale", "800x600", "-quality", "80", "-format", "jpeg", "fd:1")
//                Arrays.asList("/opt/local/bin/convert", "fd:0", "-format", "jpeg", "fd:1")
        ).start();
        InputStream out = process.getInputStream();
        OutputStream in = process.getOutputStream();
        in.write(data);
        in.close();
        final byte[] bytes = IOUtils.toByteArray(out);
        total.addAndGet(bytes.length);
        return null;
    }


    public static void main(String[] args) throws Exception, ExecutionException {
        final ExecutorService service = Executors.newFixedThreadPool(8);

        final byte[] bytes = FileUtils.readFileToByteArray(new File("test1.1024x768.jpeg"));

//        final byte[] result = new Scaler2(bytes).call();
//
//        FileUtils.writeByteArrayToFile(new File("test1.800x600.jpeg"), result);

        Collection<Scaler2> tasks = new ArrayList<Scaler2>();

        for(int i=0; i < 100; i++) {
            tasks.add(new Scaler2(bytes));
        }

        service.invokeAll(tasks);

        tasks = new ArrayList<Scaler2>();
        for(int i=0; i < 2000; i++) {
            tasks.add(new Scaler2(bytes));
        }
        long t1 = System.nanoTime();
        service.invokeAll(tasks);

        service.shutdownNow();

        final double count = 2000.0;
        System.out.println(count /(System.nanoTime() -  t1)* TimeUnit.SECONDS.toNanos(1));


    }
}
