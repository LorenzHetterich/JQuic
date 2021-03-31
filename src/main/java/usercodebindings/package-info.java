/**
 * This package bindings for native code in native/src. <br>
 * Currently this is mainly used for reading / writing to streams to get rid of costly native - java transitions. <br>
 * * {@link Constants}: Contains constants used in the usercode shared library as well as the library instance <br>
 * * {@link Usercode}: Contains all methods of the usercode library that have Java bindings (adding methods automatically creates bindings, JNA magic!)
 */
package usercodebindings;