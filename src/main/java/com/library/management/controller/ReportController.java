package com.library.management.controller;

import com.library.management.dto.response.*;
import com.library.management.service.ExcelExportService;
import com.library.management.service.PdfExportService;
import com.library.management.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reports and analytics")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public class ReportController {

    private final ReportService reportService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    // ============================================
    //          MOST BORROWED BOOKS
    // ============================================
    @GetMapping("/most-borrowed")
    @Operation(summary = "Get most borrowed books")
    public ResponseEntity<ApiResponse<?>> mostBorrowed(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
            "fetched most borrowed books report",
            reportService.getMostBorrowedBooks(startDate, endDate, limit)));
    }

    @GetMapping("/most-borrowed/export/excel")
    @Operation(summary = "Export most borrowed books as Excel")
    public ResponseEntity<byte[]> exportMostBorrowedExcel(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam(defaultValue = "20") int limit) throws IOException {

        var books = reportService.getMostBorrowedBooks(startDate, endDate, limit);
        byte[] data = excelExportService.exportMostBorrowedBooks(books);

        return downloadResponse(data,
            "most-borrowed-books.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/most-borrowed/export/pdf")
    @Operation(summary = "Export most borrowed books as PDF")
    public ResponseEntity<byte[]> exportMostBorrowedPdf(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam(defaultValue = "20") int limit) throws IOException {

        var books = reportService.getMostBorrowedBooks(startDate, endDate, limit);
        byte[] data = pdfExportService.exportMostBorrowedBooks(books);

        return downloadResponse(data, "most-borrowed-books.pdf", "application/pdf");
    }

    // ============================================
    //          OVERDUE REPORT
    // ============================================
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue books report")
    public ResponseEntity<ApiResponse<OverdueReportResponse>> overdueReport() {
        return ResponseEntity.ok(ApiResponse.success("fetched overdue report", reportService.getOverdueReport()));
    }

    @GetMapping("/overdue/export/excel")
    @Operation(summary = "Export overdue report as Excel")
    public ResponseEntity<byte[]> exportOverdueExcel() throws IOException {
        var report = reportService.getOverdueReport();
        byte[] data = excelExportService.exportOverdueReport(report);
        return downloadResponse(data, "overdue-report.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/overdue/export/pdf")
    @Operation(summary = "Export overdue report as PDF")
    public ResponseEntity<byte[]> exportOverduePdf() throws IOException {
        var report = reportService.getOverdueReport();
        byte[] data = pdfExportService.exportOverdueReport(report);
        return downloadResponse(data, "overdue-report.pdf", "application/pdf");
    }

    // ============================================
    //          ACTIVE MEMBERS
    // ============================================
    @GetMapping("/active-members")
    @Operation(summary = "Get most active members")
    public ResponseEntity<ApiResponse<?>> activeMembers(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
            "fetched active members report",
            reportService.getActiveMembers(startDate, endDate, limit)));
    }

    @GetMapping("/active-members/export/excel")
    public ResponseEntity<byte[]> exportActiveMembersExcel(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam(defaultValue = "20") int limit) throws IOException {

        var members = reportService.getActiveMembers(startDate, endDate, limit);
        byte[] data = excelExportService.exportActiveMembers(members);
        return downloadResponse(data, "active-members.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/active-members/export/pdf")
    public ResponseEntity<byte[]> exportActiveMembersPdf(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam(defaultValue = "20") int limit) throws IOException {

        var members = reportService.getActiveMembers(startDate, endDate, limit);
        byte[] data = pdfExportService.exportActiveMembers(members);
        return downloadResponse(data, "active-members.pdf", "application/pdf");
    }

    // ============================================
    //          FINE COLLECTION
    // ============================================
    @GetMapping("/fine-collection")
    @Operation(summary = "Get fine collection report")
    public ResponseEntity<ApiResponse<FineCollectionReportResponse>> fineCollection(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
            "fetched fine collection report",
            reportService.getFineCollectionReport(startDate, endDate)));
    }

    @GetMapping("/fine-collection/export/excel")
    public ResponseEntity<byte[]> exportFineCollectionExcel(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate) throws IOException {

        var report = reportService.getFineCollectionReport(startDate, endDate);
        byte[] data = excelExportService.exportFineCollection(report);
        return downloadResponse(data, "fine-collection.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    // ============================================
    //          INVENTORY
    // ============================================
    @GetMapping("/inventory")
    @Operation(summary = "Get inventory report")
    public ResponseEntity<ApiResponse<InventoryReportResponse>> inventory() {
        return ResponseEntity.ok(ApiResponse.success("fetched inventory report", reportService.getInventoryReport()));
    }

    // ============================================
    //          HELPER
    // ============================================
    private ResponseEntity<byte[]> downloadResponse(byte[] data,
                                                     String fileName,
                                                     String mimeType) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.parseMediaType(mimeType))
            .body(data);
    }
}