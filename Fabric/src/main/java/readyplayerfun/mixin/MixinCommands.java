package readyplayerfun.mixin;

import com.mojang.brigadier.ParseResults;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import readyplayerfun.event.ServerEventHander;

@Mixin(Commands.class)
public class MixinCommands {

    @Inject(method = "performCommand", at = @At(value = "HEAD"))
    private void injectPerformCommand(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfoReturnable<Integer> cir) {
        ServerEventHander.onCommand(parseResults);
    }

}
