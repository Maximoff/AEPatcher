package com.gmail.heagoo.apkeditor.patch;

import java.io.IOException;
import java.util.zip.ZipFile;
import ru.maximoff.aepatcher.MainActivity;
import ru.maximoff.aepatcher.R;


class PatchRule_Dummy extends PatchRule {

    private static final String strEnd = "[/DUMMY]";

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        super.startLine = br.getCurrentLine();

        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            if (strEnd.equals(line)) {
                break;
            }
            if (super.parseAsKeyword(line, br)) {
                line = br.readLine();
                continue;
            } else {
                logger.error(R.string.patch_error_cannot_parse,
                        br.getCurrentLine(), line);
            }
            line = br.readLine();
        }
    }

    @Override
    public String executeRule(MainActivity activity, ZipFile patchZip, IPatchContext logger) {
        return null;
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        return true;
    }

    @Override
    public boolean isSmaliNeeded() {
        return false;
    }
}
