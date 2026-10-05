package com.example.waylandcamerafix.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;
    @Shadow
    private boolean ignoreFirstMove;

    @Unique
    private int waylandcamerafix$grabGraceTicks = 0;

    @Inject(method = "grabMouse", at = @At("RETURN"))
    private void waylandcamerafix$onGrabMouse(CallbackInfo ci) {
        this.accumulatedDX = 0.0;
        this.accumulatedDY = 0.0;
        this.ignoreFirstMove = true;
        // Set a grace period of 10 frame-handling ticks after grabMouse()
        this.waylandcamerafix$grabGraceTicks = 10;
    }

    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void waylandcamerafix$onHandleAccumulatedMovement(CallbackInfo ci) {
        if (this.waylandcamerafix$grabGraceTicks > 0) {
            this.waylandcamerafix$grabGraceTicks--;
            // During the 10-tick grace window after grabMouse, discard any transition delta spikes (> 30px)
            if (Math.abs(this.accumulatedDX) > 30.0 || Math.abs(this.accumulatedDY) > 30.0) {
                this.accumulatedDX = 0.0;
                this.accumulatedDY = 0.0;
                this.ignoreFirstMove = true;
            }
        }
    }
}
