
/**
 * Module for all functionality of the barcode recognition plugin.
 *
 * @since 4.11.0
 */
public class Scandit {
	
	/**
	 * A convenience class for points with an x and y coordinate.
	 *
	 * @since 4.11.0
	 */
	public class Point {
		public number x;
		public number y;
		
		/**
		 * @param x The x coordinate.
		 * @param y the y coordinate.
		 */
		public Point(number x, number y);
	}

	/**
	 * A convenience class for Rectangles.
	 *
	 * @since 4.11.0
	 */
	public class Rect {
		public number x;
		public number y;
		public number width;
		public number height;
		
		/**
		 * @param x The x coordinate.
		 * @param y The y coordinate.
		 * @param width The rectangle's width.
		 * @param height The rectangle's height.
		 */
		public Rect(number x, number y, number width, number height);
	}
	
	/**
	 * @brief A 2-dimensional polygon with 4 corners.
	 *
	 * @since 4.11.0
	 */
	public class Quadrilateral {
    	/**
    	 * @brief Top left corner of the quadrilateral.
    	 */
		public Point topLeft;
		/**
		 * @brief Top right corner of the quadrilateral.
		 */
		public Point topRight;
		/**
		 * @brief Bottom left corner of the quadrilateral.
		 */
		public Point bottomLeft;
		/**
		 * @brief Bottom right corner of the quadrilateral.
		 */
		public Point bottomRight;
	}
	
	/**
	 * @brief Margins for a view.
	 *
	 * @since 4.11.0
	 */
	public class Margins {
		public int left;
		public int top;
		public int right;
		public int left;
		
		/**
		 * @param left The left margin.
		 * @param top The top margin.
		 * @param right The top margin.
		 * @param bottom The bottom margin.
		 */
		public Margins(int left, int top, int right, int bottom);
	}
	
	/**
	 * @brief Represents a recognized/localized barcode/2D code.
	 *
	 * The Barcode class represents a  barcode, or 2D code that has been localized or recognized
	 * by the barcode recognition engine.
	 *
	 * @since 4.11.0
	 */
	public class Barcode {
	    /**
	     * @brief An enumeration of all supported barcode symbologies
	     */
		public enum Symbology  {
    		/**
    		 * @brief Sentinel value to represent an unknown symbology
    		 */
			UNKNOWN,
    		/**
     		 * @brief EAN13 1D barcode symbology.
    		 */
			EAN13,
    		/**
    		 * @brief EAN8 1D barcode symbology.
    		 */
			EAN8,
    		/**
    		 * @brief UPC12/UPCA 1D barcode symbology.
    		 */
			UPCA,
    		/**
    		 * @brief UPCE 1D barcode symbology.
    		 */
			UPCE,
    		/**
    		 * @brief Code 11 barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			CODE11,
    		/**
    		 * @brief Code 128 1D barcode symbology, including GS1-Code128. Only available in the
    		 *    Professional and Enterprise Packages.
    		 */
			CODE128,
    		/**
    		 * @brief Code39 barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			CODE39,
    		/**
    		 * @brief Code 93 barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			CODE93,
    		/**
    		 * @brief Interleaved-Two-of-Five (ITF) 1D barcode symbology. Only available in the Professional and
    		 * Enterprise Packages.
    		 */
			ITF,
    		/**
    		 * @brief QR Code 2D barcode symbology.
    		 */
			QR,
    		/**
    		 * @brief Datamatrix 2D barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			DATA_MATRIX,
    		/**
    		 * @brief PDF417 barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			PDF417,
    		/**
    		 * @brief MSI Plessey 1D barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			MSI_PLESSEY,
    		/**
    		 * @brief Databar 1D barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			GS1_DATABAR,
    		/**
    		 * @brief Databar Limited 1D barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			GS1_DATABAR_LIMITED,
    		/**
    		 * @brief Databar Expanded 1D barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			GS1_DATABAR_EXPANDED,
    		/**
    		 * @brief Codabar 1D barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			CODABAR,
    		/**
    		 * @brief Aztec 2D barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			AZTEC,
    		/**
    		 * @brief Maxicode 2D barcode symbology. Only available in the Professional and Enterprise Packages.
    		 */
			MAXICODE,
    		/**
    		 * @brief Five-digit add-on for UPC and EAN codes.
    		 *
    		 * In order to scan five-digit add-on codes, at least one of these symbologies must be activated
    		 * as well: \ref SYMBOLOGY_EAN13, \ref SYMBOLOGY_UPCA, \ref SYMBOLOGY_UPCE, or
    		 * \ref SYMBOLOGY_EAN8 and the maximum number of codes per frame has to be set to at least 2.
    		 *
    		 * Only available in the Professional and Enterprise Packages.
    		 */
			FIVE_DIGIT_ADD_ON,
    		/**
    		 * @brief Two-digit add-on for UPC and EAN codes.
    		 *
    		 * In order to scan two-digit add-on codes, at least one of these symbologies must be activated
    		 * as well: \ref SYMBOLOGY_EAN13, \ref SYMBOLOGY_UPCA, \ref SYMBOLOGY_UPCE, or
    		 * \ref SYMBOLOGY_EAN8 and the maximum number of codes per frame has to be set to at least 2.
    		 *
    		 * Only available in the Professional and Enterprise Packages.
    		 */
			TWO_DIGIT_ADD_ON
		}
		
