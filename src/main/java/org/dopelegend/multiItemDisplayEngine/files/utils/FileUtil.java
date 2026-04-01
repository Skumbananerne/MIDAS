package org.dopelegend.multiItemDisplayEngine.files.utils;

import java.io.File;

public class FileUtil {
    public static boolean deleteDirectory(File file)
    {
        try{
            for (File subfile : file.listFiles()) {

                if (subfile.isDirectory()) {
                    deleteDirectory(subfile);
                }

                subfile.delete();
            }
            file.delete();
            return true;
        } catch(Exception ex){
            return false;
        }
    }
}
