/**
 * <p>A resource in the neon engine has three main properties: an id, a type and 
 * a namespace. The type is used by the {@code ResourceManager} to determine 
 * which {@code ResourceLoader} to use to load a resource. The namespace is 
 * used by the file system to determine where the resource file is stored.</p>
 * 
 * <p>It is possible that resources in a single namespace have different types 
 * and vice versa. As an example, the <i>items</i> namespace contains all the
 * items used in a game. There are different types of items (containers,
 * doors, ...) that may require different loaders.</p>
 * 
 * <p>To load a type of resource, its {@code ResourceLoader} should first be
 * registered with the {@code ResourceManager}. Without it, the resource will
 * not be loaded.</p>
 * 
 * @author mdriesen
 *
 */
package neon.system.resources;