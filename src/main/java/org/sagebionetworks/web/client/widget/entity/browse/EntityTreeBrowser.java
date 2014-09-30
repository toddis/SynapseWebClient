package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeBrowser implements EntityTreeBrowserView.Presenter, SynapseWidgetPresenter {
	
	private EntityTreeBrowserView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private HandlerManager handlerManager = new HandlerManager(this);
	private IconsImageBundle iconsImageBundle;
	AdapterFactory adapterFactory;
	EntityTypeProvider entityTypeProvider;
	private Set<EntityTreeItem> alreadyFetchedEntityChildren;
	
	private String currentSelection;
	
	private final int MAX_FOLDER_LIMIT = 500;
	public static final Long OFFSET_ZERO = 0L;
	
	@Inject
	public EntityTreeBrowser(EntityTreeBrowserView view,
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			EntityTypeProvider entityTypeProvider,
			GlobalApplicationState globalApplicationState,
			IconsImageBundle iconsImageBundle,
			AdapterFactory adapterFactory) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.entityTypeProvider = entityTypeProvider;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.iconsImageBundle = iconsImageBundle;
		this.adapterFactory = adapterFactory;
		alreadyFetchedEntityChildren = new HashSet<EntityTreeItem>();
		
		view.setPresenter(this);
	}	
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
	}
	
	public void clear() {
		view.clear();
	}

	/**
	 * Configure tree view with given entityId's children as start set
	 * @param entityId
	 */
	public void configure(String entityId, final boolean sort) {
		view.clear();
		view.showLoading();
		getFolderChildren(entityId, new AsyncCallback<EntityQueryResults>() {
			@Override
			public void onSuccess(EntityQueryResults results) {
//				if (sort) TODO: sorted by default?
//					EntityBrowserUtils.sortEntityHeadersByName(result);
				view.setRootEntities(results);
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view);
			}
		});
	}
	
	private EntityQuery createGetChildrenQuery(String parentId) {
		EntityQuery newQuery = new EntityQuery();
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.name.name());
		sort.setDirection(SortDirection.ASC);
		newQuery.setSort(sort);
		Condition condition = EntityQueryUtils.buildCondition(EntityFieldName.parentId, Operator.EQUALS, parentId);
		newQuery.setConditions(Arrays.asList(condition));
		newQuery.setLimit((long) MAX_FOLDER_LIMIT);
		newQuery.setOffset(OFFSET_ZERO);
		return newQuery;
	}
	
	/**
	 * Configure tree view to be filled initially with the given headers.
	 * @param rootEntities
	 */
	public void configure(List<EntityHeader> rootEntities, boolean sort) {
		if (sort)
			EntityBrowserUtils.sortEntityHeadersByName(rootEntities);
		view.setRootEntities(rootEntities);
	}
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}
	
	@Override
	public void getFolderChildren(String entityId, final AsyncCallback<EntityQueryResults> asyncCallback) {
		EntityQuery childrenQuery = createGetChildrenQuery(entityId);
		
		// NOTE: this is fragile, but there doesn't seem to be a way around querying by nodeType. 
		// a query on concreteType!=org...TableEntity eliminates nodes who do not have concreteType defined
		final String TABLE_ENTITY_NODE_TYPE_ID = "17"; 
		
		// TODO: Query by node type?
		synapseClient.executeEntityQuery(childrenQuery, new AsyncCallback<EntityQueryResults>() {
				@Override
				public void onSuccess(EntityQueryResults results) {
					asyncCallback.onSuccess(results);
				}
				@Override
				public void onFailure(Throwable caught) {
					DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view);				
					asyncCallback.onFailure(caught);
				}
			});
		
	}

	@Override
	public void setSelection(String id) {
		currentSelection = id;
		fireEntitySelectedEvent();
	}	
	
	public String getSelected() {
		return currentSelection;
	}
		
	@SuppressWarnings("unchecked")
	public void addEntitySelectedHandler(EntitySelectedHandler handler) {
		handlerManager.addHandler(EntitySelectedEvent.getType(), handler);		
	}

	@SuppressWarnings("unchecked")
	public void removeEntitySelectedHandler(EntitySelectedHandler handler) {
		handlerManager.removeHandler(EntitySelectedEvent.getType(), handler);
	}
	
	@SuppressWarnings("unchecked")
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);		
	}

	@SuppressWarnings("unchecked")
	public void removeEntityUpdatedHandler(EntityUpdatedHandler handler) {
		handlerManager.removeHandler(EntityUpdatedEvent.getType(), handler);
	}
	
	@Override
	public int getMaxLimit() {
		return MAX_FOLDER_LIMIT;
	}

	/**
	 * Rather than linking to the Entity Page, a clicked entity
	 * in the tree will become selected.
	 */
	public void makeSelectable() {
		view.makeSelectable();
	}
	
	/**
	 * When a node is expanded, if its children have not already
	 * been fetched and placed into the tree, it will delete the dummy
	 * child node and fetch the actual children of the expanded node.
	 * During this process, the icon of the expanded node is switched
	 * to a loading indicator.
	 */
	@Override
	public void expandTreeItemOnOpen(final EntityTreeItem target) {
		if (!alreadyFetchedEntityChildren.contains(target)) {
			// We have not already fetched children for this entity.
			
			// Change to loading icon.
			target.showLoadingIcon();
			
			getFolderChildren(target.getHeader().getId(), new AsyncCallback<EntityQueryResults>() {
				
				@Override
				public void onSuccess(EntityQueryResults results) {
					// We got the children.
					alreadyFetchedEntityChildren.add(target);
					target.asTreeItem().removeItems();	// Remove the dummy item.
					
					// Make a tree item for each child and place them in the tree.
					for (EntityQueryResult result : results.getEntities()) {
						view.createAndPlaceTreeItem(result, target);
					}
					
					// Change back to type icon.
					target.showTypeIcon();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {                    
						view.showErrorMessage(caught.getMessage());
					}
				}
				
			});
		}
	}
	
	@Override
	public void clearRecordsFetchedChildren() {
		alreadyFetchedEntityChildren.clear();
	}
	
	/*
	 * Private Methods
	 */
	private void fireEntitySelectedEvent() {
		handlerManager.fireEvent(new EntitySelectedEvent());
	}

	@Override
	public ImageResource getIconForType(String type) {
		return getIconForType(type, entityTypeProvider, iconsImageBundle);
	}
	
	public static ImageResource getIconForType(String type, EntityTypeProvider entityTypeProvider, IconsImageBundle iconsImageBundle) {
		if(type == null) return null;
		EntityType entityType;
		if(type.startsWith("org.")) entityType = entityTypeProvider.getEntityTypeForClassName(type); 			
		else entityType = entityTypeProvider.getEntityTypeForString(type);
		if (entityType == null) return null;
		return DisplayUtils.getSynapseIconForEntityClassName(entityType.getClassName(), DisplayUtils.IconSize.PX16, iconsImageBundle);
	}

}
