package archunit;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

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
public class CyclicDependencyRulesArchTestIT {

    @ArchTest
    static final ArchRule no_cycles_by_method_calls_between_slices =
            slices().matching("..(adapter).(*)..")
                    .namingSlices("$2 of $1")
                    .should()
                    .beFreeOfCycles();

}
