package scripting;

import configuration.EnvProperties;
import lombok.extern.slf4j.Slf4j;
import server.Randomizer;
import utils.HexTool;
import utils.datastructures.Pair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

@Slf4j
public class LieDetectorScript {
    private static final String IMG_DIRECTORY = "scripts/lieDetector";
    private static final String CAPTCHA_VERIFIER = "98818D40B83AECCFB7AFD7FD9653E1037519AC61";
    private static final String CAPTCHA_SERVER = "http://localhost/captcha.php?verify=98818D40B83AECCFB7AFD7FD9653E1037519AC61";

    public static Pair<String, String> getImageBytes() {
        try {
            final URL url = new URL(CAPTCHA_SERVER);
            final InputStream inputStream = url.openStream();
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            final String imgByte = HexTool.toString(output.toByteArray());
            return new Pair<String, String>(imgByte.substring(39, imgByte.length()), output.toString().split("CAPTCHA")[0]);
        } catch (IOException ex) {
            ex.printStackTrace();
            final File directory = Paths.get(EnvProperties.scriptsPath, "lieDetector").toFile();
            if (!directory.exists()) {
                log.error("lieDetector folder does not exist!");
                return null;
            }
            final String[] filename = directory.list();
            String answer = filename[Randomizer.nextInt(filename.length)];
            answer = answer.substring(0, answer.length() - 4);
            try {
                return new Pair<String, String>(HexTool.toString(getBytesFromFile(Paths.get(EnvProperties.scriptsPath, "lieDetector", answer + ".jpg").toFile())), answer);
            } catch (IOException ex2) {
                ex2.printStackTrace();
                return null;
            }
        }
    }

    public static byte[] getBytesFromFile(final File file) throws IOException {
        byte[] bytes = null;
        try {
            final InputStream is = new FileInputStream(file);
            final long length = file.length();
            if (length > 2147483647L) {
                return null;
            }
            bytes = new byte[(int) length];
            int offset = 0;
            for (int numRead = 0; offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0; offset += numRead) {
            }
            if (offset < bytes.length) {
                log.error("[Lie Detector Script] Could not completely read file " + file.getName());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return bytes;
    }
}
