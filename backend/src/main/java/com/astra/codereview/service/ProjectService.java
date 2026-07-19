package com.astra.codereview.service;

import com.astra.codereview.dto.ProjectDto;
import com.astra.codereview.entity.Project;
import com.astra.codereview.entity.UploadType;
import com.astra.codereview.entity.User;
import com.astra.codereview.exception.ApiExceptions;
import com.astra.codereview.repository.ProjectRepository;
import com.astra.codereview.repository.UserRepository;
import com.astra.codereview.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileStorageUtil fileStorageUtil;

    @Transactional
    public ProjectDto upload(Long userId, String projectName, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("User not found"));

        FileStorageUtil.StoredProject stored = fileStorageUtil.store(userId, file);

        Project project = Project.builder()
                .user(user)
                .projectName((projectName == null || projectName.isBlank()) ? stored.originalName() : projectName)
                .uploadType(stored.wasZip() ? UploadType.ZIP_PROJECT : UploadType.SINGLE_FILE)
                .storagePath(stored.rootPath())
                .build();

        return ProjectDto.from(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> listForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("User not found"));
        return projectRepository.findByUserOrderByCreatedAtDesc(user).stream().map(ProjectDto::from).toList();
    }

    @Transactional(readOnly = true)
    public Project getOwned(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("Project not found"));
        if (!project.getUser().getId().equals(userId)) {
            throw new ApiExceptions.ResourceNotFoundException("Project not found");
        }
        // Touch the lazy collection now, while the transaction/session is open, so
        // callers building a DTO after this method returns don't hit a closed session.
        project.getReviews().size();
        return project;
    }
}