		/**
		 * The symbology of a recognized barcode. Codes for which {@link isRecognized()} returns 
		 * false, {@link SYMBOLOGY_UNKNOWN} is returned. For all other codes, the symbology of the 
		 * recognized code is returned.
		 */
		public Symbology symbology;

		/**
		 * @brief Whether the code is a GS1 data carrier
		 *
		 * @return True if the code is a GS1 data carrier, false if not. False is returned for codes
		 *     that have only been localized but not recognized.
		 */
		public boolean isGs1DataCarrier();

		/**
		 * @brief Whether the code was completely recognized.
		 *
		 * This property is true for barcodes that were completely recognized and false for
		 * codes that were localized but not recognized. For codes returned by
		 * {@link BarcodeScannerSession.getNewlyRecognizedCodes()} and
		 * {@link BarcodeScannerSession.getAllRecognizedCodes()} this method always returns
		 * true, for codes returned by {@link BarcodeScannerSession.getNewlyLocalizedCodes()}
		 * {@link isRecognized()} always returns false.
		 *
		 * @return True if the code has been recognized.
		 */
		public boolean isRecognized();

		/**
		 *  @brief The data contained in the barcode/2D code, e.g. the 13 digit number
		 *     of a EAN13 code.
		 *
		 * For some types of barcodes/2D codes (for example DATAMATRIX, AZTEC, PDF417), the
		 * data string may contain non-printable characters and nul-bytes in the middle of
		 * the string. Use {@link getRawData()} if your application scans these types of codes
		 * and you are expecting binary/non-printable data.
		 *
		 * @return the data contained in the code.
		 */
		public String data;
		
		/**
		 * @brief The location of the code in the image.
		 *
		 * The location is returned as a a polygon with 4 corners. The corners are in the
		 * coordinate system of the raw preview image. In order to be displayed they must be
		 * transformed to the coordinate system of the view. The meaning of the values of topLeft,
		 * topRight etc is such that the topLeft point corresponds to the top-left corner of the
		 * barcode  regardless of how it is oriented in the image.
		 *
		 * @return The location of the code as a quadrilateral.
		 *
		 * @see Scandit.BarcodePicker#convertPointToPickerCoordinates(Point)
		 */
		public Quadrilateral location;
	}

	/**
	 * @brief The main class for scanning barcodes with the Scandit Barcode Scanner.
	 *<p>
	 * This class sets up the recognition process, the preview view, controls the camera and provides
	 * a callback interface when barcodes are recognized.
	 * <p>
	 * The picker class is a able to run in full-screen or as a subview in an existing activity.
	 *
	 * @since 4.11.0
	 */
	public class BarcodePicker {
	
		/**
		 * Orientation of the device.
		 */
		public enum Orientation {
			/**
			 * Portrait orientation - for phones this is the natural orientation, for tablets the
			 * natural bottom of the device is on the left.
			 */
			PORTRAIT,
			/**
			 * Upside-down portrait orientation - for phones this is the opposite of the natural
			 * orientation, for tablets the natural bottom of the device is on the right.
			 */
			PORTRAIT_UPSIDE_DOWN,
			/**
			 * Landscape right orientation - for phones the natural bottom of the device is on the 
			 * right, for tablets this is the natural orientation.
			 */
			LANDSCAPE_RIGHT,
			/**
			 * Landscape left orientation - for phones the natural bottom of the device is on the
			 * left, for tablets this is the opposite of the natural orientation.
			 */
			LANDSCAPE_LEFT
		}
        
        /**
         * Visibly shows the picker to the user. This should be called after the picker has been
         * fully configured. Once it is shown make sure to call
         * {@link startScanning() startScanning()} if you want to start the video feed and scan for
         * barcodes.
         * The success callback takes a {@link ScanSession} as parameter containing the information
         * about the scanned codes. The manual callback takes a String as parameter containing the
         * data.
         *
         * @param success callback for successfull scan
         * @param manual callback for manual scan using search bar
         * @param failure callback if operation fails
         */
        public void show(function success, function manual, function failure);
        
        /**
         * Cancels the picker by stopping it and removing it from the screen. If the picker is not
         * in continuous mode this will happen automatically when a code is recognized.
         */
        public void cancel();
		
    	/**
    	 * @brief Reconfigure the barcode picker with new settings
    	 *
    	 * The settings are applied asynchronously. Once they have been applied,  all new frames
    	 * will be processed with the new settings.
    	 *
    	 * @param settings the settings to apply.
    	 *
    	 * @since 4.11.0
    	 */
	    public void applyScanSettings(ScanSettings settings);
	    
