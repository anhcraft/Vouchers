package dev.anhcraft.vouchers.util;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressUtils {
    public static void compressAndWriteString(String content, File filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fos);
        gzipOutputStream.write(content.getBytes());
        gzipOutputStream.close();
        fos.close();
    }

    public static String readAndDecompressString(File filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        GZIPInputStream gzipInputStream = new GZIPInputStream(fis);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int bufferSize = 4096;
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = gzipInputStream.read(buffer, 0, bufferSize)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        gzipInputStream.close();
        byteArrayOutputStream.close();

        return byteArrayOutputStream.toString();
    }
}
