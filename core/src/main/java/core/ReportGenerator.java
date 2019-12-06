package core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lumutator.Mutant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates an HTML report of MuRa.
 */
public class ReportGenerator {

    /**
     * Generate a report from a list of mutants.
     *
     * @param mutants   The list of mutants.
     * @param outputDir The output directory where the report will be generated.
     */
    public static void generateReport(final List<Mutant> mutants, final String outputDir) throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setDirectoryForTemplateLoading(new File(ReportGenerator.class.getClassLoader().getResource("templates").getFile()));
        cfg.setDefaultEncoding("UTF-8");

        Template template = cfg.getTemplate("index.ftl");

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("mutants", mutants);
        if (!mutants.isEmpty()) {
            final List<Coefficient> coeffs = ((RankedMutant) mutants.get(0)).getRankCoefficients();
            templateData.put("rankers", coeffs);
        }

        // TODO: create document and load data dynamically in report? (https://datatables.net/reference/option/deferRender)
        Writer fileWriter = new FileWriter(new File("index.html"));
        try {
            template.process(templateData, fileWriter);
        } catch (TemplateException e) {
            // Should not happen
        } finally {
            fileWriter.close();
        }
    }

}
