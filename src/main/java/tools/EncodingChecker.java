package tools;

import scripting.EncodingDetect;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class EncodingChecker {

    private static Map<String, Integer> encodingCounts = new HashMap<>();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java EncodingChecker <directory> <targetEncoding>");
            return;
        }

        String directoryPath = args[0];
        String targetEncoding = args[1];

        try {
            checkDirectoryEncoding(Paths.get(directoryPath), targetEncoding);
            printEncodingCounts();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkDirectoryEncoding(Path path, String targetEncoding) throws IOException {
        // 遍历目录
        Files.walk(path)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        String encoding = detectFileEncoding(file.toFile());
                        updateEncodingCounts(encoding);
                        if (!"ASCII".equals(encoding) && !"UTF-8".equals(encoding)) {
                            System.out.println("File: " + file + " has encoding: " + encoding);
                        }
                        if (encoding.equals(targetEncoding)) {
                            convertFileToUtf8(file.toFile(), encoding);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static void updateEncodingCounts(String encoding) {
        encodingCounts.merge(encoding, 1, Integer::sum);
    }

    private static String detectFileEncoding(File file) throws IOException {
        String encoding = EncodingDetect.getJavaEncode(file);
        return encoding == null ? "Unknown" : encoding;
    }

    private static void printEncodingCounts() {
        System.out.println("Encoding Counts:");
        encodingCounts.forEach((encoding, count) -> System.out.println(encoding + ": " + count));
    }


    private static void convertFileToUtf8(File file, String srcEncoding) throws IOException {
        byte[] content = Files.readAllBytes(file.toPath());
        String fileContent = new String(content, Charset.forName(srcEncoding));
        Files.write(file.toPath(), fileContent.getBytes(StandardCharsets.UTF_8));
        System.out.println("Converted " + file + " to UTF-8.");
    }
}
