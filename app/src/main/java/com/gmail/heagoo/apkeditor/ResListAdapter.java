package com.gmail.heagoo.apkeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import com.gmail.heagoo.common.IOUtils;
import com.gmail.heagoo.common.SDCard;

public class ResListAdapter {
	private File projectDir;
	private String rootPath;

	public ResListAdapter(String prj) {
		rootPath = prj;
		projectDir = new File(prj);
	}

	public Object addFile(String targetPath, InputStream input) {
		File f = new File(targetPath);
		if (!f.exists()) {
			try {
				f.getParentFile().mkdirs();
				FileOutputStream out = new FileOutputStream(f);
				IOUtils.copy(input, out);
				IOUtils.closeQuietly(input);
				IOUtils.closeQuietly(out);
				return Boolean.TRUE;
			} catch (Exception e) {}
		}
		return null;
	}

	public void deleteFile(String dirPath, String fileName, boolean p2) {
		File f = new File(dirPath, fileName);
		if (f.isDirectory()) {
			SDCard.deleteDir(f);
		} else {
			f.delete();
		}
	}

	public void addFolderReportError(String dirPath, String folderName, boolean p2) throws Exception {
		if (folderName.equals("META-INF") && dirPath.equals(rootPath)) {
            throw new Exception(folderName);
        }
		File dir = new File(dirPath);
        // Add the folder into decode path
        if (dir.exists() && !dirPath.equals(rootPath)) {
            File f = new File(dirPath, folderName);
            if (f.exists()) {
                throw new Exception(folderName);
            }
			f.mkdirs();
		}
	}

	public boolean isFolderExist(String path) {
		File f = new File(path);
        if (f.exists()) {
            return true;
        }

        // Special root path
        if (rootPath.equals(path)) {
            return true;
        }
		return false;
	}

	public void fileModified(String fakeSmali, String targetPath) {
		// ???
	}
}
