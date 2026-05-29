package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.electrical_panel.PanelAttachmentMode;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CEEPanelAttachmentTypes {
    private static final DeferredRegister<PanelAttachmentType> PANEL_ATTACHMENT_TYPE =
            DeferredRegister.create(CEERegistries.PANEL_ATTACHMENT_TYPE, CreateElectroEnergetics.ID);

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> AMMETER = PANEL_ATTACHMENT_TYPE
            .register("ammeter", () -> new PanelAttachmentType(GaugePanelAttachment::ammeter, CEEBlocks.AMMETER, PanelAttachmentMode.HALF));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> VOLTMETER = PANEL_ATTACHMENT_TYPE
            .register("voltmeter", () -> new PanelAttachmentType(GaugePanelAttachment::voltmeter, CEEBlocks.VOLTMETER, PanelAttachmentMode.HALF));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> ESTOP = PANEL_ATTACHMENT_TYPE
            .register("emergency_stop_button", () -> new PanelAttachmentType(EStopPanelAttachment::new, CEEBlocks.EMERGENCY_STOP_BUTTON, PanelAttachmentMode.HALF));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> ENERGY_METER = PANEL_ATTACHMENT_TYPE
            .register("energy_meter", () -> new PanelAttachmentType(EnergyMeterAttachment::new, CEEBlocks.ENERGY_METER, PanelAttachmentMode.FULL_DOUBLE));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> MINIATURE_CIRCUIT_BREAKER = PANEL_ATTACHMENT_TYPE
            .register("miniature_circuit_breaker", () -> new PanelAttachmentType(MCBPanelAttachment::new, CEEItems.MINIATURE_CIRCUIT_BREAKER, PanelAttachmentMode.THIRD));

    public static void register(IEventBus bus) {
        PANEL_ATTACHMENT_TYPE.register(bus);
    }

}
