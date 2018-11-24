/**
 * <p>A resource in the neon engine has two main properties: an id, and 
 * a namespace. The namespace is used by the {@code ResourceManager} to determine 
 * which {@code ResourceLoader} to use to load a resource.</p>
 * 
 * <p>All resources in the same namespace should be loaded by the same
 * resource loader.</p>
 * 
 * <p>To load a type of resource, its {@code ResourceLoader} should first be
 * registered with the {@code ResourceManager}. Without it, the resource will
 * not be loaded.</p>
 * 
 * @author mdriesen
 *
 */
package neon.common.resources;