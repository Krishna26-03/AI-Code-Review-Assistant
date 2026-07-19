package com.astra.codereview.controller;

import com.astra.codereview.dto.ProjectDto;
import com.astra.codereview.security.AppUserPrincipal;
import com.astra.codereview.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ProjectDto> upload(@AuthenticationPrincipal AppUserPrincipal principal,
                                              @RequestParam(value = "projectName", required = false) String projectName,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        ProjectDto dto = projectService.upload(principal.getId(), projectName, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(projectService.listForUser(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> get(@AuthenticationPrincipal AppUserPrincipal principal,
                                           @PathVariable Long id) {
        return ResponseEntity.ok(ProjectDto.from(projectService.getOwned(id, principal.getId())));
    }
}
