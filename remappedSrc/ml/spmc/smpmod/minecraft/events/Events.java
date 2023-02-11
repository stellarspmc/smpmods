package ml.spmc.smpmod.minecraft.events;

public abstract class Events {
    abstract void onEvent();

    abstract void onEventEnd();
    abstract int durationInSeconds();
}
