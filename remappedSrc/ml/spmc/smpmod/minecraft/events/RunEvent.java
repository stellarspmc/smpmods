package ml.spmc.smpmod.minecraft.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RunEvent {
    public static void pickEvent() {
        Random random = new Random();
        // TODO: write events
        List<Class<? extends Events>> list = new ArrayList<>();
        list.add(EyeBoss.class);
        //list.add(EventList.EYE);
        Collections.shuffle(list);
        new EyeBoss().onEvent();
    }
}
