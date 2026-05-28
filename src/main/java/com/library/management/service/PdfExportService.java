package com.library.management.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.library.management.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    @Value("${library.report.company-name}")
    private String companyName;

    @Value("${library.report.company-address}")
    private String companyAddress;

    @Value("${library.report.company-phone}")
    private String companyPhone;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ============================================
    //          MOST BORROWED BOOKS
    // ============================================
    public byte[] exportMostBorrowedBooks(List<MostBorrowedBookResponse> books)
        throws IOException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc, PageSize.A4)) {

            PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");
            PdfFont regular = PdfFontFactory.createFont("Helvetica");

            addHeader(doc, bold, regular, "Most Borrowed Books Report");

            // Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 5, 2, 2}))
                .useAllAvailableWidth();

            addTableHeader(table, bold, "Rank", "Book Title", "ISBN", "Borrow Count");

            for (MostBorrowedBookResponse book : books) {
                addTableCell(table, regular, String.valueOf(book.getRank()));
                addTableCell(table, regular, book.getTitle());
                addTableCell(table, regular, book.getIsbn());
                addTableCell(table, regular, String.valueOf(book.getBorrowCount()));
            }

            doc.add(table);
            addFooter(doc, regular);

            doc.close();
            return baos.toByteArray();
        }
    }

    // ============================================
    //          OVERDUE REPORT
    // ============================================
    public byte[] exportOverdueReport(OverdueReportResponse report) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc, PageSize.A4.rotate())) {

            PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");
            PdfFont regular = PdfFontFactory.createFont("Helvetica");

            addHeader(doc, bold, regular, "Overdue Books Report");

            // Summary box
            Table summary = new Table(2).useAllAvailableWidth();
            summary.addCell(createSummaryCell("Total Overdue:", bold));
            summary.addCell(createSummaryCell(
                String.valueOf(report.getTotalOverdueBorrowings()), regular));
            summary.addCell(createSummaryCell("Total Estimated Fines:", bold));
            summary.addCell(createSummaryCell(
                "₹" + report.getTotalOverdueFines(), regular));

            doc.add(summary);
            doc.add(new Paragraph("\n"));

            // Main table
            Table table = new Table(
                UnitValue.createPercentArray(new float[]{1, 2, 2.5f, 3, 2.5f, 1.5f, 1.5f, 1, 1.5f}))
                .useAllAvailableWidth();

            addTableHeader(table, bold, "ID", "Member #", "Name", "Email",
                "Book", "Borrow Date", "Due Date", "Days Late", "Fine (₹)");

            for (OverdueReportResponse.OverdueItem item : report.getOverdueList()) {
                addTableCell(table, regular, String.valueOf(item.getBorrowingId()));
                addTableCell(table, regular, item.getMembershipNumber());
                addTableCell(table, regular, item.getMemberName());
                addTableCell(table, regular, item.getMemberEmail());
                addTableCell(table, regular, item.getBookTitle());
                addTableCell(table, regular, item.getBorrowDate().format(DATE_FORMATTER));
                addTableCell(table, regular, item.getDueDate().format(DATE_FORMATTER));
                addTableCell(table, regular, String.valueOf(item.getOverdueDays()));
                addTableCell(table, regular, "₹" + item.getEstimatedFine());
            }

            doc.add(table);
            addFooter(doc, regular);

            doc.close();
            return baos.toByteArray();
        }
    }

    // ============================================
    //          ACTIVE MEMBERS
    // ============================================
    public byte[] exportActiveMembers(List<ActiveMemberResponse> members) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc, PageSize.A4)) {

            PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");
            PdfFont regular = PdfFontFactory.createFont("Helvetica");

            addHeader(doc, bold, regular, "Active Members Report");

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 3, 3, 2}))
                .useAllAvailableWidth();

            addTableHeader(table, bold, "Rank", "Member #", "Name", "Email", "Borrowed");

            for (ActiveMemberResponse m : members) {
                addTableCell(table, regular, String.valueOf(m.getRank()));
                addTableCell(table, regular, m.getMembershipNumber());
                addTableCell(table, regular, m.getFullName());
                addTableCell(table, regular, m.getEmail());
                addTableCell(table, regular, String.valueOf(m.getBorrowCount()));
            }

            doc.add(table);
            addFooter(doc, regular);

            doc.close();
            return baos.toByteArray();
        }
    }

    // ============================================
    //          HEADER & FOOTER HELPERS
    // ============================================

    private void addHeader(Document doc, PdfFont bold, PdfFont regular, String title) {
        // Company name
        Paragraph company = new Paragraph(companyName)
            .setFont(bold)
            .setFontSize(18)
            .setFontColor(new DeviceRgb(0, 51, 102))
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(company);

        Paragraph address = new Paragraph(companyAddress + " | " + companyPhone)
            .setFont(regular)
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(address);

        // Separator line
        doc.add(new Paragraph("_____________________________________________")
            .setTextAlignment(TextAlignment.CENTER));

        // Title
        Paragraph titlePara = new Paragraph(title)
            .setFont(bold)
            .setFontSize(14)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10);
        doc.add(titlePara);

        // Generated date
        Paragraph generated = new Paragraph(
            "Generated: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
            .setFont(regular)
            .setFontSize(9)
            .setTextAlignment(TextAlignment.RIGHT);
        doc.add(generated);
        doc.add(new Paragraph("\n"));
    }

    private void addFooter(Document doc, PdfFont regular) {
        doc.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph(
            "© " + companyName + " - Library Management System")
            .setFont(regular)
            .setFontSize(9)
            .setFontColor(ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(footer);
    }

    private void addTableHeader(Table table, PdfFont bold, String... headers) {
        for (String header : headers) {
            Cell cell = new Cell()
                .add(new Paragraph(header).setFont(bold).setFontSize(10))
                .setBackgroundColor(new DeviceRgb(0, 51, 102))
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
            table.addHeaderCell(cell);
        }
    }

    private void addTableCell(Table table, PdfFont regular, String value) {
        Cell cell = new Cell()
            .add(new Paragraph(value == null ? "" : value)
                .setFont(regular)
                .setFontSize(9))
            .setPadding(5);
        table.addCell(cell);
    }

    private Cell createSummaryCell(String value, PdfFont font) {
        return new Cell()
            .add(new Paragraph(value).setFont(font).setFontSize(11))
            .setBorder(Border.NO_BORDER)
            .setPadding(3);
    }
}