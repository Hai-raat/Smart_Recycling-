package simu.model;

import simu.framework.IEventType;

public enum EventType implements IEventType {
    ARRIVAL,
    DEP_RECEPTION, DEP_WEIGHING, DEP_SORTING,
    DEP_PLASTIC, DEP_PAPER, DEP_GLASS, DEP_METAL,
    DEP_REJECT, DEP_DISPATCH
}
