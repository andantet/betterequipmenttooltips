package dev.andante.betterequipmenttooltips.mixin;

import java.util.List;
import java.util.Objects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unchecked")
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique
    private static final MutableText BET_ENCHANTMENTS_HEADER_TEXT = Text.translatable("text.betterequipmenttooltips.enchantments").formatted(Formatting.GRAY);

    /**
     * Appends an 'Enchantments' header to the top of the enchantments tooltip.
     */
    @Inject(method = "appendEnchantments", at = @At("HEAD"))
    private static void onAppendEnchantments(List<Text> tooltip, NbtList enchantments, CallbackInfo ci) {
        if (!enchantments.isEmpty()) {
            tooltip.add(BET_ENCHANTMENTS_HEADER_TEXT);
        }
    }

    /**
     * Modifies each enchantment's text tooltip.
     * Adds a space before each entry, and recolors appropriately.
     */
    @ModifyArg(
            method = "method_17869",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    ordinal = 0,
                    remap = false
            ),
            index = 0,
            remap = false
    )
    private static <E> E onEnchantmentTooltipAdd(E e) {
        if (e instanceof MutableText text) {
            Style style = text.getStyle();
            TextColor color = style.getColor();
            if (color != null && Objects.equals(color.getName(), Formatting.GRAY.getName())) {
                text.formatted(Formatting.GREEN);
            }

            return (E) Text.literal(" ").append(text);
        }

        return e;
    }

    /**
     * Adds a space before attribute modifier entries in tooltips.
     */
    @ModifyArg(
            method = "getTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    remap = false
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/item/ItemStack$TooltipSection;MODIFIERS:Lnet/minecraft/item/ItemStack$TooltipSection;"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;hasNbt()Z",
                            ordinal = 1,
                            shift = At.Shift.BEFORE
                    )
            ),
            index = 0
    )
    private <E> E onModifierTooltipAdd(E e) {
        if (e instanceof Text text) {
            if (text.getContent() instanceof TranslatableTextContent content) {
                String key = content.getKey();
                if (key.startsWith("attribute.modifier.")) {
                    return (E) Text.literal(" ").append(text);
                }
            }
        }

        return e;
    }
}
