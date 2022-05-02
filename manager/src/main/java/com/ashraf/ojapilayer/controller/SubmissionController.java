package com.ashraf.ojapilayer.controller;

import com.ashraf.ojapilayer.api.requestmodels.FilterQueryRequest;
import com.ashraf.ojapilayer.api.requestmodels.SubmissionRequest;
import com.ashraf.ojapilayer.mapper.SubmissionMapper;
import com.ashraf.ojapilayer.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissionService;
    private final SubmissionMapper submissionMapper;

    @PostMapping("/submit")
    public ResponseEntity<?> submitSolution(@RequestPart("file") MultipartFile file,
                                            @RequestPart("metaData")SubmissionRequest metaData) {
        metaData.setFile(file);
        return ResponseEntity.ok(submissionMapper.submissionToSubmissionDTO(
                submissionService.submitSolution(metaData)));

    }

    @GetMapping("/submission/{submissionId}/status")
    public ResponseEntity<?> getSubmissionStatus(@PathVariable("submissionId") String submissionId) {
        return ResponseEntity.ok(submissionMapper.submissionToSubmissionDTO(
                submissionService.getSubmissionById(submissionId).orElse(null)));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllSubmissions(
            @RequestParam(name = "page", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "size", defaultValue = "40") Integer size) {
        return ResponseEntity.ok(submissionService.getAllSubmissionsForPage(pageNumber, size));
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getSubmissionsByFilter(
            @RequestParam("query")String queryString,
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "size", defaultValue = "40") Integer size
            ) {
        FilterQueryRequest filterQueryRequest = FilterQueryRequest.builder().query(queryString)
                .pageNumber(pageNumber).size(size).build();
        return ResponseEntity.ok(submissionService.getSubmissionByFilter(filterQueryRequest));
    }
}
