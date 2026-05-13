package com.app.file.pdf;

import com.app.resume.model.Resume;
import com.app.resume.model.ResumeVersion;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Generates a professionally formatted PDF from resume content.
 * Uses OpenHTMLtoPDF (backed by Apache PDFBox) to render an HTML/CSS template.
 */
@Slf4j
@Component
public class PdfGenerator {

    public byte[] generate(Resume resume, ResumeVersion version) {
        String html = buildHtml(resume, version);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF generation failed for resumeId={}", resume.getId(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    // ── HTML builder ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String buildHtml(Resume resume, ResumeVersion version) {
        Map<String, Object> sections = version.getContent() != null
                ? version.getContent() : Collections.emptyMap();

        Map<String, Object> personal = asMap(sections.get("personalInfo"));
        String fullName   = str(personal.get("fullName"));
        String email      = str(personal.get("email"));
        String phone      = str(personal.get("phone"));
        String location   = str(personal.get("location"));
        String linkedIn   = str(personal.get("linkedIn"));
        String website    = str(personal.get("website"));
        String summary    = str(sections.get("summary"));

        List<Map<String, Object>> experience     = asList(sections.get("experience"));
        List<Map<String, Object>> education      = asList(sections.get("education"));
        List<Map<String, Object>> projects       = asList(sections.get("projects"));
        List<Map<String, Object>> certifications = asList(sections.get("certifications"));
        List<String> skills = strList(sections.get("skills"));

        StringBuilder sb = new StringBuilder();
        sb.append("""
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8"/>
                <style>
                  * { margin: 0; padding: 0; box-sizing: border-box; }
                  body {
                    font-family: Arial, Helvetica, sans-serif;
                    font-size: 9.5pt;
                    color: #111;
                    margin: 36px 48px;
                    line-height: 1.35;
                  }
                  h1 {
                    text-align: center;
                    font-size: 18pt;
                    font-weight: bold;
                    text-transform: uppercase;
                    letter-spacing: 1px;
                    margin-bottom: 4px;
                  }
                  .contact-row {
                    text-align: center;
                    font-size: 9pt;
                    margin-bottom: 2px;
                  }
                  .contact-row a { color: #1a0dab; text-decoration: none; }
                  .section-title {
                    font-size: 10pt;
                    font-weight: bold;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    border-bottom: 1.5px solid #111;
                    margin-top: 12px;
                    margin-bottom: 5px;
                    padding-bottom: 1px;
                  }
                  .entry { margin-bottom: 7px; }
                  .entry-header {
                    display: block;
                    width: 100%;
                  }
                  .entry-header-left  { font-weight: bold; }
                  .entry-header-right { float: right; font-size: 9pt; }
                  .entry-sub { font-style: italic; font-size: 9pt; margin-top: 1px; }
                  ul.bullets {
                    margin-left: 16px;
                    margin-top: 3px;
                  }
                  ul.bullets li { margin-bottom: 2px; }
                  .skills-line { margin-top: 3px; }
                  .cert-list { margin-left: 16px; margin-top: 3px; }
                  .cert-list li { margin-bottom: 2px; }
                  .proj-tech { font-size: 8.5pt; color: #444; margin-top: 2px; }
                  .clearfix::after { content: ""; display: block; clear: both; }
                </style>
                </head>
                <body>
                """);

        // ── Header ────────────────────────────────────────────────────────
        if (!fullName.isEmpty()) {
            sb.append("<h1>").append(esc(fullName)).append("</h1>\n");
        }

        // Contact line: phone | email
        List<String> contactParts = new ArrayList<>();
        if (!phone.isEmpty())    contactParts.add(esc(phone));
        if (!email.isEmpty())    contactParts.add("<a href='mailto:" + esc(email) + "'>" + esc(email) + "</a>");
        if (!contactParts.isEmpty()) {
            sb.append("<div class='contact-row'>")
              .append(String.join(" | ", contactParts))
              .append("</div>\n");
        }

        // Location
        if (!location.isEmpty()) {
            sb.append("<div class='contact-row'>").append(esc(location)).append("</div>\n");
        }

        // Links line: LinkedIn | Portfolio
        List<String> linkParts = new ArrayList<>();
        if (!linkedIn.isEmpty()) {
            String href = linkedIn.startsWith("http") ? linkedIn : "https://" + linkedIn;
            linkParts.add("<a href='" + esc(href) + "'>LinkedIn</a>");
        }
        if (!website.isEmpty()) {
            String href = website.startsWith("http") ? website : "https://" + website;
            linkParts.add("<a href='" + esc(href) + "'>Portfolio</a>");
        }
        if (!linkParts.isEmpty()) {
            sb.append("<div class='contact-row'>")
              .append(String.join(" | ", linkParts))
              .append("</div>\n");
        }

        // ── Summary ───────────────────────────────────────────────────────
        if (!summary.isEmpty()) {
            sb.append("<div class='section-title'>Summary</div>\n");
            sb.append("<div>").append(esc(summary)).append("</div>\n");
        }

        // ── Experience ────────────────────────────────────────────────────
        if (!experience.isEmpty()) {
            sb.append("<div class='section-title'>Experience</div>\n");
            for (Map<String, Object> exp : experience) {
                String company   = str(exp.get("company"));
                String role      = str(exp.get("role"));
                String startDate = str(exp.get("startDate"));
                String endDate   = str(exp.get("endDate"));
                boolean current  = Boolean.TRUE.equals(exp.get("current"));
                String location2 = str(exp.get("location"));

                String dateRange = startDate;
                if (!dateRange.isEmpty()) {
                    dateRange += " \u2013 " + (current ? "Present" : (endDate.isEmpty() ? "" : endDate));
                }

                sb.append("<div class='entry'>\n");
                sb.append("<div class='entry-header clearfix'>");
                sb.append("<span class='entry-header-right'>").append(esc(location2));
                if (!location2.isEmpty() && !dateRange.isEmpty()) sb.append(", ");
                sb.append(esc(dateRange)).append("</span>");
                sb.append("<span class='entry-header-left'>").append(esc(company)).append("</span>");
                sb.append("</div>\n");
                if (!role.isEmpty()) {
                    sb.append("<div class='entry-sub'>").append(esc(role)).append("</div>\n");
                }
                appendBullets(sb, strList(exp.get("bullets")));
                sb.append("</div>\n");
            }
        }

        // ── Education ─────────────────────────────────────────────────────
        if (!education.isEmpty()) {
            sb.append("<div class='section-title'>Education</div>\n");
            for (Map<String, Object> edu : education) {
                String institution = str(edu.get("institution"));
                String degree      = str(edu.get("degree"));
                String field       = str(edu.get("field"));
                String startDate   = str(edu.get("startDate"));
                String endDate     = str(edu.get("endDate"));

                String degreeField = degree;
                if (!field.isEmpty()) degreeField += (degreeField.isEmpty() ? "" : " ") + field;

                String dateRange = startDate;
                if (!dateRange.isEmpty() && !endDate.isEmpty()) {
                    dateRange += " \u2013 " + endDate;
                }

                sb.append("<div class='entry'>\n");
                sb.append("<div class='entry-header clearfix'>");
                if (!dateRange.isEmpty()) {
                    sb.append("<span class='entry-header-right'>").append(esc(dateRange)).append("</span>");
                }
                sb.append("<span class='entry-header-left'>").append(esc(institution)).append("</span>");
                sb.append("</div>\n");
                if (!degreeField.isEmpty()) {
                    sb.append("<div class='entry-sub'>").append(esc(degreeField)).append("</div>\n");
                }
                sb.append("</div>\n");
            }
        }

        // ── Skills ────────────────────────────────────────────────────────
        if (!skills.isEmpty()) {
            sb.append("<div class='section-title'>Skills</div>\n");
            sb.append("<div class='skills-line'>").append(esc(String.join(", ", skills))).append("</div>\n");
        }

        // ── Projects ──────────────────────────────────────────────────────
        if (!projects.isEmpty()) {
            sb.append("<div class='section-title'>Projects</div>\n");
            for (Map<String, Object> proj : projects) {
                String name        = str(proj.get("name"));
                String description = str(proj.get("description"));
                List<String> techs = strList(proj.get("technologies"));

                sb.append("<div class='entry'>\n");
                sb.append("<div class='entry-header-left'>").append(esc(name)).append("</div>\n");
                if (!description.isEmpty()) {
                    sb.append("<div>").append(esc(description)).append("</div>\n");
                }
                appendBullets(sb, strList(proj.get("bullets")));
                if (!techs.isEmpty()) {
                    sb.append("<div class='proj-tech'>").append(esc(String.join(", ", techs))).append("</div>\n");
                }
                sb.append("</div>\n");
            }
        }

        // ── Certifications ────────────────────────────────────────────────
        if (!certifications.isEmpty()) {
            sb.append("<div class='section-title'>Certifications</div>\n");
            sb.append("<ul class='cert-list'>\n");
            for (Map<String, Object> cert : certifications) {
                String name   = str(cert.get("name"));
                String issuer = str(cert.get("issuer"));
                String date   = str(cert.get("date"));
                sb.append("<li>").append(esc(name));
                if (!issuer.isEmpty()) sb.append(" \u2013 ").append(esc(issuer));
                if (!date.isEmpty())   sb.append(" (").append(esc(date)).append(")");
                sb.append("</li>\n");
            }
            sb.append("</ul>\n");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void appendBullets(StringBuilder sb, List<String> bullets) {
        List<String> nonempty = bullets.stream().filter(b -> !b.isBlank()).toList();
        if (nonempty.isEmpty()) return;
        sb.append("<ul class='bullets'>\n");
        for (String b : nonempty) {
            sb.append("<li>").append(esc(b)).append("</li>\n");
        }
        sb.append("</ul>\n");
    }

    private String str(Object val) {
        return val instanceof String s ? s.trim() : "";
    }

    @SuppressWarnings("unchecked")
    private List<String> strList(Object val) {
        if (val instanceof List<?> list) {
            return list.stream()
                    .filter(o -> o instanceof String)
                    .map(o -> ((String) o).trim())
                    .toList();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object val) {
        if (val instanceof List<?> list) {
            return list.stream()
                    .filter(o -> o instanceof Map)
                    .map(o -> (Map<String, Object>) o)
                    .toList();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object val) {
        return val instanceof Map<?, ?> m ? (Map<String, Object>) m : Collections.emptyMap();
    }

    /** HTML-escape user text to prevent XSS in the generated document. */
    private String esc(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
