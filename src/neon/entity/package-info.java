/**
 * <p>The engine uses (tries to use) an entity-component-system architecture.</p>
 * 
 * <p>Entities are stored by uid. The entity uid is a 64 bit number with the following 
 * structure: XXXX YYYY ZZZZZZZZ. </p>
 * 
 * <ul>
 * 	<li>XXXX: 16 bit module uid, indicating the module the entity belongs to</li>
 * 	<li>YYYY: 16 bit map uid, indicating the map the entity belongs to</li>
 * 	<li>ZZZZZZZZ: 32 bits, indicating the entity</li>
 * </ul>
 * 
 * <p>Module uid 0 is preserved for special purposes. This leaves
 * 65535 modules for a user to load.</p>
 * 
 * <p>Map uid 0 in every module is preserved for entities that do not strictly 
 * belong to a certain map. This leaves 65535 possible maps in every module.</p>
 * 
 * <p>Each map my contain up to 4294967296 separate entities.</p>
 * 
 * @author mdriesen
 *
 */
package neon.entity;