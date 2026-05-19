package archunit;

import static com.tngtech.archunit.PublicAPI.Usage.ACCESS;

import com.tngtech.archunit.PublicAPI;

/**
 * Offers convenience to assert typical architectures:
 * <ul>
 * <li>{@link #hexagonalArchitecture()}</li>
 * </ul>
 * */
public final class Architectures {

    private Architectures() {
    }

    @PublicAPI(usage = ACCESS)
    public static HexagonalArchitectureIT hexagonalArchitecture(String basePackage) {
        return HexagonalArchitectureIT.boundedContext(basePackage);
    }

}
