package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeBrowserViewImpl extends FlowPanel implements EntityTreeBrowserView {

	public static final String EMPTY_DISPLAY = "&#8212" + " " + DisplayConstants.EMPTY;
	
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	private IconsImageBundle iconsImageBundle;
		
	private boolean isSelectable = false;
	private Tree entityTree;
	private Map<TreeItem, EntityTreeItem> treeItem2entityTreeItem;
	private Map<EntityHeader, EntityTreeItem> header2entityTreeItem;	// for removing
	private EntityTreeItem selectedItem;
	private SageImageBundle sageImageBundle;

	@Inject
	public EntityTreeBrowserViewImpl(PortalGinInjector ginInjector, IconsImageBundle iconsImageBundle, SageImageBundle sageImageBundle) {
		this.ginInjector = ginInjector;
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		treeItem2entityTreeItem = new HashMap<TreeItem, EntityTreeItem>();
		header2entityTreeItem = new HashMap<EntityHeader, EntityTreeItem>();
		
		entityTree = new Tree(new EntityTreeResources());
		
		this.add(entityTree);
		entityTree.addOpenHandler(new OpenHandler<TreeItem>() {
			
			@Override
			public void onOpen(OpenEvent<TreeItem> event) {
				final EntityTreeItem target = treeItem2entityTreeItem.get(event.getTarget());
				presenter.expandTreeItemOnOpen(target);
			}
			
		});
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
		TreeItem loadingTreeItem = new TreeItem(new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle)));
		loadingTreeItem.addStyleName("entityTreeItem-font");
		entityTree.addItem(loadingTreeItem);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		entityTree.clear();
		treeItem2entityTreeItem.clear();
		header2entityTreeItem.clear();
		presenter.clearRecordsFetchedChildren();
	}

	@Override
	public void setRootEntities(List<EntityHeader> rootEntities) {
		clear();
		
		if(rootEntities == null) rootEntities = new ArrayList<EntityHeader>();
		if(rootEntities.isEmpty()) {
			TreeItem emptyItem = new TreeItem(new HTMLPanel("<div>" + EMPTY_DISPLAY + "</div>"));
			emptyItem.addStyleName("entityTreeItem-font");
			entityTree.addItem(emptyItem);
		} else {
			for (final EntityHeader header : rootEntities) {
				createAndPlaceTreeItem(header, null);
			}
		}
	}
	
	@Override
	public void setRootEntities(EntityQueryResults entities) {
		clear();
		List<EntityQueryResult> rootEntities;
		if (entities == null || entities.getEntities() == null) {
			rootEntities = new ArrayList<EntityQueryResult>();
		} else {
			rootEntities = entities.getEntities();
		}
		
		if (rootEntities.isEmpty()) {
			TreeItem emptyItem = new TreeItem(new HTMLPanel("<div>" + EMPTY_DISPLAY + "</div>"));
			emptyItem.addStyleName("entityTreeItem-font");
			entityTree.addItem(emptyItem);
		} else {
			for (final EntityQueryResult header : rootEntities) {
				createAndPlaceTreeItem(header, null);
			}
		}
		
	}
	
	@Override
	public void makeSelectable() {
		this.isSelectable = true;
		entityTree.addSelectionHandler(new SelectionHandler<TreeItem>() {

			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				final EntityTreeItem targetItem = treeItem2entityTreeItem.get(event.getSelectedItem());
				selectEntity(targetItem);
			}
			
		});
	}

	@Override
	public void removeEntity(EntityHeader entityHeader) {
		header2entityTreeItem.get(entityHeader).asTreeItem().remove();
	}
	
	@Override
	public void createAndPlaceTreeItem(EntityHeader header, EntityTreeItem parent) {
		EntityTreeItem childItem = ginInjector.getEntityTreeItemWidget();
		childItem.configure(header, parent == null);
		placeTreeItem(childItem, parent);
	}
	
	@Override
	public void createAndPlaceTreeItem(EntityQueryResult header, EntityTreeItem parent) {
		EntityTreeItem childItem = ginInjector.getEntityTreeItemWidget();
		childItem.configure(header, parent == null);
		placeTreeItem(childItem, parent);
	}
	
	@Override
	public void placeTreeItem(final EntityTreeItem child, EntityTreeItem parent) {
		// Make tree item.
		if (isSelectable) {
			// Add select functionality.
			child.setClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					selectEntity(child);
				}
				
			});
			
		}
		
		// Update fields.
		treeItem2entityTreeItem.put(child.asTreeItem(), child);
		header2entityTreeItem.put(child.getHeader(),  child);
				
		// Add dummy item to childItem to make expandable.
		child.asTreeItem().addItem(createDummyItem());
		
		// Place the created child in the tree as the child of the given parent entity.
		if (parent == null) {
			entityTree.addItem(child);
		} else {
			parent.asTreeItem().addItem(child);
		}
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void selectEntity(EntityTreeItem itemToSelect) {
		if (selectedItem != null) {
			selectedItem.asWidget().removeStyleName("entityTreeItem-selected");
		}
		selectedItem = itemToSelect;
		selectedItem.asWidget().addStyleName("entityTreeItem-selected");
		presenter.setSelection(selectedItem.getHeader().getId());
	}

	/**
	 * Creates the dummy item used to make a tree item expandable.
	 * @return the dummy tree item.
	 */
	private TreeItem createDummyItem() {
		TreeItem result = new TreeItem();
		result.setVisible(false);
		return result;
	}
	
	
	/*
	 * Image Resources for Tree Expand/Collapse Icons
	 */
	
	public class EntityTreeResources implements Tree.Resources {
		@Override
	    public ImageResource treeClosed() {
	        return iconsImageBundle.arrowRightDir16();
	    }

	    @Override
	    public ImageResource treeOpen() {
	        return iconsImageBundle.arrowDownDir16();
	    }

		@Override
		public ImageResource treeLeaf() {
			return EntityTreeImageBundle.DEFAULT_RESOURCES.treeLeaf();
		}
	}
	
	public interface EntityTreeImageBundle extends ClientBundle, ClientBundleWithLookup {
		Tree.Resources DEFAULT_RESOURCES = GWT.create(Tree.Resources.class);
	}

}