	    /**
	     * @brief Allows to scan multiple codes without closing the scanner.
	     *
	     * By default continuousMode is false and the scanner closes after scanning a code.
	     *
	     * @since 4.11.0
	     */
	    public boolean continuousMode;
	    
	    /**
	     * @brief The orientations to which the barcode picker is allowed to rotate to.
	     *
	     * @since 4.11.0
	     */
	    public Orientation[] orientations;
	    
	    /**
	     * @brief Sets the margins of the barcode picker.
	     *
	     * Non-zero margins cause the barcode picker to be added as a subview on top of the webview
	     * instead of as full screen in a new view controller or activity. Margins can either be
	     * specified in pixel density independent platform dependent units by passing integers
	     * or in percents by passing strings ending with '%%'
	     *
	     * @param portraitMargins Margins for when the device is in portrait or upside-down portrait 
	     *        orientation. Can be null to indicate no margins.
	     * @param landscapeMargins Margins for when the device is in landscape left or right 
	     *		  orientation. Can be null to indicate no margins.
	     * @param animationDuration The duration the margin change takes to animate. At 0 it will 
	     *        instantly change.
	     *
	     * @since 4.11.0
	     */
		public void setMargins(Margins portraitMargins, Margins landscapeMargins, number animationDuration);

    	/**
    	 * @brief Asynchronously pause the scanning process while keeping the camera preview running.
    	 *
    	 * Use this method when you plan to briefly pause the scanning process and intend to
    	 * resume it later. It is not recommended to use this method when you are interrupting the
    	 * scanning process for longer periods of time as the camera preview will use considerable
    	 * power. For such scenarios use {@link stopScanning} instead.
    	 *
    	 * @since 4.11.0
    	 */
    	public void pauseScanning();

    	/**
    	 * @brief Asynchronously resume a previously paused scanning process
    	 *
    	 * Use this method to resume scanning after {@link ScanSession.pauseScanning} or
    	 * {@link pauseScanning} was called. Calling resume on a picker that was not previously
    	 * started with with startScanning() is undefined.
    	 *
    	 * @since 4.11.0
    	 */
    	public void resumeScanning();

    	/**
    	 * @brief Asynchronously stop the scanning process and camera preview
    	 *
    	 * @since 4.11.0
    	 */
    	public void stopScanning();

    	/**
    	 * @brief Asynchronously start the camera preview and scanning process
    	 *
    	 * @since 4.11.0
    	 */
	    public void startScanning();

    	/**
    	 * @brief Asynchronously start the camera preview and scanning process
    	 *
    	 * Allows to start the camera preview without immediately also starting the scanning process.
    	 *
    	 * @param startInPausedState Whether the scanning process should be paused.
    	 *
    	 * @since 4.11.0
    	 */
    	public void startScanning(boolean startInPausedState);

		/**
	 	 * Switches the torch on or off.
	 	 *
	 	 * @param on Whether the torch should be switched on.
    	 *
    	 * @since 4.11.0
 		 */
    	public void switchTorchOn(boolean on);

		/**
		 * Returns the overlay view which contains the main UI.
    	 *
    	 * @since 4.11.0
		 */
	    public ScanOverlay getOverlayView();
	    
		/**
		 * @brief Converts a point of an Barcode's location into this picker's coordinate system.
		 *
		 * The conversion takes the current resolution of the camera feed into consideration which means
		 * that if the resolution should change, converting a previously retrieved point successfully is
		 * no longer possible. A change in resolution happens for example if
		 * {@link ScanSettings.setHighDensityModeEnabled(boolean)} is changed
		 * or the camera is switched from back to front or vice versa.
		 *
		 * @param point The point to be converted.
		 * @return The point in this picker's coordinate system. Can be null if an error occurs.
    	 *
    	 * @since 4.11.0
		 *
		 * @see {@link Scandit.Barcode.location}
		 */
		public Point convertPointToPickerCoordinates(Point point);
	}


    /**
     * @brief Settings to configure the decoding process
     */
	public class ScanSettings {
    	
		/**
		 * The possible working ranges for the barcode picker
		 *
		 * \since 4.11.0
		 */
		public enum WorkingRange {
			/**
			 * The camera tries to focus on barcodes which are close to the camera. To scan far-
			 * away codes (30-40cm+), user must tap the screen. This is the default working range
			 * and works best for most use-cases. Only change the default value if you expect the
			 * users to often scan codes which are far away.
			 */
			STANDARD,
			/**
			 * The camera tries to focus on barcodes which are far from the camera. This will make
			 * it easier to scan codes that are far away but degrade performance for very close
			 * codes.
			 */
			LONG
		}
	
    	/**
    	 * Camera facing direction.
    	 */
		public enum CameraFacing {
			/**
			 * Facing away from the user.
			 */
			BACK,
			/**
			 * Facing towards the user (Facetime camera).
			 */
			FRONT
		}

  	    /** @name Working Range/Focus Control
  	     *
  	     */
  	    ///@{

