# ScanAndCount5

A simple demo application showing how to use the Honeywell Scanning SDK
* Claim/Release scanner
* Enable/disable scanning by disabling the Trigger mode
* Uses Client mode control of the scanner. The trigger will control scanner on/off, by code.
* Uses Button.OnTuchListener to switch scanner on/off during the the touch of an on-screen button

The screen is composed by a TextView to show scanned data. An EditText to enter the amount. There are number buttons on the screen to enter an amount directly.But you may invoke the device's software keyboard by tapping the amount field. If the device does have a hardware keypad, the Enter key will issue the OK function and the ESC key will issue the Clear function.

The OK button will clear the scanned data and amount field. Nothing will be saved.

The on-screen Clear button will clear the amount field.

The scanner will be disabled, if there is scanned data but no amount entered. 

I tried the immersive fullscreen mode without luck. every time the software keyboard is shown/hidden, the top bar stays visible. Did not spend more time on this weired stuff. You may play with this after setting bUseFullScreen to true.

The number buttons are arranged inside a GridLayout.
