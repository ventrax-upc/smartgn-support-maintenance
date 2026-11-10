package com.ventrax.support.application.outbound;

import java.util.*;

public interface AccessPort {
    Map actor();

    UUID actorId();

    void admin();

    UUID meter(UUID meter, String function);

    void identity(UUID supplied, UUID actual);

    Set<UUID> permittedMeters(String function);
}
