package archunit;

import static com.tngtech.archunit.library.DependencyRules.NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;

@ArchTag("arch-test")
@AnalyzeClasses(packages = "com.kpn.ndsal.resourcemanager",
                importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class},
                cacheMode = CacheMode.FOREVER)
public class DependencyRulesArchTestIT {

    @ArchTest
    static final ArchRule no_accesses_to_upper_package = NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;
}