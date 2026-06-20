package com.videoapp.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "file.upload.path=target/test-uploads")
@SuppressWarnings("null")
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final Path UPLOAD_DIR = Paths.get("target/test-uploads");

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(UPLOAD_DIR);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(UPLOAD_DIR)) {
            Files.walk(UPLOAD_DIR)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (IOException ignored) {}
                    });
        }
    }

    // --- POST /api/files/upload ---

    @Test
    void uploadFile_image_returnsUrlAndMediaTypeImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(startsWith("/api/files/")))
                .andExpect(jsonPath("$.mediaType").value("image"))
                .andExpect(jsonPath("$.originalName").value("photo.jpg"));
    }

    @Test
    void uploadFile_video_returnsMediaTypeVideo() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "clip.mp4", "video/mp4", "fake-video-content".getBytes());

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaType").value("video"));
    }

    @Test
    void uploadFile_pdf_returnsMediaTypeFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "fake-pdf-content".getBytes());

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaType").value("file"));
    }

    @Test
    void uploadFile_noExtension_returnsMediaTypeFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "datafile", "application/octet-stream", "binary".getBytes());

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaType").value("file"));
    }

    @Test
    void uploadFile_imageByExtension_returnsImageType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "picture.png", "application/octet-stream", "data".getBytes());

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaType").value("image"));
    }

    @Test
    void uploadFile_videoByExtension_returnsVideoType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "movie.avi", "application/octet-stream", "data".getBytes());

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaType").value("video"));
    }

    @Test
    void uploadFile_urlContainsUuidPattern() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(matchesPattern("/api/files/[a-f0-9\\-]{36}\\.jpg")));
    }

    // --- GET /api/files/{filename} ---

    @Test
    void serveFile_existingFile_returnsFileContent() throws Exception {
        byte[] content = "hello world".getBytes();
        Path filePath = UPLOAD_DIR.resolve("test.txt");
        Files.write(filePath, content);

        mockMvc.perform(get("/api/files/test.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("inline")));
    }

    @Test
    void serveFile_withDownloadParam_setsAttachmentDisposition() throws Exception {
        byte[] content = "download me".getBytes();
        Files.write(UPLOAD_DIR.resolve("download.txt"), content);

        mockMvc.perform(get("/api/files/download.txt").param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")));
    }

    @Test
    void serveFile_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/files/nonexistent.txt"))
                .andExpect(status().isNotFound());
    }

    @Test
    void serveFile_setsAccessControlAllowOriginHeader() throws Exception {
        Files.write(UPLOAD_DIR.resolve("cors.txt"), "data".getBytes());

        mockMvc.perform(get("/api/files/cors.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
}
