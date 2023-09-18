/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowable.demo;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;

@Service
public class AddModelsToFilesystem {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddModelsToFilesystem.class);

    private byte[] buffer = new byte[1024];

    public void saveZipToFilesystem(MultipartFile file, String destDir, String deploymentKey, Boolean explode ) throws IOException{
        File destination = new File(destDir + File.separator + deploymentKey);
        if(!explode){
            FileOutputStream fos = new FileOutputStream(destDir + File.separator + file.getOriginalFilename());
            fos.write(file.getBytes());
            fos.close();
        } else {
            ZipInputStream zis = new ZipInputStream(file.getInputStream());
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                File newFile = newFile(destination, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    
                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }
    }

    public void addModelsToGitRepo(String destDir, String AppId) throws IOException, NoFilepatternException, GitAPIException{
        Git repo = Git.open(new File(destDir));

        // Add existing files
        repo.add().setUpdate(true)
        .addFilepattern(AppId)
        .call();
        // Add new files
        repo.add().setUpdate(false)
        .addFilepattern(AppId)
        .call();

        repo.commit()
        .setMessage("Update Deployed Application : " + AppId)
        .call();
        
    }

    protected static File newFile(File destination, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destination, zipEntry.getName());
    
        String destDirPath = destination.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
    
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
    
        return destFile;
    }
}
