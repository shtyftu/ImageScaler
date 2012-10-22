

import com.sun.imageio.plugins.jpeg.JPEGImageReader;
import com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi;
import com.sun.imageio.plugins.jpeg.JPEGImageWriter;
import com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author mikhail.bezoyan
 */
public class Scaler implements Callable<byte[]> {

    final static int PRE_TEST_NUMBER = 3;
    final static int IMAGES_NUMBER = 1;
    final static int TARGET_WIDTH = 800;
    final static int TARGET_HEIGHT = 600;
    final static boolean WRITE_TO_FILE = true;


   // static ImageWriteParam param;
    static AtomicLong decodeTime = new AtomicLong(0);
    static AtomicLong scaleTime = new AtomicLong(0);
    static AtomicLong encodeTime = new AtomicLong(0);

    private byte[] data;

    public Scaler(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] call() throws Exception {

        long t1 = System.nanoTime();

        ByteArrayInputStream in = new ByteArrayInputStream(data);

        final BufferedImage srcImage;
        //srcImage = decode(in);
        srcImage = ImageIO.read(in);

        IOUtils.closeQuietly(in);

        long t2 = System.nanoTime();


        BufferedImage outImage;
        outImage = scaleImage(srcImage);

        long t3 = System.nanoTime();

        final byte [] bytes;
        //bytes = encodeSimple(outImage);
        bytes = encodeProgressive(outImage);

        if (WRITE_TO_FILE){
            writeToFile(bytes);
        }


        long t4 = System.nanoTime();

        decodeTime.addAndGet(t2-t1);
        scaleTime.addAndGet(t3-t2);
        encodeTime.addAndGet(t4-t3);
        return null;
    }

    private void writeToFile(byte[] bytes) throws IOException {
        FileOutputStream fos = new FileOutputStream("output/test.jpg");
        fos.write(bytes);
        fos.close();
    }

    private BufferedImage scaleImage(BufferedImage srcImage) {
        int srcImageWidth = srcImage.getWidth();
        int srcImageHeight = srcImage.getHeight();
        double ratio = Math.min((double) Scaler.TARGET_WIDTH / srcImageWidth,
                            (double) Scaler.TARGET_HEIGHT / srcImageHeight);
        int trgImageWidth = (int) Math.round(ratio*srcImageWidth);
        int trgImageHeight = (int) Math.round(ratio*srcImageHeight);
        final BufferedImage outImage = new BufferedImage(
                trgImageWidth, trgImageHeight,
                BufferedImage.TYPE_INT_BGR);
        final Graphics2D graphics = outImage.createGraphics();
        //graphics.setBackground(Color.WHITE);
        //graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //graphics.setComposite(AlphaComposite.Src);
        graphics.drawImage(srcImage, 0, 0, trgImageWidth, trgImageHeight, null);
        graphics.dispose();
        return outImage;
    }

    private byte[] encodeSimple(BufferedImage outImage) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(outImage, "jpg", bos);
        return bos.toByteArray();
    }

    private byte[] encodeProgressive(BufferedImage outImage) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageWriter writer = new JPEGImageWriterSpi().createWriterInstance();

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.85f);


        writer.setOutput(ImageIO.createImageOutputStream(bos));
        writer.write(null,new IIOImage(outImage,null,null), param);
        return bos.toByteArray();
    }

    private BufferedImage decode(ByteArrayInputStream in) throws IOException {
        ImageReader imageReader = new JPEGImageReaderSpi().createReaderInstance();
        ImageInputStream iis = ImageIO.createImageInputStream(in);
        imageReader.setInput(iis);
        return imageReader.read(0);

    }


    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        final ExecutorService service = Executors.newFixedThreadPool(8);
        final byte[] bytes = FileUtils.readFileToByteArray(new File("input/test1.1024x768.jpeg"));
        Collection<Scaler> tasks = new ArrayList<Scaler>();
        decodeTime.set(0); scaleTime.set(0); encodeTime.set(0);


        for(int i = 0; i < PRE_TEST_NUMBER; i++) {
            tasks.add(new Scaler(bytes));
        }
        service.invokeAll(tasks);

        tasks = new ArrayList<Scaler>();
        for(int i=0; i < IMAGES_NUMBER; i++) {
            tasks.add(new Scaler(bytes));
        }
        long t1 = System.nanoTime();
        service.invokeAll(tasks);
        service.shutdownNow();

        final double count = IMAGES_NUMBER;
        System.out.println(count /(System.nanoTime() -  t1)* TimeUnit.SECONDS.toNanos(1));
        System.out.println(String.format(
                "%d %d %d",
                TimeUnit.NANOSECONDS.toMillis(decodeTime.get())/IMAGES_NUMBER,
                TimeUnit.NANOSECONDS.toMillis(scaleTime.get())/IMAGES_NUMBER,
                TimeUnit.NANOSECONDS.toMillis(encodeTime.get())/IMAGES_NUMBER
        ));
    }
}
