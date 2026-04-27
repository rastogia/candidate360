package com.recruit.c360.service.report;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.*;
import com.itextpdf.kernel.font.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.recruit.c360.dto.response.ProfileSummaryResponse;
import com.recruit.c360.entity.Profile360;
import com.recruit.c360.exception.ResourceNotFoundException;
import com.recruit.c360.repository.Profile360Repository;
import com.recruit.c360.service.ProfileOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
@Slf4j @Service @RequiredArgsConstructor
public class PdfReportService {
    private final Profile360Repository profileRepository;
    private final ProfileOrchestrationService orchestrationService;

    public byte[] generateReport(java.util.UUID profileId) {
        Profile360 profile = profileRepository.findById(profileId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        ProfileSummaryResponse summary = orchestrationService.getSummary(profileId);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(bos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            PdfFont bold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Header
            doc.add(new Paragraph("360° Candidate Profile Report")
                .setFont(bold).setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(0, 80, 120)));
            doc.add(new Paragraph("Generated: " + java.time.LocalDateTime.now().toString().substring(0,19))
                .setFont(normal).setFontSize(9).setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));
            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine()));
            doc.add(new Paragraph("\n"));

            // Candidate Info
            doc.add(new Paragraph("Candidate: " + summary.getCandidateName()).setFont(bold).setFontSize(14));
            doc.add(new Paragraph("Job Role:  " + summary.getJobRoleName()).setFont(normal).setFontSize(11));
            doc.add(new Paragraph("Status:    " + summary.getStatus()).setFont(normal).setFontSize(11));
            doc.add(new Paragraph("\n"));

            // Composite Score
            if (summary.getCompositeScore() != null) {
                doc.add(new Paragraph("Overall Score: " + summary.getCompositeScore() + "/100 — " + summary.getCompositeLabel())
                    .setFont(bold).setFontSize(14).setFontColor(new DeviceRgb(0,120,0)));
                doc.add(new Paragraph("Data Coverage: " + summary.getDataCoveragePct() + "%")
                    .setFont(normal).setFontSize(11));
            }
            doc.add(new Paragraph("\n"));

            // Dimension Scores
            if (summary.getDimensionScores() != null && !summary.getDimensionScores().isEmpty()) {
                doc.add(new Paragraph("Dimension Scores").setFont(bold).setFontSize(13));
                Table table = new Table(new float[]{3,2,2,2}).useAllAvailableWidth();
                table.addHeaderCell(new Cell().add(new Paragraph("Dimension").setFont(bold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Score").setFont(bold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Label").setFont(bold)));
                table.addHeaderCell(new Cell().add(new Paragraph("Weight").setFont(bold)));
                for (ProfileSummaryResponse.DimensionScore ds : summary.getDimensionScores()) {
                    table.addCell(String.valueOf(ds.getDimension()));
                    table.addCell(String.valueOf(ds.getScore()));
                    table.addCell(String.valueOf(ds.getLabel()));
                    table.addCell(ds.getWeightUsed() != null ? ds.getWeightUsed() + "%" : "-");
                }
                doc.add(table);
                doc.add(new Paragraph("\n"));
            }

            // Red Flags
            if (summary.getRedFlags() != null && !summary.getRedFlags().isEmpty()) {
                doc.add(new Paragraph("Red Flags").setFont(bold).setFontSize(13)
                    .setFontColor(new DeviceRgb(180,0,0)));
                for (ProfileSummaryResponse.RedFlagItem f : summary.getRedFlags()) {
                    doc.add(new Paragraph("• [" + f.getSeverity() + "] " + f.getFlagType() + ": " + f.getDescription())
                        .setFont(normal).setFontSize(10));
                }
            }
            doc.close();
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("PDF generation error: {}", e.getMessage());
            throw new RuntimeException("PDF generation failed: " + e.getMessage());
        }
    }
}