	    /**
     	 * The working range tells the engine at which distance barcodes are to be
    	 * expected. When set to {@link WORKING_RANGE_STANDARD} (the default), the focus is optimized
    	 * for barcodes close to the camera. When set to {@link WORKING_RANGE_LONG}, the focus is
    	 * optimized for far-away codes.
    	 * <p>
    	 * When using non-standard working range, it is better to directly pass the
    	 * working range when constructing the barcode picker, because the camera
    	 * can already start to adjust the focus at an earlier point in time.
    	 * <p>
    	 * The working range hint is ignored on cameras with fixed-focus.
    	 */
    	public int workingRange;

   		///@}


    	/** @name Scan Session Configuration
    	 * @{
    	 */
    	/**
    	 * The duration (in milliseconds) for which barcodes are kept in the
    	 * session.
    	 * When set to values larger than zero, the value is
    	 * interpreted as milliseconds for which the barcodes should be kept in
    	 * the session. If set to zero, barcodes are discarded before processing
    	 * the next frame. Passing -1 will keep the codes for the duration of
    	 * the scan session.
    	 *
    	 * The default value is -1
    	 */
   		public int codeCachingDuration;

    	/**
    	 * The duration of the duplicate filter in milliseconds.
    	 * When set to values larger than zero, barcodes with the same symbology
    	 * and data are filtered out if they are decoded less than the specified
    	 * milliseconds apart. Set this value to zero, if you do not want to
    	 * filter duplicates. When set to -1, barcodes are filtered as duplicates
    	 * if they match an already decoded barcode in the session.
    	 *
    	 * By default, the duplicate filter is set to 500ms
    	 *
    	 * @see #codeCachingDuration
    	 */
    	public int codeDuplicateFilter;

    	/**
    	 * @}
    	 */

    	/**
    	 * High density mode enables phones to work at higher camera resolution,
    	 * provided they support it. When enabled, phones that are able to run the
    	 * video preview at 1080p (1920x1080) will use 1080p and not just 720p
    	 * (1280x720). High density mode gives better decode ranges at the
    	 * expense of processing speed and allows to decode smaller code in the near
    	 * range, or codes that further away.
    	 * <p>
    	 * By default, high density mode is disabled.
    	 */
    	public boolean highDensityModeEnabled;

    	/**
    	 * Sets the active scan area for portrait mode scanning.
    	 *
    	 * By default, the barcode recognition engine searches the whole image for barcodes.
    	 * Use this method to define the area in which barcodes are to be searched. Rectangle
    	 * coordinates run from 0 to 1.
    	 *
    	 * @since 4.11.0
    	 *
    	 * Invoking this method with invalid rectangles, e.g. rectangles whose x, y, width or height
    	 * attributes are outside the allowed range of 0.0-1.0 will have no effect.
    	 */
    	public Rect activeScanningAreaPortrait;

    	/**
    	 * Sets the active scan area for landscape mode scanning.
    	 *
    	 * By default, the barcode recognition engine searches the whole image for barcodes.
    	 * Use this method to define the area in which barcodes are to be searched. Rectangle
    	 * coordinates run from 0 to 1.
    	 *
    	 * @since 4.11.0
    	 *
    	 * Invoking this method with invalid rectangles, e.g. rectangles whose top, left,
    	 * right, or bottom attributes are outside the allowed range of 0.0-1.0, or rectangles
    	 * with negative width/height will have no effect.
    	 */
		public Rect activeScanningAreaLandscape;

    	/**
    	 * Sets the device name to identify the current device when looking at analytics tools. Sends a request to
    	 * the server to set this as soon as a connection is available.
		 *
    	 * @param deviceName A device name.
    	 */
    	public String deviceName;
    	
    	/**
    	 * Hash containing the symbology settings for each available symbology.
    	 */
    	public HashMap<Barcode.Symbology, SymbologySettings> symbologies;

    	/**
    	 * @brief Retrieve symbology specific-settings
    	 *
    	 * @param symbology the symbology settings to retrieve
    	 * @return the symbology settings object, or null, if symbology is an invalid symbology.
    	 *
    	 * @since 4.11.0
    	 */
    	public SymbologySettings getSymbologySettings(Barcode.Symbology symbology);

    	/**
    	 * @brief Enable/disable decoding of a certain symbology.
    	 *
    	 * This function provides a convenient shortcut to enabling/disabling decoding of a
    	 * particular symbology without having to go through SymbologySettings.
    	 *
    	 * @param symbology The symbology to be enabled.
    	 * @param enabled true when decoding of the symbology should be enabled, false if not.
    	 *
    	 * @since 4.11.0
    	 */
    	public void setSymbologyEnabled(int symbology, boolean enabled);
    
    	/**
    	 * The picker first gives preference to cameras of the specified direction. When
    	 * the device has no such camera, cameras of the opposite face are tried as
    	 * well.
    	 * <p>
    	 * By default, the back-facing camera is preferred.
    	 *
    	 * @since 4.11.0
    	 */
    	public CameraFacing cameraFacingPreference;

