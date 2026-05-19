package archunit;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("A Test Suite for all ArchUnit tests")
@IncludeClassNamePatterns(value = {"^.*ArchTest$"})
@SelectPackages("com.kpn.ndsal")
public class ArchTestSuit {
}
