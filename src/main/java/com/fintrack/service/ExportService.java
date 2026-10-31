
package com.fintrack.service;

import com.fintrack.dto.dashboard.DashboardResponse;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ExportService {

    private final DashboardService dashboardService;

    public ExportService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public byte[] generatePdf(
            String email,
            LocalDate fromDate,
            LocalDate toDate) throws IOException {

        DashboardResponse dashboard =
                dashboardService.getDashboard(email, fromDate, toDate);

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);

            try {
                PdfWriter.getInstance(document, outputStream);
                document.open();

                Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
                Font headingFont = new Font(Font.HELVETICA, 13, Font.BOLD);
                Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

                Paragraph title =
                        new Paragraph("FinTrack Financial Report", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                document.add(new Paragraph(" "));
                document.add(new Paragraph(
                        "From Date: " + fromDate, normalFont));
                document.add(new Paragraph(
                        "To Date: " + toDate, normalFont));
                document.add(new Paragraph(" "));

                // Financial Summary
                document.add(new Paragraph("Financial Summary", headingFont));
                document.add(new Paragraph(" "));

                PdfPTable summaryTable = new PdfPTable(2);
                summaryTable.setWidthPercentage(100);
                summaryTable.setWidths(new float[]{3, 2});

                addPdfCell(summaryTable, "Metric");
                addPdfCell(summaryTable, "Amount (INR)");

                addPdfCell(summaryTable, "Total Income");
                addPdfCell(summaryTable, formatAmount(dashboard.totalIncome()));

                addPdfCell(summaryTable, "Total Expenses");
                addPdfCell(summaryTable, formatAmount(dashboard.totalExpenses()));

                addPdfCell(summaryTable, "Net Cash Flow");
                addPdfCell(summaryTable, formatAmount(dashboard.netCashFlow()));

                addPdfCell(summaryTable, "Current Account Balance");
                addPdfCell(summaryTable, formatAmount(dashboard.totalBalance()));

                document.add(summaryTable);
                document.add(new Paragraph(" "));

                // Category-wise Expenses
                document.add(new Paragraph(
                        "Category-wise Expenses", headingFont));
                document.add(new Paragraph(" "));

                PdfPTable categoryTable = new PdfPTable(2);
                categoryTable.setWidthPercentage(100);
                categoryTable.setWidths(new float[]{3, 2});

                addPdfCell(categoryTable, "Category");
                addPdfCell(categoryTable, "Total Expense (INR)");

                dashboard.categoryWiseExpenses().forEach(category -> {
                    addPdfCell(categoryTable, category.categoryName());
                    addPdfCell(
                            categoryTable,
                            formatAmount(category.totalExpense()));
                });

                document.add(categoryTable);
                document.add(new Paragraph(" "));

                // Daily Summary
                document.add(new Paragraph("Daily Summary", headingFont));
                document.add(new Paragraph(" "));

                PdfPTable dailyTable = new PdfPTable(3);
                dailyTable.setWidthPercentage(100);
                dailyTable.setWidths(new float[]{2, 2, 2});

                addPdfCell(dailyTable, "Date");
                addPdfCell(dailyTable, "Income (INR)");
                addPdfCell(dailyTable, "Expense (INR)");

                dashboard.dailySummary().forEach(day -> {
                    addPdfCell(dailyTable, day.date().toString());
                    addPdfCell(dailyTable, formatAmount(day.income()));
                    addPdfCell(dailyTable, formatAmount(day.expense()));
                });

                document.add(dailyTable);
                document.close();

                return outputStream.toByteArray();

            } catch (DocumentException exception) {
                throw new IOException("Could not generate PDF report", exception);
            } finally {
                if (document.isOpen()) {
                    document.close();
                }
            }
        }
    }

    public byte[] generateExcel(
            String email,
            LocalDate fromDate,
            LocalDate toDate) throws IOException {

        DashboardResponse dashboard =
                dashboardService.getDashboard(email, fromDate, toDate);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Sheet summary = workbook.createSheet("Financial Summary");

            int rowIndex = 0;

            Row titleRow = summary.createRow(rowIndex++);
            titleRow.createCell(0)
                    .setCellValue("FinTrack Financial Summary");

            Row fromRow = summary.createRow(rowIndex++);
            fromRow.createCell(0).setCellValue("From Date");
            fromRow.createCell(1).setCellValue(fromDate.toString());

            Row toRow = summary.createRow(rowIndex++);
            toRow.createCell(0).setCellValue("To Date");
            toRow.createCell(1).setCellValue(toDate.toString());

            rowIndex++;

            Row header = summary.createRow(rowIndex++);
            header.createCell(0).setCellValue("Metric");
            header.createCell(1).setCellValue("Amount (INR)");

            addAmountRow(summary, rowIndex++, "Total Income",
                    dashboard.totalIncome());
            addAmountRow(summary, rowIndex++, "Total Expenses",
                    dashboard.totalExpenses());
            addAmountRow(summary, rowIndex++, "Net Cash Flow",
                    dashboard.netCashFlow());
            addAmountRow(summary, rowIndex++, "Current Account Balance",
                    dashboard.totalBalance());

            summary.autoSizeColumn(0);
            summary.autoSizeColumn(1);

            Sheet categories = workbook.createSheet("Category Expenses");

            Row categoryHeader = categories.createRow(0);
            categoryHeader.createCell(0).setCellValue("Category");
            categoryHeader.createCell(1).setCellValue("Total Expense (INR)");

            int categoryRowIndex = 1;

            for (var category : dashboard.categoryWiseExpenses()) {
                Row row = categories.createRow(categoryRowIndex++);
                row.createCell(0).setCellValue(category.categoryName());
                setNumericCell(row.createCell(1), category.totalExpense());
            }

            categories.autoSizeColumn(0);
            categories.autoSizeColumn(1);

            Sheet daily = workbook.createSheet("Daily Summary");

            Row dailyHeader = daily.createRow(0);
            dailyHeader.createCell(0).setCellValue("Date");
            dailyHeader.createCell(1).setCellValue("Income (INR)");
            dailyHeader.createCell(2).setCellValue("Expense (INR)");

            int dailyRowIndex = 1;

            for (var day : dashboard.dailySummary()) {
                Row row = daily.createRow(dailyRowIndex++);
                row.createCell(0).setCellValue(day.date().toString());
                setNumericCell(row.createCell(1), day.income());
                setNumericCell(row.createCell(2), day.expense());
            }

            daily.autoSizeColumn(0);
            daily.autoSizeColumn(1);
            daily.autoSizeColumn(2);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void addPdfCell(PdfPTable table, String value) {
        table.addCell(new Phrase(value == null ? "" : value));
    }

    private String formatAmount(BigDecimal amount) {
        return (amount == null ? BigDecimal.ZERO : amount).toPlainString();
    }

    private void addAmountRow(
            Sheet sheet,
            int rowIndex,
            String label,
            BigDecimal amount) {

        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(label);
        setNumericCell(row.createCell(1), amount);
    }

    private void setNumericCell(Cell cell, BigDecimal amount) {
        cell.setCellValue(
                amount == null ? 0.0 : amount.doubleValue()
        );
    }
}
