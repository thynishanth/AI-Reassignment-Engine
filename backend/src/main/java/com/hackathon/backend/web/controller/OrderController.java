package com.hackathon.backend.web.controller;

import com.hackathon.backend.domain.enums.OrderStatus;
import com.hackathon.backend.service.OrderService;
import com.hackathon.backend.web.dto.request.CreateOrderRequest;
import com.hackathon.backend.web.dto.response.OrderResponse;
import com.hackathon.backend.web.dto.response.ReassignmentSuggestionResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping
    public List<OrderResponse> listOrders(@RequestParam(required = false) OrderStatus status) {
        return orderService.listOrders(status);
    }

    @PostMapping("/{id}/suggest")
    public ReassignmentSuggestionResponse suggest(@PathVariable String id) {
        return orderService.suggest(id);
    }
}
