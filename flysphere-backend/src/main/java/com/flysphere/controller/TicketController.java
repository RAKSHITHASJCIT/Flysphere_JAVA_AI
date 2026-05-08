package com.flysphere.controller;

import com.flysphere.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{bookingId}/pdf")
    public ResponseEntity<byte[]> downloadTicket(
            @PathVariable String bookingId) {

        byte[] pdfBytes = ticketService.generateTicketPdf(bookingId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + bookingId + "_Eticket.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdfBytes);
    }
}
