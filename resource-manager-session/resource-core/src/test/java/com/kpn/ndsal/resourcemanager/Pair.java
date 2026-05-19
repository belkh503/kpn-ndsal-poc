package com.kpn.ndsal.resourcemanager;

import lombok.Data;

@Data(staticConstructor = "of")
public class Pair<A, B> {

    private final A left;
    private final B right;

}
