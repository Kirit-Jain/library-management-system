package com.library.management.service;

import com.library.management.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ============================================
    //          MOST BORROWED BOOKS
    // ============================================
    public byte[] exportMostBorrowedBooks(List<MostBorrowedBookResponse> books)
        throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Most Borrowed Books");
            sheet.setColumnWidth(0, 2000);
            sheet.setColumnWidth(1, 10000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 4000);

            CellStyle headerStyle = createHeaderStyle(workbook);

            // Title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Most Borrowed Books Report");
            CellStyle titleStyle = createTitleStyle(workbook);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

            // Header Row
            Row headerRow = sheet.createRow(2);
            createCell(headerRow, 0, "Rank", headerStyle);
            createCell(headerRow, 1, "Book Title", headerStyle);
            createCell(headerRow, 2, "ISBN", headerStyle);
            createCell(headerRow, 3, "Borrow Count", headerStyle);

            // Data Rows
            int rowNum = 3;
            for (MostBorrowedBookResponse book : books) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(book.getRank());
                row.createCell(1).setCellValue(book.getTitle());
                row.createCell(2).setCellValue(book.getIsbn());
                row.createCell(3).setCellValue(book.getBorrowCount());
            }

            return workbookToBytes(workbook);
        }
    }

    // ============================================
    //          OVERDUE REPORT
    // ============================================
    public byte[] exportOverdueReport(OverdueReportResponse report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Overdue Report");

            // Set column widths
            sheet.setColumnWidth(0, 2000);
            sheet.setColumnWidth(1, 5000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 8000);
            sheet.setColumnWidth(4, 8000);
            sheet.setColumnWidth(5, 4000);
            sheet.setColumnWidth(6, 4000);
            sheet.setColumnWidth(7, 3000);
            sheet.setColumnWidth(8, 3000);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle summaryStyle = createSummaryStyle(workbook);

            // Title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Overdue Books Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));

            // Summary
            Row summaryRow1 = sheet.createRow(2);
            Cell totalCell = summaryRow1.createCell(0);
            totalCell.setCellValue("Total Overdue: " + report.getTotalOverdueBorrowings());
            totalCell.setCellStyle(summaryStyle);

            Row summaryRow2 = sheet.createRow(3);
            Cell finesCell = summaryRow2.createCell(0);
            finesCell.setCellValue("Total Estimated Fines: ₹" + report.getTotalOverdueFines());
            finesCell.setCellStyle(summaryStyle);

            // Headers
            Row headerRow = sheet.createRow(5);
            createCell(headerRow, 0, "ID", headerStyle);
            createCell(headerRow, 1, "Member #", headerStyle);
            createCell(headerRow, 2, "Member Name", headerStyle);
            createCell(headerRow, 3, "Email", headerStyle);
            createCell(headerRow, 4, "Book Title", headerStyle);
            createCell(headerRow, 5, "Borrow Date", headerStyle);
            createCell(headerRow, 6, "Due Date", headerStyle);
            createCell(headerRow, 7, "Days Late", headerStyle);
            createCell(headerRow, 8, "Fine (₹)", headerStyle);

            // Data
            int rowNum = 6;
            for (OverdueReportResponse.OverdueItem item : report.getOverdueList()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getBorrowingId());
                row.createCell(1).setCellValue(item.getMembershipNumber());
                row.createCell(2).setCellValue(item.getMemberName());
                row.createCell(3).setCellValue(item.getMemberEmail());
                row.createCell(4).setCellValue(item.getBookTitle());
                row.createCell(5).setCellValue(item.getBorrowDate().format(DATE_FORMATTER));
                row.createCell(6).setCellValue(item.getDueDate().format(DATE_FORMATTER));
                row.createCell(7).setCellValue(item.getOverdueDays());
                row.createCell(8).setCellValue(item.getEstimatedFine().doubleValue());
            }

            return workbookToBytes(workbook);
        }
    }

    // ============================================
    //          ACTIVE MEMBERS
    // ============================================
    public byte[] exportActiveMembers(List<ActiveMemberResponse> members) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Active Members");
            sheet.setColumnWidth(0, 2000);
            sheet.setColumnWidth(1, 4500);
            sheet.setColumnWidth(2, 6000);
            sheet.setColumnWidth(3, 7000);
            sheet.setColumnWidth(4, 4000);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Active Members Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));

            Row headerRow = sheet.createRow(2);
            createCell(headerRow, 0, "Rank", headerStyle);
            createCell(headerRow, 1, "Member #", headerStyle);
            createCell(headerRow, 2, "Name", headerStyle);
            createCell(headerRow, 3, "Email", headerStyle);
            createCell(headerRow, 4, "Books Borrowed", headerStyle);

            int rowNum = 3;
            for (ActiveMemberResponse m : members) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(m.getRank());
                row.createCell(1).setCellValue(m.getMembershipNumber());
                row.createCell(2).setCellValue(m.getFullName());
                row.createCell(3).setCellValue(m.getEmail());
                row.createCell(4).setCellValue(m.getBorrowCount());
            }

            return workbookToBytes(workbook);
        }
    }

    // ============================================
    //          FINE COLLECTION
    // ============================================
    public byte[] exportFineCollection(FineCollectionReportResponse report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fine Collection");
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 4000);
            sheet.setColumnWidth(2, 4000);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle summaryStyle = createSummaryStyle(workbook);

            // Title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Fine Collection Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

            // Summary
            Row summary = sheet.createRow(2);
            Cell sumCell = summary.createCell(0);
            sumCell.setCellValue("Period: " + report.getStartDate() + " to " + report.getEndDate());
            sumCell.setCellStyle(summaryStyle);

            Row total = sheet.createRow(3);
            Cell totalCell = total.createCell(0);
            totalCell.setCellValue("Total Collected: ₹" + report.getTotalCollected());
            totalCell.setCellStyle(summaryStyle);

            // Headers
            Row headerRow = sheet.createRow(5);
            createCell(headerRow, 0, "Date", headerStyle);
            createCell(headerRow, 1, "Amount (₹)", headerStyle);
            createCell(headerRow, 2, "Payment Count", headerStyle);

            // Data
            int rowNum = 6;
            for (var daily : report.getDailyCollections()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(daily.getDate().format(DATE_FORMATTER));
                row.createCell(1).setCellValue(daily.getAmount().doubleValue());
                row.createCell(2).setCellValue(daily.getPaymentCount());
            }

            return workbookToBytes(workbook);
        }
    }

    // ============================================
    //          STYLES & HELPERS
    // ============================================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createSummaryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private byte[] workbookToBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}