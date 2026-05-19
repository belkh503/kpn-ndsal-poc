package archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.springframework.stereotype.Service;

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
public class NamingConventionArchTestIT {

    @ArchTest
    static ArchRule services_should_be_suffixed =
            classes()
                    .that().resideInAPackage("..service..")
                    .and().areAnnotatedWith(Service.class)
                    .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static ArchRule classes_named_service_should_be_in_a_service_package =
            classes()
                    .that().haveSimpleNameContaining("Service")
                    .should().resideInAPackage("..service..");

    @ArchIgnore
    @ArchTest
    static ArchRule classes_must_not_be_suffixed_with_impl =
            noClasses()
                    .should().haveSimpleNameEndingWith("Impl")
                    .because("seriously, it is old fashion. you can do better than that");

}
