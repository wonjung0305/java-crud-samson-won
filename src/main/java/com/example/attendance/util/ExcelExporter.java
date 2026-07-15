package com.example.attendance.util;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    // 내보낼 한 줄(학번, 이름, 오운완 인증 횟수, 정모 참여 여부, 벌금, 인증 날짜 텍스트)
    public record ExportRow(String studentId, String name, int workoutCount, boolean attended, int fine, String workoutDatesText) {}

    private static final byte[] GRAY_HEADER = {(byte) 0xD9, (byte) 0xD9, (byte) 0xD9};
    private static final byte[] GRAY_STRIPE = {(byte) 0xF2, (byte) 0xF2, (byte) 0xF2};

    // 주차별 출석부를 .xlsx로 저장 (줄무늬 배경색 + 테두리 적용)
    public static void exportWeeklyAttendance(String filePath, String semester, int week, List<ExportRow> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(semester + " " + week + "주차");

            CellStyle headerStyle = createColorStyle(workbook, GRAY_HEADER, true);
            CellStyle evenStyle = createColorStyle(workbook, null, false);          // 흰색(기본 배경)
            CellStyle oddStyle = createColorStyle(workbook, GRAY_STRIPE, false);    // 연한 회색

            String[] headers = {"학번", "이름", "오운완", "정모", "벌금", "인증일"};
            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < headers.length; c++) {
                Cell cell = headerRow.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < rows.size(); i++) {
                ExportRow r = rows.get(i);
                Row row = sheet.createRow(i + 1);
                CellStyle style = (i % 2 == 0) ? evenStyle : oddStyle;   // 붙어있는 행끼리 색 다르게

                int effectiveWorkout = r.workoutCount() + (r.attended() ? 1 : 0);

                setCell(row, 0, r.studentId(), style);
                setCell(row, 1, r.name(), style);
                setCell(row, 2, r.workoutCount() + "(" + effectiveWorkout + ")", style);
                setCell(row, 3, r.attended() ? "참석" : "불참", style);
                setCell(row, 4, r.fine(), style);
                setCell(row, 5, r.workoutDatesText(), style);
            }

            sheet.setColumnWidth(0, 12 * 256);
            sheet.setColumnWidth(1, 10 * 256);
            sheet.setColumnWidth(2, 22 * 256);
            sheet.setColumnWidth(3, 8 * 256);
            sheet.setColumnWidth(4, 12 * 256);
            sheet.setColumnWidth(5, 120 * 256);   // 마지막 컬럼이라 넉넉하게 최대로 잡아서 안 튀어나오게

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    private static void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void setCell(Row row, int col, int value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    // rgb가 null이면 배경색 없이(흰색) 테두리/폰트만 적용
    private static CellStyle createColorStyle(Workbook workbook, byte[] rgb, boolean bold) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();

        if (bold) {
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
        }

        if (rgb != null) {
            style.setFillForegroundColor(new XSSFColor(rgb, null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        applyBorder(style);
        return style;
    }

    private static void applyBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