    	/**
    	 * The location in the image where barcodes are decoded with the
    	 * highest priority.
    	 * <p>
		 * This variable shows a slightly different behavior depending on whether the
    	 * full screen scanning is active or not. In Full screen scanning mode:
    	 * <p>
    	 * Sets the location in the image which is decoded with the highest priority
    	 * when multiple barcodes are present in the image.
    	 * <p>
    	 * Changes the location of the spot where the barcode decoder actively scans
    	 * for barcodes.
    	 * <p>
    	 * X and Y can be between 0 and 1, where 0/0 corresponds to the top left
    	 * corner and 1/1 to the bottom right corner.
    	 *
    	 * The default hotspot is centered on the image (0.5,0.5)
    	 */
    	public Point scanningHotSpot;

    	/**
    	 * The percentage of the max zoom (between 0 and 1).
    	 */
    	public number relativeZoom;
    
    	/**
    	 * Maximum number of codes to be decoded every frame. 
    	 * The values are clamped to the range 1 to 6.
    	 */
		public int maxNumberOfCodesPerFrame;

    	/**
    	 * Sets a property.
    	 * <p>
    	 * This function is for internal use only and any functionality that can be
    	 * accessed through it can and will vanish without public notice from one
    	 * version to the next. Do not call this function unless you specifically
    	 * have to.
    	 *
    	 * @param key The property's name.
		 * @param value The property's value.
		 */
    	public void setProperty(String key, Object value);
    }
    
    
	/**
	 * @brief Abstract scan UI class
	 *
	 * The ScanOverlay implements the scan UI displayed on top of the video feed. It is responsible
	 * for highlighting barcodes and draw the viewfinder rectangle or laser UI.
	 *
	 * @since 4.11.0
	 */
    public class ScanOverlay {

		/**
		 * @brief Camera Switch Button Visibility
		 */
		public enum CameraSwitchVisibility  {
			/**
			 * @brief The camera switch button is always hidden.
			 */
			NEVER,
			/**
			 * @brief The camera switch button is shown on tablet devices with front and back cameras.
			 */
			ON_TABLET,
			/**
			 * @brief The camera switch button is shown on all devices that have front and back cameras.
			 */
			ALWAYS
		}

		/**
		 * @brief Scan UI style
		 */
		public enum GuiStyle  {
		    /**
		     * A rectangular viewfinder with rounded corners is shown in the specified size. Recognized
		     * codes are marked with four corners.
		     */
			DEFAULT,
		    /**
		     * A laser line is shown with the specified width while the height is not changeable. This mode
		     * should generally not be used if the recognition is running on the whole screen as it
		     * indicates that the code should be placed at the location of the laser line.
		     */
			LASER,
		    /**
		     * No UI is shown to indicate where the barcode should be placed. Be aware that the Scandit
		     * logo continues to be displayed as showing it is part of the license agreement.
		     */
			NONE
		}
		/**
		 * @brief The GUI style drawn to display the indicator where the code should be scanned and the
		 *   visualization of recognized codes.
		 *
		 * @param guiStyle Must be one of 
         * {@link ScanOverlay.GuiStyle.DEFAULT Scandit.ScanOverlay.GuiStyle.DEFAULT}, 
         * {@link ScanOverlay.GuiStyle.LASER Scandit.ScanOverlay.GuiStyle.LASER} or
         * {@link ScanOverlay.GuiStyle.NONE Scandit.ScanOverlay.GuiStyle.NONE}. By default this is 
         * Scandit.ScanOverlay.GuiStyle.DEFAULT.
		 *
		 * @since 4.11.0
		 */
		public void setGuiStyle(GuiStyle guiStyle);


		/** @name Sound Configuration
		 *  Customize the scan sound.
		 */
		///@{

		/**
		 * Enables (or disables) the sound when a barcode is recognized. If the phone's ring mode
		 * is set to muted or vibrate, no beep will be played regardless of the value.
		 *
		 * Enabled by default.
		 *
		 * @since 4.11.0
		 *
		 * @param enabled Whether the beep is enabled.
		 */
		public void setBeepEnabled(boolean enabled);

		/**
		 * Enables or disables the vibration when a code was recognized. If the phone's ring mode
		 * is set to muted, no beep will be played regardless of the value.
		 *
		 * Enabled by default.
		 *
		 * @since 4.11.0
		 *
		 * @param enabled Whether vibrate is enabled.
		 */
		public void setVibrateEnabled(boolean enabled);
		///@}


		/** @name Torch Configuration
		 *  Enable and customize appearance of the torch icon.
		 */
		///@{

		/**
		 * Enables or disables the torch toggle button for all devices/cameras that support a torch.
		 *
		 * By default it is enabled. The torch icon is never shown when the camera does not have a
		 * torch (most tablets, front cameras, etc).
		 *
		 * @since 4.11.0
		 *
		 * @param enabled Whether the torch button should be shown.
		 */
		public void setTorchEnabled(boolean enabled);

