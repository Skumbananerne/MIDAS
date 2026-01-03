package org.dopelegend.multiItemDisplayEngine.files.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
    public static void zip(File source, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            if (source.isDirectory()) {
                File[] children = source.listFiles();
                if (children == null) return;

                for (File child : children) {
                    zipRecursive(child, child.getName(), zos);
                }
            } else {
                zipRecursive(source, source.getName(), zos);
            }
        }
    }

    private static void zipRecursive(File file, String entryName, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            if (!entryName.endsWith("/")) entryName += "/";

            zos.putNextEntry(new ZipEntry(entryName));
            zos.closeEntry();

            File[] children = file.listFiles();
            if (children == null) return;

            for (File child : children) {
                zipRecursive(child, entryName + child.getName(), zos);
            }
        } else {
            zos.putNextEntry(new ZipEntry(entryName));
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.transferTo(zos);
            }
            zos.closeEntry();
        }
    }
}
