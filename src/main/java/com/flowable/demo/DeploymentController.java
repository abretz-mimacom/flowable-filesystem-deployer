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

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
public class DeploymentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentController.class);
    
    @Autowired AddModelsToFilesystem addModelsToFilesystem;

    @Value("${destination.dir}")
    private String destDir;
    
    @Value("${is.git.repository}")
    private String isGitRepo;

    @Value("${explode.archive}")
    private String explode;

    @PostMapping(value = "/app-repository/deployments", produces = "application/json", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadDeployment(
        @RequestParam("deploymentKey") String deploymentKey,
        @RequestParam(value = "tenantId", required = false) String tenantId,
        HttpServletRequest request, HttpServletResponse response) throws IllegalAccessException, IOException, NoFilepatternException {

        if (!(request instanceof MultipartHttpServletRequest)) {
            throw new IllegalAccessException("Multipart request is required");
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        if (multipartRequest.getFileMap().size() == 0) {
            throw new IllegalArgumentException("Multipart request with file content is required");
        }

        MultipartFile file = multipartRequest.getFileMap().values().iterator().next();
        addModelsToFilesystem.saveZipToFilesystem(file, destDir, deploymentKey, Boolean.parseBoolean(explode) );
        if(Boolean.parseBoolean(isGitRepo)){
            try {
                addModelsToFilesystem.addModelsToGitRepo(destDir, deploymentKey);
            } catch(GitAPIException ge){
                LOGGER.error(destDir + " is not a git repository");
            }
        }

        return new ResponseEntity<String>("Successfully Deployed to Filesystem",HttpStatus.CREATED);
    }

}