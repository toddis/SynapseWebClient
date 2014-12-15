package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor.HasChangesHandler;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidgetView;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

// TODO: Generalize entire class with ACLModalWidget??
public class WikiSubpagesOrderEditorModalWidgetImpl implements	WikiSubpagesOrderEditorModalWidget,
																AccessControlListModalWidgetView.Presenter,
																HasChangesHandler {
	
	AccessControlListModalWidgetView view;	// TODO: Generalize this ACLModalWidgetView
	WikiSubpagesOrderEditor editor;
	Callback changeCallback;
	
	@Inject
	public WikiSubpagesOrderEditorModalWidgetImpl(
			AccessControlListModalWidgetView view,	
			WikiSubpagesOrderEditor editor) {
		super();
		this.view = view;
		this.editor = editor;
		this.view.setPresenter(this);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void show(Callback changeCallback) {
		this.changeCallback = changeCallback;
		//this.editor.refresh();
		view.setLoading(false);
		view.showDialog();
	}

	@Override
	public void configure(Tree subpagesTree, Callback updateOrderCallback) {
		editor.configure(subpagesTree, this, updateOrderCallback);
		view.addEditor(editor.asWidget());
	}

	@Override
	public void onPrimary() {
		view.setLoading(true);
		editor.pushChangesToSynapse(new Callback() {
			@Override
			public void invoke() {
				view.hideDialog();
				changeCallback.invoke();
			}
		});
	}

	@Override
	public void hasChanges(boolean hasChanges) {
		view.setLoading(false);
		view.setPrimaryButtonEnabled(hasChanges);
	}

	@Override
	public Tree getTree() {
		return editor.getTree();
	}
	
	@Override
	public void setSize(ModalSize size) {
		view.setSize(size);
	}
	
	// TODO: Remove
//	@Override
//	public List<String> getCurrentOrderIdList() {
//		return getCurrentOrderList(editor.getTree());
//	}
//	
//	private List<String> getCurrentOrderList(Tree tree) {
//		List<String> idList = new LinkedList<String>();
//		for (int i = 0; i < tree.getItemCount(); i++) {
//			recurseAddIds(idList, tree.getItem(i));
//		}
//		return idList;
//	}
//
//	private void recurseAddIds(List<String> idList, TreeItem root) {
//		idList.add(((SubPageTreeItem) root).getHeader().getId());
//		for (int i = 0; i < root.getChildCount(); i++) {
//			recurseAddIds(idList, root.getChild(i));
//		}
//	}

}