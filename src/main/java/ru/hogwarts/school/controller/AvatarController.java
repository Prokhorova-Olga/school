package ru.hogwarts.school.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;


@RestController
@RequestMapping("avatar")
public class AvatarController {

    private final AvatarService avatarService;

    @Autowired
    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping(value = "/{studentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(
            @PathVariable Long studentId,
            @RequestParam MultipartFile avatar) throws IOException {
        avatarService.uploadAvatar(studentId, avatar);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{studentId}/preview")
    public ResponseEntity<byte[]> downloadAvatarPreview(@PathVariable Long studentId) {
        Optional<Avatar> optionalAvatar = avatarService.findAvatar(studentId);
        if (optionalAvatar.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Avatar avatar = optionalAvatar.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(avatar.getMediaType()));
        headers.setContentLength(avatar.getData().length);
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(avatar.getData());
    }

    @GetMapping(value = "/{studentId}")
    public void downLoadAvatar(@PathVariable Long studentId, HttpServletResponse response) throws IOException {
        Optional<Avatar> optionalAvatar = avatarService.findAvatar(studentId);
        if (optionalAvatar.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Avatar avatar = optionalAvatar.get();
        Path path = Path.of(avatar.getFilePath());

        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream()) {
            response.setContentType(avatar.getMediaType());
            response.setContentLength((int) avatar.getFileSize());
            is.transferTo(os);


        }
    }

}
