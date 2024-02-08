package ru.seloid.pdf.merger.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.seloid.pdf.merger.service.Merger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/merger")
public class MergerRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MergerRestController.class);

    @PostMapping(value = "/merge", produces = MediaType.APPLICATION_PDF_VALUE)
    public Resource merge(@RequestParam("files") final MultipartFile[] files) throws Exception {
        final List<InputStream> inputStreams = Arrays.stream(files).map(file -> {
            try {
                if (file.getOriginalFilename() == null || !file.getOriginalFilename().contains(".pdf")) {
                    throw new RuntimeException("Unsupported file given");
                }
                return file.getInputStream();
            } catch (IOException e) {
                LOGGER.debug("Error: ", e);
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        return new ByteArrayResource(Merger.mergeStreams(inputStreams));
    }
}