		/**
		 * @brief Sets the position at which the button to enable the torch is drawn.
		 *
		 * By default the margins are 15 and width and height are 40.
		 *
		 * @since 4.11.0
		 *
		 * @param leftMargin Left margin in points.
		 * @param topMargin Top margin in points.
		 * @param width Width in points.
		 * @param height Height in points.
		 */
		public void setTorchButtonMarginsAndSize(int leftMargin, int topMargin, int width, int height);
		
		/**
		 * @brief Sets the accessibility label and hint for the torch button while the torch is off.
		 *
		 * The accessibility label and hint give vision-impaired users voice over guidance for the torch
		 * button while the torch is turned on. The default label is "Torch Switch (Currently Off)", the
		 * default hint "Double-tap to switch the torch on". The hint is only set on iOS devices as Android 
		 * does not support it.
		 *
		 * @since 4.11.0
		 *
		 * @param label The accessibility label.
		 * @param hint The accessibility hint.
		 */
		public void setTorchButtonOffAccessibility(String label, String hint);

		/**
		 * @brief Sets the accessibility label and hint for the torch button while the torch is on.
		 *
		 * The accessibility label and hint give vision-impaired users voice over guidance for the torch
		 * button while the torch is turned on. The default label is "Torch Switch (Currently On)", the
		 * default hint "Double-tap to switch the torch off". The hint is only set on iOS devices as Android 
		 * does not support it.
		 *
		 * @since 4.11.0
		 *
		 * @param label The accessibility label.
		 * @param hint The accessibility hint.
		 */
		public void setTorchButtonOnAccessibility(String label, String hint);
		///@}


		/** @name Camera Switch Configuration
		 *  Enable camera switch and set icons
		 */
		///@{

		/**
		 * Sets when the camera switch button is visible for devices that have more than one camera.
		 *
		 * By default it is {@link ScanOverlay.CameraSwitchVisibility.NEVER Scandit.ScanOverlay.CameraSwitchVisibility.NEVER}.
		 *
		 * @since 4.11.0
		 *
		 * @param visibility The visibility of the camera switch button 
         *                   ({@link ScanOverlay.CameraSwitchVisibility.NEVER Scandit.ScanOverlay.CameraSwitchVisibility.NEVER},
		 *                   {@link ScanOverlay.CameraSwitchVisibility.ON_TABLET Scandit.ScanOverlay.CameraSwitchVisibility.ON_TABLET}, 
         *                   {@link ScanOverlay.CameraSwitchVisibility.ALWAYS Scandit.ScanOverlay.CameraSwitchVisibility.ALWAYS})
		 */
		public void setCameraSwitchVisibility(CameraSwitchVisibility visibility);

		/**
		 * @brief Sets the position at which the button to switch the camera is drawn.
		 *
		 * By default the margins are 15 and width and height are 40.
		 *
		 * @since 4.11.0
		 *
		 * @param rightMargin Right margin in dp.
		 * @param topMargin Top margin in dp.
		 * @param width Width in dp.
		 * @param height Height in dp.
		 */
		public void setCameraSwitchButtonMarginsAndSize(
				int rightMargin, int topMargin, int width, int height);

		/**
		 * @brief Sets the accessibility label and hint for the camera switch button while the back-facing
		 * camera is active.
		 *
		 * The accessibility label and hint give vision-impaired users voice over guidance for the camera
		 * switch button while the back-facing camera is active. The default label is "Camera Switch 
		 * (Currently back-facing)", the default hint "Double-tap to switch to the front-facing camera".
		 * The hint is only set on iOS devices as Android does not support it.
		 *
		 * @since 4.11.0
		 *
		 * @param label The accessibility label.
		 * @param hint The accessibility hint.
		 */
		public void setCameraSwitchButtonBackAccessibility(String label, String hint);

		/**
		 * @brief Sets the accessibility label and hint for the camera switch button while the front-facing
		 * camera is active.
		 *
		 * The accessibility label and hint give vision-impaired users voice over guidance for the camera
		 * switch button while the front-facing camera is active. The default label is "Camera Switch 
		 * (Currently front-facing)", the default hint "Double-tap to switch to the back-facing camera".
		 * The hint is only set on iOS devices as Android does not support it.
		 *
		 * @since 4.11.0
		 *
		 * @param label The accessibility label.
		 * @param hint The accessibility hint.
		 */
		public void setCameraSwitchButtonFrontAccessibility(String label, String hint);
		///@}

		/** @name Viewfinder Configuration
		 *  Customize the viewfinder where the barcode location is highlighted.
		 */
		///@{

		/**
		 * Sets the size of the viewfinder relative to the size of the screen size.
		 *
		 * Changing this value does not(!) affect the area in which barcodes are successfully
		 * recognized. It only changes the size of the box drawn onto the scan screen. To restrict the
		 * active scanning area, use the methods listed below.
		 *
		 * @see ScanSettings.setScanningHotSpot(number, number)
		 * @see ScanSettings.setScanningHotSpotHeight(number)
		 *
		 * By default the width is 0.8, height is 0.4, landscapeWidth is 0.6, landscapeHeight is 0.4
		 *
		 * @since 4.11.0
		 *
		 * @param width Width of the viewfinder rectangle in portrait orientation.
		 * @param height Height of the viewfinder rectangle in portrait orientation.
		 * @param landscapeWidth Width of the viewfinder rectangle in landscape orientation.
		 * @param landscapeHeight Height of the viewfinder rectangle in landscape orientation.
		 */
		public void setViewfinderDimension(
				number width, number height, number landscapeWidth, number landscapeHeight);

