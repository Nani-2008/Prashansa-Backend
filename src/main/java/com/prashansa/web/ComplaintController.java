package com.prashansa.web;

import com.prashansa.dto.ComplaintDto;
import com.prashansa.dto.ComplaintPatchRequest;
import com.prashansa.dto.CreateComplaintRequest;
import com.prashansa.entity.AppUser;
import com.prashansa.service.ComplaintService;
import com.prashansa.storage.FileStorageService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final FileStorageService fileStorageService;

    public ComplaintController(ComplaintService complaintService, FileStorageService fileStorageService) {
        this.complaintService = complaintService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public List<ComplaintDto> list(@RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user) {
        return complaintService.listForUser(user);
    }

    @PostMapping
    public ComplaintDto create(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user,
            @RequestBody CreateComplaintRequest body) {
        try {
            return complaintService.create(user, body);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ComplaintDto patch(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user,
            @RequestBody ComplaintPatchRequest body) {
        return complaintService.patch(id, user, body);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public void delete(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user) {
        complaintService.delete(id, user);
    }

    @PostMapping("/upload")
    public Map<String, String> uploadEvidence(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user,
            @RequestParam("file") MultipartFile file) {
        String url = fileStorageService.storeMedia(file);
        return Map.of("url", url);
    }

    @PostMapping("/live-location")
    public void updateLiveLocation(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user,
            @RequestParam("location") String location) {
        complaintService.updateLiveLocation(user, location);
    }
}
