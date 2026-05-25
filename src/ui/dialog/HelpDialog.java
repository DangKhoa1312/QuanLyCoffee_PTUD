package ui.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class HelpDialog extends JDialog {

    public HelpDialog(JFrame parent) {
        super(parent, "Hướng Dẫn Sử Dụng", true);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        
        String htmlContent = loadMarkdownAsHtml("documents/user_manual.md");
        editorPane.setText(htmlContent);
        
        // Croll to top
        editorPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(null);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Roboto", Font.BOLD, 14));
        btnClose.setBackground(new Color(231, 76, 60));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());
        
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBottom.setOpaque(false);
        pnlBottom.add(btnClose);
        
        contentPane.add(pnlBottom, BorderLayout.SOUTH);
        
        setContentPane(contentPane);
    }

    private String loadMarkdownAsHtml(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return "<html><body><h2>Không tìm thấy file Hướng Dẫn Sử Dụng.</h2><p>Vui lòng kiểm tra lại đường dẫn: " + filePath + "</p></body></html>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: sans-serif; font-size: 13px; padding: 10px; color: #333;'>");
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Convert basic Markdown to HTML
                if (line.startsWith("### ")) {
                    sb.append("<h3 style='color: #2980b9;'>").append(line.substring(4)).append("</h3>");
                } else if (line.startsWith("#### ")) {
                    sb.append("<h4 style='color: #e67e22;'>").append(line.substring(5)).append("</h4>");
                } else if (line.startsWith("# ")) {
                    sb.append("<h1 style='color: #2c3e50; text-align: center;'>").append(line.substring(2)).append("</h1>");
                } else if (line.startsWith("---")) {
                    sb.append("<hr color='#ddd'/>");
                } else if (line.startsWith("- ")) {
                    // Xử lý in đậm trong bullet
                    String formatted = line.substring(2).replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
                    formatted = formatted.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
                    sb.append("<ul><li style='margin-bottom: 5px;'>").append(formatted).append("</li></ul>");
                } else if (line.matches("^\\d+\\.\\s+.*")) {
                    // Numbered list
                    String formatted = line.replaceFirst("^\\d+\\.\\s+", "");
                    formatted = formatted.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
                    sb.append("<ol><li style='margin-bottom: 5px;'>").append(formatted).append("</li></ol>");
                } else if (line.trim().isEmpty()) {
                    sb.append("<br>");
                } else {
                    String formatted = line.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
                    formatted = formatted.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
                    sb.append("<p style='line-height: 1.5; margin: 0;'>").append(formatted).append("</p>");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "<html><body>Lỗi đọc file: " + e.getMessage() + "</body></html>";
        }
        
        sb.append("</body></html>");
        
        // Xóa khoảng trắng thừa giữa danh sách (list)
        String finalHtml = sb.toString();
        finalHtml = finalHtml.replaceAll("</ul><ul>", "");
        finalHtml = finalHtml.replaceAll("</ol><ol>", "");
        
        return finalHtml;
    }
}
