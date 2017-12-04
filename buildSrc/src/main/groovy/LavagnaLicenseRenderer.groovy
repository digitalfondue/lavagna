import com.github.jk1.license.ImportedModuleBundle
import com.github.jk1.license.ImportedModuleData
import com.github.jk1.license.License
import com.github.jk1.license.LicenseReportPlugin.LicenseReportExtension
import com.github.jk1.license.ModuleData
import com.github.jk1.license.PomData
import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.ReportRenderer
import org.gradle.api.Project

class LavagnaLicenseRenderer implements ReportRenderer {

    private String name
    private String fileName
    private Project project
    private LicenseReportExtension config
    private File output
    private Map<String, Map<String, String>> overrides = [:]

    LavagnaLicenseRenderer(String fileName = 'THIRD-PARTY-LICENSES.txt', String name = null, File overridesFilename = null) {
        this.name = name
        this.fileName = fileName
        if (overridesFilename) overrides = parseOverrides(overridesFilename)
    }

    private Map<String, Map<String, String>> parseOverrides(File file) {
        overrides = [:]
        file.withReader { Reader reader ->
            String line
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(/\|/)
                String groupNameVersion = columns[0]
                overrides[groupNameVersion] = [projectUrl: safeGet(columns, 1), license: safeGet(columns, 2), licenseUrl: safeGet(columns, 3)]
            }
        }
        return overrides
    }

    void render(ProjectData data) {
        project = data.project
        if (name == null) name = project.name
        config = project.licenseReport
        output = new File(config.outputDir, fileName)
        output.text = """
This product includes/uses the following libraries:

"""
        def inventory = buildLicenseInventory(data)
        def externalInventories = buildExternalInventories(data)
        printDependencies(inventory, externalInventories)
        output << """"""
    }

    private Map<String, List<ModuleData>> buildLicenseInventory(ProjectData data) {
        Map<String, List<ModuleData>> inventory = [:]
        data.allDependencies.each { ModuleData module ->
            if (!module.poms.isEmpty()) {
                PomData pom = module.poms.first()
                if (pom.licenses.isEmpty()) {
                    addModule(inventory, module.licenseFiles.isEmpty() ? "Unknown" : "Embedded", module)
                } else {
                    pom.licenses.each { License license ->
                        addModule(inventory, license.name, module)
                    }
                }
            } else {
                addModule(inventory, module.licenseFiles.isEmpty() ? "Unknown" : "Embedded", module)
            }
        }
        return inventory
    }

    private Map<String, Map<String, List<ImportedModuleData>>> buildExternalInventories(ProjectData data) {
        Map<String, Map<String, List<ImportedModuleData>>> results = [:]
        data.importedModules.each { ImportedModuleBundle module ->
            Map<String, List<ImportedModuleData>> bundle = [:]
            module.modules.each { ImportedModuleData moduleData ->
                if (!bundle.containsKey(moduleData.license)) bundle[moduleData.license] = []
                bundle[moduleData.license] << moduleData
            }
            results[module.name] = bundle
        }
        return results
    }

    private void addModule(Map<String, List<ModuleData>> inventory, String key, ModuleData module) {
        String gnv = "${module.group}:${module.name}:${module.version}"
        if (key == "Unknown" && overrides.containsKey(gnv)) {
            if (!inventory.containsKey(overrides[gnv].license)) inventory[overrides[gnv].license] = []
            inventory[overrides[gnv].license] << module
        } else {
            if (!inventory.containsKey(key)) inventory[key] = []
            inventory[key] << module
        }
    }

    private void printDependencies(Map<String, List<ModuleData>> inventory, Map<String, Map<String, List<ImportedModuleData>>> externalInventories) {
        inventory.keySet().sort().each { String license ->
            inventory[license].sort({ ModuleData a, ModuleData b -> a.group <=> b.group }).each { ModuleData data ->
                printDependency(data)
            }
        }

        externalInventories.keySet().sort().each { String name ->
            externalInventories[name].each { String license, List<ImportedModuleData> dependencies ->
                dependencies.each { ImportedModuleData importedData ->
                    printImportedDependency(importedData)
                }
            }
        }
    }

    private void printDependency(ModuleData data) {
        String projectUrl = null
        String licenseUrl = null
        String licenseName = null

        if (!data.poms.isEmpty()) {
            PomData pomData = data.poms.first()
            if (pomData.projectUrl) {
                projectUrl = pomData.projectUrl
            }
            if (pomData.licenses) {
                pomData.licenses.each { License license ->
                    if (license.url) {
                        licenseUrl = license.url
                        licenseName = license.name
                    } else {
                        licenseName = license.name
                    }
                }
            }
        }

        output << " - ${data.name} (${projectUrl}) ${data.group}:${data.name}:${data.version}\n"
        output << "     License: ${licenseName} ($licenseUrl)\n\n"
    }

    private printImportedDependency(ImportedModuleData data) {
        output << " - ${data.name} v${data.version} ($data.projectUrl)\n"
        output << "     License: ${data.license} ($data.licenseUrl)"
        output << "\n\n"
    }

    private String safeGet(String[] arr, int index) {
        arr.length > index ? arr[index] : null
    }
}
