export class Enums {
  public GuiStyle;
  public CameraSwitchVisibility;
  public ScannerState;

  public ScanningMode;

  constructor() {
    enum GuiStyle {
      default = Scandit.ScanOverlay.GuiStyle.DEFAULT,
      laser = Scandit.ScanOverlay.GuiStyle.LASER,
      none = Scandit.ScanOverlay.GuiStyle.NONE,
    };
    this.GuiStyle = GuiStyle;

    enum CameraSwitchVisibility {
      never = Scandit.ScanOverlay.CameraSwitchVisibility.NEVER,
      onTablet = Scandit.ScanOverlay.CameraSwitchVisibility.ON_TABLET,
      always = Scandit.ScanOverlay.CameraSwitchVisibility.ALWAYS,
    };
    this.CameraSwitchVisibility = CameraSwitchVisibility;

    enum ScannerState {
      stopped = Scandit.BarcodePicker.State.STOPPED,
      active = Scandit.BarcodePicker.State.ACTIVE,
      paused = Scandit.BarcodePicker.State.PAUSED,
    };
    this.ScannerState = ScannerState;

    enum ScanningMode {
      iban,
      gs1,
      price,
    };
    this.ScanningMode = ScanningMode;
  }
}
