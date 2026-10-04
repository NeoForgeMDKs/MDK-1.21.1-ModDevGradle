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
    private int waylandcamerafix$suppressFrames = 0;

    @Inject(method = "grabMouse", at = @At("RETURN"))
    private void waylandcamerafix$onGrabMouse(CallbackInfo ci) {
        this.accumulatedDX = 0.0;
        this.accumulatedDY = 0.0;
        this.ignoreFirstMove = true;
        this.waylandcamerafix$suppressFrames = 2;
    }

    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void waylandcamerafix$onHandleAccumulatedMovement(CallbackInfo ci) {
        if (this.waylandcamerafix$suppressFrames > 0) {
            this.waylandcamerafix$suppressFrames--;
            this.accumulatedDX = 0.0;
            this.accumulatedDY = 0.0;
            this.ignoreFirstMove = true;
        }
    }
}
