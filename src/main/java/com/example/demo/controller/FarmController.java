package com.example.demo.controller;

import com.example.demo.dto.response.FarmDetailDto;
import com.example.demo.dto.response.FarmListDto;
import com.example.demo.dto.response.FarmSearchDto;
import com.example.demo.service.FarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/farm")
public class FarmController {
    private final FarmService farmService;

    // 전체 목록 조회
    @GetMapping
    public ResponseEntity<List<FarmListDto>> getAllFarms() {
        return ResponseEntity.ok(farmService.getAllFarms());
    }

    // 개별 상세 조회
    @GetMapping("/{farmId}")
    public ResponseEntity<FarmDetailDto> getFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(farmService.getFarmDetail(farmId));
    }

    // 제목 기반 검색
    @GetMapping(params = "title") // /farm?title=강남
    public ResponseEntity<List<FarmSearchDto>> searchByTitle(@RequestParam String title) {
        return ResponseEntity.ok(farmService.searchByTitle(title));
    }
}
