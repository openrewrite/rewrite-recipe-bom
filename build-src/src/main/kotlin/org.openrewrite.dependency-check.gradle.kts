import org.owasp.dependencycheck.reporting.ReportGenerator

plugins {
    id("org.owasp.dependencycheck")
}

configure<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension> {
    analyzers.nodeAudit { enabled = false }
    analyzers.nodePackage { enabled = false }
    analyzers.assemblyEnabled = false
    failBuildOnCVSS = 9.0F
    suppressionFile = "suppressions.xml"
    format = ReportGenerator.Format.valueOf(project.findProperty("dependencyCheckFormat") as String? ?: "HTML").toString()
}
