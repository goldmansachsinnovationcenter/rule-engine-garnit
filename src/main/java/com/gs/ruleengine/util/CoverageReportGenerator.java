package com.gs.ruleengine.util;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.nio.file.*;

/**
 * Utility class to generate a custom HTML report showing code coverage and cyclomatic complexity
 * in ascending order of coverage.
 */
public class CoverageReportGenerator {

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("Usage: CoverageReportGenerator <jacoco-report-xml-path>");
                return;
            }

            String jacocoXmlPath = args[0];
            File jacocoXmlFile = new File(jacocoXmlPath);
            
            if (!jacocoXmlFile.exists()) {
                System.out.println("JaCoCo XML report not found at: " + jacocoXmlPath);
                return;
            }

            // Parse the JaCoCo XML report
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(jacocoXmlFile);

            // Extract coverage data
            NodeList packageNodes = document.getElementsByTagName("package");
            List<ClassCoverageData> classCoverageList = new ArrayList<>();

            for (int i = 0; i < packageNodes.getLength(); i++) {
                Element packageElement = (Element) packageNodes.item(i);
                String packageName = packageElement.getAttribute("name");
                
                NodeList classNodes = packageElement.getElementsByTagName("class");
                
                for (int j = 0; j < classNodes.getLength(); j++) {
                    Element classElement = (Element) classNodes.item(j);
                    String className = classElement.getAttribute("name");
                    
                    // Get method count as a simple proxy for cyclomatic complexity
                    NodeList methodNodes = classElement.getElementsByTagName("method");
                    int methodCount = methodNodes.getLength();
                    
                    // Calculate coverage
                    NodeList counterNodes = classElement.getElementsByTagName("counter");
                    int coveredInstructions = 0;
                    int missedInstructions = 0;
                    
                    for (int k = 0; k < counterNodes.getLength(); k++) {
                        Element counterElement = (Element) counterNodes.item(k);
                        String type = counterElement.getAttribute("type");
                        
                        if ("INSTRUCTION".equals(type)) {
                            coveredInstructions = Integer.parseInt(counterElement.getAttribute("covered"));
                            missedInstructions = Integer.parseInt(counterElement.getAttribute("missed"));
                            break;
                        }
                    }
                    
                    int totalInstructions = coveredInstructions + missedInstructions;
                    double coverage = totalInstructions > 0 ? (double) coveredInstructions / totalInstructions * 100 : 0;
                    
                    ClassCoverageData data = new ClassCoverageData(
                            packageName + "." + className,
                            coverage,
                            methodCount,
                            coveredInstructions,
                            missedInstructions
                    );
                    
                    classCoverageList.add(data);
                }
            }
            
            // Sort by coverage (ascending)
            classCoverageList.sort(Comparator.comparingDouble(ClassCoverageData::getCoverage));
            
            // Generate HTML report
            generateHtmlReport(classCoverageList);
            
            System.out.println("Coverage report generated successfully at: build/reports/coverage/index.html");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void generateHtmlReport(List<ClassCoverageData> classCoverageList) throws IOException {
        Path reportDir = Paths.get("build/reports/coverage");
        Files.createDirectories(reportDir);
        
        Path htmlFile = reportDir.resolve("index.html");
        
        try (BufferedWriter writer = Files.newBufferedWriter(htmlFile)) {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("    <title>Code Coverage and Complexity Report</title>\n");
            writer.write("    <style>\n");
            writer.write("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
            writer.write("        h1 { color: #333; }\n");
            writer.write("        table { border-collapse: collapse; width: 100%; }\n");
            writer.write("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            writer.write("        th { background-color: #f2f2f2; }\n");
            writer.write("        tr:nth-child(even) { background-color: #f9f9f9; }\n");
            writer.write("        .coverage-bar { height: 20px; background-color: #eee; position: relative; }\n");
            writer.write("        .coverage-value { height: 100%; background-color: #4CAF50; position: absolute; left: 0; top: 0; }\n");
            writer.write("        .low-coverage { background-color: #f44336; }\n");
            writer.write("        .medium-coverage { background-color: #ff9800; }\n");
            writer.write("        .high-coverage { background-color: #4CAF50; }\n");
            writer.write("    </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("    <h1>Code Coverage and Complexity Report</h1>\n");
            writer.write("    <p>Classes sorted by ascending code coverage</p>\n");
            writer.write("    <table>\n");
            writer.write("        <tr>\n");
            writer.write("            <th>Class Name</th>\n");
            writer.write("            <th>Coverage (%)</th>\n");
            writer.write("            <th>Coverage Visualization</th>\n");
            writer.write("            <th>Cyclomatic Complexity (Method Count)</th>\n");
            writer.write("            <th>Covered Instructions</th>\n");
            writer.write("            <th>Missed Instructions</th>\n");
            writer.write("        </tr>\n");
            
            for (ClassCoverageData data : classCoverageList) {
                String coverageClass = data.getCoverage() < 50 ? "low-coverage" : 
                                      (data.getCoverage() < 80 ? "medium-coverage" : "high-coverage");
                
                writer.write("        <tr>\n");
                writer.write("            <td>" + data.getClassName() + "</td>\n");
                writer.write("            <td>" + String.format("%.2f", data.getCoverage()) + "%</td>\n");
                writer.write("            <td>\n");
                writer.write("                <div class=\"coverage-bar\">\n");
                writer.write("                    <div class=\"coverage-value " + coverageClass + "\" style=\"width: " + 
                             Math.min(100, data.getCoverage()) + "%\"></div>\n");
                writer.write("                </div>\n");
                writer.write("            </td>\n");
                writer.write("            <td>" + data.getComplexity() + "</td>\n");
                writer.write("            <td>" + data.getCoveredInstructions() + "</td>\n");
                writer.write("            <td>" + data.getMissedInstructions() + "</td>\n");
                writer.write("        </tr>\n");
            }
            
            writer.write("    </table>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
        }
    }
    
    static class ClassCoverageData {
        private final String className;
        private final double coverage;
        private final int complexity;
        private final int coveredInstructions;
        private final int missedInstructions;
        
        public ClassCoverageData(String className, double coverage, int complexity, 
                                int coveredInstructions, int missedInstructions) {
            this.className = className;
            this.coverage = coverage;
            this.complexity = complexity;
            this.coveredInstructions = coveredInstructions;
            this.missedInstructions = missedInstructions;
        }
        
        public String getClassName() {
            return className;
        }
        
        public double getCoverage() {
            return coverage;
        }
        
        public int getComplexity() {
            return complexity;
        }
        
        public int getCoveredInstructions() {
            return coveredInstructions;
        }
        
        public int getMissedInstructions() {
            return missedInstructions;
        }
    }
}
