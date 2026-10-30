package com.kiranalink.controller;

import com.kiranalink.dto.groupbuy.GroupBuyCreateRequest;
import com.kiranalink.dto.groupbuy.GroupBuyJoinRequest;
import com.kiranalink.entity.GroupBuy;
import com.kiranalink.service.GroupBuyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-buy")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;

    @GetMapping
    public ResponseEntity<List<GroupBuy>> getDeals(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radius) {
        List<GroupBuy> deals = groupBuyService.getDeals(latitude, longitude, radius);
        return ResponseEntity.ok(deals);
    }

    @PostMapping
    public ResponseEntity<GroupBuy> createDeal(@Valid @RequestBody GroupBuyCreateRequest request) {
        GroupBuy deal = groupBuyService.createDeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(deal);
    }

    @PatchMapping("/{id}/join")
    public ResponseEntity<GroupBuy> joinDeal(
            @PathVariable Long id,
            @Valid @RequestBody GroupBuyJoinRequest request) {
        GroupBuy deal = groupBuyService.joinDeal(id, request);
        return ResponseEntity.ok(deal);
    }
}
