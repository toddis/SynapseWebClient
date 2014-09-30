package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityTreeBrowserView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * 
	 * @param rootEntities entity query results with which to make root level nodes for in the tree
	 */
	void setRootEntities(EntityQueryResults rootEntities);
	
	/**
	 * 
	 * @param rootEntities list of entities to make root level nodes for in the tree
	 */
	void setRootEntities(List<EntityHeader> rootEntities);
	
	/**
	 * Remove an entity from the view identified by entityId
	 * @param entityModel
	 */
	void removeEntity(EntityHeader entityHeader);

	/**
	 * Rather than linking to the Entity Page, a clicked entity
	 * in the tree will become selected.
	 */
	void makeSelectable();
	
	/**
	 * Places the given child item in the tree. Gives the created item a "dummy"
	 * child so that the item can be expanded.
	 * @param child The EntityTreeItem to be placed in the tree.
	 * @param parent The EntityTreeItem that will be the parent of the inserted
	 *               child item. If parent == null, then the item
	 * 				 will be placed at the root of the tree.
	 */
	void placeTreeItem(EntityTreeItem child, EntityTreeItem parent);
	
	/**
	 * Makes a tree item from the given EntityQueryResult and places it in the tree.
	 * @param header The EntityQueryResult who's information will be used to create
	 *               the tree item to be placed.
	 * @param parent The EntityTreeItem that will be the parent of the inserted
	 *               child item. If parent == null, then the item
	 * 				 will be placed at the root of the tree.
	 */
	public void createAndPlaceTreeItem(EntityQueryResult header, EntityTreeItem parent);
	
	/**
	 * Makes a tree item from the given EntityHeader and places it in the tree.
	 * @param header The EntityHeader who's information will be used to create
	 *               the tree item to be placed.
	 * @param parent The EntityTreeItem that will be the parent of the inserted
	 *               child item. If parent == null, then the item
	 * 				 will be placed at the root of the tree.
	 */
	public void createAndPlaceTreeItem(EntityHeader header, EntityTreeItem parent);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void getFolderChildren(String entityId, AsyncCallback<EntityQueryResults> asyncCallback);

		void setSelection(String id);

		int getMaxLimit();

		ImageResource getIconForType(String type);
		
		void expandTreeItemOnOpen(final EntityTreeItem target);
		
		void clearRecordsFetchedChildren();

	}

}