		/**
		 * Sets the color of the viewfinder before a bar code has been recognized
		 *
		 * Note: This feature is only available with the Scandit SDK Enterprise Packages.
		 *
		 * By default this is: white (1.0, 1.0, 1.0)
		 *
		 * @since 4.11.0
		 *
		 * @param hexColor String of the color's hex value ("FF0000" for red, "00FF00" for green).
		 */
		public void setViewfinderColor(String hexColor);

		/**
		 * Sets the color of the viewfinder once the bar code has been recognized.
		 * <p>
		 * Note: This feature is only available with the Scandit SDK Enterprise Packages.
		 *
		 * By default this is: light blue (0.222, 0.753, 0.8)
		 *
		 * @since 4.11.0
		 *
		 * @param hexColor String of the color's hex value ("FF0000" for red, "00FF00" for green).
		 */
		public void setViewfinderDecodedColor(String hexColor);
		///@}


		/** @name Search Bar Configuration
		 *  Customize the search bar.
		 */
		///@{
		
		/**
		 * Shows (or hides) a search bar at the top of the scan screen. When data is
		 * submitted from the search bar the callback specified in
		 * {@link BarcodePicker.show BarcodePicker.show} is calledfloat passed as
		 * parameter in a string.
		 *
		 * @since 4.11.0
		 *
		 * @param show Whether the search bar should be visible.
		 */
		public void showSearchBar(boolean show);

		/**
		 * @brief Sets the caption of the search button at the top of the numerical keyboard.
		 *
		 * By default this is: "Go"
		 *
		 * @since 4.11.0
		 *
		 * @param caption Caption of the search button.
		 */
		public void setSearchBarActionButtonCaption(String caption);

		/**
		 * Sets the text shown in the manual entry field when nothing has been entered yet. To take
		 * effect this has to be called after
		 *
		 * By default this is: "Scan barcode or enter it here"
		 *
		 * @since 4.11.0
		 *
		 * @param text A placeholder text shown when the search bar is empty.
		 */
		public void setSearchBarPlaceholderText(String text);
		///@}

		/** @name Non-Official Methods
		 */
		///@{
		/**
		 * Set custom property
		 *
		 * This function is for internal use/and or experimental features and any functionality that
		 * can be accessed through it can and will vanish without public notice from one version to
		 * the next. Do not use this method unless you specifically have to.
		 *
		 * @since 4.11.0
		 *
		 * @param key The name of the property
		 * @param value the value for the property.
		 */
		public void setProperty(String key, Object value);
		///@}
    }
    
    
    /**
     * @brief Holds settings specific to a particular symbology (1d, 2d)
     */
    public class SymbologySettings {
    
    	/**
    	 * Checksums for the symbology.
    	 */
    	public enum Checksum {
    		/**
			 * Modulo 10 checksum.
			 */
    		MOD_10,
    		/**
			 * Modulo 11 checksum.
			 */
    		MOD_11,
    		/**
			 * Modulo 47 checksum.
			 */
    		MOD_47,
    		/**
			 * Modulo 43 checksum.
			 */
    		MOD_43,
    		/**
			 * Modulo 103 checksum.
			 */
    		MOD_103,
    		/**
			 * Two modulo 10 checksums.
			 */
    		MOD_1010,
    		/**
			 * A modulo 11 and a modulo 10 checksum.
			 */
    		MOD_1110,
		}
		
		/**
		 * Extensions for the symbology.
		 */
		public enum Extension {
			/**
			 * Enable decoder optimizations for small data-matrix codes.
			 */
			TINY,
			/**
			 * Full-ASCII Code39 extension.
			 */
			FULL_ASCII, 
			/**
			 * Remove leading zero of UPCA codes. When enabled, the leading zero of UPCA codes is 
			 * removed. When false (the default), the leading zero is returned as part of the 
			 * barcode data string.
			 */
			REMOVE_LEADING_ZERO
		}
		

    	/**
    	 * Enables/disables decoding of dark codes on bright background only. If color-
    	 * inverted (bright on dark) codes for this symbology are required, enable them through the
    	 * colorInvertedEnabled property. By default decoding of all symbologies is disabled.
    	 *
    	 * It is advised to only enable symbologies that are required by the application as every enabled
    	 * symbology adds processing overhead.
    	 *
    	 * @since 4.11.0
    	 */
    	public boolean enabled;

    	/**
    	 * Enables/disables decoding of bright codes on dark background only. By default
    	 * color-inverted decoding of all symbologies is disabled.
    	 *
    	 * It is advised to only enable symbologies that are required by the application as every enabled
    	 * symbology adds processing overhead.
    	 *
    	 * @since 4.11.0
    	 */
    	public boolean colorInvertedEnabled;
    
