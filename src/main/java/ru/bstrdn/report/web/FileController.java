package ru.bstrdn.report.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.bstrdn.report.configurations.MyUserDetails;
import ru.bstrdn.report.fireBird.repository.JdbcBuhRepository;
import ru.bstrdn.report.fireBird.service.FileService;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@AllArgsConstructor
@Controller
public class FileController {
    private static final String FILE_BASE_PATH = "C:/Portal/";
    private static final String CONTENT_TYPE = "application/octet-stream";

    @Autowired
    FileService fileService;
    @Autowired
    JdbcBuhRepository buhRepository;


    @PostMapping("/download")
    public ResponseEntity downloadFileFromLocal2(@AuthenticationPrincipal MyUserDetails user,
                                                 @RequestParam @Nullable String startDate,
                                                 @RequestParam @Nullable String endDate,
                                                 @RequestParam(defaultValue = "0") Integer legalEntitiesWithId) throws Exception {

        int userId = user.getDcode();
        String generatedFile = fileService.generateAktSverki(startDate, endDate, legalEntitiesWithId, userId);
        Path path = Paths.get(FILE_BASE_PATH + generatedFile);
        Resource resource = null;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String URLEncodedFileName = URLEncoder.encode(resource.getFilename(), "UTF-8");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                                                         + URLEncodedFileName + "\"")
                .body(resource);
    }
}
