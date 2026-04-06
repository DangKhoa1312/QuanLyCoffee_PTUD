package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import dto.CartItem;
import dto.CartItem.CartTopping;
import entity.HoaDon;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PDFPrinter {

    private static final String FONT_PATH = "C:\\Windows\\Fonts\\arial.ttf";

    public static String exportBill(HoaDon hoaDon, List<CartItem> cartItems) throws Exception {
        // Create exports dir if not exists
        File exportDir = new File("exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        String fileName = "exports/HoaDon_" + hoaDon.getMaHD() + ".pdf";
        // Khổ giấy in nhiệt K80 (80mm ~ 226pt), chiều dài linh hoạt
        Rectangle pageSize = new Rectangle(226f, 500f);
        Document document = new Document(pageSize, 10f, 10f, 15f, 15f);
        PdfWriter.getInstance(document, new FileOutputStream(fileName));

        document.open();

        // Cài đặt font tiếng Viêt
        BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fontTitle = new Font(bf, 14, Font.BOLD);
        Font fontBold = new Font(bf, 10, Font.BOLD);
        Font fontNormal = new Font(bf, 9, Font.NORMAL);
        Font fontSmall = new Font(bf, 8, Font.NORMAL);
        Font fontItalicSmall = new Font(bf, 8, Font.ITALIC);

        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

        // --- Header ---
        Paragraph pTitle = new Paragraph("COFFEE 11:01", fontTitle);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(pTitle);

        Paragraph pAddress = new Paragraph("123 \u0110\u01B0\u1EDDng ABC, Qu\u1EADn 1, TP.HCM", fontSmall);
        pAddress.setAlignment(Element.ALIGN_CENTER);
        document.add(pAddress);

        Paragraph pDashes = new Paragraph("--------------------------------------------------------------", fontNormal);
        pDashes.setAlignment(Element.ALIGN_CENTER);
        document.add(pDashes);

        Paragraph pInfo = new Paragraph("H\u00D3A \u0110\u01A0N THANH TO\u00C1N", fontBold);
        pInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(pInfo);
        document.add(new Paragraph(" ", fontSmall)); // Spacing

        // --- Thông tin hóa đơn ---
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String time = hoaDon.getThoiGianThanhToan() != null ? hoaDon.getThoiGianThanhToan().format(dtf) : "";

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);

        addInfoCell(infoTable, "M\u00E3 HD:", hoaDon.getMaHD(), fontSmall);
        addInfoCell(infoTable, "Ng\u00E0y:", time, fontSmall);
        addInfoCell(infoTable, "Thu ng\u00E2n:", hoaDon.getMaNV() != null ? hoaDon.getMaNV() : "Admin", fontSmall);

        document.add(infoTable);
        document.add(pDashes);

        // --- Bảng danh sách món ---
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 3f, 0.8f, 1.5f });

        addHeaderCell(table, "M\u00F3n", fontBold);
        addHeaderCell(table, "SL", fontBold, Element.ALIGN_CENTER);
        addHeaderCell(table, "Th\u00E0nh ti\u1EC1n", fontBold, Element.ALIGN_RIGHT);

        for (CartItem item : cartItems) {
            // 1. Dòng món chính
            String name = item.getMon().getTenMon() + " (" + item.getSize().getTenSize() + ")";
            String unitPriceStr = nf.format(item.getDonGiaSize()) + " \u0111";

            addItemCell(table, name, fontNormal);
            addItemCell(table, String.valueOf(item.getSoLuong()), fontNormal, Element.ALIGN_CENTER);
            addItemCell(table, nf.format(item.getDonGiaSize() * item.getSoLuong()) + " \u0111", fontNormal,
                    Element.ALIGN_RIGHT);

            // 2. Dòng Ghi chú (nếu có)
            if (!item.getGhiChu().isEmpty()) {
                addItemCell(table, "  *Ghi ch\u00FA: " + item.getGhiChu(), fontItalicSmall);
                table.addCell(createEmptyCell());
                table.addCell(createEmptyCell());
            }

            // 3. Dòng Toppings
            if (!item.getToppings().isEmpty()) {
                for (CartTopping ctx : item.getToppings()) {
                    String topName = "  + " + ctx.topping.getTenTopping() + " (" + nf.format(ctx.giaTopping) + ")";
                    int totalTopQty = item.getSoLuong() * ctx.soLuong;
                    double topTotal = totalTopQty * ctx.giaTopping;

                    addItemCell(table, topName, fontSmall);
                    addItemCell(table, String.valueOf(totalTopQty), fontSmall, Element.ALIGN_CENTER);
                    addItemCell(table, nf.format(topTotal) + " \u0111", fontSmall, Element.ALIGN_RIGHT);
                }
            }

            // Dòng trống nhẹ giữa các món chính
            PdfPCell spacer = new PdfPCell(new Phrase(" ", fontSmall));
            spacer.setBorder(Rectangle.NO_BORDER);
            spacer.setColspan(3);
            spacer.setFixedHeight(2f);
            table.addCell(spacer);
        }
        document.add(table);
        document.add(pDashes);

        // --- Tổng kết ---
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);

        String hinhThuc = hoaDon.getHinhThucThanhToan() != null ? hoaDon.getHinhThucThanhToan().name()
                : "TI\u1EC0N M\u1EB6T";
        addTotalRow(totalTable, "H\u00ECnh th\u1EE9c TT:", hinhThuc, fontNormal);
        addTotalRow(totalTable, "T\u1ED4NG C\u1ED8NG:", nf.format(hoaDon.getTongTienPhaiTra()) + " \u0111", fontBold);

        document.add(totalTable);

        Paragraph pFooter = new Paragraph("\nXin c\u1EA3m \u01A1n v\u00E0 h\u1EB9n g\u1EB7p l\u1EA1i!", fontNormal);
        pFooter.setAlignment(Element.ALIGN_CENTER);
        document.add(pFooter);

        document.close();
        return fileName;
    }

    private static void addInfoCell(PdfPTable table, String title, String val, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(title, font));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(val, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        addHeaderCell(table, text, font, Element.ALIGN_LEFT);
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setPaddingBottom(5f);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private static void addItemCell(PdfPTable table, String text, Font font) {
        addItemCell(table, text, font, Element.ALIGN_LEFT);
    }

    private static void addItemCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(2f);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private static PdfPCell createEmptyCell() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static void addTotalRow(PdfPTable table, String title, String val, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(title, font));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPaddingTop(5f);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(val, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPaddingTop(5f);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }
}
