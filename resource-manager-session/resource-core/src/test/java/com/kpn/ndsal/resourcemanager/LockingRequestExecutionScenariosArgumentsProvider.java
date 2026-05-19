package com.kpn.ndsal.resourcemanager;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockCommand;

import lombok.Data;

public class LockingRequestExecutionScenariosArgumentsProvider implements ArgumentsProvider {

    @Data(staticConstructor = "of")
    public static class ParametersPair<A> {
        private final A commands;
        private final Boolean expectedResult;

        public static <A> ParametersPair<A> withSuccessful(A commands) {
            return ParametersPair.of(commands, true);
        }
    }

    @Data(staticConstructor = "create")
    public static class ParametersAggregator {
        private final String name;
        private final List<ParametersPair<AcquireLockCommand>> commands;
        private final String description;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {

        return TestHelpers.getFieldsAnnotatedWithGraphGraphLockingRequest(this).stream()
                .map(Arguments::of);
    }

    @GraphLockingRequestSimplified(request = "")
    public ParametersAggregator acquireLockCommandForGraph1 = ParametersAggregator.create("1", List.of(
            ParametersPair.withSuccessful(new AcquireLockCommand(List.of(
                    new AcquireLockCommand.LockGroup(
                            List.of(
                                    new AcquireLockCommand.LockObject("NODE", "nl-pbl-cpe-01", false),
                                    new AcquireLockCommand.LockObject("PORT", "nl-pbl-cpe-01:1/1/2", false),
                                    new AcquireLockCommand.LockObject("EAS", "EAS000002", true),
                                    new AcquireLockCommand.LockObject("EVA", "EVA000001", false),
                                    new AcquireLockCommand.LockObject("EVS", "EVS000001", false)
                            )
                    )
            ))),
            ParametersPair.withSuccessful(new AcquireLockCommand(List.of(
                    new AcquireLockCommand.LockGroup(
                            List.of(
                                    new AcquireLockCommand.LockObject("NODE", "nl-pbl-cpe-01", false),
                                    new AcquireLockCommand.LockObject("PORT", "nl-pbl-cpe-01:1/1/2", false),
                                    new AcquireLockCommand.LockObject("EAS", "EAS000002", true),
                                    new AcquireLockCommand.LockObject("EVA", "EVA000001", false),
                                    new AcquireLockCommand.LockObject("EVS", "EVS000001", false)
                            )
                    )
            )))
    ), "It should NOT be possible to lock multiple NODEs with one request.");
}
