package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.electrical_panel.PanelAttachmentMode;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class CEEPanelAttachmentTypes {
    private static final DeferredRegister<PanelAttachmentType> PANEL_ATTACHMENT_TYPE =
            DeferredRegister.create(CEERegistries.PANEL_ATTACHMENT_TYPE, CreateElectroEnergetics.ID);

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> AMMETER = PANEL_ATTACHMENT_TYPE
            .register("ammeter", () -> new PanelAttachmentType(GaugePanelAttachment::ammeter, CEEBlocks.AMMETER, PanelAttachmentMode.HALF));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> VOLTMETER = PANEL_ATTACHMENT_TYPE
            .register("voltmeter", () -> new PanelAttachmentType(GaugePanelAttachment::voltmeter, CEEBlocks.VOLTMETER, PanelAttachmentMode.HALF));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> MINIATURE_AMMETER = PANEL_ATTACHMENT_TYPE
            .register("miniature_ammeter", () -> new PanelAttachmentType(GaugePanelAttachment::smolAmmeter, CEEItems.MINIATURE_AMMETER, PanelAttachmentMode.THIRD));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> MINIATURE_VOLTMETER = PANEL_ATTACHMENT_TYPE
            .register("miniature_voltmeter", () -> new PanelAttachmentType(GaugePanelAttachment::smolVoltmeter, CEEItems.MINIATURE_VOLTMETER, PanelAttachmentMode.THIRD));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> ESTOP = PANEL_ATTACHMENT_TYPE
            .register("emergency_stop_button", () -> new PanelAttachmentType(EStopPanelAttachment::new, CEEBlocks.EMERGENCY_STOP_BUTTON, PanelAttachmentMode.HALF));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> CUT_OFF_SWITCH = PANEL_ATTACHMENT_TYPE
            .register("cut_off_switch", () -> new PanelAttachmentType(CutOffSwitchPanelAttachment::normal, CEEBlocks.CUT_OFF_SWITCH, PanelAttachmentMode.HALF));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> INDICATOR_BULB = PANEL_ATTACHMENT_TYPE
            .register("indicator_bulb", () -> new PanelAttachmentType(IndicatorBulbPanelAttachment::normal, CEEBlocks.INDICATOR_BULB, PanelAttachmentMode.HALF));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> MINIATURE_INDICATOR_BULB = PANEL_ATTACHMENT_TYPE
            .register("miniature_indicator_bulb", () -> new PanelAttachmentType(IndicatorBulbPanelAttachment::miniature, CEEItems.MINIATURE_INDICATOR_BULB, PanelAttachmentMode.THIRD));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> MOMENTARY_SWITCH = PANEL_ATTACHMENT_TYPE
            .register("momentary_switch", () -> new PanelAttachmentType(MomentarySwitchPanelAttachment::normal, CEEBlocks.MOMENTARY_SWITCH, PanelAttachmentMode.HALF));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> MINIATURE_MOMENTARY_SWITCH = PANEL_ATTACHMENT_TYPE
            .register("miniature_momentary_switch", () -> new PanelAttachmentType(MomentarySwitchPanelAttachment::miniature, CEEItems.MINIATURE_MOMENTARY_SWITCH, PanelAttachmentMode.THIRD));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> ENERGY_METER = PANEL_ATTACHMENT_TYPE
            .register("energy_meter", () -> new PanelAttachmentType(EnergyMeterAttachment::new, CEEBlocks.ENERGY_METER, PanelAttachmentMode.FULL_DOUBLE));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> TRI_POLAR_ENERGY_METER = PANEL_ATTACHMENT_TYPE
            .register("tri_polar_energy_meter", () -> new PanelAttachmentType(TriPolarEnergyMeterAttachment::new, CEEBlocks.TRI_POLAR_ENERGY_METER, PanelAttachmentMode.FULL_TRIPLE));

    public static final DeferredHolder<PanelAttachmentType, PanelAttachmentType> MINIATURE_CIRCUIT_BREAKER = PANEL_ATTACHMENT_TYPE
            .register("miniature_circuit_breaker", () -> new PanelAttachmentType(MCBPanelAttachment::new, CEEItems.MINIATURE_CIRCUIT_BREAKER, PanelAttachmentMode.THIRD));

    public static void register(IEventBus bus) {
        PANEL_ATTACHMENT_TYPE.register(bus);
    }

}
