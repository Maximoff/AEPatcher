package com.gmail.heagoo.apkeditor.patch;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import ru.maximoff.aepatcher.MainActivity;

public interface IBeforeAddFile {

    // Return true means already consumed
    public boolean consumeAddedFile(MainActivity activity, ZipFile zfile,
                                    ZipEntry entry) throws Exception;
}