    	/**
    	 * An array of all active custom extensions for the symbology.
    	 *
    	 * Extensions are custom features that are only supported by a small number of
    	 * symbologies. For a list of supported extensions, consult the constants of this class.
    	 *
    	 * @since 4.11.0
    	 */
    	public Extension[] extensions;

    	/**
    	 * Array of additional checksums for this symbology. When a barcode has been
    	 * decoded, the checksums contained are evaluated in addition to any mandatory checksum defined by
    	 * the symbology specification. If any of the checksums matches, the code is returned as
    	 * recognized, otherwise it is discarded.
    	 *
    	 * @since 4.11.0
    	 */
    	public Checksum[] checksums;

    	/**
    	 * Array containing the length of barcodes to be decoded for this symbology. Change
    	 * this property to enable decoding of long codes which can not be decoded with the default
    	 * settings, or to optimize decoder performance for codes of certain lengths. This is useful when
    	 * it is known that the application only requires scanning of particular barcode lengths.
    	 *
    	 * The mapping from characters to symbols is symbology-specific. For some symbologies, the start
    	 * and end characters are included, others include checksums characters in the symbol counts.
    	 *
    	 * The active symbol count setting is ignored for fixed-size barcodes (the EAN and UPC family of
    	 * codes) as well as 2d codes. For other symbologies, see <a href="../c_api/symbologies.html">
    	 * Calculating symbol counts for variable-length symbologies</a>.
    	 *
    	 * @since 4.11.0
    	 */
    	public int[] activeSymbolCounts;
    }
    
    
	/**
	 * The scan session holds all barcodes that were decoded in the current
	 * session. These codes are available as {@link getAllRecognizedCodes()}.
	 *
	 * <h2>Configuring Session Behaviour</h2>
	 *
	 * The scan session is responsible for determining the list of "relevant" barcodes
	 * by filtering out duplicates. Depending on your app, different duplicate removal
	 * is required. For some applications, only one barcode is required. The scanning
	 * process is stopped as soon as one code is decoded. For other applications,
	 * multiple codes are scanned after another. For example, a scanner at the cash
	 * desk may need to scan multiple products. To avoid duplicates, the same barcode
	 * should not be scanned in short succession. The same barcode (data, symbology)
	 * should not count as a duplicate if encountered again after a few seconds.
	 * <p>
	 * By default, if a barcode has the same symbology and data as code that was
	 * decoded less than 500ms ago, it is filtered out as a duplicate. The exact
	 * filtering behaviour can be changed by setting the "code duplicate filter", see
	 * {@link ScanSettings.setCodeDuplicateFilter} for details.
	 *
	 * <h2>Session Lifetime</h2>
	 *
	 * The session is cleared when either {@link BarcodePicker.startScanning} or ,
	 * {@link BarcodePicker.stopScanning} is called, or when the user manually clears
	 * the session using {@link clear}.
	 *
	 * @see ScanSettings.setCodeCachingDuration
	 * @see ScanSettings.setCodeDuplicateFilter
	 *
	 * @since 4.11.0
	 *
	 */
	public class ScanSession {
		/**
		 * List of barcodes that have been successfully recognized in the last frame.
		 *
		 * @since 4.11.0
		 */
		Barcode[] newlyRecognizedCodes;

		/**
		 * List of barcodes that have been localized in the last frame. This list does not include 
		 * barcodes that have been successfully recognized.
		 *
		 * @since 4.11.0
		 */
		Barcode[] newlyLocalizedCodes;

		/**
		 * @brief Returns the list of barcodes (data, symbology) that have been recognized
		 *     in this session.
		 *
		 * Depending on the code caching and duplicate filtering behaviour, different
		 * sets of codes are returned by this method.
		 *
		 * @see ScanSettings.setCodeCachingDuration
		 * @see ScanSettings.setCodeDuplicateFilter
		 *
		 * @return a new copy of the list of barcodes that have been successfully
		 *    decoded in this session
		 *
		 * @since 4.11.0
		 */
		Barcode[] allRecognizedCodes;
		
		/**
		 * @brief Immediately Pauses barcode recognition, but keeps camera preview open.
		 *
		 * This is useful for briefly pausing the barcode recognition to show the
		 * recognized code in an overlay and then resume the scan process to scan
		 * more codes.
		 * <p>
		 * When only scanning one code and then returning to another part of the
		 * application, it is recommended to call {@link stopScanning()} instead.
		 * <p>
		 * Pausing will not clear the scan session. To remove all codes from the
		 * scan session call {@link clear()}.
		 *
		 * @see BarcodePicker.resumeScanning()
		 *
		 * @since 4.11.0
		 */
		void pauseScanning();

		/**
		 * Immediately stops the scanning and clears the scan session
		 * <p>
		 * Calling stop will release the camera, so that other applications can use
		 * it.
		 *
		 * @see BarcodePicker.stopScanning(), pauseScanning()
		 *
		 * @since 4.11.0
		 */
		void stopScanning();
	}
}
