/**
 * <p>Provides JavaFX based components.</p>
 * <p>Feature switches:</p>
 * <dl>
 * <dt>-Dapplication.feature.embossing=off</dt>
 * <dd>Run the application without being able to actually send documents to the embosser.
 * This switch is used for development purposes and the default is to allow documents to 
 * be sent to the embosser. Note that this only applies to the new embossing menu. The old 
 * one is deactivated, but can still be used and is NOT guarded by this feature switch.
 * When embossing is deactivated, a message is logged when the embosser dialog is first 
 * opened and again when the emboss button is pressed.</dd>
 * </dl>
 */
package application.ui;