package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.Attachments;

/**
 * The abstraction of an entity dialog.
 * @author John
 *
 */
public interface EntityEditorDialog {
	
	public interface Callback {
		/**
		 * Will be called when/if the user selects 'save' after making changes.
		 * @param updatedEntity
		 * @param updatedAnnotations
		 */
		public void saveEntity(JSONObjectAdapter newAdapter, Annotations newAnnos);
	}
	

	/**
	 * Show the edit entity dialog.
	 * @param windowTitle
	 * @param newAdapter
	 * @param schema
	 * @param newAnnos
	 * @param filter
	 * @param callback
	 */
	public void showEditEntityDialog(String windowTitle, EntityBundle bundle, Attachments attachmentsWidget, JSONObjectAdapter newAdapter,	ObjectSchema schema, Annotations newAnnos, Set<String> filter,
			Callback callback);
	
	/**
	 * Show an error dialog
	 * @param message
	 */
	public void showErrorMessage(String message);

}
