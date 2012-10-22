import magick.CompressionType;
import magick.ImageInfo;
import magick.InterlaceType;
import magick.MagickImage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
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
public class Scaler3 implements Callable<byte[]> {
    private static final AtomicLong total = new AtomicLong();
    final static String FILENAME = "test1.1024x768.jpeg";
    private byte[] data;


    public Scaler3(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] call() throws Exception {
        final ImageInfo imageInfo = new ImageInfo(FILENAME);
        MagickImage image = new MagickImage(imageInfo);

        image.profileImage("*", null);
        final MagickImage result = image.scaleImage(800, 600);
        imageInfo.setQuality(85);
        imageInfo.setInterlace(InterlaceType.LineInterlace);
        imageInfo.setCompression(CompressionType.JPEGCompression);
        result.setImageFormat("jpg");
        byte[] bytes = result.imageToBlob(imageInfo);
        total.addAndGet(bytes.length);
        return bytes;
    }


    public static void main(String[] args) throws Exception, ExecutionException {
        final ExecutorService service = Executors.newFixedThreadPool(8);

        final byte[] bytes = FileUtils.readFileToByteArray(new File(FILENAME));

        final byte[] result = new Scaler3(bytes).call();
//
        FileUtils.writeByteArrayToFile(new File("test1.800x600.jpeg"), result);

/*
        Collection<Scaler3> tasks = new ArrayList<Scaler3>();

        for(int i=0; i < 100; i++) {
            tasks.add(new Scaler3(bytes));
        }

        service.invokeAll(tasks);

        tasks = new ArrayList<Scaler3>();
        for(int i=0; i < 2000; i++) {
            tasks.add(new Scaler3(bytes));
        }
        long t1 = System.nanoTime();
        service.invokeAll(tasks);

        service.shutdownNow();

        final double count = 2000.0;
        System.out.println(count /(System.nanoTime() -  t1)* TimeUnit.SECONDS.toNanos(1));

*/

    }
}
