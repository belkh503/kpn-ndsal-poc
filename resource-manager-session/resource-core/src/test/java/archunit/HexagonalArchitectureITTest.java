package archunit;

import static archunit.Architectures.hexagonalArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchIgnore;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;

@ArchTag("arch-test")
@AnalyzeClasses(packages = "com.kpn.ndsal.resourcemanager",
				importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class},
				cacheMode = CacheMode.FOREVER)
class HexagonalArchitectureITTest {

	// TODO: vsapiha
	/**
	 * Add:
	 * 1. adapters shouldn't depend on services
	 * 2. adapters should depend only on in/out ports
	 * 3. code in adapters should be by default 'package private' ??
	 * 4. ...
	 */

	@ArchIgnore
	@ArchTest
	static final ArchRule no_classes_should_depend_on_service =
			noClasses().should().dependOnClassesThat().resideInAPackage("..service..");

	@Test
	void validateRegistrationContextArchitecture() {
		hexagonalArchitecture("com.kpn.ndsal.resourcemanager")

				.withDomainLayer("domain")

				.withAdaptersLayer("adapter")
				.incoming("in")
				.outgoing("out.persistence")
				.and()

				.withApplicationLayer("application")
				.services("service")
				.incomingPorts("port.in")
				.outgoingPorts("port.out")
				.and()

				.withConfiguration("configuration")
				.check(new ClassFileImporter()
						.importPackages("com.kpn.ndsal.."));
	}

	@Test
	void testPackageDependencies() {
		noClasses()
				.that()
				.resideInAPackage("com.kpn.ndsal.resourcemanager.domain..")
				.should()
				.dependOnClassesThat()
				.resideInAnyPackage("com.kpn.ndsal.resourcemanager.application..")
				.check(new ClassFileImporter()
						.importPackages("com.kpn.ndsal.resourcemanager.."));
	}

}
